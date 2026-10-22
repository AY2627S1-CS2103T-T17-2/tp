package trackflow.logic;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertSame;
import static trackflow.logic.Messages.MESSAGE_EMPTY_COMMAND;
import static trackflow.logic.Messages.MESSAGE_INVALID_PERSON_DISPLAYED_INDEX;
import static trackflow.logic.Messages.MESSAGE_UNKNOWN_COMMAND;
import static trackflow.logic.commands.CommandTestUtil.EMAIL_DESC_AMY;
import static trackflow.logic.commands.CommandTestUtil.NAME_DESC_AMY;
import static trackflow.logic.commands.CommandTestUtil.PHONE_DESC_AMY;
import static trackflow.testutil.Assert.assertThrows;

import java.io.IOException;
import java.nio.file.AccessDeniedException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import trackflow.commons.core.GuiSettings;
import trackflow.logic.commands.AddCommand;
import trackflow.logic.commands.CommandResult;
import trackflow.logic.commands.ListAthleteCommand;
import trackflow.logic.commands.exceptions.CommandException;
import trackflow.logic.parser.exceptions.ParseException;
import trackflow.model.Model;
import trackflow.model.ModelManager;
import trackflow.model.ReadOnlyRoster;
import trackflow.model.UserPrefs;
import trackflow.model.person.AgeCategory;
import trackflow.model.person.Person;
import trackflow.storage.JsonRosterStorage;
import trackflow.storage.JsonUserPrefsStorage;
import trackflow.storage.StorageManager;

public class LogicManagerTest {
    private static final IOException DUMMY_IO_EXCEPTION = new IOException("dummy IO exception");
    private static final IOException DUMMY_AD_EXCEPTION = new AccessDeniedException("dummy access denied exception");

    @TempDir
    public Path temporaryFolder;

    private Model model = new ModelManager();
    private Logic logic;

    @BeforeEach
    public void setUp() {
        JsonRosterStorage rosterStorage =
                new JsonRosterStorage(temporaryFolder.resolve("roster.json"));
        JsonUserPrefsStorage userPrefsStorage = new JsonUserPrefsStorage(temporaryFolder.resolve("userPrefs.json"));
        StorageManager storage = new StorageManager(rosterStorage, userPrefsStorage);
        logic = new LogicManager(model, storage);
    }

    @Test
    public void execute_invalidCommandFormat_throwsParseException() {
        String invalidCommand = "uicfhmowqewca";
        assertParseException(invalidCommand, String.format(MESSAGE_UNKNOWN_COMMAND, invalidCommand));
    }

    @Test
    public void execute_emptyCommand_throwsParseException() {
        assertParseException("", MESSAGE_EMPTY_COMMAND);
    }

    @Test
    public void execute_commandExecutionError_throwsCommandException() {
        String deleteCommand = "delete 9";
        assertCommandException(deleteCommand, MESSAGE_INVALID_PERSON_DISPLAYED_INDEX);
    }

    @Test
    public void execute_validCommand_success() throws Exception {
        String listCommand = ListAthleteCommand.COMMAND_WORD;
        assertCommandSuccess(listCommand, ListAthleteCommand.MESSAGE_EMPTY_ROSTER, model);
    }

    @Test
    public void execute_rosterChangingCommand_reportsDataSaved() throws Exception {
        CommandResult result = logic.execute(
                "add n/Avery Tan a/Under 14 p/91234567 e/avery@example.com addr/123 Main Street");

        assertEquals(true, result.isDataSaved());
    }

    @Test
    public void execute_viewCommandsWithUnavailableStorage_doNotSave() throws Exception {
        JsonRosterStorage unavailableStorage = new JsonRosterStorage(temporaryFolder.resolve("unavailable.json")) {
            @Override
            public void saveRoster(ReadOnlyRoster roster) throws IOException {
                throw DUMMY_IO_EXCEPTION;
            }
        };
        Logic readOnlyLogic = new LogicManager(model, new StorageManager(unavailableStorage,
                new JsonUserPrefsStorage(temporaryFolder.resolve("readOnlyPrefs.json"))));

        for (String command : List.of("list", "find amy", "filter a/Open", "sort name", "help", "exit")) {
            assertEquals(false, readOnlyLogic.execute(command).isDataSaved());
        }
    }

    @Test
    public void execute_mixedCaseListWithAthlete_returnsAthleteCount() throws Exception {
        logic.execute("add n/Avery Tan a/Under 14 p/91234567 e/avery.tan@example.com addr/123 Main Street");
        Model expectedModel = new ModelManager(model.getRoster(), new UserPrefs());
        assertCommandSuccess("LiSt", ListAthleteCommand.MESSAGE_SUCCESS_SINGLE_ATHLETE, expectedModel);
    }

