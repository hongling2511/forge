#set( $symbol_pound = '#' )
#set( $symbol_dollar = '$' )
#set( $symbol_escape = '\' )
package ${package}.auth;

import com.fasterxml.jackson.annotation.JsonInclude;

import java.time.Instant;

/**
 * Standard success message response format.
 */
@JsonInclude(JsonInclude.Include.NON_NULL)
public record MessageResponse(
        String message,
        Instant timestamp
) {
    /**
     * Creates a message response with current timestamp.
     */
    public static MessageResponse of(String message) {
        return new MessageResponse(message, Instant.now());
    }
}
