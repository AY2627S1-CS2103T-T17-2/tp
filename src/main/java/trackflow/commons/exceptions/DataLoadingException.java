package trackflow.commons.exceptions;

/**
 * Represents an error during loading of data from a file.
 */
public class DataLoadingException extends Exception {
    /**
     * Constructs a data-loading error that preserves the original {@code cause}.
     */
    public DataLoadingException(Exception cause) {
        super(cause);
    }

}
