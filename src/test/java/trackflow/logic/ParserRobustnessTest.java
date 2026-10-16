package trackflow.logic;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertSame;
import static trackflow.testutil.Assert.assertThrows;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import trackflow.logic.commands.exceptions.CommandException;
import trackflow.logic.parser.Prefix;
import trackflow.logic.parser.TrackFlowParser;
import trackflow.logic.parser.exceptions.ParseException;
import trackflow.model.Model;
import trackflow.model.ModelManager;
import trackflow.model.ReadOnlyRoster;
import trackflow.model.person.Person;
import trackflow.storage.JsonRosterStorage;
import trackflow.storage.JsonUserPrefsStorage;
import trackflow.storage.StorageManager;

public class ParserRobustnessTest {

    @TempDir
    public Path temporaryFolder;

    private Model model;
    private Logic logic;
    private JsonRosterStorage rosterStorage;
    private Path savedPath;

    @BeforeEach
    public void setUp() throws Exception {
        model = new ModelManager();
        savedPath = temporaryFolder.resolve("roster.json");
        rosterStorage = new JsonRosterStorage(savedPath);
        logic = new LogicManager(model, new StorageManager(rosterStorage,
                new JsonUserPrefsStorage(temporaryFolder.resolve("preferences.json"))));
        logic.execute("add n/Avery Tan a/Under 14 p/91234567 e/avery@example.com addr/Home");
    }

    @Test
    public void execute_escapedSlashText_persistsAndCanBeFound() throws Exception {
        logic.execute("edit 1 addr/Block A//B addr//Annex");
        logic.execute("remark 1 r/Practise r//relay x//y Mon/Tue");

        Person athlete = model.getFilteredPersonList().getFirst();
        assertEquals("Block A/B addr/Annex", athlete.getAddress().value);
        assertEquals("Practise r/relay x/y Mon/Tue", athlete.getRemark().value);
        assertEquals(athlete, rosterStorage.readRoster().orElseThrow().getPersonList().getFirst());

        for (String command : List.of("find A//B", "find addr/Block A//B", "find r/r//relay")) {
            logic.execute(command);
            assertEquals(List.of(athlete), model.getFilteredPersonList());
        }
    }

    @Test
    public void execute_addWithOrdinaryAddress_preservesSlashesAndBackslashes() throws Exception {
        logic.execute("add n/Ben Lim a/Open p/92345678 e/ben@example.com"
                + " addr/23 Bishan #05-110 C:\\Training Mon/Tue");

        Person athlete = model.getFilteredPersonList().getLast();
        assertEquals("23 Bishan #05-110 C:\\Training Mon/Tue", athlete.getAddress().value);
        assertEquals(athlete, rosterStorage.readRoster().orElseThrow().getPersonList().getLast());
    }

    @Test
    public void execute_literalBackslashBeforePrefix_roundTripsThroughSearchAndStorage() throws Exception {
        logic.execute("edit 1 addr/Block \\A/B");
        logic.execute("remark 1 r/Use \\r/relay");
        Person athlete = model.getFilteredPersonList().getFirst();

        assertEquals("Block \\A/B", athlete.getAddress().value);
        assertEquals("Use \\r/relay", athlete.getRemark().value);
        logic.execute("find addr/\\A/B");
        assertEquals(List.of(athlete), model.getFilteredPersonList());
        assertEquals(athlete, rosterStorage.readRoster().orElseThrow().getPersonList().getFirst());
    }

    @Test
    public void execute_escapedTokens_doNotSatisfyRequiredPrefixesOrBypassValidation() throws Exception {
        String savedBefore = Files.readString(savedPath);
        Person athlete = model.getFilteredPersonList().getFirst();
        for (String command : List.of(
                "add n/Ben a/Open p/92345678 e/ben@example.com addr//Home",
                "remark 1 r//Note", "edit 1 p//999", "edit 1 n/x//value",
                "edit 1 p/x//999", "edit 1 e/x//example.com", "edit 1 a/a//Open",
                "remark 1 r/first r//literal R/second",
                "edit 1 addr/Home x//literal x/unknown")) {
            assertThrows(ParseException.class, () -> logic.execute(command));
            assertEquals(List.of(athlete), model.getFilteredPersonList(), command);
            assertEquals(savedBefore, Files.readString(savedPath), command);
        }
    }

