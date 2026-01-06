#set( $symbol_pound = '#' )
#set( $symbol_dollar = '$' )
#set( $symbol_escape = '\' )
package ${package}.auth;

import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

import java.util.Optional;
import java.util.UUID;

/**
 * Service for accessing the currently authenticated user.
 */
@Service
public class CurrentUserService {

    private final UserRepository userRepository;

    public CurrentUserService(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    /**
     * Gets the authenticated user principal from the security context.
     *
     * @return the authenticated principal, or empty if not authenticated
     */
    public Optional<AuthenticatedPrincipal> getAuthenticatedPrincipal() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();

        if (authentication == null || !authentication.isAuthenticated()) {
            return Optional.empty();
        }

        Object principal = authentication.getPrincipal();
        if (principal instanceof AuthenticatedPrincipal authenticatedPrincipal) {
            return Optional.of(authenticatedPrincipal);
        }

        return Optional.empty();
    }

    /**
     * Gets the current user's ID.
     *
     * @return the user ID, or empty if not authenticated
     */
    public Optional<UUID> getCurrentUserId() {
        return getAuthenticatedPrincipal().map(AuthenticatedPrincipal::getId);
    }

    /**
     * Gets the current user's email.
     *
     * @return the user email, or empty if not authenticated
     */
    public Optional<String> getCurrentUserEmail() {
        return getAuthenticatedPrincipal().map(AuthenticatedPrincipal::getEmail);
    }

    /**
     * Gets the full User entity for the currently authenticated user.
     *
     * @return the User entity, or empty if not authenticated
     */
    public Optional<User> getCurrentUser() {
        return getCurrentUserId().flatMap(userRepository::findById);
    }

    /**
     * Checks if the current user has a specific role.
     *
     * @param role the role to check
     * @return true if the user has the role, false otherwise
     */
    public boolean hasRole(String role) {
        return getAuthenticatedPrincipal()
                .map(p -> p.getRoles().contains(role))
                .orElse(false);
    }

    /**
     * Checks if there is an authenticated user.
     *
     * @return true if a user is authenticated
     */
    public boolean isAuthenticated() {
        return getAuthenticatedPrincipal().isPresent();
    }
}
