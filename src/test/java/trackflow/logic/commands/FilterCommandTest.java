package trackflow.logic.commands;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.List;

import org.junit.jupiter.api.Test;

import trackflow.logic.parser.TrackFlowParser;
import trackflow.model.Model;
import trackflow.model.ModelManager;
import trackflow.model.person.AgeCategory;
import trackflow.model.person.Person;
import trackflow.testutil.PersonBuilder;

/**
 * Exercises filtering and its interaction with existing commands through the parser and model.
 */
public class FilterCommandTest {
    private final Person open = new PersonBuilder().withName("Alex").withAgeCategory("Open").build();
    private final Person junior = new PersonBuilder().withName("Beth").withAgeCategory("Under 14").build();
    private final Person otherJunior = new PersonBuilder().withName("Cara").withAgeCategory("Under 14").build();
    private final Model model = new ModelManager();
    private final TrackFlowParser parser = new TrackFlowParser();

    private void populateRoster() {
        model.addPerson(open);
        model.addPerson(junior);
        model.addPerson(otherJunior);
    }

    @Test
    public void execute_multipleMatches_preservesRosterAndOrder() throws Exception {
        populateRoster();
        assertEquals("Displaying 2 athletes in age category Under 14.",
                parser.parseCommand("FiLtEr a/under   14").execute(model).getFeedbackToUser());
        assertEquals(List.of(junior, otherJunior), model.getFilteredPersonList());
        assertEquals(List.of(open, junior, otherJunior), model.getRoster().getPersonList());
    }

    @Test
    public void execute_emptyRoster_displaysNoMatches() throws Exception {
        assertEquals("No athletes found in age category Open (0 matches).",
                parser.parseCommand("filter a/Open").execute(model).getFeedbackToUser());
        assertTrue(model.getFilteredPersonList().isEmpty());
    }

    @Test
    public void execute_noMatches_clearsDisplay() throws Exception {
        populateRoster();
        assertEquals("No athletes found in age category Under 20 (0 matches).",
                parser.parseCommand("filter a/Under 20").execute(model).getFeedbackToUser());
        assertTrue(model.getFilteredPersonList().isEmpty());
        assertEquals(List.of(open, junior, otherJunior), model.getRoster().getPersonList());
    }

    @Test
    public void execute_previousCategoryFilter_replacesFilter() throws Exception {
        populateRoster();
        parser.parseCommand("filter a/Under 14").execute(model);

        assertEquals("Displaying 1 athlete in age category Open.",
                parser.parseCommand("filter a/Open").execute(model).getFeedbackToUser());
        assertEquals(List.of(open), model.getFilteredPersonList());
    }

    @Test
    public void equals() {
        FilterCommand command = new FilterCommand(new AgeCategory("Under 14"));
        assertTrue(command.equals(command));
        assertEquals(command, new FilterCommand(new AgeCategory("under 14")));
        assertFalse(command.equals(new FilterCommand(new AgeCategory("Open"))));
        assertFalse(command.equals(null));
        assertFalse(command.equals("Under 14"));
    }

    @Test
    public void constructor_nullCategory_throwsNullPointerException() {
        assertThrows(NullPointerException.class, () -> new FilterCommand(null));
    }

    @Test
    public void execute_nullModel_throwsNullPointerException() {
        FilterCommand command = new FilterCommand(new AgeCategory("Under 14"));
        assertThrows(NullPointerException.class, () -> command.execute(null));
    }

    @Test
    public void toStringMethod() {
        AgeCategory ageCategory = new AgeCategory("Under 14");
        FilterCommand command = new FilterCommand(ageCategory);
        String expected = FilterCommand.class.getCanonicalName() + "{ageCategory=" + ageCategory + "}";
        assertEquals(expected, command.toString());
    }
}
