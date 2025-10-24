package DPG.geo_calculation_engine.exception;

/**
 * Exception thrown when a required input parameter was expected but not found.
 * This typically occurs during the processing of a request or the execution
 * of a workflow task where essential data needed to proceed is absent
 * from the provided parameters (e.g., initial parameters or results from dependencies).
 *
 * Extends {@link RuntimeException}, making it an unchecked exception.
 */
public class MissingRequiredParameterException extends RuntimeException {

    /**
     * Constructs a new MissingRequiredParameterException with the specified detail message.
     * The message should typically indicate which parameter is missing.
     *
     * @param message the detail message (which is saved for later retrieval by the getMessage() method).
     */
    public MissingRequiredParameterException(String message) {
        super(message);
    }

    /**
     * Constructs a new MissingRequiredParameterException with the specified detail message and cause.
     *
     * @param message the detail message (which is saved for later retrieval by the getMessage() method).
     * @param cause   the cause (which is saved for later retrieval by the getCause() method).
     * (A null value is permitted, and indicates that the cause is nonexistent or unknown.)
     */
    public MissingRequiredParameterException(String message, Throwable cause) {
        super(message, cause);
    }
}