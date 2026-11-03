package trackflow.logic;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import trackflow.logic.commands.RemarkCommand;
import trackflow.logic.commands.exceptions.CommandException;
import trackflow.logic.parser.exceptions.ParseException;
import trackflow.model.AthleteRoster;
import trackflow.model.Model;
import trackflow.model.ModelManager;
import trackflow.model.UserPrefs;
import trackflow.model.person.Person;
import trackflow.model.person.Phone;
import trackflow.model.util.SampleDataUtil;
import trackflow.storage.JsonRosterStorage;
import trackflow.storage.JsonUserPrefsStorage;
import trackflow.storage.StorageManager;
import trackflow.testutil.PersonBuilder;

/**
 * Release regressions through the real parser, staged model and JSON persistence.
 */
public class ReleaseRegressionTest {
    @TempDir
    public Path folder;

    private LogicManager createLogic(Model model, JsonRosterStorage storage) {
        return new LogicManager(model, new StorageManager(storage,
                new JsonUserPrefsStorage(folder.resolve("preferences.json"))));
    }

    @Test
    public void legacySingaporePairs_surviveLoadingEditsAndRestart_withoutAllowingNewDuplicates() throws Exception {
        Person local = new PersonBuilder().withName("Avery Tan").withPhone("91234567")
                .withEmail("local@example.com").build();
        Person international = new PersonBuilder(local).withPhone("+65 9123 4567")
                .withEmail("international@example.com").build();
        AthleteRoster legacy = new AthleteRoster();
        legacy.setPersons(List.of(local, international));
        JsonRosterStorage storage = new JsonRosterStorage(folder.resolve("roster.json"));
        storage.saveRoster(legacy);
        Model model = new ModelManager(storage.readRoster().orElseThrow(), new UserPrefs());
        LogicManager logic = createLogic(model, storage);

        logic.execute("sort phone desc");
        logic.execute("edit 1 addr/New address");
        logic.execute("remark 1 r/Recovered legacy record");
        logic.execute("tag 1 t/100m");
        logic.execute("edit 1 a/Under 14");
        assertEquals(2, storage.readRoster().orElseThrow().getPersonList().size());
        String saved = Files.readString(storage.getRosterFilePath());
        assertThrows(CommandException.class, () -> logic.execute(
                "add n/AVERY TAN a/Open p/(+65) 91234567 e/new@example.com addr/Home"));
        // The international record sorts first. Making its phone identical to the local record is rejected.
        assertThrows(CommandException.class, () -> logic.execute("edit 1 p/91234567"));
        assertEquals(saved, Files.readString(storage.getRosterFilePath()));

        Model restarted = new ModelManager(storage.readRoster().orElseThrow(), new UserPrefs());
        LogicManager restartedLogic = createLogic(restarted, storage);
        restartedLogic.execute("edit 1 e/distinct@example.com");
        restartedLogic.execute("delete 2");
        assertEquals(1, storage.readRoster().orElseThrow().getPersonList().size());
    }

    @Test
    public void invalidNewPhones_leaveSavedRosterUnchanged_whileLegacyValuesRemainReadable() throws Exception {
        JsonRosterStorage storage = new JsonRosterStorage(folder.resolve("roster.json"));
        Model model = new ModelManager();
        LogicManager logic = createLogic(model, storage);
        logic.execute("add n/Avery Tan a/Open p/91234567 e/avery@example.com addr/Home");
        String saved = Files.readString(storage.getRosterFilePath());
        for (String phone : List.of("000", "00-0", "+65 1", "(+65) 12", "+65 000", "1".repeat(16), "1".repeat(42),
                "9".repeat(5000), "9123 (HP) 4567", "(+65) (+65) 9123", "91234567 ext 123")) {
            assertThrows(ParseException.class, () -> logic.execute("edit 1 p/" + phone), phone);
            assertEquals(saved, Files.readString(storage.getRosterFilePath()), phone);
        }
        for (String phone : List.of("911", "1".repeat(15), "+65 91234567", "+6591234567", "(+65)91234567",
                "+1 12345678901234", "91234567 (ext 123)")) {
            logic.execute("edit 1 p/" + phone);
        }
        assertTrue(Phone.isValidPhone("0".repeat(5000)));
        assertFalse(Phone.isValidNewPhone("0".repeat(5000)));
        AthleteRoster legacy = new AthleteRoster();
        legacy.addPerson(new PersonBuilder().withPhone("0".repeat(5000)).build());
        storage.saveRoster(legacy);
        assertEquals("0".repeat(5000), storage.readRoster().orElseThrow().getPersonList().get(0).getPhone().value);
    }

