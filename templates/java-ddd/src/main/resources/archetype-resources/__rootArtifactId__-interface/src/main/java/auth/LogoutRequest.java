#set( $symbol_pound = '#' )
#set( $symbol_dollar = '$' )
#set( $symbol_escape = '\' )
package ${package}.auth;

import jakarta.validation.constraints.NotBlank;

/**
 * Request DTO for logout.
 */
public record LogoutRequest(
        @NotBlank(message = "Refresh token is required")
        String refreshToken
) {
}
