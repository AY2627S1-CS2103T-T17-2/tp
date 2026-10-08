package seedu.address.logic;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static seedu.address.logic.Messages.MESSAGE_EMPTY_COMMAND;
import static seedu.address.logic.Messages.MESSAGE_INVALID_PERSON_DISPLAYED_INDEX;
import static seedu.address.logic.Messages.MESSAGE_UNKNOWN_COMMAND;
import static seedu.address.logic.commands.CommandTestUtil.EMAIL_DESC_AMY;
import static seedu.address.logic.commands.CommandTestUtil.NAME_DESC_AMY;
import static seedu.address.logic.commands.CommandTestUtil.PHONE_DESC_AMY;
import static seedu.address.testutil.Assert.assertThrows;
import static seedu.address.testutil.TypicalPersons.AMY;

import java.io.IOException;
import java.nio.file.AccessDeniedException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import seedu.address.logic.commands.AddCommand;
import seedu.address.logic.commands.CommandResult;
import seedu.address.logic.commands.ListAthleteCommand;
import seedu.address.logic.commands.exceptions.CommandException;
import seedu.address.logic.parser.exceptions.ParseException;
import seedu.address.model.Model;
import seedu.address.model.ModelManager;
import seedu.address.model.ReadOnlyAddressBook;
import seedu.address.model.UserPrefs;
import seedu.address.model.person.AgeCategory;
import seedu.address.model.person.Person;
import seedu.address.storage.JsonAddressBookStorage;
import seedu.address.storage.JsonUserPrefsStorage;
import seedu.address.storage.StorageManager;
import seedu.address.testutil.PersonBuilder;

public class LogicManagerTest {
    private static final IOException DUMMY_IO_EXCEPTION = new IOException("dummy IO exception");
    private static final IOException DUMMY_AD_EXCEPTION = new AccessDeniedException("dummy access denied exception");

    @TempDir
    public Path temporaryFolder;

    private Model model = new ModelManager();
    private Logic logic;

