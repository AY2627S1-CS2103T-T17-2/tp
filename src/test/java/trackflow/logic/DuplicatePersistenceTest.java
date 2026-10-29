package trackflow.logic;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static trackflow.testutil.Assert.assertThrows;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.Map;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import trackflow.logic.commands.AddCommand;
import trackflow.logic.commands.EditCommand;
import trackflow.logic.commands.exceptions.CommandException;
import trackflow.model.Model;
import trackflow.model.ModelManager;
import trackflow.model.UserPrefs;
import trackflow.model.person.Person;
import trackflow.storage.JsonRosterStorage;
import trackflow.storage.JsonUserPrefsStorage;
import trackflow.storage.StorageManager;

/**
 * Checks duplicate decisions and warnings across saved and filtered rosters.
 */
public class DuplicatePersistenceTest {

    @TempDir
    public Path temporaryFolder;

    @Test
    public void execute_duplicateHiddenAfterReload_rejectsAddAndEditWithoutChangingData() throws Exception {
        JsonRosterStorage rosterStorage = new JsonRosterStorage(temporaryFolder.resolve("trackflow.json"));
        StorageManager storage = new StorageManager(rosterStorage,
                new JsonUserPrefsStorage(temporaryFolder.resolve("preferences.json")));
        Logic initialLogic = new LogicManager(new ModelManager(), storage);
        initialLogic.execute("add n/Avery Tan a/Under 14 p/91234567 e/avery@example.com addr/Home");
        initialLogic.execute("add n/Jordan Lee a/Open p/(+65) 92345678 e/jordan@example.com addr/School");

        Model reloaded = new ModelManager(rosterStorage.readRoster().orElseThrow(), new UserPrefs());
        Logic logic = new LogicManager(reloaded, storage);
        logic.execute("find n/Jordan");
        List<Person> originalDisplay = List.copyOf(reloaded.getFilteredPersonList());
        String originalFile = Files.readString(rosterStorage.getRosterFilePath());

        Map<String, String> duplicateCommands = Map.of(
                "add n/AVERY   TAN a/Under 16 p/91234567 e/other@example.com addr/Elsewhere",
                String.format(AddCommand.MESSAGE_DUPLICATE_PERSON, "AVERY   TAN"),
                "add n/Avery Tan a/Open p/91234567 (Office 2) e/other@example.com addr/Elsewhere",
                String.format(AddCommand.MESSAGE_DUPLICATE_PERSON, "Avery Tan"),
                "add n/avery tan a/Open p/93456789 e/AVERY@example.com addr/Elsewhere",
                String.format(AddCommand.MESSAGE_DUPLICATE_PERSON, "avery tan"),
                "edit 1 n/AVERY   TAN e/AVERY@example.com",
                String.format(EditCommand.MESSAGE_DUPLICATE_PERSON, "AVERY   TAN"),
                "edit 1 n/Avery Tan p/91234567 (Office 2)",
                String.format(EditCommand.MESSAGE_DUPLICATE_PERSON, "Avery Tan"),
                "add n/Jordan Lee a/Open p/6592345678 (Office 2) e/other@example.com addr/Elsewhere",
                String.format(AddCommand.MESSAGE_DUPLICATE_PERSON, "Jordan Lee"));
        for (var entry : duplicateCommands.entrySet()) {
            assertThrows(CommandException.class, entry.getValue(), () -> logic.execute(entry.getKey()));
            assertEquals(originalDisplay, reloaded.getFilteredPersonList());
            assertEquals(originalFile, Files.readString(rosterStorage.getRosterFilePath()));
        }
        assertEquals(rosterStorage.readRoster().orElseThrow(), reloaded.getRoster());
    }

    @Test
    public void execute_sharedContactAfterReload_warnsOnAddAndEditAndPreservesDistinctAthletes() throws Exception {
        JsonRosterStorage rosterStorage = new JsonRosterStorage(temporaryFolder.resolve("trackflow.json"));
        StorageManager storage = new StorageManager(rosterStorage,
                new JsonUserPrefsStorage(temporaryFolder.resolve("preferences.json")));
        Logic initialLogic = new LogicManager(new ModelManager(), storage);
        initialLogic.execute("add n/Avery Tan a/Under 14 p/91234567 e/avery@example.com addr/Home");

        Model reloaded = new ModelManager(rosterStorage.readRoster().orElseThrow(), new UserPrefs());
        Logic logic = new LogicManager(reloaded, storage);
        logic.execute("find n/Nobody");
        String addFeedback = logic.execute(
                "add n/Jordan Lee a/Open p/91234567 e/jordan@example.com addr/School").getFeedbackToUser();
        assertEquals("New athlete added: Jordan Lee; Age category: Open; Phone: 91234567; Email: jordan@example.com"
                + String.format(Messages.MESSAGE_POSSIBLE_DUPLICATE, "Avery Tan"), addFeedback);

        logic.execute("find n/Jordan");
        String editFeedback = logic.execute("edit 1 p/92345678 e/AVERY@example.com").getFeedbackToUser();
        assertEquals("Edited athlete: Jordan Lee; Age category: Open; Phone: 92345678; Email: AVERY@example.com"
                + String.format(Messages.MESSAGE_POSSIBLE_DUPLICATE, "Avery Tan"), editFeedback);

        Model restarted = new ModelManager(rosterStorage.readRoster().orElseThrow(), new UserPrefs());
        assertEquals(reloaded.getRoster(), restarted.getRoster());
        assertEquals(2, restarted.getRoster().getPersonList().size());
        Person avery = restarted.getRoster().getPersonList().get(0);
        Person jordan = restarted.getRoster().getPersonList().get(1);
        assertFalse(avery.isSamePerson(jordan));
        assertTrue(avery.hasSameContactDetails(jordan));
    }
}
