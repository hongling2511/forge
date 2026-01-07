#set( $symbol_pound = '#' )
#set( $symbol_dollar = '$' )
#set( $symbol_escape = '\' )
package ${package}.auth;

import java.time.Instant;
import java.util.Set;
import java.util.UUID;

/**
 * Response DTO for successful user registration.
 */
public record RegisterResponse(
        UUID id,
        String username,
        String email,
        String firstName,
        String lastName,
        Set<String> roles,
        Instant createdAt
) {
    /**
     * Creates a RegisterResponse from a User entity.
     */
    public static RegisterResponse from(User user) {
        Set<String> roleNames = user.getRoles().stream()
                .map(Role::name)
                .collect(java.util.stream.Collectors.toSet());

        return new RegisterResponse(
                user.getId(),
                user.getUsername(),
                user.getEmail(),
                user.getFirstName(),
                user.getLastName(),
                roleNames,
                user.getCreatedAt()
        );
    }
}
