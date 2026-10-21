package trackflow.logic.commands;

import trackflow.logic.commands.exceptions.CommandException;
import trackflow.model.Model;

/**
 * Represents a command with hidden internal logic and the ability to be executed.
 */
public abstract class Command {

    /**
     * Returns whether executing this command changes roster data that must be persisted.
     * The default is false. Mutating commands override this so the logic layer stages and saves their changes.
     */
    public boolean changesRoster() {
        return false;
    }

    /**
     * Executes the command against the supplied model and returns feedback and any requested UI actions.
     * This method does not persist roster data; the logic layer manages saving for mutating commands.
     *
     * @param model {@code Model} which the command should operate on.
     * @return The result of executing the command.
     * @throws CommandException If an error occurs during command execution.
     */
    public abstract CommandResult execute(Model model) throws CommandException;

}
