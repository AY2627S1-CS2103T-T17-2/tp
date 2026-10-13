package trackflow.logic.commands;

import static java.util.Objects.requireNonNull;

import trackflow.model.AthleteRoster;
import trackflow.model.Model;

/**
 * Clears the athlete roster.
 */
public class ClearCommand extends Command {

    public static final String COMMAND_WORD = "clear";
    public static final String MESSAGE_USAGE = COMMAND_WORD + ": Clears the athlete roster.\n"
            + "Example: " + COMMAND_WORD;
    public static final String MESSAGE_SUCCESS = "Athlete roster has been cleared!";


    @Override
    public CommandResult execute(Model model) {
        requireNonNull(model);
        model.setRoster(new AthleteRoster());
        return new CommandResult(MESSAGE_SUCCESS);
    }
}
