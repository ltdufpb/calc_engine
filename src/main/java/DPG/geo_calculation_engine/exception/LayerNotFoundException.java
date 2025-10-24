package DPG.geo_calculation_engine.exception;

/**
 * Exception thrown when a requested Layer cannot be found in the persistent store
 * (e.g., database) by its identifier (ID or name), or if the layer exists but
 * is marked as inactive when an active layer was required.
 *
 * Extends {@link RuntimeException}, making it an unchecked exception.
 */
public class LayerNotFoundException extends RuntimeException {

    /**
     * Constructs a new LayerNotFoundException with the specified detail message.
     *
     * @param message the detail message (which is saved for later retrieval by the getMessage() method).
     */
    public LayerNotFoundException(String message) {
        super(message);
    }

    /**
     * Constructs a new LayerNotFoundException with the specified detail message and cause.
     *
     * @param message the detail message (which is saved for later retrieval by the getMessage() method).
     * @param cause   the cause (which is saved for later retrieval by the getCause() method).
     * (A null value is permitted, and indicates that the cause is nonexistent or unknown.)
     */
    public LayerNotFoundException(String message, Throwable cause) {
        super(message, cause);
    }
}