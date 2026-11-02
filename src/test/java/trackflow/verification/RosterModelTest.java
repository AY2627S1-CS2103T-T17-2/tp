package trackflow.verification;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.DynamicTest.dynamicTest;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Random;
import java.util.Set;
import java.util.stream.IntStream;
import java.util.stream.Stream;

import org.junit.jupiter.api.DynamicTest;
import org.junit.jupiter.api.TestFactory;
import org.junit.jupiter.api.io.TempDir;

import trackflow.logic.LogicManager;
import trackflow.logic.commands.CommandResult;
import trackflow.logic.commands.exceptions.CommandException;
import trackflow.logic.parser.exceptions.ParseException;
import trackflow.model.AthleteRoster;
import trackflow.model.ModelManager;
import trackflow.model.ReadOnlyRoster;
import trackflow.model.UserPrefs;
import trackflow.model.person.Person;
import trackflow.storage.JsonRosterStorage;
import trackflow.storage.JsonUserPrefsStorage;
import trackflow.storage.StorageManager;

/**
 * Generated command histories are checked against independent records and view state after every transition.
 * The oracle never executes production commands, predicates or comparison helpers to calculate expected state.
 */
public class RosterModelTest {
    private static final int SCENARIOS = 8;
    private static final int STEPS = 100;
    private static final List<String> EVENTS = List.of("100m", "200m", "Long Jump", "Women's 100m",
            "Hammer 7.26kg", "Saut-Élan");

    @TempDir
    public Path folder;

    @TestFactory
    public Stream<DynamicTest> generatedHistories_matchReferenceRosterAndSavedJson() {
        long baseSeed = Long.getLong("verification.seed", 2103L);
        return IntStream.range(0, SCENARIOS).mapToObj(index -> {
            long seed = baseSeed + 104729L * index;
            return dynamicTest("roster history seed=" + seed, () -> {
                Scenario scenario = new Scenario(folder.resolve(Long.toUnsignedString(seed)));
                Random random = new Random(seed);
                for (int step = 0; step < STEPS; step++) {
                    try {
                        scenario.step(random);
                    } catch (Exception | AssertionError e) {
                        throw new AssertionError("seed=" + seed + ", step=" + step + "\n" + scenario.trace, e);
                    }
                }
            });
        });
    }

    private static class Scenario {
        private final FailingJsonStorage storage;
        private final JsonUserPrefsStorage preferences;
        private final List<ExpectedAthlete> roster = new ArrayList<>();
        private final List<String> trace = new ArrayList<>();
        private ModelManager model;
        private LogicManager logic;
        private Integer selectedId;
        private String sortField;
        private boolean descending;
        private int nextId;

        Scenario(Path directory) throws Exception {
            storage = new FailingJsonStorage(directory.resolve("roster.json"));
            preferences = new JsonUserPrefsStorage(directory.resolve("preferences.json"));
            storage.saveRoster(new AthleteRoster());
            restart();
        }

        void step(Random random) throws Exception {
            byte[] before = Files.readAllBytes(storage.getRosterFilePath());
            List<ExpectedAthlete> displayed = view();
            int action = random.nextInt(13);
            if (displayed.isEmpty() && (action == 1 || action == 2 || action == 3 || action == 11)) {
                action = 0;
            }
            int index = displayed.isEmpty() ? 0 : random.nextInt(displayed.size());
            ExpectedAthlete target = displayed.isEmpty() ? null : displayed.get(index);
            boolean mutates = false;
            switch (action) {
                case 0:
                    ExpectedAthlete added = new ExpectedAthlete(nextId++, "Home", "", Set.of());
                    execute(addCommand(added));
                    roster.add(added);
                    mutates = true;
                    break;
                case 1:
                    String address = "Block A/B " + random.nextLong();
                    execute("edit " + (index + 1) + " addr/" + address.replace("A/B", "A//B"));
                    replace(target, new ExpectedAthlete(target.id, address, target.remark, target.events));
                    mutates = true;
                    break;
                case 2:
                    String remark = "Practise w/coach " + random.nextLong();
                    execute("remark " + (index + 1) + " r/" + remark.replace("w/", "w//"));
                    replace(target, new ExpectedAthlete(target.id, target.address, remark, target.events));
                    mutates = true;
                    break;
                case 3:
                    execute("delete " + (index + 1));
                    roster.remove(target);
                    mutates = true;
                    break;
                case 4:
                    selectedId = roster.isEmpty() || random.nextBoolean() ? -1
                            : roster.get(random.nextInt(roster.size())).id;
                    execute("find n/" + name(selectedId).toUpperCase(Locale.ROOT));
                    break;
                case 5:
                    sortField = random.nextBoolean() ? "name" : "phone";
                    descending = random.nextBoolean();
                    execute("sort " + sortField + (descending ? " desc" : " asc"));
                    break;
                case 6:
                    execute("list");
                    selectedId = null;
                    sortField = null;
                    break;
                case 7:
                    String invalid = List.of("delete 0", "edit 999999999999 addr/Home", "find n/",
                            "add n/Bad a/Open p/000 e/bad@example.com addr/Home").get(random.nextInt(4));
                    reject(invalid);
                    break;
                case 8:
                    if (!roster.isEmpty()) {
                        ExpectedAthlete original = roster.get(random.nextInt(roster.size()));
                        reject("add n/" + name(original.id).toUpperCase(Locale.ROOT) + " a/Under 14 p/"
                                + phone(original.id) + " e/different@example.com addr/Home");
                    }
                    break;
                case 9:
                    var predicate = model.getPersonListPredicate();
                    var comparator = model.getPersonListComparator();
                    storage.failNextSave = true;
                    String command = addCommand(new ExpectedAthlete(nextId++, "Failure", "", Set.of()));
                    trace.add("[injected save failure] " + command);
                    assertThrows(CommandException.class, () -> logic.execute(command));
                    assertSame(predicate, model.getPersonListPredicate());
                    assertSame(comparator, model.getPersonListComparator());
                    break;
                case 10:
                    restart();
                    trace.add("[restart]");
                    break;
                case 11:
                    String event = EVENTS.get(random.nextInt(EVENTS.size()));
                    Set<String> changed = new HashSet<>(target.events);
                    String word = changed.remove(event) ? "untag" : "tag";
                    if (word.equals("tag")) {
                        changed.add(event);
                    }
                    execute(word + " " + (index + 1) + " t/" + event);
                    replace(target, new ExpectedAthlete(target.id, target.address, target.remark, changed));
                    mutates = true;
                    break;
                default:
                    boolean matches = random.nextBoolean();
                    execute("filter a/" + (matches ? "Open" : "Under 14"));
                    selectedId = matches ? null : -1;
                    break;
            }
            if (mutates) {
                // Deletion retains the filter; additions and updates restore the complete roster.
                if (action != 3) {
                    selectedId = null;
                }
            } else {
                assertArrayEquals(before, Files.readAllBytes(storage.getRosterFilePath()));
            }
            assertRecords(roster, model.getRoster().getPersonList());
            assertRecords(roster, storage.readRoster().orElseThrow().getPersonList());
            assertRecords(view(), model.getFilteredPersonList());
        }

