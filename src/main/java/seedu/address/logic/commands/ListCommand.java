package seedu.address.logic.commands;

import static java.util.Objects.requireNonNull;
import static seedu.address.model.Model.PREDICATE_SHOW_ALL_PERSONS;

import seedu.address.model.Model;

/**
 * Lists all athletes in the roster to the user.
 */
public class ListCommand extends Command {

    public static final String COMMAND_WORD = "list";

    public static final String MESSAGE_USAGE = "The list command does not accept parameters. Format: list";
    public static final String MESSAGE_SUCCESS = "Displaying %1$d athletes.";
    public static final String MESSAGE_EMPTY_ROSTER = "The athlete roster is empty.";

    @Override
    public CommandResult execute(Model model) {
        requireNonNull(model);
        model.updateFilteredPersonList(PREDICATE_SHOW_ALL_PERSONS);
        int athleteCount = model.getFilteredPersonList().size();
        String feedbackToUser = athleteCount == 0
                ? MESSAGE_EMPTY_ROSTER
                : String.format(MESSAGE_SUCCESS, athleteCount);
        return new CommandResult(feedbackToUser);
    }
}
