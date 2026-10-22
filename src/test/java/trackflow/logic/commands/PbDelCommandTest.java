package trackflow.logic.commands;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static trackflow.testutil.Assert.assertThrows;
import static trackflow.testutil.TypicalIndexes.INDEX_FIRST_PERSON;
import static trackflow.testutil.TypicalIndexes.INDEX_SECOND_PERSON;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import trackflow.commons.core.index.Index;
import trackflow.logic.Messages;
import trackflow.logic.commands.exceptions.CommandException;
import trackflow.model.Model;
import trackflow.model.ModelManager;
import trackflow.model.person.Person;
import trackflow.testutil.PersonBuilder;

public class PbDelCommandTest {

    private Model model;

    @BeforeEach
    public void setUp() {
        model = new ModelManager();
        model.addPerson(new PersonBuilder().withName("Avery Tan")
                .withPerformances("100m|12.34", "Long Jump|6.21m").build());
    }

    private Person firstAthlete() {
        return model.getFilteredPersonList().get(0);
    }

    @Test
    public void execute_validIndexes_removesPerformance() throws Exception {
        CommandResult result = new PbDelCommand(INDEX_FIRST_PERSON, INDEX_FIRST_PERSON).execute(model);
        assertEquals(String.format(PbDelCommand.MESSAGE_SUCCESS, "Avery Tan", "100m 12.34"),
                result.getFeedbackToUser());
        assertEquals(1, firstAthlete().getPerformances().size());
        assertEquals("Long Jump", firstAthlete().getPerformances().get(0).getEvent());
    }

    @Test
    public void execute_invalidPerformanceIndex_throws() {
        assertThrows(CommandException.class, PbDelCommand.MESSAGE_INVALID_PERFORMANCE_INDEX, () ->
                new PbDelCommand(INDEX_FIRST_PERSON, Index.fromOneBased(3)).execute(model));
    }

    @Test
    public void execute_invalidAthleteIndex_throws() {
        assertThrows(CommandException.class, Messages.MESSAGE_INVALID_PERSON_DISPLAYED_INDEX, () ->
                new PbDelCommand(INDEX_SECOND_PERSON, INDEX_FIRST_PERSON).execute(model));
    }

    @Test
    public void execute_noPerformances_throws() {
        Model empty = new ModelManager();
        empty.addPerson(new PersonBuilder().withName("Ben Lee").build());
        assertThrows(CommandException.class,
                String.format(PbDelCommand.MESSAGE_NO_PERFORMANCES, "Ben Lee"), () ->
                new PbDelCommand(INDEX_FIRST_PERSON, INDEX_FIRST_PERSON).execute(empty));
    }

    @Test
    public void equals() {
        PbDelCommand command = new PbDelCommand(INDEX_FIRST_PERSON, INDEX_FIRST_PERSON);
        assertEquals(command, command);
        assertEquals(command, new PbDelCommand(INDEX_FIRST_PERSON, INDEX_FIRST_PERSON));
    }
}
