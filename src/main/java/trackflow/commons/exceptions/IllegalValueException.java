package trackflow.commons.exceptions;

/**
 * Signals that some given data does not fulfill some constraints.
 */
public class IllegalValueException extends Exception {
    /**
     * Creates an exception describing a failed data constraint.
     *
     * @param message Description of the failed constraints.
     */
    public IllegalValueException(String message) {
        super(message);
    }

    /**
     * Creates an exception describing a failed data constraint and its cause.
     *
     * @param message Description of the failed constraints.
     * @param cause Underlying cause of the constraint failure.
     */
    public IllegalValueException(String message, Throwable cause) {
        super(message, cause);
    }
}
