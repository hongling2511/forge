#set( $symbol_pound = '#' )
#set( $symbol_dollar = '$' )
#set( $symbol_escape = '\' )
package ${package}.auth;

import java.util.Set;
import java.util.UUID;

/**
 * Response DTO for successful login.
 */
public record LoginResponse(
        String accessToken,
        String refreshToken,
        String tokenType,
        long expiresIn,
        UserInfo user
) {
    /**
     * Creates a login response with token information.
     */
    public static LoginResponse of(String accessToken, String refreshToken, long expiresIn, User user) {
        return new LoginResponse(
                accessToken,
                refreshToken,
                "Bearer",
                expiresIn,
                UserInfo.from(user)
        );
    }

    /**
     * User information included in login response.
     */
    public record UserInfo(
            UUID id,
            String username,
            String email,
            String firstName,
            String lastName,
            Set<String> roles
    ) {
        public static UserInfo from(User user) {
            Set<String> roleNames = user.getRoles().stream()
                    .map(Role::name)
                    .collect(java.util.stream.Collectors.toSet());

            return new UserInfo(
                    user.getId(),
                    user.getUsername(),
                    user.getEmail(),
                    user.getFirstName(),
                    user.getLastName(),
                    roleNames
            );
        }
    }
}
