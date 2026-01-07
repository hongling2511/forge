#set( $symbol_pound = '#' )
#set( $symbol_dollar = '$' )
#set( $symbol_escape = '\' )
package ${package}.auth;

import ${package}.common.ApiResponse;
import ${package}.common.ErrorCode;
import jakarta.servlet.http.HttpServletRequest;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.core.AuthenticationException;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.util.HashMap;
import java.util.Map;

/**
 * Global exception handler for REST API endpoints.
 * Returns consistent ApiResponse format for all errors.
 */
@RestControllerAdvice
public class GlobalExceptionHandler {

    private static final Logger logger = LoggerFactory.getLogger(GlobalExceptionHandler.class);

    /**
     * Handles validation errors from @Valid annotations.
     */
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ApiResponse<Map<String, String>>> handleValidationException(
            MethodArgumentNotValidException ex,
            HttpServletRequest request) {

        Map<String, String> fieldErrors = new HashMap<>();
        for (FieldError error : ex.getBindingResult().getFieldErrors()) {
            fieldErrors.put(error.getField(), error.getDefaultMessage());
        }

        ApiResponse<Map<String, String>> response = ApiResponse.error(
                ErrorCode.VALIDATION_ERROR.getCode(),
                ErrorCode.VALIDATION_ERROR.getMessage(),
                fieldErrors
        );

        return ResponseEntity.badRequest().body(response);
    }

    /**
     * Handles bad credentials during authentication.
     */
    @ExceptionHandler(BadCredentialsException.class)
    public ResponseEntity<ApiResponse<Void>> handleBadCredentialsException(
            BadCredentialsException ex,
            HttpServletRequest request) {

        logger.warn("Bad credentials attempt for request: {}", request.getRequestURI());

        ApiResponse<Void> response = ApiResponse.error(
                ErrorCode.INVALID_CREDENTIALS.getCode(),
                ErrorCode.INVALID_CREDENTIALS.getMessage()
        );

        return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(response);
    }

    /**
     * Handles authentication failures.
     */
    @ExceptionHandler(AuthenticationException.class)
    public ResponseEntity<ApiResponse<Void>> handleAuthenticationException(
            AuthenticationException ex,
            HttpServletRequest request) {

        logger.warn("Authentication failed for request: {} - {}", request.getRequestURI(), ex.getMessage());

        ApiResponse<Void> response = ApiResponse.error(
                ErrorCode.UNAUTHORIZED.getCode(),
                ErrorCode.UNAUTHORIZED.getMessage()
        );

        return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(response);
    }

    /**
     * Handles access denied (insufficient permissions).
     */
    @ExceptionHandler(AccessDeniedException.class)
    public ResponseEntity<ApiResponse<Void>> handleAccessDeniedException(
            AccessDeniedException ex,
            HttpServletRequest request) {

        logger.warn("Access denied for request: {}", request.getRequestURI());

        ApiResponse<Void> response = ApiResponse.error(
                ErrorCode.FORBIDDEN.getCode(),
                ErrorCode.FORBIDDEN.getMessage()
        );

        return ResponseEntity.status(HttpStatus.FORBIDDEN).body(response);
    }

    /**
     * Handles email already exists exception.
     */
    @ExceptionHandler(UserRegistrationService.EmailAlreadyExistsException.class)
    public ResponseEntity<ApiResponse<Void>> handleEmailAlreadyExists(
            UserRegistrationService.EmailAlreadyExistsException ex,
            HttpServletRequest request) {

        ApiResponse<Void> response = ApiResponse.error(
                ErrorCode.EMAIL_ALREADY_EXISTS.getCode(),
                ErrorCode.EMAIL_ALREADY_EXISTS.getMessage()
        );

        return ResponseEntity.status(HttpStatus.CONFLICT).body(response);
    }

    /**
     * Handles username already exists exception.
     */
    @ExceptionHandler(UserRegistrationService.UsernameAlreadyExistsException.class)
    public ResponseEntity<ApiResponse<Void>> handleUsernameAlreadyExists(
            UserRegistrationService.UsernameAlreadyExistsException ex,
            HttpServletRequest request) {

        ApiResponse<Void> response = ApiResponse.error(
                ErrorCode.USERNAME_ALREADY_EXISTS.getCode(),
                ErrorCode.USERNAME_ALREADY_EXISTS.getMessage()
        );

        return ResponseEntity.status(HttpStatus.CONFLICT).body(response);
    }

    /**
     * Handles weak password exception.
     */
    @ExceptionHandler(UserRegistrationService.WeakPasswordException.class)
    public ResponseEntity<ApiResponse<Void>> handleWeakPassword(
            UserRegistrationService.WeakPasswordException ex,
            HttpServletRequest request) {

        ApiResponse<Void> response = ApiResponse.error(
                ErrorCode.WEAK_PASSWORD.getCode(),
                ex.getMessage()
        );

        return ResponseEntity.badRequest().body(response);
    }

    /**
     * Handles invalid token exception.
     */
    @ExceptionHandler(TokenService.InvalidTokenException.class)
    public ResponseEntity<ApiResponse<Void>> handleInvalidToken(
            TokenService.InvalidTokenException ex,
            HttpServletRequest request) {

        ApiResponse<Void> response = ApiResponse.error(
                ErrorCode.TOKEN_INVALID.getCode(),
                ex.getMessage()
        );

        return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(response);
    }

    /**
     * Handles account disabled exception.
     */
    @ExceptionHandler(AuthenticationService.AccountDisabledException.class)
    public ResponseEntity<ApiResponse<Void>> handleAccountDisabled(
            AuthenticationService.AccountDisabledException ex,
            HttpServletRequest request) {

        ApiResponse<Void> response = ApiResponse.error(
                ErrorCode.ACCOUNT_DISABLED.getCode(),
                ErrorCode.ACCOUNT_DISABLED.getMessage()
        );

        return ResponseEntity.status(HttpStatus.FORBIDDEN).body(response);
    }

    /**
     * Handles illegal argument exceptions (business validation errors).
     */
    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<ApiResponse<Void>> handleIllegalArgumentException(
            IllegalArgumentException ex,
            HttpServletRequest request) {

        ApiResponse<Void> response = ApiResponse.error(
                ErrorCode.INVALID_PARAMETER.getCode(),
                ex.getMessage()
        );

        return ResponseEntity.badRequest().body(response);
    }

    /**
     * Handles all other uncaught exceptions.
     */
    @ExceptionHandler(Exception.class)
    public ResponseEntity<ApiResponse<Void>> handleGenericException(
            Exception ex,
            HttpServletRequest request) {

        logger.error("Unexpected error for request: {}", request.getRequestURI(), ex);

        ApiResponse<Void> response = ApiResponse.error(
                ErrorCode.INTERNAL_ERROR.getCode(),
                ErrorCode.INTERNAL_ERROR.getMessage()
        );

        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
    }
}
