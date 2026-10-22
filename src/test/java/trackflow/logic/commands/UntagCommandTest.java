package trackflow.logic.commands;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static trackflow.logic.commands.CommandTestUtil.assertCommandFailure;
import static trackflow.logic.commands.CommandTestUtil.assertCommandSuccess;
import static trackflow.logic.commands.CommandTestUtil.showPersonAtIndex;
import static trackflow.model.util.SampleDataUtil.getTagSet;
import static trackflow.testutil.TypicalIndexes.INDEX_FIRST_PERSON;
import static trackflow.testutil.TypicalIndexes.INDEX_SECOND_PERSON;
import static trackflow.testutil.TypicalPersons.getTypicalRoster;

import org.junit.jupiter.api.Test;

import trackflow.commons.core.index.Index;
import trackflow.logic.Messages;
import trackflow.model.AthleteRoster;
import trackflow.model.Model;
import trackflow.model.ModelManager;
import trackflow.model.UserPrefs;
import trackflow.model.person.Person;
import trackflow.testutil.PersonBuilder;

/**
 * Contains integration tests (interaction with the Model) and unit tests for {@code UntagCommand}.
 */
public class UntagCommandTest {

    private final Model model = new ModelManager(getTypicalRoster(), new UserPrefs());

    @Test
    public void execute_existingEvent_removesEvent() {
        // BENSON has 'owesMoney' and 'friends'; matching ignores case and reports the stored name
        Person athlete = model.getFilteredPersonList().get(INDEX_SECOND_PERSON.getZeroBased());
        UntagCommand command = new UntagCommand(INDEX_SECOND_PERSON, getTagSet("OWESMONEY"));
        Person untaggedAthlete = new PersonBuilder(athlete).withTags("friends").build();

        String expectedMessage = String.format(UntagCommand.MESSAGE_SUCCESS, "owesMoney", athlete.getName());
        Model expectedModel = new ModelManager(new AthleteRoster(model.getRoster()), new UserPrefs());
        expectedModel.setPerson(athlete, untaggedAthlete);

        assertCommandSuccess(command, model, expectedMessage, expectedModel);
    }

    @Test
    public void execute_allEvents_removesAllEvents() {
        Person athlete = model.getFilteredPersonList().get(INDEX_SECOND_PERSON.getZeroBased());
        UntagCommand command = new UntagCommand(INDEX_SECOND_PERSON, getTagSet("owesMoney", "friends"));
        Person untaggedAthlete = new PersonBuilder(athlete).withTags().build();

        String expectedMessage = String.format(UntagCommand.MESSAGE_SUCCESS, "friends, owesMoney",
                athlete.getName());
        Model expectedModel = new ModelManager(new AthleteRoster(model.getRoster()), new UserPrefs());
        expectedModel.setPerson(athlete, untaggedAthlete);

        assertCommandSuccess(command, model, expectedMessage, expectedModel);
    }

    @Test
    public void execute_eventNotPresent_failureAndNothingRemoved() {
        Person athlete = model.getFilteredPersonList().get(INDEX_SECOND_PERSON.getZeroBased());
        UntagCommand command = new UntagCommand(INDEX_SECOND_PERSON, getTagSet("friends", "100m", "200m"));

        assertCommandFailure(command, model,
                String.format(UntagCommand.MESSAGE_EVENT_NOT_FOUND, athlete.getName(), "100m, 200m"));
    }

    @Test
    public void execute_filteredList_untagsDisplayedAthlete() {
        showPersonAtIndex(model, INDEX_SECOND_PERSON);
        Person athlete = model.getFilteredPersonList().get(INDEX_FIRST_PERSON.getZeroBased());
        UntagCommand command = new UntagCommand(INDEX_FIRST_PERSON, getTagSet("owesMoney"));
        Person untaggedAthlete = new PersonBuilder(athlete).withTags("friends").build();

        String expectedMessage = String.format(UntagCommand.MESSAGE_SUCCESS, "owesMoney", athlete.getName());
        Model expectedModel = new ModelManager(new AthleteRoster(model.getRoster()), new UserPrefs());
        expectedModel.setPerson(athlete, untaggedAthlete);

        assertCommandSuccess(command, model, expectedMessage, expectedModel);
    }

    @Test
    public void execute_invalidIndexUnfilteredList_failure() {
        Index outOfBoundIndex = Index.fromOneBased(model.getFilteredPersonList().size() + 1);
        UntagCommand command = new UntagCommand(outOfBoundIndex, getTagSet("friends"));

        assertCommandFailure(command, model, Messages.MESSAGE_INVALID_PERSON_DISPLAYED_INDEX);
    }

    @Test
    public void execute_invalidIndexFilteredList_failure() {
        showPersonAtIndex(model, INDEX_FIRST_PERSON);
        UntagCommand command = new UntagCommand(INDEX_SECOND_PERSON, getTagSet("friends"));

        assertCommandFailure(command, model, Messages.MESSAGE_INVALID_PERSON_DISPLAYED_INDEX);
    }

    @Test
    public void equals() {
        UntagCommand standardCommand = new UntagCommand(INDEX_FIRST_PERSON, getTagSet("100m"));

        // same values -> returns true
        assertTrue(standardCommand.equals(new UntagCommand(INDEX_FIRST_PERSON, getTagSet("100m"))));

        // same events in different case -> returns true
        assertTrue(standardCommand.equals(new UntagCommand(INDEX_FIRST_PERSON, getTagSet("100M"))));

        // same object -> returns true
        assertTrue(standardCommand.equals(standardCommand));

        // null -> returns false
        assertFalse(standardCommand.equals(null));

        // tag command with same values -> returns false
        assertFalse(standardCommand.equals(new TagCommand(INDEX_FIRST_PERSON, getTagSet("100m"))));

        // different index -> returns false
        assertFalse(standardCommand.equals(new UntagCommand(INDEX_SECOND_PERSON, getTagSet("100m"))));

        // different events -> returns false
        assertFalse(standardCommand.equals(new UntagCommand(INDEX_FIRST_PERSON, getTagSet("200m"))));
    }
}