        private void execute(String command) throws Exception {
            trace.add(command);
            CommandResult result = logic.execute(command);
            boolean shouldSave = command.startsWith("add ") || command.startsWith("edit ")
                    || command.startsWith("remark ") || command.startsWith("delete ")
                    || command.startsWith("tag ") || command.startsWith("untag ");
            assertEquals(shouldSave, result.isDataSaved());
        }

        private void reject(String command) {
            trace.add("[rejected] " + command);
            Exception error = assertThrows(Exception.class, () -> logic.execute(command));
            assertTrue(error instanceof ParseException || error instanceof CommandException,
                    "Rejection must be a controlled command error, not an unexpected runtime exception");
        }

        private void restart() throws Exception {
            model = new ModelManager(storage.readRoster().orElseThrow(), new UserPrefs());
            logic = new LogicManager(model, new StorageManager(storage, preferences));
            selectedId = null;
            sortField = null;
        }

        private List<ExpectedAthlete> view() {
            Stream<ExpectedAthlete> result = roster.stream()
                    .filter(person -> selectedId == null || person.id == selectedId);
            if (sortField != null) {
                Comparator<ExpectedAthlete> comparator = sortField.equals("phone")
                        ? Comparator.comparingLong(person -> phoneNumber(person.id))
                        : Comparator.comparing(person -> name(person.id));
                result = result.sorted(descending ? comparator.reversed() : comparator);
            }
            return result.toList();
        }

        private void replace(ExpectedAthlete original, ExpectedAthlete replacement) {
            roster.set(roster.indexOf(original), replacement);
        }
    }

    private static void assertRecords(List<ExpectedAthlete> expected, List<Person> actual) {
        assertEquals(expected.size(), actual.size());
        for (int index = 0; index < expected.size(); index++) {
            ExpectedAthlete record = expected.get(index);
            Person person = actual.get(index);
            assertEquals(name(record.id), person.getName().fullName);
            assertEquals("Open", person.getAgeCategory().value);
            assertEquals(phone(record.id), person.getPhone().value);
            assertEquals("athlete" + record.id + "@example.com", person.getEmail().value);
            assertEquals(record.address, person.getAddress().value);
            assertEquals(record.remark, person.getRemark().value);
            assertEquals(record.events, person.getTags().stream().map(tag -> tag.tagName)
                    .collect(java.util.stream.Collectors.toSet()));
        }
    }

    private static String addCommand(ExpectedAthlete record) {
        return "add n/" + name(record.id) + " a/Open p/" + phone(record.id)
                + " e/athlete" + record.id + "@example.com addr/" + record.address;
    }

    private static String name(int id) {
        return "Athlete " + String.format(Locale.ROOT, "%04d", id);
    }

    private static long phoneNumber(int id) {
        return 10_000_000L + id * 7919L % 10_000L;
    }

    private static String phone(int id) {
        String digits = Long.toString(phoneNumber(id));
        return id % 2 == 0 ? digits : digits.substring(0, 4) + "-" + digits.substring(4);
    }

    private record ExpectedAthlete(int id, String address, String remark, Set<String> events) {
        ExpectedAthlete {
            events = Set.copyOf(events);
        }
    }

    private static class FailingJsonStorage extends JsonRosterStorage {
        private boolean failNextSave;

        FailingJsonStorage(Path path) {
            super(path);
        }

        @Override
        public void saveRoster(ReadOnlyRoster roster) throws IOException {
            if (failNextSave) {
                failNextSave = false;
                throw new IOException("Injected generated-scenario save failure");
            }
            super.saveRoster(roster);
        }
    }
}
