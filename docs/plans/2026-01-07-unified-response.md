# Unified Response & TraceId Implementation Plan

> **For Claude:** REQUIRED SUB-SKILL: Use superpowers:executing-plans to implement this plan task-by-task.

**Goal:** Add unified `ApiResponse<T>` wrapper with traceId support to java-ddd template for consistent API responses.

**Architecture:** Create a TraceIdFilter that generates UUID traceId per request and stores in MDC. Wrap all responses in `ApiResponse<T>` with code/message/data/traceId. Use `ResponseBodyAdvice` to automatically wrap successful responses, and modify `GlobalExceptionHandler` to return wrapped error responses.

**Tech Stack:** Spring Web (Filter, ResponseBodyAdvice), SLF4J MDC, Java Records

---

## Path Conventions

All paths relative to: `templates/java-ddd/src/main/resources/archetype-resources/`

Module abbreviations:
- `infra/` = `__rootArtifactId__-infrastructure/src/main/java/`
- `iface/` = `__rootArtifactId__-interface/src/main/java/`

---

## Task 1: Create ApiResponse Record

**Files:**
- Create: `iface/common/ApiResponse.java`

**Step 1: Create the ApiResponse record**

```java
#set( $symbol_pound = '#' )
#set( $symbol_dollar = '$' )
#set( $symbol_escape = '\' )
package ${package}.common;

import com.fasterxml.jackson.annotation.JsonInclude;

/**
 * Unified API response wrapper.
 * All API responses follow this structure for consistency.
 *
 * @param <T> the type of the response data
 */
@JsonInclude(JsonInclude.Include.NON_NULL)
public record ApiResponse<T>(
        int code,
        String message,
        T data,
        String traceId
) {
    /** Success code */
    public static final int SUCCESS = 0;

    /** Generic error code */
    public static final int ERROR = -1;

    /**
     * Creates a successful response with data.
     */
    public static <T> ApiResponse<T> success(T data) {
        return new ApiResponse<>(SUCCESS, "success", data, TraceIdContext.getTraceId());
    }

    /**
     * Creates a successful response with custom message.
     */
    public static <T> ApiResponse<T> success(String message, T data) {
        return new ApiResponse<>(SUCCESS, message, data, TraceIdContext.getTraceId());
    }

    /**
     * Creates a successful response without data.
     */
    public static <T> ApiResponse<T> success() {
        return new ApiResponse<>(SUCCESS, "success", null, TraceIdContext.getTraceId());
    }

    /**
     * Creates an error response.
     */
    public static <T> ApiResponse<T> error(int code, String message) {
        return new ApiResponse<>(code, message, null, TraceIdContext.getTraceId());
    }

    /**
     * Creates an error response with details.
     */
    public static <T> ApiResponse<T> error(int code, String message, T data) {
        return new ApiResponse<>(code, message, data, TraceIdContext.getTraceId());
    }
}
```

**Step 2: Commit**

```bash
git add iface/common/ApiResponse.java
git commit -m "feat: add ApiResponse unified response wrapper"
```

---

## Task 2: Create TraceIdContext Utility

**Files:**
- Create: `iface/common/TraceIdContext.java`

**Step 1: Create TraceIdContext**

```java
#set( $symbol_pound = '#' )
#set( $symbol_dollar = '$' )
#set( $symbol_escape = '\' )
package ${package}.common;

import org.slf4j.MDC;

/**
 * Utility class for managing trace ID in the current request context.
 * Uses SLF4J MDC for thread-local storage and log correlation.
 */
public final class TraceIdContext {

    public static final String TRACE_ID_KEY = "traceId";
    public static final String TRACE_ID_HEADER = "X-Trace-Id";

    private TraceIdContext() {
        // Utility class
    }

    /**
     * Sets the trace ID for the current request.
     */
    public static void setTraceId(String traceId) {
        MDC.put(TRACE_ID_KEY, traceId);
    }

    /**
     * Gets the trace ID for the current request.
     */
    public static String getTraceId() {
        return MDC.get(TRACE_ID_KEY);
    }

    /**
     * Clears the trace ID from the current context.
     */
    public static void clear() {
        MDC.remove(TRACE_ID_KEY);
    }
}
```

**Step 2: Commit**

```bash
git add iface/common/TraceIdContext.java
git commit -m "feat: add TraceIdContext for MDC-based trace ID management"
```

---

## Task 3: Create TraceIdFilter

**Files:**
- Create: `infra/filter/TraceIdFilter.java`

**Step 1: Create TraceIdFilter**