    @Test
    public void execute_escapedValuesInSortedFilteredViews_updatesDisplayedAthletes() throws Exception {
        logic.execute("add n/Zoe Tan a/Under 16 p/92345678 e/zoe@example.com addr/Home");
        logic.execute("add n/Ben Lim a/Open p/93456789 e/ben@example.com addr/Home");
        logic.execute("sort name desc");
        logic.execute("filter a/Under 16");
        assertEquals("Zoe Tan", model.getFilteredPersonList().getFirst().getName().fullName);

        logic.execute("edit 0001 addr/Block A//B");
        logic.execute("find n/Ben");
        logic.execute("remark 0001 r/Practise r//relay");
        logic.execute("find addr/Block A//B");
        assertEquals(1, model.getFilteredPersonList().size());
        assertEquals("Zoe Tan", model.getFilteredPersonList().getFirst().getName().fullName);
        logic.execute("delete 0001");
        assertEquals(List.of(), model.getFilteredPersonList());

        List<Person> reloaded = rosterStorage.readRoster().orElseThrow().getPersonList();
        assertEquals(List.of("Avery Tan", "Ben Lim"),
                reloaded.stream().map(person -> person.getName().fullName).toList());
        assertEquals("Practise r/relay", reloaded.getLast().getRemark().value);
        assertEquals("", reloaded.getFirst().getRemark().value);
    }

    @Test
    public void execute_quotesAndBackslashes_remainOrdinaryText() throws Exception {
        logic.execute("remark 1 r/Coach said \"go\" in r//relay");
        assertEquals("Coach said \"go\" in r/relay", model.getFilteredPersonList().getFirst().getRemark().value);
        logic.execute("remark 1 r/\"Quoted\"");
        assertEquals("\"Quoted\"", model.getFilteredPersonList().getFirst().getRemark().value);
        logic.execute("remark 1 r/C:\\Training \"unfinished");
        assertEquals("C:\\Training \"unfinished", model.getFilteredPersonList().getFirst().getRemark().value);
        logic.execute("remark 1 r/");
        assertEquals("", model.getFilteredPersonList().getFirst().getRemark().value);
        assertEquals("", rosterStorage.readRoster().orElseThrow().getPersonList().getFirst().getRemark().value);
    }

    @Test
    public void execute_escapedAddressInAnyPosition_preservesOtherFields() throws Exception {
        for (String command : List.of(
                "add addr/Block A//B p//000 x//value n/Ben First a/Open p/92345678 e/first@example.com",
                "add n/Ben Middle a/Open addr/Block A//B p//000 x//value p/92345678 e/middle@example.com",
                "add n/Ben Last a/Open p/92345678 e/last@example.com addr/Block A//B p//000 x//value")) {
            logic.execute(command);
            Person athlete = model.getFilteredPersonList().getLast();
            assertEquals("Block A/B p/000 x/value", athlete.getAddress().value);
            assertEquals("92345678", athlete.getPhone().value);
            assertEquals("Open", athlete.getAgeCategory().value);
        }
        assertEquals(4, rosterStorage.readRoster().orElseThrow().getPersonList().size());
    }

    @Test
    public void execute_escapedSearch_combinesWithFieldSearch() throws Exception {
        logic.execute("edit 1 addr/Block A//B");
        logic.execute("add n/Ben Lim a/Open p/92345678 e/ben@example.com addr/Block C");

        logic.execute("find A//B");
        assertEquals(List.of("Avery Tan"),
                model.getFilteredPersonList().stream().map(person -> person.getName().fullName).toList());
        logic.execute("find A//B n/Ben");
        assertEquals(2, model.getFilteredPersonList().size());
    }

    @Test
    public void execute_autocompleteAfterEscapedAddress_addsActualMissingPrefix() throws Exception {
        AutocompleteEngine autocomplete = new AutocompleteEngine();
        String command = "edit 1 n/Avery Tan p/91234567 e/avery@example.com addr/Block A//B";
        String nextPrefix = autocomplete.getNextPrefix(command);
        assertEquals(" a/", nextPrefix);

        logic.execute(command + nextPrefix + "Open");
        Person athlete = model.getFilteredPersonList().getFirst();
        assertEquals("Block A/B", athlete.getAddress().value);
        assertEquals("Open", athlete.getAgeCategory().value);
    }

