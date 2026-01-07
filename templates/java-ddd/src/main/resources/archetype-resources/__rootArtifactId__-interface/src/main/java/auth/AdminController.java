#set( $symbol_pound = '#' )
#set( $symbol_dollar = '$' )
#set( $symbol_escape = '\' )
package ${package}.auth;

import ${package}.common.ApiResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;

/**
 * REST controller for admin user management endpoints.
 * All endpoints require ADMIN role.
 */
@RestController
@RequestMapping("/api/admin/users")
@PreAuthorize("hasRole('ADMIN')")
public class AdminController {

    private static final Logger logger = LoggerFactory.getLogger(AdminController.class);

    private final UserRepository userRepository;
    private final TokenService tokenService;

    public AdminController(UserRepository userRepository, TokenService tokenService) {
        this.userRepository = userRepository;
        this.tokenService = tokenService;
    }

    /**
     * Lists all users (admin only).
     *
     * @return list of all users
     */
    @GetMapping
    public ResponseEntity<List<AdminUserResponse>> listUsers() {
        logger.info("Admin listing all users");

        List<AdminUserResponse> users = userRepository.findAll().stream()
                .map(AdminUserResponse::from)
                .collect(Collectors.toList());

        return ResponseEntity.ok(users);
    }

    /**
     * Enables a user account (admin only).
     *
     * @param id the user ID
     * @return the updated user
     */
    @PostMapping("/{id}/enable")
    public ResponseEntity<AdminUserResponse> enableUser(@PathVariable UUID id) {
        logger.info("Admin enabling user: {}", id);

        User user = userRepository.findById(id)
                .orElseThrow(() -> new UserNotFoundException("User not found with ID: " + id));

        user.enable();
        User savedUser = userRepository.save(user);

        return ResponseEntity.ok(AdminUserResponse.from(savedUser));
    }

    /**
     * Disables a user account (admin only).
     *
     * @param id the user ID
     * @return the updated user
     */
    @PostMapping("/{id}/disable")
    public ResponseEntity<AdminUserResponse> disableUser(@PathVariable UUID id) {
        logger.info("Admin disabling user: {}", id);

        User user = userRepository.findById(id)
                .orElseThrow(() -> new UserNotFoundException("User not found with ID: " + id));

        user.disable();
        User savedUser = userRepository.save(user);

        // Revoke all tokens for disabled user
        tokenService.revokeAllUserTokens(id);

        return ResponseEntity.ok(AdminUserResponse.from(savedUser));
    }

    /**
     * Updates user roles (admin only).
     *
     * @param id      the user ID
     * @param request the role update request
     * @return the updated user
     */
    @PutMapping("/{id}/roles")
    public ResponseEntity<AdminUserResponse> updateUserRoles(
            @PathVariable UUID id,
            @RequestBody UpdateRolesRequest request) {
        logger.info("Admin updating roles for user: {}", id);

        User user = userRepository.findById(id)
                .orElseThrow(() -> new UserNotFoundException("User not found with ID: " + id));

        Set<Role> newRoles = request.roles().stream()
                .map(Role::valueOf)
                .collect(Collectors.toSet());

        user.updateRoles(newRoles);
        User savedUser = userRepository.save(user);

        // Revoke tokens so user gets new roles on next login
        tokenService.revokeAllUserTokens(id);

        return ResponseEntity.ok(AdminUserResponse.from(savedUser));
    }

    /**
     * Deletes a user (admin only).
     *
     * @param id the user ID
     * @return success message
     */
    @DeleteMapping("/{id}")
    public ResponseEntity<ApiResponse<Void>> deleteUser(@PathVariable UUID id) {
        logger.info("Admin deleting user: {}", id);

        if (!userRepository.findById(id).isPresent()) {
            throw new UserNotFoundException("User not found with ID: " + id);
        }

        // Revoke all tokens before deletion
        tokenService.revokeAllUserTokens(id);
        userRepository.deleteById(id);

        return ResponseEntity.ok(ApiResponse.success("User deleted successfully", null));
    }

    /**
     * Response DTO for admin user listing.
     */
    public record AdminUserResponse(
            UUID id,
            String username,
            String email,
            String firstName,
            String lastName,
            Set<String> roles,
            boolean enabled,
            java.time.Instant createdAt,
            java.time.Instant updatedAt
    ) {
        public static AdminUserResponse from(User user) {
            Set<String> roleNames = user.getRoles().stream()
                    .map(Role::name)
                    .collect(Collectors.toSet());

            return new AdminUserResponse(
                    user.getId(),
                    user.getUsername(),
                    user.getEmail(),
                    user.getFirstName(),
                    user.getLastName(),
                    roleNames,
                    user.isEnabled(),
                    user.getCreatedAt(),
                    user.getUpdatedAt()
            );
        }
    }

    /**
     * Request DTO for updating user roles.
     */
    public record UpdateRolesRequest(Set<String> roles) {
    }

    /**
     * Exception thrown when user is not found.
     */
    public static class UserNotFoundException extends RuntimeException {
        public UserNotFoundException(String message) {
            super(message);
        }
    }
}
