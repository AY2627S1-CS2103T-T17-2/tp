package trackflow.logic.commands;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static trackflow.logic.commands.CommandTestUtil.assertCommandSuccess;
import static trackflow.testutil.Assert.assertThrows;
import static trackflow.testutil.TypicalPersons.CARL;
import static trackflow.testutil.TypicalPersons.ELLE;
import static trackflow.testutil.TypicalPersons.FIONA;
import static trackflow.testutil.TypicalPersons.getTypicalRoster;

import java.util.List;
import java.util.Map;

import org.junit.jupiter.api.Test;

import trackflow.model.Model;
import trackflow.model.ModelManager;
import trackflow.model.UserPrefs;
import trackflow.model.person.Person;
import trackflow.model.person.PersonContainsKeywordsPredicate;
import trackflow.model.person.PersonContainsKeywordsPredicate.SearchField;
import trackflow.testutil.AthleteRosterBuilder;
import trackflow.testutil.PersonBuilder;

/**
 * Contains integration tests (interaction with the Model) for {@code FindCommand}.
 */
public class FindCommandTest {
    private Model model = new ModelManager(getTypicalRoster(), new UserPrefs());
    private Model expectedModel = new ModelManager(getTypicalRoster(), new UserPrefs());

    @Test
    public void constructor_null_throwsNullPointerException() {
        assertThrows(NullPointerException.class, () -> new FindCommand(null));
    }

    @Test
    public void execute_nullModel_throwsNullPointerException() {
        FindCommand command = new FindCommand(new PersonContainsKeywordsPredicate(List.of("first")));
        assertThrows(NullPointerException.class, () -> command.execute(null));
    }

    @Test
    public void equals() {
        PersonContainsKeywordsPredicate firstPredicate =
                new PersonContainsKeywordsPredicate(List.of("first"));
        PersonContainsKeywordsPredicate secondPredicate =
                new PersonContainsKeywordsPredicate(List.of("second"));

        FindCommand findFirstCommand = new FindCommand(firstPredicate);
        FindCommand findSecondCommand = new FindCommand(secondPredicate);

        // same object -> returns true
        assertTrue(findFirstCommand.equals(findFirstCommand));

        // same values -> returns true
        FindCommand findFirstCommandCopy = new FindCommand(firstPredicate);
        assertTrue(findFirstCommand.equals(findFirstCommandCopy));

        // different types -> returns false
        assertFalse(findFirstCommand.equals(1));

        // null -> returns false
        assertFalse(findFirstCommand.equals(null));

        // different person -> returns false
        assertFalse(findFirstCommand.equals(findSecondCommand));
    }

    @Test
    public void execute_zeroKeywords_noAthleteFound() {
        PersonContainsKeywordsPredicate predicate = preparePredicate(" ");
        FindCommand command = new FindCommand(predicate);
        expectedModel.updateFilteredPersonList(predicate);
        assertCommandSuccess(command, model, FindCommand.MESSAGE_NO_MATCHES, expectedModel);
        assertEquals(List.of(), model.getFilteredPersonList());
    }

    @Test
    public void execute_partialEmail_oneAthleteFound() {
        PersonContainsKeywordsPredicate predicate = preparePredicate("heinz@");
        FindCommand command = new FindCommand(predicate);
        expectedModel.updateFilteredPersonList(predicate);
        assertCommandSuccess(command, model, FindCommand.MESSAGE_SUCCESS_SINGLE, expectedModel);
        assertEquals(List.of(CARL), model.getFilteredPersonList());
    }

    @Test
    public void execute_keywordsAcrossFields_multipleAthletesFound() {
        PersonContainsKeywordsPredicate predicate = preparePredicate("Kurz werner 2427");
        FindCommand command = new FindCommand(predicate);
        expectedModel.updateFilteredPersonList(predicate);
        String expectedMessage = String.format(FindCommand.MESSAGE_SUCCESS, 3);
        assertCommandSuccess(command, model, expectedMessage, expectedModel);
        assertEquals(List.of(CARL, ELLE, FIONA), model.getFilteredPersonList());
    }

    @Test
    public void execute_nameScopedKeyword_ignoresMatchesInOtherFields() {
        Person namedUnderwood = new PersonBuilder().withName("Underwood Tan").withAgeCategory("Open").build();
        Person under16Athlete = new PersonBuilder().withName("Avery Tan").withAgeCategory("Under 16")
                .withPhone("81234567").withEmail("avery@example.com").build();
        model = new ModelManager(new AthleteRosterBuilder().withPerson(under16Athlete)
                .withPerson(namedUnderwood).build(), new UserPrefs());
        PersonContainsKeywordsPredicate predicate = new PersonContainsKeywordsPredicate(List.of(),
                Map.of(SearchField.NAME, List.of("und")));
        FindCommand command = new FindCommand(predicate);
        expectedModel = new ModelManager(model.getRoster(), new UserPrefs());
        expectedModel.updateFilteredPersonList(predicate);

        assertCommandSuccess(command, model, FindCommand.MESSAGE_SUCCESS_SINGLE, expectedModel);
        assertEquals(List.of(namedUnderwood), model.getFilteredPersonList());
    }

    @Test
    public void toStringMethod() {
        PersonContainsKeywordsPredicate predicate = new PersonContainsKeywordsPredicate(List.of("keyword"));
        FindCommand findCommand = new FindCommand(predicate);
        String expected = FindCommand.class.getCanonicalName() + "{predicate=" + predicate + "}";
        assertEquals(expected, findCommand.toString());
    }

    /**
     * Parses {@code userInput} into a {@code PersonContainsKeywordsPredicate}.
     */
    private PersonContainsKeywordsPredicate preparePredicate(String userInput) {
        return new PersonContainsKeywordsPredicate(List.of(userInput.split("\\s+")));
    }
}
