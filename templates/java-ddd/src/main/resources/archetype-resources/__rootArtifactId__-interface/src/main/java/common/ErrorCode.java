#set( $symbol_pound = '#' )
#set( $symbol_dollar = '$' )
#set( $symbol_escape = '\' )
package ${package}.common;

/**
 * Standardized error codes for API responses.
 *
 * Code ranges:
 * - 0: Success
 * - 1000-1999: Authentication errors
 * - 2000-2999: Authorization errors
 * - 3000-3999: Validation errors
 * - 4000-4999: Business logic errors
 * - 5000-5999: System errors
 */
public enum ErrorCode {

    // Success
    SUCCESS(0, "Success"),

    // Authentication errors (1000-1999)
    UNAUTHORIZED(1000, "Authentication required"),
    INVALID_CREDENTIALS(1001, "Invalid email or password"),
    TOKEN_EXPIRED(1002, "Token has expired"),
    TOKEN_INVALID(1003, "Invalid token"),
    ACCOUNT_DISABLED(1004, "Account is disabled"),

    // Authorization errors (2000-2999)
    FORBIDDEN(2000, "Access denied"),
    INSUFFICIENT_PERMISSIONS(2001, "Insufficient permissions"),

    // Validation errors (3000-3999)
    VALIDATION_ERROR(3000, "Validation failed"),
    INVALID_PARAMETER(3001, "Invalid parameter"),

    // Business errors (4000-4999)
    RESOURCE_NOT_FOUND(4000, "Resource not found"),
    USER_NOT_FOUND(4001, "User not found"),
    EMAIL_ALREADY_EXISTS(4002, "Email is already registered"),
    USERNAME_ALREADY_EXISTS(4003, "Username is already taken"),
    WEAK_PASSWORD(4004, "Password does not meet requirements"),

    // System errors (5000-5999)
    INTERNAL_ERROR(5000, "Internal server error"),
    SERVICE_UNAVAILABLE(5001, "Service temporarily unavailable");

    private final int code;
    private final String message;

    ErrorCode(int code, String message) {
        this.code = code;
        this.message = message;
    }

    public int getCode() {
        return code;
    }

    public String getMessage() {
        return message;
    }
}
