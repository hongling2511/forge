#set( $symbol_pound = '#' )
#set( $symbol_dollar = '$' )
#set( $symbol_escape = '\' )
package ${package}.auth;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Set;

/**
 * Application service for user registration operations.
 */
@Service
public class UserRegistrationService {

    private static final Logger logger = LoggerFactory.getLogger(UserRegistrationService.class);

    private final UserRepository userRepository;
    private final PasswordService passwordService;

    public UserRegistrationService(UserRepository userRepository, PasswordService passwordService) {
        this.userRepository = userRepository;
        this.passwordService = passwordService;
    }

    /**
     * Registers a new user with the provided details.
     *
     * @param command the registration command containing user details
     * @return the created user
     * @throws EmailAlreadyExistsException if email is already registered
     * @throws UsernameAlreadyExistsException if username is already taken
     * @throws WeakPasswordException if password doesn't meet strength requirements
     */
    @Transactional
    public User register(RegisterCommand command) {
        logger.info("Processing registration for email: {}", command.email());

        // Validate email uniqueness
        if (userRepository.existsByEmail(command.email())) {
            throw new EmailAlreadyExistsException("Email is already registered: " + command.email());
        }

        // Validate username uniqueness
        if (userRepository.existsByUsername(command.username())) {
            throw new UsernameAlreadyExistsException("Username is already taken: " + command.username());
        }

        // Validate password strength
        var passwordValidation = passwordService.validatePasswordStrength(command.password());
        if (!passwordValidation.valid()) {
            throw new WeakPasswordException(passwordValidation.message());
        }

        // Hash password
        String passwordHash = passwordService.hashPassword(command.password());

        // Create user entity
        User user = User.create(
                command.username(),
                command.email(),
                passwordHash,
                command.firstName(),
                command.lastName(),
                Set.of(Role.USER)  // Default role
        );

        // Persist user
        User savedUser = userRepository.save(user);
        logger.info("Successfully registered user with ID: {}", savedUser.getId());

        return savedUser;
    }

    /**
     * Command object for user registration.
     */
    public record RegisterCommand(
            String username,
            String email,
            String password,
            String firstName,
            String lastName
    ) {
    }

    /**
     * Exception thrown when email is already registered.
     */
    public static class EmailAlreadyExistsException extends RuntimeException {
        public EmailAlreadyExistsException(String message) {
            super(message);
        }
    }

    /**
     * Exception thrown when username is already taken.
     */
    public static class UsernameAlreadyExistsException extends RuntimeException {
        public UsernameAlreadyExistsException(String message) {
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