```java
#set( $symbol_pound = '#' )
#set( $symbol_dollar = '$' )
#set( $symbol_escape = '\' )
package ${package}.filter;

import ${package}.common.TraceIdContext;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.UUID;

/**
 * Filter that generates or propagates trace ID for each request.
 * Trace ID is stored in MDC for logging and returned in response header.
 */
@Component
@Order(Ordered.HIGHEST_PRECEDENCE)
public class TraceIdFilter extends OncePerRequestFilter {

    @Override
    protected void doFilterInternal(HttpServletRequest request,
                                    HttpServletResponse response,
                                    FilterChain filterChain) throws ServletException, IOException {
        try {
            // Get trace ID from header or generate new one
            String traceId = request.getHeader(TraceIdContext.TRACE_ID_HEADER);
            if (!StringUtils.hasText(traceId)) {
                traceId = UUID.randomUUID().toString().replace("-", "");
            }

            // Set in MDC for logging
            TraceIdContext.setTraceId(traceId);

            // Add to response header
            response.setHeader(TraceIdContext.TRACE_ID_HEADER, traceId);

            filterChain.doFilter(request, response);
        } finally {
            // Clean up MDC
            TraceIdContext.clear();
        }
    }
}
```

**Step 2: Commit**

```bash
git add infra/filter/TraceIdFilter.java
git commit -m "feat: add TraceIdFilter for trace ID generation and propagation"
```

---

## Task 4: Create ErrorCode Enum

**Files:**
- Create: `iface/common/ErrorCode.java`

**Step 1: Create ErrorCode enum**

```java
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
```

**Step 2: Commit**

```bash
git add iface/common/ErrorCode.java
git commit -m "feat: add ErrorCode enum with standardized error codes"
```

---

## Task 5: Create ResponseWrapper (ResponseBodyAdvice)

**Files:**
- Create: `iface/common/ResponseWrapper.java`

**Step 1: Create ResponseWrapper**

```java
#set( $symbol_pound = '#' )
#set( $symbol_dollar = '$' )
#set( $symbol_escape = '\' )
package ${package}.common;

import org.springframework.core.MethodParameter;
import org.springframework.http.MediaType;
import org.springframework.http.converter.HttpMessageConverter;
import org.springframework.http.server.ServerHttpRequest;
import org.springframework.http.server.ServerHttpResponse;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.servlet.mvc.method.annotation.ResponseBodyAdvice;

/**
 * Automatically wraps controller responses in ApiResponse.
 * Excludes responses that are already ApiResponse or error responses.
 */
@RestControllerAdvice(basePackages = "${package}")
public class ResponseWrapper implements ResponseBodyAdvice<Object> {

    @Override
    public boolean supports(MethodParameter returnType, Class<? extends HttpMessageConverter<?>> converterType) {
        // Skip if already wrapped or is an error response
        Class<?> declaringClass = returnType.getDeclaringClass();
        String className = declaringClass.getSimpleName();

        // Skip GlobalExceptionHandler responses (they handle their own wrapping)
        if (className.contains("ExceptionHandler")) {
            return false;
        }

        return true;
    }

    @Override
    public Object beforeBodyWrite(Object body,
                                   MethodParameter returnType,
                                   MediaType selectedContentType,
                                   Class<? extends HttpMessageConverter<?>> selectedConverterType,
                                   ServerHttpRequest request,
                                   ServerHttpResponse response) {
        // Already wrapped
        if (body instanceof ApiResponse) {
            return body;
        }

        // Wrap in ApiResponse
        return ApiResponse.success(body);
    }
}
```

**Step 2: Commit**

```bash
git add iface/common/ResponseWrapper.java
git commit -m "feat: add ResponseWrapper to auto-wrap responses in ApiResponse"
```

---

## Task 6: Update GlobalExceptionHandler

**Files:**
- Modify: `iface/auth/GlobalExceptionHandler.java`

**Step 1: Update GlobalExceptionHandler to return ApiResponse**

Replace the entire file content:

```java
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
```

**Step 2: Commit**

```bash
git add iface/auth/GlobalExceptionHandler.java
git commit -m "refactor: update GlobalExceptionHandler to return ApiResponse"
```

---

## Task 7: Update AuthController to Remove Custom Exception Handlers

**Files:**
- Modify: `iface/auth/AuthController.java`

**Step 1: Remove local @ExceptionHandler methods (lines 113-201)**

The GlobalExceptionHandler now handles all exceptions. Remove the duplicate handlers from AuthController.

Update AuthController to keep only the endpoint methods (lines 1-111).

**Step 2: Commit**

```bash
git add iface/auth/AuthController.java
git commit -m "refactor: remove duplicate exception handlers from AuthController"
```

---

## Task 8: Update logback configuration for traceId

**Files:**
- Modify: `__rootArtifactId__-bootstrap/src/main/resources/application.yml`

**Step 1: Add logging pattern with traceId**

Add to application.yml:

```yaml
logging:
  pattern:
    console: "%d{yyyy-MM-dd HH:mm:ss} [%thread] [%X{traceId}] %-5level %logger{36} - %msg%n"
```

**Step 2: Commit**

```bash
git add __rootArtifactId__-bootstrap/src/main/resources/application.yml
git commit -m "feat: add traceId to logging pattern"
```

---

## Task 9: Delete ErrorResponse.java (now obsolete)

**Files:**
- Delete: `iface/auth/ErrorResponse.java`
- Delete: `iface/auth/MessageResponse.java`

**Step 1: Delete obsolete files**

