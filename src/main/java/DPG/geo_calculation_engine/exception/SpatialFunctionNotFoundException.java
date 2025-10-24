package DPG.geo_calculation_engine.exception;

/**
 * Exception thrown when a requested Spatial Function definition cannot be found
 * in the persistent store (e.g., database). This could be when searching by ID,
 * by name and version, or when specifically requesting the active version for a given name
 * and no active version exists or the function name itself is not found.
 *
 * Extends {@link RuntimeException}, making it an unchecked exception.
 */
public class SpatialFunctionNotFoundException extends RuntimeException {

    /**
     * Constructs a new SpatialFunctionNotFoundException with the specified detail message.
     *
     * @param message the detail message (which is saved for later retrieval by the getMessage() method).
     */
    public SpatialFunctionNotFoundException(String message) {
        super(message);
    }

    /**
     * Constructs a new SpatialFunctionNotFoundException with the specified detail message and cause.
     *
     * @param message the detail message (which is saved for later retrieval by the getMessage() method).
     * @param cause   the cause (which is saved for later retrieval by the getCause() method).
     * (A null value is permitted, and indicates that the cause is nonexistent or unknown.)
     */
    public SpatialFunctionNotFoundException(String message, Throwable cause) {
        super(message, cause);
    }
}