    @Test
    public void execute_editAthlete_persistsCategoryAndPreservesOtherDetails() throws Exception {
        String command = "add n/Avery Tan a/Under 14 p/91234567 e/avery.tan@example.com addr/123 Main Street";
        assertEquals("New athlete added: Avery Tan; Age category: Under 14; Phone: 91234567; "
                + "Email: avery.tan@example.com", logic.execute(command).getFeedbackToUser());
        logic.execute("edit 1 a/Under 16 addr/Training centre p/92345678");
        logic.execute("remark 1 r/Sprints");
        Person athlete = model.getFilteredPersonList().get(0);
        assertEquals(new AgeCategory("Under 16"), athlete.getAgeCategory());
        assertEquals("Sprints", athlete.getRemark().value);
        assertEquals("Training centre", athlete.getAddress().value);
        assertEquals(0, athlete.getTags().size());
        assertEquals("92345678", athlete.getPhone().value);
        JsonRosterStorage reloadedStorage =
                new JsonRosterStorage(temporaryFolder.resolve("roster.json"));
        Model reloaded = new ModelManager(reloadedStorage.readRoster().orElseThrow(), new UserPrefs());
        assertEquals(athlete, reloaded.getFilteredPersonList().get(0));
    }

    @Test
    public void execute_sameNameMatchingContact_rejectsDuplicates() throws Exception {
        logic.execute("add n/Avery Tan a/Under 14 p/91234567 e/avery.tan@example.com addr/123 Main Street");
        String[] variants = {"a/Under 16 p/91234567 e/avery.tan@example.com",
            "a/Under 14 p/92345678 e/avery.tan@example.com",
            "a/Under 14 p/91234567 e/other@example.com"};
        for (String variant : variants) {
            assertThrows(CommandException.class, () ->
                logic.execute("add n/Avery Tan " + variant + " addr/123 Main Street"));
        }
        assertEquals(1, model.getFilteredPersonList().size());
        assertThrows(CommandException.class, "This athlete already exists in the roster: AVERY   TAN.", () ->
            logic.execute("add n/AVERY   TAN a/under 14 p/91234567 e/AVERY.TAN@example.com addr/123 Main Street"));
        assertEquals(1, model.getFilteredPersonList().size());
    }

    @Test
    public void execute_addAthleteWithRequiredAddress_survivesReload() throws Exception {
        logic.execute("add n/Avery Tan a/under   16 p/91234567 e/Avery.Tan@example.com addr/123 Main Street");
        JsonRosterStorage storage = new JsonRosterStorage(temporaryFolder.resolve("roster.json"));
        Model reloaded = new ModelManager(storage.readRoster().orElseThrow(), new UserPrefs());
        Person athlete = reloaded.getFilteredPersonList().get(0);
        assertEquals(1, reloaded.getFilteredPersonList().size());
        assertEquals("Avery Tan", athlete.getName().fullName);
        assertEquals("Under 16", athlete.getAgeCategory().value);
        assertEquals("91234567", athlete.getPhone().value);
        assertEquals("Avery.Tan@example.com", athlete.getEmail().value);
        assertEquals("123 Main Street", athlete.getAddress().value);
        assertEquals("", athlete.getRemark().value);
        assertEquals(0, athlete.getTags().size());
    }

    @Test
    public void execute_invalidAdd_preservesRosterAndSavedFile() throws Exception {
        String validCommand = "add n/Avery Tan a/Under 14 p/91234567 e/avery.tan@example.com addr/123 Main Street";
        logic.execute(validCommand);
        Path savedPath = temporaryFolder.resolve("roster.json");
        String savedData = Files.readString(savedPath);
        Model expectedModel = new ModelManager(model.getRoster(), new UserPrefs());
        String[] invalidCommands = {validCommand.replace(" a/Under 14", ""),
            validCommand.replace(" addr/123 Main Street", ""),
            validCommand.replace("addr/123 Main Street", "addr/"),
            validCommand.replace("Under 14", "Under 15"),
            validCommand.replace("91234567", "911a"),
            validCommand.replace("avery.tan@example.com", "invalid"),
            validCommand + " n/Other Athlete", validCommand + " t/invalid-tag", validCommand + " r/note r/other",
            validCommand + " addr/"};
        for (String invalidCommand : invalidCommands) {
            assertThrows(ParseException.class, () -> logic.execute(invalidCommand));
            assertEquals(expectedModel, model);
            assertEquals(savedData, Files.readString(savedPath));
        }
    }

