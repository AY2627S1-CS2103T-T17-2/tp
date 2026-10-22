package trackflow.logic.commands;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static trackflow.testutil.Assert.assertThrows;
import static trackflow.testutil.TypicalIndexes.INDEX_FIRST_PERSON;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import trackflow.commons.core.index.Index;
import trackflow.logic.Messages;
import trackflow.logic.commands.exceptions.CommandException;
import trackflow.model.Model;
import trackflow.model.ModelManager;
import trackflow.model.person.Performance;
import trackflow.model.person.Person;
import trackflow.testutil.PersonBuilder;

public class PbCommandTest {

    private Model model;

    @BeforeEach
    public void setUp() {
        model = new ModelManager();
        model.addPerson(new PersonBuilder().withName("Avery Tan").build());
    }

    private Person firstAthlete() {
        return model.getFilteredPersonList().get(0);
    }

    @Test
    public void execute_newEvent_recordsPerformance() throws Exception {
        Performance performance = new Performance("100m", "12.34", null);
        CommandResult result = new PbCommand(INDEX_FIRST_PERSON, performance).execute(model);
        assertEquals(String.format(PbCommand.MESSAGE_RECORDED, "Avery Tan", performance),
                result.getFeedbackToUser());
        assertEquals(1, firstAthlete().getPerformances().size());
        assertEquals(performance, firstAthlete().getPerformances().get(0));
    }

    @Test
    public void execute_fasterTime_overwritesPreviousBest() throws Exception {
        new PbCommand(INDEX_FIRST_PERSON, new Performance("100m", "12.34", null)).execute(model);
        Performance faster = new Performance("100m", "12.10", null);
        CommandResult result = new PbCommand(INDEX_FIRST_PERSON, faster).execute(model);
        assertEquals(String.format(PbCommand.MESSAGE_IMPROVED, "Avery Tan", "100m", "12.10", "12.34"),
                result.getFeedbackToUser());
        assertEquals(1, firstAthlete().getPerformances().size());
        assertEquals("12.10", firstAthlete().getPerformances().get(0).getResult());
    }

    @Test
    public void execute_longerDistance_overwritesPreviousBest() throws Exception {
        new PbCommand(INDEX_FIRST_PERSON, new Performance("Long Jump", "6.21m", null)).execute(model);
        CommandResult result =
                new PbCommand(INDEX_FIRST_PERSON, new Performance("Long Jump", "6.50m", null)).execute(model);
        assertEquals(String.format(PbCommand.MESSAGE_IMPROVED, "Avery Tan", "Long Jump", "6.50m", "6.21m"),
                result.getFeedbackToUser());
        assertEquals("6.50m", firstAthlete().getPerformances().get(0).getResult());
    }

    @Test
    public void execute_slowerTime_keepsPreviousBestAndThrows() throws Exception {
        new PbCommand(INDEX_FIRST_PERSON, new Performance("100m", "12.34", null)).execute(model);
        Performance slower = new Performance("100m", "12.50", null);
        assertThrows(CommandException.class,
                String.format(PbCommand.MESSAGE_NOT_IMPROVED, "Avery Tan", "100m", "12.34", "12.50"), () ->
                        new PbCommand(INDEX_FIRST_PERSON, slower).execute(model));
        assertEquals("12.34", firstAthlete().getPerformances().get(0).getResult());
    }

    @Test
    public void execute_equalTime_keepsPreviousBestAndThrows() throws Exception {
        new PbCommand(INDEX_FIRST_PERSON, new Performance("100m", "12.34", null)).execute(model);
        assertThrows(CommandException.class, () ->
                new PbCommand(INDEX_FIRST_PERSON, new Performance("100m", "12.34", null)).execute(model));
    }

    @Test
    public void execute_timeAgainstExistingDistance_throws() throws Exception {
        new PbCommand(INDEX_FIRST_PERSON, new Performance("100m", "6.21m", null)).execute(model);
        assertThrows(CommandException.class, () ->
                new PbCommand(INDEX_FIRST_PERSON, new Performance("100m", "12.34", null)).execute(model));
    }

    @Test
    public void execute_invalidAthleteIndex_throws() {
        assertThrows(CommandException.class, Messages.MESSAGE_INVALID_PERSON_DISPLAYED_INDEX, () ->
                new PbCommand(Index.fromOneBased(5), new Performance("100m", "12.34", null)).execute(model));
    }

    @Test
    public void equals() {
        PbCommand command = new PbCommand(INDEX_FIRST_PERSON, new Performance("100m", "12.34", null));
        assertEquals(command, command);
        assertEquals(command, new PbCommand(INDEX_FIRST_PERSON, new Performance("100m", "12.34", null)));
    }
}
