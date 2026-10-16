package trackflow.logic.parser.exceptions;

import trackflow.commons.exceptions.IllegalValueException;

/**
 * Represents a parse error encountered by a parser.
 */
public class ParseException extends IllegalValueException {

    /**
     * Constructs a parse error with the user-facing {@code message}.
     */
    public ParseException(String message) {
        super(message);
    }

    /**
     * Constructs a parse error with the user-facing {@code message} and original {@code cause}.
     */
    public ParseException(String message, Throwable cause) {
        super(message, cause);
    }
}
