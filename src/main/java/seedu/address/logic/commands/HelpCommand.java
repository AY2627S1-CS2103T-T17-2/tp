package seedu.address.logic.commands;

import java.util.List;

import seedu.address.model.Model;

/**
 * Formats full help instructions for every command for display.
 */
public class HelpCommand extends Command {

    public static final String COMMAND_WORD = "help";

    public static final String MESSAGE_USAGE = COMMAND_WORD + ": Shows program usage instructions.\n"
            + "Example: " + COMMAND_WORD;

    public static final String SHOWING_HELP_MESSAGE = "Opened help window.";

    public static final String HELP_INTRODUCTION = "Available commands\n"
            + "Type a command in the command box and press Enter.\n"
            + "Words in CAPITALS are values you supply; square brackets mark optional fields.\n"
            + "INDEX is the positive number shown beside an athlete in the current list.";

    public static final List<String> COMMAND_USAGES = List.of(
                    AddCommand.MESSAGE_USAGE,
                    EditCommand.MESSAGE_USAGE,
                    DeleteCommand.MESSAGE_USAGE,
                    "list: Shows all athletes and removes any active filter.\nExample: list",
                    FindCommand.MESSAGE_USAGE,
                    FilterCommand.MESSAGE_USAGE,
                    SortCommand.MESSAGE_USAGE,
                    RemarkCommand.MESSAGE_USAGE + "\nTo remove a remark: remark 1 r/",
                    "clear: Removes all athletes from the roster.\nExample: clear",
                    MESSAGE_USAGE,
                    "exit: Closes the application.\nExample: exit");

    public static final String HELP_MESSAGE = HELP_INTRODUCTION + "\n\n" + String.join("\n\n", COMMAND_USAGES);

    @Override
    public CommandResult execute(Model model) {
        return new CommandResult(SHOWING_HELP_MESSAGE, true, false);
    }
}