    @BeforeEach
    public void setUp() {
        JsonAddressBookStorage addressBookStorage =
                new JsonAddressBookStorage(temporaryFolder.resolve("addressBook.json"));
        JsonUserPrefsStorage userPrefsStorage = new JsonUserPrefsStorage(temporaryFolder.resolve("userPrefs.json"));
        StorageManager storage = new StorageManager(addressBookStorage, userPrefsStorage);
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
    public void execute_mixedCaseListWithAthlete_returnsAthleteCount() throws Exception {
        logic.execute("add n/Avery Tan a/Under 14 p/91234567 e/avery.tan@example.com");
        Model expectedModel = new ModelManager(model.getAddressBook(), new UserPrefs());
        assertCommandSuccess("LiSt", ListAthleteCommand.MESSAGE_SUCCESS_SINGLE_ATHLETE, expectedModel);
    }

    @Test
    public void execute_listWithArguments_throwsParseException() {
        assertParseException("list 1", ListAthleteCommand.MESSAGE_USAGE);
    }

    @Test
    public void execute_editAthlete_persistsCategoryAndPreservesOtherDetails() throws Exception {
        String command = "add n/Avery Tan a/Under 14 p/91234567 e/avery.tan@example.com";
        assertEquals("New athlete added: Avery Tan; Age category: Under 14; Phone: 91234567; "
                + "Email: avery.tan@example.com", logic.execute(command).getFeedbackToUser());
        logic.execute("edit 1 a/Under 16 addr/Training centre p/92345678 t/sprinter t/relay");
        logic.execute("remark 1 r/Sprints");
        Person athlete = model.getFilteredPersonList().get(0);
        assertEquals(new AgeCategory("Under 16"), athlete.getAgeCategory());
        assertEquals("Sprints", athlete.getRemark().value);
        assertEquals("Training centre", athlete.getAddress().value);
        assertEquals(2, athlete.getTags().size());
        assertEquals("92345678", athlete.getPhone().value);
        JsonAddressBookStorage reloadedStorage =
                new JsonAddressBookStorage(temporaryFolder.resolve("addressBook.json"));
        Model reloaded = new ModelManager(reloadedStorage.readAddressBook().orElseThrow(), new UserPrefs());
        assertEquals(athlete, reloaded.getFilteredPersonList().get(0));
        assertEquals("Sprints", reloaded.getFilteredPersonList().get(0).getRemark().value);
    }

    @Test
    public void execute_sameNameMatchingContact_rejectsDuplicates() throws Exception {
        logic.execute("add n/Avery Tan a/Under 14 p/91234567 e/avery.tan@example.com");
        String[] variants = {"a/Under 16 p/91234567 e/avery.tan@example.com",
            "a/Under 14 p/92345678 e/avery.tan@example.com",
            "a/Under 14 p/91234567 e/other@example.com"};
        for (String variant : variants) {
            assertThrows(CommandException.class, () -> logic.execute("add n/Avery Tan " + variant));
        }
        assertEquals(1, model.getFilteredPersonList().size());
        assertThrows(CommandException.class, "This athlete already exists in the roster: AVERY   TAN.", () ->
            logic.execute("add n/AVERY   TAN a/under 14 p/91234567 e/AVERY.TAN@example.com"));
        assertEquals(1, model.getFilteredPersonList().size());
    }

    @Test
    public void execute_addAthleteWithoutOptionalData_survivesReload() throws Exception {
        logic.execute("add n/Avery Tan a/under   16 p/91234567 e/Avery.Tan@example.com");
        JsonAddressBookStorage storage = new JsonAddressBookStorage(temporaryFolder.resolve("addressBook.json"));
        Model reloaded = new ModelManager(storage.readAddressBook().orElseThrow(), new UserPrefs());
        Person athlete = reloaded.getFilteredPersonList().get(0);
        assertEquals(1, reloaded.getFilteredPersonList().size());
        assertEquals("Avery Tan", athlete.getName().fullName);
        assertEquals("Under 16", athlete.getAgeCategory().value);
        assertEquals("91234567", athlete.getPhone().value);
        assertEquals("Avery.Tan@example.com", athlete.getEmail().value);
        assertEquals("", athlete.getAddress().value);
        assertEquals("", athlete.getRemark().value);
        assertEquals(0, athlete.getTags().size());
    }

    @Test
    public void execute_addAthleteWithAllFields_survivesReload() throws Exception {
        logic.execute("add n/Avery Tan a/Under 16 p/91234567 e/avery.tan@example.com"
                + " addr/123 Main Street r/Prefers morning training t/sprints t/relay t/sprints");
        JsonAddressBookStorage storage = new JsonAddressBookStorage(temporaryFolder.resolve("addressBook.json"));
        Model reloaded = new ModelManager(storage.readAddressBook().orElseThrow(), new UserPrefs());
        Person athlete = reloaded.getFilteredPersonList().get(0);
        assertEquals(1, reloaded.getFilteredPersonList().size());
        assertEquals("Avery Tan", athlete.getName().fullName);
        assertEquals("Under 16", athlete.getAgeCategory().value);
        assertEquals("91234567", athlete.getPhone().value);
        assertEquals("avery.tan@example.com", athlete.getEmail().value);
        assertEquals("123 Main Street", athlete.getAddress().value);
        assertEquals("Prefers morning training", athlete.getRemark().value);
        assertEquals(new PersonBuilder().withTags("sprints", "relay").build().getTags(), athlete.getTags());
    }

    @Test
    public void execute_invalidAdd_preservesRosterAndSavedFile() throws Exception {
        String validCommand = "add n/Avery Tan a/Under 14 p/91234567 e/avery.tan@example.com";
        logic.execute(validCommand);
        Path savedPath = temporaryFolder.resolve("addressBook.json");
        String savedData = Files.readString(savedPath);
        Model expectedModel = new ModelManager(model.getAddressBook(), new UserPrefs());
        String[] invalidCommands = {validCommand.replace(" a/Under 14", ""),
            validCommand.replace("Under 14", "Under 15"),
            validCommand.replace("91234567", "+65 9123 4567"),
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
        logic.execute("add n/Zoe Tan a/Under 18 p/999 e/zoe@example.com");
        logic.execute("add n/Amy Lim a/Under 14 p/1000 e/amy@example.com");

        assertEquals("Sorted the displayed athlete list by name in ascending order.",
                logic.execute("sort name").getFeedbackToUser());
        assertEquals(List.of("Amy Lim", "Zoe Tan"), getDisplayedNames(model));
        assertEquals(List.of("Zoe Tan", "Amy Lim"), getStoredNames(model.getAddressBook()));

        JsonAddressBookStorage storage =
                new JsonAddressBookStorage(temporaryFolder.resolve("addressBook.json"));
        ReadOnlyAddressBook reloadedAddressBook = storage.readAddressBook().orElseThrow();
        assertEquals(List.of("Zoe Tan", "Amy Lim"), getStoredNames(reloadedAddressBook));
    }

    @Test
    public void execute_duplicateAfterReload_preservesRosterAndSavedFile() throws Exception {
        logic.execute("add n/Avery Tan a/Under 14 p/91234567 e/avery.tan@example.com");
        Path savedPath = temporaryFolder.resolve("addressBook.json");
        String savedData = Files.readString(savedPath);
        JsonAddressBookStorage storage = new JsonAddressBookStorage(savedPath);
        Model reloaded = new ModelManager(storage.readAddressBook().orElseThrow(), new UserPrefs());
        Logic reloadedLogic = new LogicManager(reloaded, new StorageManager(storage,
                new JsonUserPrefsStorage(temporaryFolder.resolve("reloadedPrefs.json"))));
        assertThrows(CommandException.class, "This athlete already exists in the roster: avery   tan.", () ->
            reloadedLogic.execute("add n/avery   tan a/UNDER 14 p/91234567 e/AVERY.TAN@example.com"));
        assertEquals(model.getAddressBook(), reloaded.getAddressBook());
        assertEquals(savedData, Files.readString(savedPath));
    }

    @Test
    public void execute_storageThrowsIoException_throwsCommandException() {
        assertCommandFailureForExceptionFromStorage(DUMMY_IO_EXCEPTION, String.format(
                LogicManager.FILE_OPS_ERROR_FORMAT, DUMMY_IO_EXCEPTION.getMessage()));
    }

    @Test
    public void execute_storageThrowsAdException_throwsCommandException() {
        assertCommandFailureForExceptionFromStorage(DUMMY_AD_EXCEPTION, String.format(
                LogicManager.FILE_OPS_PERMISSION_ERROR_FORMAT, DUMMY_AD_EXCEPTION.getMessage()));
    }

    @Test
    public void getFilteredPersonList_modifyList_throwsUnsupportedOperationException() {
        assertThrows(UnsupportedOperationException.class, () -> logic.getFilteredPersonList().remove(0));
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
        Model expectedModel = new ModelManager(model.getAddressBook(), new UserPrefs());
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

        // Inject LogicManager with a JsonAddressBookStorage that throws the IOException e when saving
        JsonAddressBookStorage addressBookStorage = new JsonAddressBookStorage(prefPath) {
            @Override
            public void saveAddressBook(ReadOnlyAddressBook addressBook) throws IOException {
                throw e;
            }
        };

        JsonUserPrefsStorage userPrefsStorage =
                new JsonUserPrefsStorage(temporaryFolder.resolve("ExceptionUserPrefs.json"));
        StorageManager storage = new StorageManager(addressBookStorage, userPrefsStorage);

        logic = new LogicManager(model, storage);

        // Triggers the saveAddressBook method by executing an add command
        String addCommand = AddCommand.COMMAND_WORD + NAME_DESC_AMY + PHONE_DESC_AMY
                + EMAIL_DESC_AMY + " a/Open";
        Person expectedPerson = new PersonBuilder(AMY).withTags().withAddress("").withRemark("").build();
        ModelManager expectedModel = new ModelManager();
        expectedModel.addPerson(expectedPerson);
        assertCommandFailure(addCommand, CommandException.class, expectedMessage, expectedModel);
    }

    private static List<String> getDisplayedNames(Model model) {
        return model.getFilteredPersonList().stream()
                .map(person -> person.getName().fullName)
                .toList();
    }

    private static List<String> getStoredNames(ReadOnlyAddressBook addressBook) {
        return addressBook.getPersonList().stream()
                .map(person -> person.getName().fullName)
                .toList();
    }
}
