#set( $symbol_pound = '#' )
#set( $symbol_dollar = '$' )
#set( $symbol_escape = '\' )
package ${package}.common;

import org.slf4j.MDC;

/**
 * Utility class for managing trace ID in the current request context.
 * Uses SLF4J MDC for thread-local storage and log correlation.
 */
public final class TraceIdContext {

    public static final String TRACE_ID_KEY = "traceId";
    public static final String TRACE_ID_HEADER = "X-Trace-Id";

    private TraceIdContext() {
        // Utility class
    }

    /**
     * Sets the trace ID for the current request.
     */
    public static void setTraceId(String traceId) {
        MDC.put(TRACE_ID_KEY, traceId);
    }

    /**
     * Gets the trace ID for the current request.
     */
    public static String getTraceId() {
        return MDC.get(TRACE_ID_KEY);
    }

    /**
     * Clears the trace ID from the current context.
     */
    public static void clear() {
        MDC.remove(TRACE_ID_KEY);
    }
}