    @Test
    public void clear_acceptsWords_blankSubmissionsKeepConfirmationArmedButFailuresCancelIt() throws Exception {
        JsonRosterStorage storage = new JsonRosterStorage(folder.resolve("roster.json"));
        Model model = new ModelManager();
        LogicManager logic = createLogic(model, storage);
        logic.execute("add n/Avery a/Open p/91234567 e/avery@example.com addr/Home");
        logic.execute("clear");
        logic.execute(" NO ");
        assertEquals(1, model.getRoster().getPersonList().size());
        for (String input : List.of("", " ")) {
            logic.execute("clear");
            assertTrue(logic.execute(input).isWarning()); // the warning is shown again and the clear stays armed
            assertThrows(ParseException.class, () -> logic.execute("invalid")); // a failing command disarms it
            assertThrows(ParseException.class, () -> logic.execute("yes"));
            assertEquals(1, storage.readRoster().orElseThrow().getPersonList().size());
        }
        logic.execute("clear");
        logic.execute("\u2003YeS\u00a0");
        assertTrue(storage.readRoster().orElseThrow().getPersonList().isEmpty());
    }

    @Test
    public void warnings_onlyRepeatWhenIdentityChanges_andRemarksDescribeTheActualChange() throws Exception {
        JsonRosterStorage storage = new JsonRosterStorage(folder.resolve("roster.json"));
        LogicManager logic = createLogic(new ModelManager(), storage);
        logic.execute("add n/Avery a/Open p/91234567 e/avery@example.com addr/Home");
        assertTrue(logic.execute("add n/Avery a/Open p/92345678 e/other@example.com addr/Home")
                .getFeedbackToUser().contains("Matches: name"));
        assertFalse(logic.execute("edit 2 addr/New home").getFeedbackToUser().contains("Warning:"));
        assertFalse(logic.execute("edit 2 p/9234 5678").getFeedbackToUser().contains("Warning:"));
        assertTrue(logic.execute("edit 2 e/new@example.com").getFeedbackToUser().contains("Matches: name"));
        assertTrue(logic.execute("remark 2 r/First").getFeedbackToUser().startsWith("Added remark"));
        assertTrue(logic.execute("remark 2 r/Second").getFeedbackToUser().startsWith("Updated remark"));
        String saved = Files.readString(storage.getRosterFilePath());
        CommandException unchanged = assertThrows(CommandException.class, () -> logic.execute("remark 2 r/Second"));
        assertTrue(unchanged.getMessage().startsWith(RemarkCommand.MESSAGE_UNCHANGED_REMARK.split("%1")[0]));
        assertEquals(saved, Files.readString(storage.getRosterFilePath()));
    }

    @Test
    public void slashErrors_explainEscaping_andEscapedValuesRoundTrip() throws Exception {
        JsonRosterStorage storage = new JsonRosterStorage(folder.resolve("roster.json"));
        LogicManager logic = createLogic(new ModelManager(), storage);
        logic.execute("add n/Avery a/Open p/91234567 e/avery@example.com addr/Home");
        for (String command : List.of("edit 1 addr/Home c/o Coach", "remark 1 r/Work w/ Coach",
                "edit 1 addr/Home a/Coach a/Open", "remark 1 r/Work r/Coach")) {
            ParseException error = assertThrows(ParseException.class, () -> logic.execute(command));
            assertTrue(error.getMessage().contains("double a parameter-like slash"));
        }
        logic.execute("edit 1 addr/c//o Coach");
        logic.execute("remark 1 r/w// Coach");
        Person saved = storage.readRoster().orElseThrow().getPersonList().get(0);
        assertEquals("c/o Coach", saved.getAddress().value);
        assertEquals("w/ Coach", saved.getRemark().value);
        assertEquals(Messages.MESSAGE_INVALID_PERSON_DISPLAYED_INDEX,
                assertThrows(ParseException.class, () -> logic.execute("delete 0")).getMessage());
    }

    @Test
    public void rosterChange_afterTwoSorts_keepsTiedAthletesInDisplayedOrder() throws Exception {
        JsonRosterStorage storage = new JsonRosterStorage(folder.resolve("roster.json"));
        LogicManager logic = createLogic(new ModelManager(SampleDataUtil.getSampleRoster(), new UserPrefs()),
                storage);
        logic.execute("sort name desc");
        logic.execute("sort age");
        List<String> shownBefore = getDisplayedNames(logic);
        assertEquals(List.of("Daniel Lim", "Avery Tan"), shownBefore.subList(0, 2));

        // Changing an untied athlete must not reorder the tied Under 14 athletes above it
        logic.execute("remark 9 r/note");
        logic.execute("tag 9 t/Relay");
        logic.execute("add n/Zed Ong a/Open p/81110000 e/zed@example.com addr/Home");
        assertEquals(shownBefore.subList(0, 2), getDisplayedNames(logic).subList(0, 2));

        logic.execute("delete 1");
        assertFalse(storage.readRoster().orElseThrow().getPersonList().stream()
                .anyMatch(person -> person.getName().fullName.equals("Daniel Lim")));
    }

    private static List<String> getDisplayedNames(LogicManager logic) {
        return logic.getFilteredPersonList().stream().map(person -> person.getName().fullName).toList();
    }
}
