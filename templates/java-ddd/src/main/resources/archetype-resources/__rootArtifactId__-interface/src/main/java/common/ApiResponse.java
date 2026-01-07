#set( $symbol_pound = '#' )
#set( $symbol_dollar = '$' )
#set( $symbol_escape = '\' )
package ${package}.common;

import com.fasterxml.jackson.annotation.JsonInclude;
import org.slf4j.MDC;

/**
 * Unified API response wrapper.
 * All API responses follow this structure for consistency.
 *
 * @param <T> the type of the response data
 */
@JsonInclude(JsonInclude.Include.NON_NULL)
public record ApiResponse<T>(
        int code,
        String message,
        T data,
        String traceId
) {
    /** Success code */
    public static final int SUCCESS = 0;

    /** Generic error code */
    public static final int ERROR = -1;

    /** Trace ID key in MDC */
    private static final String TRACE_ID_KEY = "traceId";

    /**
     * Gets current trace ID from MDC.
     */
    private static String getCurrentTraceId() {
        return MDC.get(TRACE_ID_KEY);
    }

    /**
     * Creates a successful response with data.
     */
    public static <T> ApiResponse<T> success(T data) {
        return new ApiResponse<>(SUCCESS, "success", data, getCurrentTraceId());
    }

    /**
     * Creates a successful response with custom message.
     */
    public static <T> ApiResponse<T> success(String message, T data) {
        return new ApiResponse<>(SUCCESS, message, data, getCurrentTraceId());
    }

    /**
     * Creates a successful response without data.
     */
    public static <T> ApiResponse<T> success() {
        return new ApiResponse<>(SUCCESS, "success", null, getCurrentTraceId());
    }

    /**
     * Creates an error response.
     */
    public static <T> ApiResponse<T> error(int code, String message) {
        return new ApiResponse<>(code, message, null, getCurrentTraceId());
    }

    /**
     * Creates an error response with details.
     */
    public static <T> ApiResponse<T> error(int code, String message, T data) {
        return new ApiResponse<>(code, message, data, getCurrentTraceId());
    }
}