```bash
rm iface/auth/ErrorResponse.java
rm iface/auth/MessageResponse.java
```

**Step 2: Update references**

Update AuthController and UserController to remove MessageResponse usage (logout endpoint returns `ApiResponse.success("Successfully logged out", null)`).

**Step 3: Commit**

```bash
git add -A
git commit -m "refactor: remove obsolete ErrorResponse and MessageResponse DTOs"
```

---

## Task 10: Update archetype-metadata.xml

**Files:**
- Modify: `META-INF/maven/archetype-metadata.xml` (in templates/java-ddd/src/main/resources/)

**Step 1: Verify common package is included**

The existing `**/*.java` pattern should automatically include the new `common/` package files. Verify this is the case.

**Step 2: Commit (if changes needed)**

```bash
git add META-INF/maven/archetype-metadata.xml
git commit -m "chore: update archetype metadata for common package"
```

---

## Task 11: Update template.yaml version

**Files:**
- Modify: `templates/java-ddd/template.yaml`

**Step 1: Update version to 1.2.0**

```yaml
name: java-ddd
version: 1.2.0
description: "Java DDD 多模块工程 (Spring Boot 3.x) - 含 JWT 认证和统一响应"
```

**Step 2: Commit**

```bash
git add templates/java-ddd/template.yaml
git commit -m "chore: bump template version to 1.2.0"
```

---

## Task 12: Update CHANGELOG.md

**Files:**
- Modify: `CHANGELOG.md`

**Step 1: Add v2.3.0 entry**

Add new version entry documenting:
- Unified ApiResponse<T> wrapper
- TraceId mechanism with MDC
- ErrorCode enum
- Updated GlobalExceptionHandler

**Step 2: Commit**

```bash
git add CHANGELOG.md
git commit -m "docs: add v2.3.0 release notes for unified response"
```

---

## Task 13: Build and Test

**Step 1: Build archetype**

```bash
cd templates/java-ddd
mvn clean install -DskipTests
```

Expected: BUILD SUCCESS

**Step 2: Generate test project**

```bash
cd /tmp
rm -rf unified-test
forge new -t java-ddd -g com.example -a unified-test
```

**Step 3: Build generated project**

```bash
cd /tmp/unified-test
mvn clean package -DskipTests
```

Expected: BUILD SUCCESS

**Step 4: Start and test**

```bash
java -jar unified-test-bootstrap/target/unified-test-bootstrap-1.0.0-SNAPSHOT.jar &
sleep 5

# Test registration - should return wrapped response
curl -s -X POST http://localhost:8080/api/auth/register \
  -H "Content-Type: application/json" \
  -d '{"username":"test","email":"test@example.com","password":"Test@1234"}' | jq .

# Expected response structure:
# {
#   "code": 0,
#   "message": "success",
#   "data": { "id": "...", "username": "test", ... },
#   "traceId": "abc123..."
# }

# Check X-Trace-Id header
curl -s -I -X POST http://localhost:8080/api/auth/login \
  -H "Content-Type: application/json" \
  -d '{"email":"test@example.com","password":"Test@1234"}' | grep -i trace

# Expected: X-Trace-Id: <32-char-hex>
```

**Step 5: Commit final verification**

```bash
git add -A
git commit -m "test: verify unified response implementation"
```

---

## Summary

| Task | Description | Files |
|------|-------------|-------|
| 1 | Create ApiResponse record | `iface/common/ApiResponse.java` |
| 2 | Create TraceIdContext | `iface/common/TraceIdContext.java` |
| 3 | Create TraceIdFilter | `infra/filter/TraceIdFilter.java` |
| 4 | Create ErrorCode enum | `iface/common/ErrorCode.java` |
| 5 | Create ResponseWrapper | `iface/common/ResponseWrapper.java` |
| 6 | Update GlobalExceptionHandler | `iface/auth/GlobalExceptionHandler.java` |
| 7 | Cleanup AuthController | `iface/auth/AuthController.java` |
| 8 | Add logging pattern | `application.yml` |
| 9 | Delete obsolete DTOs | `ErrorResponse.java`, `MessageResponse.java` |
| 10 | Verify archetype metadata | `archetype-metadata.xml` |
| 11 | Update template version | `template.yaml` |
| 12 | Update changelog | `CHANGELOG.md` |
| 13 | Build and test | - |

---

## Response Format Examples

**Success Response:**
```json
{
  "code": 0,
  "message": "success",
  "data": {
    "accessToken": "eyJ...",
    "refreshToken": "abc123",
    "user": { ... }
  },
  "traceId": "a1b2c3d4e5f6789012345678"
}
```

**Error Response:**
```json
{
  "code": 1001,
  "message": "Invalid email or password",
  "data": null,
  "traceId": "a1b2c3d4e5f6789012345678"
}
```

**Validation Error Response:**
```json
{
  "code": 3000,
  "message": "Validation failed",
  "data": {
    "email": "must be a valid email",
    "password": "size must be between 8 and 128"
  },
  "traceId": "a1b2c3d4e5f6789012345678"
}
```
