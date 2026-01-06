#set( $symbol_pound = '#' )
#set( $symbol_dollar = '$' )
#set( $symbol_escape = '\' )
package ${package}.auth;

import com.fasterxml.jackson.annotation.JsonInclude;

import java.time.Instant;
import java.util.List;
import java.util.Map;

/**
 * Standard error response format for API errors.
 */
@JsonInclude(JsonInclude.Include.NON_NULL)
public record ErrorResponse(
        int status,
        String error,
        String message,
        String path,
        Instant timestamp,
        List<FieldError> fieldErrors,
        Map<String, Object> details
) {

    /**
     * Creates a simple error response without field errors.
     */
    public static ErrorResponse of(int status, String error, String message, String path) {
        return new ErrorResponse(status, error, message, path, Instant.now(), null, null);
    }

    /**
     * Creates an error response with field validation errors.
     */
    public static ErrorResponse withFieldErrors(int status, String error, String message,
                                                 String path, List<FieldError> fieldErrors) {
        return new ErrorResponse(status, error, message, path, Instant.now(), fieldErrors, null);
    }

    /**
     * Creates an error response with additional details.
     */
    public static ErrorResponse withDetails(int status, String error, String message,
                                            String path, Map<String, Object> details) {
        return new ErrorResponse(status, error, message, path, Instant.now(), null, details);
    }

    /**
     * Represents a field-level validation error.
     */
    public record FieldError(
            String field,
            String message,
            Object rejectedValue
    ) {
        public static FieldError of(String field, String message) {
            return new FieldError(field, message, null);
        }

        public static FieldError of(String field, String message, Object rejectedValue) {
            return new FieldError(field, message, rejectedValue);
        }
    }
}
