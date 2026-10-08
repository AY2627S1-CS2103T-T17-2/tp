package seedu.address.logic.commands;

import static org.junit.jupiter.api.Assertions.assertTrue;
import static seedu.address.logic.commands.CommandTestUtil.assertCommandSuccess;
import static seedu.address.logic.commands.HelpCommand.SHOWING_HELP_MESSAGE;

import java.util.List;

import org.junit.jupiter.api.Test;

import seedu.address.model.Model;
import seedu.address.model.ModelManager;

public class HelpCommandTest {
    private Model model = new ModelManager();
    private Model expectedModel = new ModelManager();

    @Test
    public void execute_help_success() {
        CommandResult expectedCommandResult = new CommandResult(SHOWING_HELP_MESSAGE, true, false);
        assertCommandSuccess(new HelpCommand(), model, expectedCommandResult, expectedModel);
    }

    @Test
    public void helpMessage_containsEveryCommandWithExamples() {
        for (String commandWord : List.of("add", "edit", "delete", "list", "find", "filter",
                "remark", "clear", "help", "exit")) {
            assertTrue(HelpCommand.HELP_MESSAGE.contains(commandWord + ":"), commandWord);
            assertTrue(HelpCommand.HELP_MESSAGE.contains("Example: " + commandWord), commandWord);
        }
    }

    @Test
    public void helpMessage_containsParameterInstructions() {
        for (String usage : List.of(AddCommand.MESSAGE_USAGE, EditCommand.MESSAGE_USAGE,
                DeleteCommand.MESSAGE_USAGE, FindCommand.MESSAGE_USAGE, FilterCommand.MESSAGE_USAGE,
                RemarkCommand.MESSAGE_USAGE)) {
            assertTrue(HelpCommand.HELP_MESSAGE.contains(usage));
        }
        assertTrue(HelpCommand.HELP_MESSAGE.contains("square brackets mark optional fields"));
        assertTrue(HelpCommand.HELP_MESSAGE.contains("remark 1 r/"));
    }
}
