#set( $symbol_pound = '#' )
#set( $symbol_dollar = '$' )
#set( $symbol_escape = '\' )
package ${package}.auth;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import jakarta.validation.Valid;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;

/**
 * REST controller for user profile endpoints.
 * All endpoints require authentication.
 */
@RestController
@RequestMapping("/api/users")
public class UserController {

    private static final Logger logger = LoggerFactory.getLogger(UserController.class);

    private final CurrentUserService currentUserService;
    private final UserRepository userRepository;
    private final PasswordService passwordService;

    public UserController(CurrentUserService currentUserService,
                          UserRepository userRepository,
                          PasswordService passwordService) {
        this.currentUserService = currentUserService;
        this.userRepository = userRepository;
        this.passwordService = passwordService;
    }

    /**
     * Gets the current authenticated user's profile.
     *
     * @return the user profile
     */
    @GetMapping("/me")
    public ResponseEntity<UserProfileResponse> getCurrentUser() {
        logger.debug("Get current user profile request");

        User user = currentUserService.getCurrentUser()
                .orElseThrow(() -> new UserNotFoundException("Current user not found"));

        return ResponseEntity.ok(UserProfileResponse.from(user));
    }

    /**
     * Gets a user profile by ID (admin only).
     *
     * @param id the user ID
     * @return the user profile
     */
    @GetMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<UserProfileResponse> getUserById(@PathVariable UUID id) {
        logger.debug("Get user profile request for ID: {}", id);

        User user = userRepository.findById(id)
                .orElseThrow(() -> new UserNotFoundException("User not found with ID: " + id));

        return ResponseEntity.ok(UserProfileResponse.from(user));
    }

    /**
     * Updates the current user's profile.
     *
     * @param request the profile update request
     * @return the updated user profile
     */
    @PutMapping("/me/profile")
    public ResponseEntity<UserProfileResponse> updateProfile(@Valid @RequestBody UpdateProfileRequest request) {
        logger.debug("Update profile request");

        User user = currentUserService.getCurrentUser()
                .orElseThrow(() -> new UserNotFoundException("Current user not found"));

        user.updateProfile(request.firstName(), request.lastName());
        User savedUser = userRepository.save(user);

        return ResponseEntity.ok(UserProfileResponse.from(savedUser));
    }

    /**
     * Changes the current user's password.
     *
     * @param request the password change request
     * @return success message
     */
    @PutMapping("/me/password")
    public ResponseEntity<PasswordChangeResponse> changePassword(@Valid @RequestBody ChangePasswordRequest request) {
        logger.debug("Change password request");

        User user = currentUserService.getCurrentUser()
                .orElseThrow(() -> new UserNotFoundException("Current user not found"));

        // Verify current password
        if (!passwordService.matches(request.currentPassword(), user.getPasswordHash())) {
            throw new InvalidPasswordException("Current password is incorrect");
        }

        // Validate new password strength
        var validation = passwordService.validatePasswordStrength(request.newPassword());
        if (!validation.valid()) {
            throw new WeakPasswordException(validation.message());
        }

        // Update password
        String newPasswordHash = passwordService.hashPassword(request.newPassword());
        user.updatePassword(newPasswordHash);
        userRepository.save(user);

        return ResponseEntity.ok(new PasswordChangeResponse("Password changed successfully"));
    }

    /**
     * Response DTO for password change operation.
     */
    public record PasswordChangeResponse(String message) {
    }

    /**
     * Response DTO for user profile.
     */
    public record UserProfileResponse(
            UUID id,
            String username,
            String email,
            String firstName,
            String lastName,
            Set<String> roles,
            boolean enabled
    ) {
        public static UserProfileResponse from(User user) {
            Set<String> roleNames = user.getRoles().stream()
                    .map(Role::name)
                    .collect(Collectors.toSet());

            return new UserProfileResponse(
                    user.getId(),
                    user.getUsername(),
                    user.getEmail(),
                    user.getFirstName(),
                    user.getLastName(),
                    roleNames,
                    user.isEnabled()
            );
        }
    }

    /**
     * Exception thrown when user is not found.
     */
    public static class UserNotFoundException extends RuntimeException {
        public UserNotFoundException(String message) {
            super(message);
        }
    }

    /**
     * Exception thrown when password is invalid.
     */
    public static class InvalidPasswordException extends RuntimeException {
        public InvalidPasswordException(String message) {
            super(message);
        }
    }

    /**
     * Exception thrown when password doesn't meet strength requirements.
     */
    public static class WeakPasswordException extends RuntimeException {
        public WeakPasswordException(String message) {
            super(message);
        }
    }
}
