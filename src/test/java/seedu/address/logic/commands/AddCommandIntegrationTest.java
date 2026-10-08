package seedu.address.logic.commands;

import static seedu.address.logic.commands.CommandTestUtil.assertCommandFailure;
import static seedu.address.logic.commands.CommandTestUtil.assertCommandSuccess;
import static seedu.address.testutil.TypicalPersons.getTypicalAddressBook;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import seedu.address.logic.Messages;
import seedu.address.model.Model;
import seedu.address.model.ModelManager;
import seedu.address.model.UserPrefs;
import seedu.address.model.person.Person;
import seedu.address.testutil.PersonBuilder;

/**
 * Contains integration tests (interaction with the Model) for {@code AddCommand}.
 */
public class AddCommandIntegrationTest {

    private Model model;

    @BeforeEach
    public void setUp() {
        model = new ModelManager(getTypicalAddressBook(), new UserPrefs());
    }

    @Test
    public void execute_sharedContactOutsideFilteredList_succeedsWithWarning() {
        Person existing = model.getAddressBook().getPersonList().get(0);
        model.updateFilteredPersonList(person -> false);
        Person candidate = new PersonBuilder(existing).withName("Different Athlete")
                .withEmail("new@example.com").build();
        Model expectedModel = new ModelManager(model.getAddressBook(), new UserPrefs());
        expectedModel.updateFilteredPersonList(person -> false);
        expectedModel.addPerson(candidate);
        String expected = String.format(AddCommand.MESSAGE_SUCCESS, Messages.formatAthlete(candidate))
                + String.format(Messages.MESSAGE_POSSIBLE_DUPLICATE, existing.getName());
        assertCommandSuccess(new AddCommand(candidate), model, expected, expectedModel);
    }

    @Test
    public void execute_sameNameDifferentContacts_succeedsWithoutWarning() {
        Person existing = model.getAddressBook().getPersonList().get(0);
        Person candidate = new PersonBuilder(existing).withPhone("81112222").withEmail("new@example.com").build();
        Model expectedModel = new ModelManager(model.getAddressBook(), new UserPrefs());
        expectedModel.addPerson(candidate);
        assertCommandSuccess(new AddCommand(candidate), model,
                String.format(AddCommand.MESSAGE_SUCCESS, Messages.formatAthlete(candidate)), expectedModel);
    }

    @Test
    public void execute_newPerson_success() {
        Person validPerson = new PersonBuilder().build();

        Model expectedModel = new ModelManager(model.getAddressBook(), new UserPrefs());
        expectedModel.addPerson(validPerson);

        assertCommandSuccess(new AddCommand(validPerson), model,
                String.format(AddCommand.MESSAGE_SUCCESS, Messages.formatAthlete(validPerson)),
                expectedModel);
    }

    @Test
    public void execute_duplicatePerson_throwsCommandException() {
        Person personInList = model.getAddressBook().getPersonList().get(0);
        assertCommandFailure(new AddCommand(personInList), model,
                String.format(AddCommand.MESSAGE_DUPLICATE_PERSON, personInList.getName()));
    }

}