    @Test
    public void execute_slashLedValues_requireSpaceAfterRealPrefix() throws Exception {
        String savedBefore = Files.readString(savedPath);
        Person athlete = model.getFilteredPersonList().getFirst();
        for (String command : List.of("edit 1 addr//home", "remark 1 r//note", "edit 1 p//999")) {
            assertThrows(ParseException.class, () -> logic.execute(command));
            assertEquals(List.of(athlete), model.getFilteredPersonList(), command);
            assertEquals(savedBefore, Files.readString(savedPath), command);
        }
        logic.execute("edit 1 addr/ /home/coach");
        logic.execute("remark 1 r/ /note");
        Person updated = model.getFilteredPersonList().getFirst();
        assertEquals("/home/coach", updated.getAddress().value);
        assertEquals("/note", updated.getRemark().value);
        assertEquals(updated, rosterStorage.readRoster().orElseThrow().getPersonList().getFirst());
    }

    @Test
    public void execute_repeatedSlashEscapes_surviveReloadAndExactFieldSearch() throws Exception {
        for (int count = 2; count <= 5; count++) {
            String entered = "A" + "/".repeat(count) + "B";
            String expected = "A" + "/".repeat(count - 1) + "B";
            logic.execute("edit 1 addr/Block " + entered);
            Person athlete = rosterStorage.readRoster().orElseThrow().getPersonList().getFirst();
            assertEquals("Block " + expected, athlete.getAddress().value);
            logic.execute("find addr/Block " + entered);
            assertEquals(List.of(athlete), model.getFilteredPersonList());
        }
    }

    @Test
    public void execute_urlsAndNetworkPaths_preservesDoubleSlashes() throws Exception {
        for (String value : List.of("https://example.com/a//b", "http://host/?n/value&p//other#addr//here",
                "file:///home/coach", "ftp://host/a//b", "//server/share", "x://host/path", "/", "//", "///")) {
            logic.execute("edit 1 addr/ " + value);
            logic.execute("remark 1 r/ " + value);
            Person athlete = rosterStorage.readRoster().orElseThrow().getPersonList().getFirst();
            assertEquals(value, athlete.getAddress().value);
            assertEquals(value, athlete.getRemark().value);
            logic.execute("find addr/ " + value);
            assertEquals(List.of(athlete), model.getFilteredPersonList());
            logic.execute("find r/ " + value);
            assertEquals(List.of(athlete), model.getFilteredPersonList());
        }
    }

    @Test
    public void execute_duplicateParametersInEveryField_rejectsEvenWithEmptyOrEscapedValues() throws Exception {
        List<String> validFields = List.of("n/Ben", "a/Open", "p/92345678", "e/ben@example.com", "addr/Home");
        List<String> invalidCommands = new ArrayList<>();
        TrackFlowParser parser = new TrackFlowParser();
        for (String field : List.of("n", "a", "p", "e", "addr", "r")) {
            String upper = field.toUpperCase(Locale.ROOT);
            for (String repeated : List.of(field + "/first " + field + "//literal " + upper + "/second",
                    field + "/ " + upper + "/second", field + "/first " + upper + "/",
                    upper + "/same " + field + "/same")) {
                List<String> commands = new ArrayList<>();
                commands.add("find " + repeated);
                if (field.equals("r")) {
                    commands.add("remark 1 " + repeated);
                } else {
                    commands.add("edit 1 " + repeated);
                    String remaining = String.join(" ", validFields.stream()
                            .filter(value -> !value.startsWith(field + "/")).toList());
                    commands.add("add " + remaining + " " + repeated);
                    if (field.equals("a")) {
                        commands.add("filter " + repeated);
                    }
                }
                for (String command : commands) {
                    // Check the reason too: invalid field content must not hide a duplicate parameter.
                    assertThrows(ParseException.class, Messages.getErrorMessageForDuplicatePrefixes(
                            new Prefix(field + "/")), () -> parser.parseCommand(command));
                }
                invalidCommands.addAll(commands);
            }
        }
        assertRejectedWithoutChanges(ParseException.class, invalidCommands);
    }

    @Test
    public void execute_emptyAndMissingRequiredValues_rejectsWithoutWriting() throws Exception {
        List<String> validFields = List.of("n/Ben", "a/Open", "p/92345678", "e/ben@example.com", "addr/Home");
        List<String> commands = new ArrayList<>();
        for (String field : List.of("n", "a", "p", "e", "addr")) {
            String remaining = String.join(" ", validFields.stream()
                    .filter(value -> !value.startsWith(field + "/")).toList());
            commands.add("add " + remaining);
            for (String blank : List.of("", " ", "\t", "\u2003")) {
                commands.add("add " + remaining + " " + field + "/" + blank);
                commands.add("edit 1 " + field + "/" + blank);
                commands.add("find " + field + "/" + blank);
            }
            commands.add("add " + field + "//literal " + remaining);
        }
        commands.addAll(List.of("remark 1", "remark 1 r//", "find r/", "find", "filter", "filter a/",
                "edit 1", "delete", "delete null", "edit null addr/Home", "remark null r/Note"));
        assertRejectedWithoutChanges(ParseException.class, commands);
    }

