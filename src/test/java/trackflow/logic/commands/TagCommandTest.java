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
 * Contains integration tests (interaction with the Model) and unit tests for {@code TagCommand}.
 */
public class TagCommandTest {

    private final Model model = new ModelManager(getTypicalRoster(), new UserPrefs());

    @Test
    public void execute_newEvents_addsEventsAndKeepsExisting() {
        Person athlete = model.getFilteredPersonList().get(INDEX_FIRST_PERSON.getZeroBased());
        TagCommand command = new TagCommand(INDEX_FIRST_PERSON, getTagSet("Long Jump", "100m"));
        Person taggedAthlete = new PersonBuilder(athlete).withTags("friends", "100m", "Long Jump").build();

        String expectedMessage = String.format(TagCommand.MESSAGE_SUCCESS, "100m, Long Jump", athlete.getName());
        Model expectedModel = new ModelManager(new AthleteRoster(model.getRoster()), new UserPrefs());
        expectedModel.setPerson(athlete, taggedAthlete);

        assertCommandSuccess(command, model, expectedMessage, expectedModel);
    }

    @Test
    public void execute_someEventsAlreadyPresent_addsOnlyNewEvents() {
        // ALICE already has 'friends'; matching ignores case
        Person athlete = model.getFilteredPersonList().get(INDEX_FIRST_PERSON.getZeroBased());
        TagCommand command = new TagCommand(INDEX_FIRST_PERSON, getTagSet("FRIENDS", "200m"));
        Person taggedAthlete = new PersonBuilder(athlete).withTags("friends", "200m").build();

        String expectedMessage = String.format(TagCommand.MESSAGE_SUCCESS, "200m", athlete.getName())
                + String.format(TagCommand.MESSAGE_SKIPPED_EVENTS, "FRIENDS");
        Model expectedModel = new ModelManager(new AthleteRoster(model.getRoster()), new UserPrefs());
        expectedModel.setPerson(athlete, taggedAthlete);

        assertCommandSuccess(command, model, expectedMessage, expectedModel);
    }

    @Test
    public void execute_allEventsAlreadyPresent_failure() {
        Person athlete = model.getFilteredPersonList().get(INDEX_FIRST_PERSON.getZeroBased());
        TagCommand command = new TagCommand(INDEX_FIRST_PERSON, getTagSet("Friends"));

        assertCommandFailure(command, model,
                String.format(TagCommand.MESSAGE_ALREADY_TAGGED, athlete.getName(), "Friends"));
    }

    @Test
    public void execute_filteredList_tagsDisplayedAthlete() {
        showPersonAtIndex(model, INDEX_SECOND_PERSON);
        Person athlete = model.getFilteredPersonList().get(INDEX_FIRST_PERSON.getZeroBased());
        TagCommand command = new TagCommand(INDEX_FIRST_PERSON, getTagSet("Javelin"));
        Person taggedAthlete = new PersonBuilder(athlete).withTags("owesMoney", "friends", "Javelin").build();

        String expectedMessage = String.format(TagCommand.MESSAGE_SUCCESS, "Javelin", athlete.getName());
        Model expectedModel = new ModelManager(new AthleteRoster(model.getRoster()), new UserPrefs());
        expectedModel.setPerson(athlete, taggedAthlete);

        assertCommandSuccess(command, model, expectedMessage, expectedModel);
    }

    @Test
    public void execute_invalidIndexUnfilteredList_failure() {
        Index outOfBoundIndex = Index.fromOneBased(model.getFilteredPersonList().size() + 1);
        TagCommand command = new TagCommand(outOfBoundIndex, getTagSet("100m"));

        assertCommandFailure(command, model, Messages.MESSAGE_INVALID_PERSON_DISPLAYED_INDEX);
    }

    @Test
    public void execute_invalidIndexFilteredList_failure() {
        showPersonAtIndex(model, INDEX_FIRST_PERSON);
        TagCommand command = new TagCommand(INDEX_SECOND_PERSON, getTagSet("100m"));

        assertCommandFailure(command, model, Messages.MESSAGE_INVALID_PERSON_DISPLAYED_INDEX);
    }

    @Test
    public void changesRoster_returnsTrue() {
        assertTrue(new TagCommand(INDEX_FIRST_PERSON, getTagSet("100m")).changesRoster());
    }

    @Test
    public void equals() {
        TagCommand standardCommand = new TagCommand(INDEX_FIRST_PERSON, getTagSet("100m"));

        // same values -> returns true
        assertTrue(standardCommand.equals(new TagCommand(INDEX_FIRST_PERSON, getTagSet("100m"))));

        // same events in different case -> returns true
        assertTrue(standardCommand.equals(new TagCommand(INDEX_FIRST_PERSON, getTagSet("100M"))));

        // same object -> returns true
        assertTrue(standardCommand.equals(standardCommand));

        // null -> returns false
        assertFalse(standardCommand.equals(null));

        // different types -> returns false
        assertFalse(standardCommand.equals(new ClearCommand()));

        // untag command with same values -> returns false
        assertFalse(standardCommand.equals(new UntagCommand(INDEX_FIRST_PERSON, getTagSet("100m"))));

        // different index -> returns false
        assertFalse(standardCommand.equals(new TagCommand(INDEX_SECOND_PERSON, getTagSet("100m"))));

        // different events -> returns false
        assertFalse(standardCommand.equals(new TagCommand(INDEX_FIRST_PERSON, getTagSet("200m"))));
    }
}
