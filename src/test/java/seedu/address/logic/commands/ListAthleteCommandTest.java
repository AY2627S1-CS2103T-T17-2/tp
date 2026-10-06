package seedu.address.logic.commands;

import static seedu.address.logic.commands.CommandTestUtil.assertCommandSuccess;
import static seedu.address.logic.commands.CommandTestUtil.showPersonAtIndex;
import static seedu.address.testutil.TypicalIndexes.INDEX_FIRST_PERSON;
import static seedu.address.testutil.TypicalPersons.getTypicalAddressBook;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import seedu.address.model.Model;
import seedu.address.model.ModelManager;
import seedu.address.model.UserPrefs;
import seedu.address.testutil.AddressBookBuilder;

/**
 * Contains integration tests (interaction with the Model) and unit tests for ListAthleteCommand.
 */
public class ListAthleteCommandTest {

    private Model model;
    private Model expectedModel;

    @BeforeEach
    public void setUp() {
        model = new ModelManager(getTypicalAddressBook(), new UserPrefs());
        expectedModel = new ModelManager(model.getAddressBook(), new UserPrefs());
    }

    @Test
    public void execute_listIsNotFiltered_showsSameList() {
        String expectedMessage = String.format(ListAthleteCommand.MESSAGE_SUCCESS,
                model.getFilteredPersonList().size());
        assertCommandSuccess(new ListAthleteCommand(), model, expectedMessage, expectedModel);
    }

    @Test
    public void execute_listIsFiltered_showsEverything() {
        showPersonAtIndex(model, INDEX_FIRST_PERSON);
        String expectedMessage = String.format(ListAthleteCommand.MESSAGE_SUCCESS,
                expectedModel.getFilteredPersonList().size());
        assertCommandSuccess(new ListAthleteCommand(), model, expectedMessage, expectedModel);
    }

    @Test
    public void execute_emptyRoster_showsEmptyRosterMessage() {
        Model emptyModel = new ModelManager();
        Model expectedEmptyModel = new ModelManager();
        assertCommandSuccess(new ListAthleteCommand(), emptyModel, ListAthleteCommand.MESSAGE_EMPTY_ROSTER,
                expectedEmptyModel);
    }

    @Test
    public void execute_oneAthlete_showsSingularMessage() {
        Model singleAthleteModel = new ModelManager(
                new AddressBookBuilder().withPerson(getTypicalAddressBook().getPersonList().getFirst()).build(),
                new UserPrefs());
        Model expectedSingleAthleteModel = new ModelManager(singleAthleteModel.getAddressBook(), new UserPrefs());
        assertCommandSuccess(new ListAthleteCommand(), singleAthleteModel,
                ListAthleteCommand.MESSAGE_SUCCESS_SINGLE_ATHLETE, expectedSingleAthleteModel);
    }
}
