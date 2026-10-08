package seedu.address.logic.commands;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static seedu.address.logic.commands.SortCommand.SortField.ADDRESS;
import static seedu.address.logic.commands.SortCommand.SortField.AGE;
import static seedu.address.logic.commands.SortCommand.SortField.EMAIL;
import static seedu.address.logic.commands.SortCommand.SortField.NAME;
import static seedu.address.logic.commands.SortCommand.SortField.PHONE;
import static seedu.address.logic.commands.SortCommand.SortOrder.ASCENDING;
import static seedu.address.logic.commands.SortCommand.SortOrder.DESCENDING;

import java.util.List;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import seedu.address.commons.core.index.Index;
import seedu.address.logic.commands.SortCommand.SortField;
import seedu.address.logic.commands.SortCommand.SortOrder;
import seedu.address.model.Model;
import seedu.address.model.ModelManager;
import seedu.address.model.UserPrefs;
import seedu.address.model.person.Person;
import seedu.address.testutil.AddressBookBuilder;
import seedu.address.testutil.PersonBuilder;

public class SortCommandTest {

    private static final Person ZOE = new PersonBuilder()
            .withName("zoe Tan")
            .withAgeCategory("Under 18")
            .withPhone("999")
            .withEmail("zulu@example.com")
            .withAddress("Zebra Road")
            .build();
    private static final Person AMY = new PersonBuilder()
            .withName("Amy Lim")
            .withAgeCategory("Under 14")
            .withPhone("1000")
            .withEmail("alpha@example.com")
            .withAddress("")
            .build();
    private static final Person MIA = new PersonBuilder()
            .withName("Mia Ong")
            .withAgeCategory("Open")
            .withPhone("10000")
            .withEmail("mike@example.com")
            .withAddress("Main Street")
            .build();
    private static final Person IAN = new PersonBuilder()
            .withName("Ian Koh")
            .withAgeCategory("Under 16")
            .withPhone("2000")
            .withEmail("ian@example.com")
            .withAddress("Hill Road")
            .build();
    private static final Person UMA = new PersonBuilder()
            .withName("Uma Lee")
            .withAgeCategory("Under 20")
            .withPhone("3000")
            .withEmail("uma@example.com")
            .withAddress("River Road")
            .build();

    private Model model;

    @BeforeEach
    public void setUp() {
        model = new ModelManager(new AddressBookBuilder()
                .withPerson(ZOE)
                .withPerson(MIA)
                .withPerson(AMY)
                .build(), new UserPrefs());
    }

    @Test
    public void execute_nameAscending_sortsAlphabeticallyIgnoringCase() {
        assertSortSuccess(NAME, ASCENDING, List.of(AMY, MIA, ZOE));
    }

    @Test
    public void execute_nameDescending_sortsReverseAlphabetically() {
        assertSortSuccess(NAME, DESCENDING, List.of(ZOE, MIA, AMY));
    }

    @Test
    public void execute_ageAscending_sortsByCompetitionCategory() {
        useModelWithAllAgeCategories();
        assertSortSuccess(AGE, ASCENDING, List.of(AMY, IAN, ZOE, UMA, MIA));
    }

    @Test
    public void execute_ageDescending_sortsByReverseCompetitionCategory() {
        useModelWithAllAgeCategories();
        assertSortSuccess(AGE, DESCENDING, List.of(MIA, UMA, ZOE, IAN, AMY));
    }

    @Test
    public void execute_phoneAscending_sortsNumerically() {
        assertSortSuccess(PHONE, ASCENDING, List.of(ZOE, AMY, MIA));
    }

    @Test
    public void execute_phoneDescending_sortsReverseNumerically() {
        assertSortSuccess(PHONE, DESCENDING, List.of(MIA, AMY, ZOE));
    }

    @Test
    public void execute_emailAscending_sortsAlphabeticallyIgnoringCase() {
        assertSortSuccess(EMAIL, ASCENDING, List.of(AMY, MIA, ZOE));
    }

    @Test
    public void execute_addressAscending_placesEmptyAddressFirst() {
        assertSortSuccess(ADDRESS, ASCENDING, List.of(AMY, MIA, ZOE));
    }

    @Test
    public void execute_addressDescending_placesEmptyAddressLast() {
        assertSortSuccess(ADDRESS, DESCENDING, List.of(ZOE, MIA, AMY));
    }

    @Test
    public void execute_filteredList_sortsMatchesAndPersistsAfterList() {
        model.updateFilteredPersonList(person -> !person.equals(MIA));
        new SortCommand(NAME, ASCENDING).execute(model);
        assertEquals(List.of(AMY, ZOE), model.getFilteredPersonList());

        new ListAthleteCommand().execute(model);
        assertEquals(List.of(AMY, MIA, ZOE), model.getFilteredPersonList());
    }

    @Test
    public void execute_emptyRoster_succeedsWithEmptyDisplay() {
        model = new ModelManager();

        assertSortSuccess(NAME, ASCENDING, List.of());
    }

    @Test
    public void execute_sortThenAdd_newAthleteAppearsInSortedPosition() {
        Person ben = new PersonBuilder()
                .withName("Ben Tan")
                .withAgeCategory("Under 16")
                .withPhone("4000")
                .withEmail("ben@example.com")
                .build();
        new SortCommand(NAME, ASCENDING).execute(model);

        model.addPerson(ben);

        assertEquals(List.of(AMY, ben, MIA, ZOE), model.getFilteredPersonList());
    }

    @Test
    public void execute_sortThenDelete_usesDisplayedSortedIndex() throws Exception {
        new SortCommand(NAME, ASCENDING).execute(model);
        new DeleteCommand(Index.fromOneBased(1)).execute(model);

        assertFalse(model.hasPerson(AMY));
        assertEquals(List.of(MIA, ZOE), model.getFilteredPersonList());
    }

    @Test
    public void equals() {
        SortCommand command = new SortCommand(NAME, ASCENDING);

        assertTrue(command.equals(command));
        assertTrue(command.equals(new SortCommand(NAME, ASCENDING)));
        assertFalse(command.equals(new SortCommand(NAME, DESCENDING)));
        assertFalse(command.equals(new SortCommand(AGE, ASCENDING)));
        assertFalse(command.equals(null));
        assertFalse(command.equals(new ClearCommand()));
    }

    @Test
    public void toStringMethod() {
        SortCommand command = new SortCommand(AGE, DESCENDING);
        String expected = SortCommand.class.getCanonicalName() + "{field=AGE, order=DESCENDING}";
        assertEquals(expected, command.toString());
    }

    private void assertSortSuccess(SortField field, SortOrder order, List<Person> expectedOrder) {
        SortCommand command = new SortCommand(field, order);
        CommandResult result = command.execute(model);
        String expectedMessage = String.format(SortCommand.MESSAGE_SUCCESS,
                field == AGE ? "age category" : field.name().toLowerCase(),
                order == ASCENDING ? "ascending" : "descending");
        assertEquals(expectedMessage, result.getFeedbackToUser());
        assertEquals(expectedOrder, model.getFilteredPersonList());
    }

    private void useModelWithAllAgeCategories() {
        model = new ModelManager(new AddressBookBuilder()
                .withPerson(ZOE)
                .withPerson(MIA)
                .withPerson(AMY)
                .withPerson(UMA)
                .withPerson(IAN)
                .build(), new UserPrefs());
    }
}
