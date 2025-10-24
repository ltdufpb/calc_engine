package DPG.geo_calculation_engine.exception;

import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ProblemDetail;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.server.ServerWebInputException;
import java.time.Instant;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Global exception handler for the application using @ControllerAdvice.
 * Catches specific exceptions and maps them to standardized HTTP responses
 * using RFC 7807 ProblemDetail format.
 */
@Slf4j
@ControllerAdvice
public class GlobalExceptionHandler {

    /**
     * Handles validation exceptions triggered by @Valid annotation on request bodies.
     * Collects field-specific errors into a list.
     *
     * @param ex The caught MethodArgumentNotValidException.
     * @return ResponseEntity with HTTP 400 Bad Request and a ProblemDetail body containing validation errors.
     */
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ProblemDetail> handleValidationExceptions(MethodArgumentNotValidException ex) {
        List<String> errors = ex.getBindingResult()
                .getFieldErrors()
                .stream()
                .map(fieldError -> fieldError.getField() + ": " + fieldError.getDefaultMessage())
                .collect(Collectors.toList());

        ProblemDetail problemDetail = ProblemDetail.forStatusAndDetail(HttpStatus.BAD_REQUEST, "Validation errors occurred on input.");
        problemDetail.setTitle("Validation Error");
        problemDetail.setProperty("errors", errors);
        problemDetail.setProperty("timestamp", Instant.now());
        log.warn("Validation failed: {}", errors);
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(problemDetail);
    }

    /**
     * Handles exceptions when a requested Layer definition is not found.
     *
     * @param ex The caught LayerNotFoundException.
     * @return ResponseEntity with HTTP 404 Not Found and a ProblemDetail body.
     */
    @ExceptionHandler(LayerNotFoundException.class)
    public ResponseEntity<ProblemDetail> handleLayerNotFoundException(LayerNotFoundException ex) {
        ProblemDetail problemDetail = ProblemDetail.forStatusAndDetail(HttpStatus.NOT_FOUND, ex.getMessage());
        problemDetail.setTitle("Layer Not Found");
        problemDetail.setProperty("timestamp", Instant.now());
        log.warn("Layer not found: {}", ex.getMessage());
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(problemDetail);
    }

    /**
     * Handles exceptions when a requested Workflow definition is not found or not active.
     *
     * @param ex The caught WorkflowNotFoundException.
     * @return ResponseEntity with HTTP 404 Not Found and a ProblemDetail body.
     */
    @ExceptionHandler(WorkflowNotFoundException.class)
    public ResponseEntity<ProblemDetail> handleWorkflowNotFoundException(WorkflowNotFoundException ex) {
        ProblemDetail problemDetail = ProblemDetail.forStatusAndDetail(HttpStatus.NOT_FOUND, ex.getMessage());
        problemDetail.setTitle("Workflow Not Found");
        problemDetail.setProperty("timestamp", Instant.now());
        log.warn("Workflow not found: {}", ex.getMessage());
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(problemDetail);
    }

    /**
     * Handles exceptions when a requested Spatial Function definition is not found or not active.
     *
     * @param ex The caught SpatialFunctionNotFoundException.
     * @return ResponseEntity with HTTP 404 Not Found and a ProblemDetail body.
     */
    @ExceptionHandler(SpatialFunctionNotFoundException.class)
    public ResponseEntity<ProblemDetail> handleSpatialFunctionNotFoundException(SpatialFunctionNotFoundException ex) {
        ProblemDetail problemDetail = ProblemDetail.forStatusAndDetail(HttpStatus.NOT_FOUND, ex.getMessage());
        problemDetail.setTitle("Spatial Function Not Found");
        problemDetail.setProperty("timestamp", Instant.now());
        log.warn("Spatial function not found: {}", ex.getMessage());
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(problemDetail);
    }

    /**
     * Handles exceptions when an invalid WKT string is provided or processed.
     *
     * @param ex The caught InvalidWKTException.
     * @return ResponseEntity with HTTP 400 Bad Request and a ProblemDetail body.
     */
    @ExceptionHandler(InvalidWKTException.class)
    public ResponseEntity<ProblemDetail> handleInvalidWKTException(InvalidWKTException ex){
        ProblemDetail problemDetail = ProblemDetail.forStatusAndDetail(HttpStatus.BAD_REQUEST, ex.getMessage());
        problemDetail.setTitle("Invalid WKT");
        problemDetail.setProperty("timestamp", Instant.now());
        log.warn("Invalid WKT format or geometry: {}", ex.getMessage());
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(problemDetail);
    }

    /**
     * Handles exceptions when a required parameter for a function or workflow is missing.
     * Catches both IllegalArgumentException (potentially from parameter validation)
     * and the custom MissingRequiredParameterException.
     *
     * @param ex The caught MissingRequiredParameterException or IllegalArgumentException.
     * @return ResponseEntity with HTTP 400 Bad Request and a ProblemDetail body.
     */
    @ExceptionHandler({MissingRequiredParameterException.class, IllegalArgumentException.class})
    public ResponseEntity<ProblemDetail> handleMissingOrIllegalArgumentException(RuntimeException ex){
        ProblemDetail problemDetail = ProblemDetail.forStatusAndDetail(HttpStatus.BAD_REQUEST, ex.getMessage());
        if (ex instanceof MissingRequiredParameterException) {
            problemDetail.setTitle("Missing Required Parameter");
        } else {
            problemDetail.setTitle("Illegal Argument / Bad Request");
        }
        problemDetail.setProperty("timestamp", Instant.now());
        log.warn("Bad Request: {}", ex.getMessage());
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(problemDetail);
    }

    /**
     * Handles exceptions related to invalid input during web processing (e.g., JSON parsing errors).
     *
     * @param ex The caught ServerWebInputException.
     * @return ResponseEntity with HTTP 400 Bad Request and a ProblemDetail body.
     */
    @ExceptionHandler(ServerWebInputException.class)
    public ResponseEntity<ProblemDetail> handleServerWebInputException(ServerWebInputException ex) {
        ProblemDetail problemDetail = ProblemDetail.forStatusAndDetail(HttpStatus.BAD_REQUEST, "Failed to read request body: " + ex.getReason());
        problemDetail.setTitle("Invalid Request Body");
        problemDetail.setProperty("timestamp", Instant.now());
        if (ex.getRootCause() != null) {
            log.warn("Invalid request body: {}", ex.getReason(), ex.getRootCause());
        } else {
            log.warn("Invalid request body: {}", ex.getReason());
        }
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(problemDetail);
    }


    /**
     * Generic handler for any other uncaught exceptions.
     * Returns a generic internal server error response.
     * Logs the full stack trace on the server side for debugging.
     * IMPORTANT: Do not expose stack traces or sensitive details in production responses.
     *
     * @param ex The caught Exception.
     * @return ResponseEntity with HTTP 500 Internal Server Error and a generic ProblemDetail body.
     */
    @ExceptionHandler(Exception.class)
    public ResponseEntity<ProblemDetail> handleGenericException(Exception ex) {
        ProblemDetail problemDetail = ProblemDetail.forStatusAndDetail(HttpStatus.INTERNAL_SERVER_ERROR, "An unexpected internal error occurred. Please contact support.");
        problemDetail.setTitle("Internal Server Error");
        problemDetail.setProperty("timestamp", Instant.now());
        log.error("An unexpected error occurred: {}", ex.getMessage(), ex);
        ex.printStackTrace();
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(problemDetail);
    }
}