    @Test
    public void execute_quotesDoNotEscapeParametersOrSuppressDuplicateChecks() throws Exception {
        assertRejectedWithoutChanges(ParseException.class, List.of(
                "remark 1 r/\"first R/second\"", "edit 1 addr/'first ADDR/second'",
                "remark 1 r/\"first x/unknown\"", "remark 1 r/'first x/unknown'"));
        logic.execute("remark 1 r/\"first r//second\"");
        assertEquals("\"first r/second\"", model.getFilteredPersonList().getFirst().getRemark().value);
    }

    @Test
    public void execute_literalNull_isTextAndDoesNotBypassFieldValidation() throws Exception {
        logic.execute("edit 1 n/null addr/null");
        logic.execute("remark 1 r/null");
        Person athlete = rosterStorage.readRoster().orElseThrow().getPersonList().getFirst();
        assertEquals("null", athlete.getName().fullName);
        assertEquals("null", athlete.getAddress().value);
        assertEquals("null", athlete.getRemark().value);
        logic.execute("find n/null");
        assertEquals(List.of(athlete), model.getFilteredPersonList());
        assertRejectedWithoutChanges(ParseException.class, List.of("edit 1 p/null", "edit 1 e/null",
                "edit 1 a/null", "filter a/null"));
        assertRejectedWithoutChanges(NullPointerException.class, java.util.Collections.singletonList(null));
    }

    @Test
    public void execute_escapedText_doesNotBypassDuplicateAthleteChecks() throws Exception {
        logic.execute("add n/Ben Lim a/Open p/92345678 e/ben@example.com addr/Home");
        logic.execute("find n/Ben");
        assertRejectedWithoutChanges(CommandException.class, List.of(
                "add n/AVERY TAN a/Open p/91234567 e/other@example.com addr/Block A//B",
                "edit 1 n/AVERY TAN p/91234567 addr/Block A//B"));
    }

    @Test
    public void execute_reloadingAndUnrelatedEdits_doNotDecodeStoredTextAgain() throws Exception {
        logic.execute("edit 1 addr/Block A////B addr///annex");
        logic.execute("remark 1 r/Use r////relay https://host/a//b");
        for (int restart = 0; restart < 3; restart++) {
            model = new ModelManager(rosterStorage.readRoster().orElseThrow(), model.getUserPrefs());
            logic = new LogicManager(model, new StorageManager(rosterStorage,
                    new JsonUserPrefsStorage(temporaryFolder.resolve("preferences.json"))));
            logic.execute("edit 1 p/91234567");
            Person athlete = model.getFilteredPersonList().getFirst();
            assertEquals("Block A///B addr//annex", athlete.getAddress().value);
            assertEquals("Use r///relay https://host/a//b", athlete.getRemark().value);
            logic.execute("find addr/Block A////B addr///annex");
            assertEquals(List.of(athlete), model.getFilteredPersonList());
        }
    }

    @Test
    public void execute_autocomplete_doesNotCountEscapedPrefixesAsParameters() throws Exception {
        AutocompleteEngine autocomplete = new AutocompleteEngine();
        assertEquals("", autocomplete.getNextPrefix(null));
        assertEquals("", autocomplete.getNextPrefix("   "));
        assertEquals(" n/", autocomplete.getNextPrefix("add n//literal"));
        assertEquals(" r/", autocomplete.getNextPrefix("remark 1 r//literal"));
        assertEquals(" a/", autocomplete.getNextPrefix("filter a//Open"));
        assertEquals("", autocomplete.getNextPrefix("remark 1 R/"));
        assertEquals("", autocomplete.getNextPrefix("unknown addr/A//B"));
        assertEquals("", autocomplete.getNextPrefix("find A//B"));
        String input = "add N/Ben P/92345678 E/ben@example.com ADDR/Block A//B x//value ";
        String completion = autocomplete.getNextPrefix(input);
        assertEquals("a/", completion);
        logic.execute(input + completion + "Open");
        assertEquals("Block A/B x/value", model.getFilteredPersonList().getLast().getAddress().value);
    }

