package trackflow.logic.commands.exceptions;

/**
 * Represents an error during execution of a {@link trackflow.logic.commands.Command}.
 */
public class CommandException extends Exception {
    /**
     * Constructs a command execution error with the user-facing {@code message}.
     */
    public CommandException(String message) {
        super(message);
    }

    /**
     * Constructs a new {@code CommandException} with the specified detail {@code message} and {@code cause}.
     */
    public CommandException(String message, Throwable cause) {
        super(message, cause);
    }
}
