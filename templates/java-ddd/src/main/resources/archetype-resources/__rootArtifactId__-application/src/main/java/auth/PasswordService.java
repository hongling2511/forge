#set( $symbol_pound = '#' )
#set( $symbol_dollar = '$' )
#set( $symbol_escape = '\' )
package ${package}.auth;

import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

/**
 * Application service for password operations.
 * Encapsulates password hashing and validation logic.
 */
@Service
public class PasswordService {

    private final PasswordEncoder passwordEncoder;

    public PasswordService(PasswordEncoder passwordEncoder) {
        this.passwordEncoder = passwordEncoder;
    }

    /**
     * Hashes a raw password using BCrypt.
     *
     * @param rawPassword the plain text password
     * @return the hashed password
     */
    public String hashPassword(String rawPassword) {
        return passwordEncoder.encode(rawPassword);
    }

    /**
     * Verifies if a raw password matches the hashed password.
     *
     * @param rawPassword    the plain text password to check
     * @param hashedPassword the hashed password to compare against
     * @return true if passwords match, false otherwise
     */
    public boolean matches(String rawPassword, String hashedPassword) {
        return passwordEncoder.matches(rawPassword, hashedPassword);
    }

    /**
     * Validates password strength according to security requirements.
     * Requirements:
     * - Minimum 8 characters
     * - At least one uppercase letter
     * - At least one lowercase letter
     * - At least one digit
     * - At least one special character
     *
     * @param password the password to validate
     * @return validation result with details
     */
    public PasswordValidationResult validatePasswordStrength(String password) {
        if (password == null || password.isEmpty()) {
            return new PasswordValidationResult(false, "Password cannot be empty");
        }

        if (password.length() < 8) {
            return new PasswordValidationResult(false, "Password must be at least 8 characters long");
        }

        if (password.length() > 128) {
            return new PasswordValidationResult(false, "Password cannot exceed 128 characters");
        }

        if (!password.matches(".*[A-Z].*")) {
            return new PasswordValidationResult(false, "Password must contain at least one uppercase letter");
        }

        if (!password.matches(".*[a-z].*")) {
            return new PasswordValidationResult(false, "Password must contain at least one lowercase letter");
        }

        if (!password.matches(".*\\d.*")) {
            return new PasswordValidationResult(false, "Password must contain at least one digit");
        }

        if (!password.matches(".*[!@#$%^&*()_+\\-=\\[\\]{};':\"\\\\|,.<>/?].*")) {
            return new PasswordValidationResult(false, "Password must contain at least one special character");
        }

        return new PasswordValidationResult(true, "Password meets all requirements");
    }

    /**
     * Result of password validation.
     */
    public record PasswordValidationResult(boolean valid, String message) {
    }
}