    @Test
    public void execute_sort_changesDisplayButPreservesSavedRosterOrder() throws Exception {
        logic.execute("add n/Zoe Tan a/Under 18 p/999 e/zoe@example.com addr/123 Main Street");
        logic.execute("add n/Amy Lim a/Under 14 p/1000 e/amy@example.com addr/123 Main Street");

        assertEquals("Sorted the displayed athlete list by name in ascending order.",
                logic.execute("sort name").getFeedbackToUser());
        assertEquals(List.of("Amy Lim", "Zoe Tan"), getDisplayedNames(model));
        assertEquals(List.of("Zoe Tan", "Amy Lim"), getStoredNames(model.getRoster()));

        JsonRosterStorage storage =
                new JsonRosterStorage(temporaryFolder.resolve("roster.json"));
        ReadOnlyRoster reloadedRoster = storage.readRoster().orElseThrow();
        assertEquals(List.of("Zoe Tan", "Amy Lim"), getStoredNames(reloadedRoster));
    }

    @Test
    public void execute_deleteAfterSort_usesSortedDisplayIndex() throws Exception {
        logic.execute("add n/Amy Lim a/Under 14 p/1000 e/amy@example.com addr/123 Main Street");
        logic.execute("add n/Zoe Tan a/Under 18 p/999 e/zoe@example.com addr/123 Main Street");
        logic.execute("sort name desc");
        assertEquals(List.of("Zoe Tan", "Amy Lim"), getDisplayedNames(model));

        logic.execute("delete 1");
        assertEquals(List.of("Amy Lim"), getDisplayedNames(model));
        assertEquals(List.of("Amy Lim"), getStoredNames(model.getRoster()));
    }

    @Test
    public void execute_storageThrowsIoException_throwsCommandException() {
        assertCommandFailureForExceptionFromStorage(DUMMY_IO_EXCEPTION, String.format(
                LogicManager.FILE_OPS_ERROR_FORMAT, DUMMY_IO_EXCEPTION.getMessage()));
    }

    @Test
    public void execute_storageThrowsAdException_throwsCommandException() {
        // The permission error names the roster file, not the internal temporary file.
        Path rosterPath = temporaryFolder.resolve("ExceptionUserPrefs.json");
        assertCommandFailureForExceptionFromStorage(DUMMY_AD_EXCEPTION, String.format(
                LogicManager.FILE_OPS_PERMISSION_ERROR_FORMAT, rosterPath));
    }

    @Test
    public void execute_failedRosterChanges_preserveModelDisplayAndSavedFile() throws Exception {
        logic.execute("add n/Zoe Tan a/Under 14 p/999 e/zoe@example.com addr/Home");
        logic.execute("add n/Amy Lim a/Open p/1000 e/amy@example.com addr/Home");
        logic.execute("sort name desc");
        logic.execute("find n/zoe");

        ReadOnlyRoster rosterBeforeFailure = new ModelManager(model.getRoster(), model.getUserPrefs()).getRoster();
        List<Person> displayBeforeFailure = List.copyOf(model.getFilteredPersonList());
        var predicateBeforeFailure = model.getPersonListPredicate();
        var comparatorBeforeFailure = model.getPersonListComparator();
        Path savedPath = temporaryFolder.resolve("roster.json");
        String savedDataBeforeFailure = Files.readString(savedPath);
        int[] publishedChanges = {0};
        model.getRoster().getPersonList().addListener(
                (javafx.collections.ListChangeListener<Person>) change -> publishedChanges[0]++);

        JsonRosterStorage failingStorage = new JsonRosterStorage(savedPath) {
            @Override
            public void saveRoster(ReadOnlyRoster roster) throws IOException {
                throw DUMMY_IO_EXCEPTION;
            }
        };
        Logic failingLogic = new LogicManager(model, new StorageManager(failingStorage,
                new JsonUserPrefsStorage(temporaryFolder.resolve("failingPrefs.json"))));

        for (String command : List.of("add n/Ben Lee a/Open p/222 e/ben@example.com addr/Home",
                "edit 1 n/Zed Tan", "remark 1 r/Injured", "delete 1", "clear")) {
            assertThrows(CommandException.class, () -> failingLogic.execute(command));
            assertEquals(rosterBeforeFailure, model.getRoster());
            assertEquals(displayBeforeFailure, model.getFilteredPersonList());
            assertSame(predicateBeforeFailure, model.getPersonListPredicate());
            assertSame(comparatorBeforeFailure, model.getPersonListComparator());
            assertEquals(savedDataBeforeFailure, Files.readString(savedPath));
            assertEquals(0, publishedChanges[0]);
        }
    }

