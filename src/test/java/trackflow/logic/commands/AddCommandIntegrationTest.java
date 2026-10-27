package trackflow.logic.commands;

import static trackflow.logic.commands.CommandTestUtil.assertCommandFailure;
import static trackflow.logic.commands.CommandTestUtil.assertCommandSuccess;
import static trackflow.testutil.TypicalPersons.getTypicalRoster;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import trackflow.logic.Messages;
import trackflow.model.Model;
import trackflow.model.ModelManager;
import trackflow.model.UserPrefs;
import trackflow.model.person.Person;
import trackflow.testutil.PersonBuilder;

/**
 * Contains integration tests (interaction with the Model) for {@code AddCommand}.
 */
public class AddCommandIntegrationTest {

    private Model model;

    @BeforeEach
    public void setUp() {
        model = new ModelManager(getTypicalRoster(), new UserPrefs());
    }

    // @@author Austin-Chia
    @Test
    public void execute_sharedContactOutsideFilteredList_succeedsWithWarning() {
        Person existing = model.getRoster().getPersonList().get(0);
        model.updateFilteredPersonList(person -> false);
        Person candidate = new PersonBuilder(existing).withName("Different Athlete")
                .withEmail("new@example.com").build();
        // @@author
        Model expectedModel = new ModelManager(model.getRoster(), new UserPrefs());
        // @@author Austin-Chia
        expectedModel.updateFilteredPersonList(person -> false);
        expectedModel.addPerson(candidate);
        String expected = String.format(AddCommand.MESSAGE_SUCCESS, Messages.formatAthlete(candidate))
                + String.format(Messages.MESSAGE_POSSIBLE_DUPLICATE, existing.getName());
        assertCommandSuccess(new AddCommand(candidate), model, expected, expectedModel);
    }

    @Test
    public void execute_sameNameDifferentContacts_succeedsWithoutWarning() {
        Person existing = model.getRoster().getPersonList().get(0);
        Person candidate = new PersonBuilder(existing).withPhone("81112222").withEmail("new@example.com").build();
        // @@author
        Model expectedModel = new ModelManager(model.getRoster(), new UserPrefs());
        expectedModel.addPerson(candidate);
        assertCommandSuccess(new AddCommand(candidate), model,
                String.format(AddCommand.MESSAGE_SUCCESS, Messages.formatAthlete(candidate)), expectedModel);
    }

    @Test
    public void execute_newPerson_success() {
        Person validPerson = new PersonBuilder().build();

        Model expectedModel = new ModelManager(model.getRoster(), new UserPrefs());
        expectedModel.addPerson(validPerson);

        assertCommandSuccess(new AddCommand(validPerson), model,
                String.format(AddCommand.MESSAGE_SUCCESS, Messages.formatAthlete(validPerson)),
                expectedModel);
    }

    @Test
    public void execute_duplicatePerson_throwsCommandException() {
        Person personInList = model.getRoster().getPersonList().get(0);
        assertCommandFailure(new AddCommand(personInList), model,
                String.format(AddCommand.MESSAGE_DUPLICATE_PERSON, personInList.getName()));
    }

}