    @Test
    public void execute_multilineSingleCommand_acceptsParameterSeparators() throws Exception {
        logic.execute("\tEdIt\r\n0001\tADDR/Training centre\nA/Under 16\t");

        Person athlete = model.getFilteredPersonList().getFirst();
        assertEquals("Training centre", athlete.getAddress().value);
        assertEquals("Under 16", athlete.getAgeCategory().value);
        logic.execute("FiNd\nN/Avery");
        assertEquals(List.of(athlete), model.getFilteredPersonList());
        logic.execute("SoRt\r\nname\tdesc");
        assertEquals(List.of(athlete), model.getFilteredPersonList());
        assertEquals(athlete, rosterStorage.readRoster().orElseThrow().getPersonList().getFirst());
    }

    @Test
    public void execute_invalidInput_preservesRosterDisplayAndFile() throws Exception {
        logic.execute("sort name desc");
        logic.execute("find n/Avery");
        ReadOnlyRoster rosterBefore = new ModelManager(model.getRoster(), model.getUserPrefs()).getRoster();
        List<Person> displayedBefore = List.copyOf(model.getFilteredPersonList());
        var predicateBefore = model.getPersonListPredicate();
        var comparatorBefore = model.getPersonListComparator();
        String savedBefore = Files.readString(savedPath);

        for (String command : List.of("", " \t\r\n", "unknown", "list extra", "help extra",
                "exit extra", "clear\nexit", "sort name asc extra", "filter a/", "find n/",
                "find n/Avery N/Ben", "edit 1 p/999 P/888", "remark 1 r/first R/second",
                "edit 1 addr/", "edit 1 x/value", "remark 1 r/Note\u2003x/value",
                "edit 1 addr/Block\nAnnex", "edit 1 addr/Block\rAnnex", "edit 1 addr/Block\u2028Annex",
                "add n/Ben a/Open p/999 e/ben@example.com", "edit 1 p/", "remark 1", "filter a//Open")) {
            assertThrows(ParseException.class, () -> logic.execute(command));
            assertEquals(rosterBefore, model.getRoster(), command);
            assertEquals(displayedBefore, model.getFilteredPersonList(), command);
            assertSame(predicateBefore, model.getPersonListPredicate(), command);
            assertSame(comparatorBefore, model.getPersonListComparator(), command);
            assertEquals(savedBefore, Files.readString(savedPath), command);
        }
    }

    @Test
    public void execute_extremeIndexes_rejectsWithoutChangingSavedData() throws Exception {
        String savedBefore = Files.readString(savedPath);
        Person athlete = model.getFilteredPersonList().getFirst();

        for (String index : List.of("0", "000", "-1", "+1", "1.0", "1 0", "\u0661", "9".repeat(10000))) {
            for (String command : List.of("delete " + index, "edit " + index + " p/999",
                    "remark " + index + " r/Changed")) {
                if (index.length() == 10000) {
                    assertThrows(CommandException.class, () -> logic.execute(command));
                } else {
                    assertThrows(ParseException.class, () -> logic.execute(command));
                }
                assertEquals(List.of(athlete), model.getFilteredPersonList());
                assertEquals(savedBefore, Files.readString(savedPath));
            }
        }
    }

    /** Checks parser/command failures against the full roster, active view, and byte-for-byte saved contents. */
    private void assertRejectedWithoutChanges(Class<? extends Exception> exceptionType, List<String> commands)
            throws Exception {
        ReadOnlyRoster rosterBefore = new ModelManager(model.getRoster(), model.getUserPrefs()).getRoster();
        List<Person> displayedBefore = List.copyOf(model.getFilteredPersonList());
        var predicateBefore = model.getPersonListPredicate();
        var comparatorBefore = model.getPersonListComparator();
        String savedBefore = Files.readString(savedPath);
        for (String command : commands) {
            assertThrows(exceptionType, () -> logic.execute(command));
            assertEquals(rosterBefore, model.getRoster(), command);
            assertEquals(displayedBefore, model.getFilteredPersonList(), command);
            assertSame(predicateBefore, model.getPersonListPredicate(), command);
            assertSame(comparatorBefore, model.getPersonListComparator(), command);
            assertEquals(savedBefore, Files.readString(savedPath), command);
        }
    }
}
