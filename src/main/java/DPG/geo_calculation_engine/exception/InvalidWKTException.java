package DPG.geo_calculation_engine.exception;

/**
 * Exception thrown when a Well-Known Text (WKT) string is found to be invalid.
 * This can occur if the WKT string has incorrect syntax, cannot be parsed by the geometry library (e.g., JTS),
 * or (optionally) if the resulting geometry is topologically invalid (e.g., self-intersecting polygon).
 *
 * Extends {@link RuntimeException}, making it an unchecked exception.
 */
public class InvalidWKTException extends RuntimeException {

    /**
     * Constructs a new InvalidWKTException with the specified detail message.
     *
     * @param message the detail message (which is saved for later retrieval by the getMessage() method).
     */
    public InvalidWKTException(String message) {
        super(message);
    }

    /**
     * Constructs a new InvalidWKTException with the specified detail message and cause.
     *
     * @param message the detail message (which is saved for later retrieval by the getMessage() method).
     * @param cause   the cause (which is saved for later retrieval by the getCause() method).
     * (A null value is permitted, and indicates that the cause is nonexistent or unknown.)
     */
    public InvalidWKTException(String message, Throwable cause) {
        super(message, cause);
    }
}