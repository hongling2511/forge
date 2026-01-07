#set( $symbol_pound = '#' )
#set( $symbol_dollar = '$' )
#set( $symbol_escape = '\' )
package ${package}.filter;

import ${package}.common.TraceIdContext;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.UUID;
import java.util.regex.Pattern;

/**
 * Filter that generates or propagates trace ID for each request.
 * Trace ID is stored in MDC for logging and returned in response header.
 */
@Component
@Order(Ordered.HIGHEST_PRECEDENCE)
public class TraceIdFilter extends OncePerRequestFilter {

    /** Maximum allowed length for trace ID to prevent DoS attacks */
    private static final int MAX_TRACE_ID_LENGTH = 64;

    /** Pattern for valid trace ID characters (alphanumeric and hyphens only) */
    private static final Pattern VALID_TRACE_ID_PATTERN = Pattern.compile("^[a-zA-Z0-9-]+${symbol_dollar}");

    @Override
    protected void doFilterInternal(HttpServletRequest request,
                                    HttpServletResponse response,
                                    FilterChain filterChain) throws ServletException, IOException {
        try {
            // Get trace ID from header or generate new one
            String traceId = request.getHeader(TraceIdContext.TRACE_ID_HEADER);

            // Validate trace ID to prevent log injection attacks
            if (!isValidTraceId(traceId)) {
                traceId = UUID.randomUUID().toString().replace("-", "");
            }

            // Set in MDC for logging
            TraceIdContext.setTraceId(traceId);

            // Add to response header
            response.setHeader(TraceIdContext.TRACE_ID_HEADER, traceId);

            filterChain.doFilter(request, response);
        } finally {
            // Clean up MDC
            TraceIdContext.clear();
        }
    }

    /**
     * Validates the trace ID to prevent log injection and DoS attacks.
     *
     * @param traceId the trace ID to validate
     * @return true if valid, false otherwise
     */
    private boolean isValidTraceId(String traceId) {
        if (!StringUtils.hasText(traceId)) {
            return false;
        }
        if (traceId.length() > MAX_TRACE_ID_LENGTH) {
            return false;
        }
        return VALID_TRACE_ID_PATTERN.matcher(traceId).matches();
    }
}