    @Test
    public void getFilteredPersonList_modifyList_throwsUnsupportedOperationException() {
        assertThrows(UnsupportedOperationException.class, () -> logic.getFilteredPersonList().remove(0));
    }

    @Test
    public void guiSettings_getAndSet_delegatesToModel() {
        GuiSettings customSettings = new GuiSettings(1280, 720, 100, 200);
        logic.setGuiSettings(customSettings);
        assertEquals(customSettings, logic.getGuiSettings());
        assertEquals(customSettings, model.getGuiSettings());
    }

    /**
     * Executes the command and confirms that
     * - no exceptions are thrown <br>
     * - the feedback message is equal to {@code expectedMessage} <br>
     * - the internal model manager state is the same as that in {@code expectedModel} <br>
     * @see #assertCommandFailure(String, Class, String, Model)
     */
    private void assertCommandSuccess(String inputCommand, String expectedMessage,
            Model expectedModel) throws CommandException, ParseException {
        CommandResult result = logic.execute(inputCommand);
        assertEquals(expectedMessage, result.getFeedbackToUser());
        assertEquals(expectedModel, model);
    }

    /**
     * Executes the command, confirms that a ParseException is thrown and that the result message is correct.
     * @see #assertCommandFailure(String, Class, String, Model)
     */
    private void assertParseException(String inputCommand, String expectedMessage) {
        assertCommandFailure(inputCommand, ParseException.class, expectedMessage);
    }

    /**
     * Executes the command, confirms that a CommandException is thrown and that the result message is correct.
     * @see #assertCommandFailure(String, Class, String, Model)
     */
    private void assertCommandException(String inputCommand, String expectedMessage) {
        assertCommandFailure(inputCommand, CommandException.class, expectedMessage);
    }

    /**
     * Executes the command, confirms that the exception is thrown and that the result message is correct.
     * @see #assertCommandFailure(String, Class, String, Model)
     */
    private void assertCommandFailure(String inputCommand, Class<? extends Throwable> expectedException,
            String expectedMessage) {
        Model expectedModel = new ModelManager(model.getRoster(), new UserPrefs());
        assertCommandFailure(inputCommand, expectedException, expectedMessage, expectedModel);
    }

    /**
     * Executes the command and confirms that
     * - the {@code expectedException} is thrown <br>
     * - the resulting error message is equal to {@code expectedMessage} <br>
     * - the internal model manager state is the same as that in {@code expectedModel} <br>
     * @see #assertCommandSuccess(String, String, Model)
     */
    private void assertCommandFailure(String inputCommand, Class<? extends Throwable> expectedException,
            String expectedMessage, Model expectedModel) {
        assertThrows(expectedException, expectedMessage, () -> logic.execute(inputCommand));
        assertEquals(expectedModel, model);
    }

    /**
     * Tests the Logic component's handling of an {@code IOException} thrown by the Storage component.
     *
     * @param e the exception to be thrown by the Storage component
     * @param expectedMessage the message expected inside exception thrown by the Logic component
     */
    private void assertCommandFailureForExceptionFromStorage(IOException e, String expectedMessage) {
        Path prefPath = temporaryFolder.resolve("ExceptionUserPrefs.json");

        // Inject LogicManager with a JsonRosterStorage that throws the IOException e when saving
        JsonRosterStorage rosterStorage = new JsonRosterStorage(prefPath) {
            @Override
            public void saveRoster(ReadOnlyRoster roster) throws IOException {
                throw e;
            }
        };

        JsonUserPrefsStorage userPrefsStorage =
                new JsonUserPrefsStorage(temporaryFolder.resolve("ExceptionUserPrefs.json"));
        StorageManager storage = new StorageManager(rosterStorage, userPrefsStorage);

        logic = new LogicManager(model, storage);

        // Triggers the saveRoster method by executing an add command
        String addCommand = AddCommand.COMMAND_WORD + NAME_DESC_AMY + PHONE_DESC_AMY
                + EMAIL_DESC_AMY + " a/Open addr/123 Main Street";
        ModelManager expectedModel = new ModelManager();
        assertCommandFailure(addCommand, CommandException.class, expectedMessage, expectedModel);
    }

    private static List<String> getDisplayedNames(Model model) {
        return model.getFilteredPersonList().stream()
                .map(person -> person.getName().fullName)
                .toList();
    }

    private static List<String> getStoredNames(ReadOnlyRoster roster) {
        return roster.getPersonList().stream()
                .map(person -> person.getName().fullName)
                .toList();
    }
}
