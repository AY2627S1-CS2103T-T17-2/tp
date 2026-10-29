package trackflow.logic.commands;

import static java.util.Objects.requireNonNull;

import trackflow.model.AthleteRoster;
import trackflow.model.Model;

/**
 * Clears the athlete roster.
 */
public class ClearCommand extends Command {

    public static final String COMMAND_WORD = "clear";
    public static final String MESSAGE_USAGE = COMMAND_WORD
            + ": Shows a warning; type Y to permanently clear the roster or N to cancel.\n"
            + "Example: " + COMMAND_WORD;
    public static final String MESSAGE_SUCCESS = "Athlete roster has been cleared!";

    public static final String MESSAGE_CONFIRMATION =
            "This permanently deletes all athletes. Type Y to proceed or N to cancel.";

    private final boolean confirmed;

    public ClearCommand() {
        this(false);
    }

    public ClearCommand(boolean confirmed) {
        this.confirmed = confirmed;
    }

    @Override
    public boolean changesRoster() {
        return confirmed;
    }

    @Override
    public CommandResult execute(Model model) {
        requireNonNull(model);
        if (!confirmed) {
            return CommandResult.warning(MESSAGE_CONFIRMATION);
        }
        model.setRoster(new AthleteRoster());
        return new CommandResult(MESSAGE_SUCCESS);
    }
}
