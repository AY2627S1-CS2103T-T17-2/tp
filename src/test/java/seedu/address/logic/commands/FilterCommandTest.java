package seedu.address.logic.commands;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.List;

import org.junit.jupiter.api.Test;

import seedu.address.logic.commands.exceptions.CommandException;
import seedu.address.logic.parser.AddressBookParser;
import seedu.address.logic.parser.exceptions.ParseException;
import seedu.address.model.Model;
import seedu.address.model.ModelManager;
import seedu.address.model.person.AgeCategory;
import seedu.address.model.person.Person;
import seedu.address.testutil.PersonBuilder;

/**
 * Exercises filtering and its interaction with existing commands through the parser and model.
 */
public class FilterCommandTest {
    private final Person open = new PersonBuilder().withName("Alex").withAgeCategory("Open").build();
    private final Person junior = new PersonBuilder().withName("Beth").withAgeCategory("Under 14").build();
    private final Person otherJunior = new PersonBuilder().withName("Cara").withAgeCategory("Under 14").build();
    private final Model model = new ModelManager();
    private final AddressBookParser parser = new AddressBookParser();

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
        assertEquals(List.of(open, junior, otherJunior), model.getAddressBook().getPersonList());
    }

    @Test
    public void execute_noMatchesAndEmptyRoster_clearDisplay() throws Exception {
        assertEquals("No athletes found in age category Open (0 matches).",
                parser.parseCommand("filter a/Open").execute(model).getFeedbackToUser());
        populateRoster();
        assertEquals("No athletes found in age category Under 20 (0 matches).",
                parser.parseCommand("filter a/Under 20").execute(model).getFeedbackToUser());
        assertTrue(model.getFilteredPersonList().isEmpty());
        assertEquals(3, model.getAddressBook().getPersonList().size());
    }

    @Test
    public void execute_replacesPreviousFilterOrFind_listRestoresEveryone() throws Exception {
        populateRoster();
        parser.parseCommand("find Alex").execute(model);
        parser.parseCommand("filter a/Under 14").execute(model);
        assertEquals(List.of(junior, otherJunior), model.getFilteredPersonList());
        assertEquals("Displaying 1 athlete in age category Open.",
                parser.parseCommand("filter a/Open").execute(model).getFeedbackToUser());
        assertEquals(List.of(open), model.getFilteredPersonList());
        parser.parseCommand("find Beth").execute(model);
        assertEquals(List.of(junior), model.getFilteredPersonList());
        parser.parseCommand("filter a/Under 20").execute(model);
        parser.parseCommand("list").execute(model);
        assertEquals(List.of(open, junior, otherJunior), model.getFilteredPersonList());
    }

    @Test
    public void execute_existingDeleteUsesFilteredIndexAndRetainsFilter() throws Exception {
        populateRoster();
        parser.parseCommand("filter a/Under 14").execute(model);
        parser.parseCommand("delete 1").execute(model);
        assertEquals(List.of(otherJunior), model.getFilteredPersonList());
        assertEquals(List.of(open, otherJunior), model.getAddressBook().getPersonList());
        assertThrows(CommandException.class, () -> parser.parseCommand("delete 2").execute(model));
        parser.parseCommand("delete 1").execute(model);
        assertTrue(model.getFilteredPersonList().isEmpty());
        assertThrows(CommandException.class, () -> parser.parseCommand("delete 1").execute(model));
        parser.parseCommand("list").execute(model);
        assertEquals(List.of(open), model.getFilteredPersonList());
    }

    @Test
    public void execute_invalidFilter_preservesPreviousDisplay() throws Exception {
        populateRoster();
        parser.parseCommand("filter a/Under 14").execute(model);
        assertThrows(ParseException.class, () -> parser.parseCommand("filter a/Under 15"));
        assertEquals(List.of(junior, otherJunior), model.getFilteredPersonList());
        assertEquals(3, model.getAddressBook().getPersonList().size());
    }

    @Test
    public void equals_comparesCategory() {
        FilterCommand command = new FilterCommand(new AgeCategory("Under 14"));
        assertTrue(command.equals(command));
        assertEquals(command, new FilterCommand(new AgeCategory("under 14")));
        assertFalse(command.equals(new FilterCommand(new AgeCategory("Open"))));
        assertFalse(command.equals(null));
        assertFalse(command.equals("Under 14"));
        assertThrows(NullPointerException.class, () -> new FilterCommand(null));
        assertThrows(NullPointerException.class, () -> command.execute(null));
    }
}
