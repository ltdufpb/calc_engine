package DPG.geo_calculation_engine.exception;

/**
 * Exception thrown when a requested workflow definition cannot be found
 * in the persistent store by its identifier (e.g., name) or if the
 * found workflow is marked as inactive.
 *
 * Extends {@link RuntimeException}, making it an unchecked exception.
 */
public class WorkflowNotFoundException extends RuntimeException {

    /**
     * Constructs a new WorkflowNotFoundException with the specified detail message.
     *
     * @param message the detail message (which is saved for later retrieval by the getMessage() method).
     */
    public WorkflowNotFoundException(String message) {
        super(message);
    }

    /**
     * Constructs a new WorkflowNotFoundException with the specified detail message and cause.
     *
     * @param message the detail message (which is saved for later retrieval by the getMessage() method).
     * @param cause   the cause (which is saved for later retrieval by the getCause() method).
     * (A null value is permitted, and indicates that the cause is nonexistent or unknown.)
     */
    public WorkflowNotFoundException(String message, Throwable cause) {
        super(message, cause);
    }
}