package trackflow.logic.commands;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static trackflow.logic.commands.SortCommand.SortField.ADDRESS;
import static trackflow.logic.commands.SortCommand.SortField.AGE;
import static trackflow.logic.commands.SortCommand.SortField.EMAIL;
import static trackflow.logic.commands.SortCommand.SortField.NAME;
import static trackflow.logic.commands.SortCommand.SortField.PHONE;
import static trackflow.logic.commands.SortCommand.SortOrder.ASCENDING;
import static trackflow.logic.commands.SortCommand.SortOrder.DESCENDING;

import java.util.List;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import trackflow.commons.core.index.Index;
import trackflow.logic.commands.SortCommand.SortField;
import trackflow.logic.commands.SortCommand.SortOrder;
import trackflow.model.Model;
import trackflow.model.ModelManager;
import trackflow.model.UserPrefs;
import trackflow.model.person.Person;
import trackflow.testutil.AthleteRosterBuilder;
import trackflow.testutil.PersonBuilder;

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
            .withEmail("MIKE@example.com")
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
        model = new ModelManager(new AthleteRosterBuilder()
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
    public void execute_nameWithRepeatedSpaces_sortsWithoutChangingNames() {
        Person leeAnn = new PersonBuilder(AMY).withName("Lee Ann").build();
        Person leeBob = new PersonBuilder(MIA).withName("Lee  Bob").build();
        Person leeAmy = new PersonBuilder(ZOE).withName("lee   Amy").build();
        model = new ModelManager(new AthleteRosterBuilder().withPerson(leeBob)
                .withPerson(leeAnn).withPerson(leeAmy).build(), new UserPrefs());

        assertSortSuccess(NAME, ASCENDING, List.of(leeAmy, leeAnn, leeBob));
        assertSortSuccess(NAME, DESCENDING, List.of(leeBob, leeAnn, leeAmy));
        assertEquals(List.of("Lee  Bob", "Lee Ann", "lee   Amy"), model.getRoster().getPersonList().stream()
                .map(person -> person.getName().fullName).toList());
    }

    @Test
    public void execute_namesDifferingOnlyInCaseAndSpacing_compareAsEqual() {
        Person leeAnn = new PersonBuilder(AMY).withName("Lee Ann").build();
        Person equivalentName = new PersonBuilder(MIA).withName("LEE   Ann").build();
        model = new ModelManager(new AthleteRosterBuilder().withPerson(leeAnn)
                .withPerson(equivalentName).build(), new UserPrefs());

        new SortCommand(NAME, ASCENDING).execute(model);

        assertEquals(0, model.getPersonListComparator().compare(leeAnn, equivalentName));
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
    public void execute_formattedPhones_sortsDigitsWithoutChangingStoredValues() {
        for (String phone : List.of("1 000", "+1-000", "(+1) 000", "1000 (HP)", "000 1000")) {
            Person formattedAmy = new PersonBuilder(AMY).withPhone(phone).build();
            Person longPhoneMia = new PersonBuilder(MIA).withPhone("9".repeat(100)).build();
            model = new ModelManager(new AthleteRosterBuilder().withPerson(longPhoneMia)
                    .withPerson(formattedAmy).withPerson(ZOE).build(), new UserPrefs());

            assertSortSuccess(PHONE, ASCENDING, List.of(ZOE, formattedAmy, longPhoneMia));
            assertSortSuccess(PHONE, DESCENDING, List.of(longPhoneMia, formattedAmy, ZOE));
            assertEquals(phone, formattedAmy.getPhone().value);
        }
    }

    @Test
    public void execute_phoneNotes_sortsSignificantDigitsAndKeepsCountryCode() {
        for (String phone : List.of("200 (5)", "2 00 (Office 9)", "000200 (1234)")) {
            Person withNote = new PersonBuilder(ZOE).withPhone(phone).build();
            Person withoutNote = new PersonBuilder(AMY).withPhone("300").build();
            Person withCountryCode = new PersonBuilder(MIA).withPhone("(+65) 200").build();
            model = new ModelManager(new AthleteRosterBuilder().withPerson(withCountryCode)
                    .withPerson(withoutNote).withPerson(withNote).build(), new UserPrefs());

            assertSortSuccess(PHONE, ASCENDING, List.of(withNote, withoutNote, withCountryCode));
            assertSortSuccess(PHONE, DESCENDING, List.of(withCountryCode, withoutNote, withNote));
            assertEquals(phone, withNote.getPhone().value);
            assertEquals("(+65) 200", withCountryCode.getPhone().value);
        }
    }

    @Test
    public void execute_numericNoteWithZeroNumber_doesNotTreatNoteAsCountryCode() {
        Person zeroNumber = new PersonBuilder(AMY).withPhone("(1) 000").build();
        model = new ModelManager(new AthleteRosterBuilder().withPerson(ZOE)
                .withPerson(zeroNumber).build(), new UserPrefs());

        assertSortSuccess(PHONE, ASCENDING, List.of(zeroNumber, ZOE));
        assertSortSuccess(PHONE, DESCENDING, List.of(ZOE, zeroNumber));
    }

    @Test
    public void execute_emailAscendingAndDescending_sortsAlphabeticallyIgnoringCase() {
        assertSortSuccess(EMAIL, ASCENDING, List.of(AMY, MIA, ZOE));
        assertSortSuccess(EMAIL, DESCENDING, List.of(ZOE, MIA, AMY));
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
    public void execute_reset_restoresRosterOrderAndRetainsFilter() {
        model.updateFilteredPersonList(person -> !person.equals(MIA));
        new SortCommand(NAME, ASCENDING).execute(model);
        assertEquals(List.of(AMY, ZOE), model.getFilteredPersonList());

        CommandResult result = SortCommand.reset().execute(model);

        assertEquals(SortCommand.MESSAGE_RESET_SUCCESS, result.getFeedbackToUser());
        assertEquals(List.of(ZOE, AMY), model.getFilteredPersonList());
        assertEquals(List.of(ZOE, MIA, AMY), model.getRoster().getPersonList());
        assertNull(model.getPersonListComparator());
        new ListAthleteCommand().execute(model);
        assertEquals(List.of(ZOE, MIA, AMY), model.getFilteredPersonList());
    }

    @Test
    public void execute_resetWithoutActiveSort_succeedsAndCanSortAgain() {
        SortCommand.reset().execute(model);
        SortCommand.reset().execute(model);
        assertEquals(List.of(ZOE, MIA, AMY), model.getFilteredPersonList());
        assertSortSuccess(NAME, ASCENDING, List.of(AMY, MIA, ZOE));

        model = new ModelManager();
        SortCommand.reset().execute(model);
        assertTrue(model.getFilteredPersonList().isEmpty());
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
        assertFalse(command.equals(SortCommand.reset()));
        assertFalse(SortCommand.reset().equals(command));
        assertTrue(SortCommand.reset().equals(SortCommand.reset()));
    }

    @Test
    public void toStringMethod() {
        SortCommand command = new SortCommand(AGE, DESCENDING);
        String expected = SortCommand.class.getCanonicalName() + "{field=AGE, order=DESCENDING}";
        assertEquals(expected, command.toString());
    }

    private void assertSortSuccess(SortField field, SortOrder order, List<Person> expectedOrder) {
        List<Person> originalRosterOrder = List.copyOf(model.getRoster().getPersonList());
        SortCommand command = new SortCommand(field, order);
        CommandResult result = command.execute(model);
        String expectedMessage = String.format(SortCommand.MESSAGE_SUCCESS,
                field == AGE ? "age category" : field.name().toLowerCase(),
                order == ASCENDING ? "ascending" : "descending");
        assertEquals(expectedMessage, result.getFeedbackToUser());
        assertEquals(expectedOrder, model.getFilteredPersonList());
        assertEquals(originalRosterOrder, model.getRoster().getPersonList());
    }

    private void useModelWithAllAgeCategories() {
        model = new ModelManager(new AthleteRosterBuilder()
                .withPerson(ZOE)
                .withPerson(MIA)
                .withPerson(AMY)
                .withPerson(UMA)
                .withPerson(IAN)
                .build(), new UserPrefs());
    }
}
