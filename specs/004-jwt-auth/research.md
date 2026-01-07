# Research: JWT Authentication for Java DDD Template

**Feature**: 004-jwt-auth
**Date**: 2026-01-05
**Status**: Complete

## Research Summary

This document captures technology decisions and best practices research for implementing JWT authentication in the java-ddd template.

---

## 1. JWT Library Selection

### Decision: **jjwt (io.jsonwebtoken) 0.12.x**

### Rationale
- Most widely used JWT library in Java ecosystem
- Actively maintained with regular security updates
- Fluent API for token creation and validation
- Built-in support for all standard JWT algorithms (HS256, RS256, ES256)
- Spring Boot compatible without additional configuration

### Alternatives Considered

| Library | Pros | Cons | Decision |
|---------|------|------|----------|
| jjwt 0.12.x | Industry standard, fluent API, active | Slightly larger footprint | ✅ Selected |
| java-jwt (auth0) | Lightweight | Fewer features, less Spring integration | ❌ Rejected |
| nimbus-jose-jwt | Comprehensive, JOSE support | Complex API, overkill for basic JWT | ❌ Rejected |
| Spring Security OAuth2 Resource Server | Built-in Spring support | Designed for OAuth2, heavier setup | ❌ Rejected |

---

## 2. Token Strategy

### Decision: **Access Token + Refresh Token with Database-Stored Refresh Tokens**

### Rationale
- Access tokens are stateless (no DB lookup required for validation)
- Refresh tokens stored in database enable explicit revocation (logout)
- Industry-standard pattern for balancing security and performance
- Access token: 15 minutes (short-lived, reduces attack window)
- Refresh token: 7 days (long-lived, enables session persistence)

### Token Flow
1. User logs in → receives access_token + refresh_token
2. Access token used for API requests (validated via signature, no DB)
3. On access token expiry → client uses refresh_token to get new access_token
4. On logout → refresh_token invalidated in database
5. On refresh token expiry → user must re-authenticate

### Alternatives Considered

| Strategy | Pros | Cons | Decision |
|----------|------|------|----------|
| Access + Refresh (DB) | Revocable, industry standard | Refresh needs DB | ✅ Selected |
| Access only (stateless) | Simple, no DB | No revocation possible | ❌ Rejected |
| Session-based | Simple revocation | Not stateless, scaling issues | ❌ Rejected |
| JWT blacklist | Revocable access tokens | DB lookup on every request | ❌ Rejected |

---

## 3. Password Hashing

### Decision: **BCrypt via Spring Security PasswordEncoder**

### Rationale
- Industry standard for password hashing
- Built into Spring Security (no additional dependencies)
- Adaptive work factor (configurable strength)
- Automatic salt generation
- Default strength of 10 is sufficient for most applications

### Alternatives Considered

| Algorithm | Pros | Cons | Decision |
|-----------|------|------|----------|
| BCrypt | Industry standard, Spring built-in | Moderate speed | ✅ Selected |
| Argon2 | Memory-hard, newer | Requires additional dependency | ❌ Rejected (complexity) |
| PBKDF2 | NIST approved | Slower, less common in Java | ❌ Rejected |
| SCrypt | Memory-hard | Less tooling support | ❌ Rejected |

---

## 4. Spring Security Configuration

### Decision: **Spring Security 6.x with SecurityFilterChain**

### Rationale
- Spring Security 6.x is the current standard for Spring Boot 3.x
- SecurityFilterChain is the modern configuration approach (replaces WebSecurityConfigurerAdapter)
- Declarative endpoint protection via annotations (@PreAuthorize)
- Built-in support for JWT via custom filter

### Configuration Approach
```
SecurityFilterChain
├── Permit: /api/auth/** (registration, login, refresh)
├── Authenticate: /** (all other endpoints require JWT)
└── JwtAuthenticationFilter (validates JWT, sets SecurityContext)
```

### Key Decisions
- Stateless session (no server-side session storage)
- CSRF disabled (not needed for stateless JWT)
- OncePerRequestFilter for JWT validation
- Custom AuthenticationEntryPoint for 401 responses
- Custom AccessDeniedHandler for 403 responses

---

## 5. Role-Based Access Control (RBAC)

### Decision: **Enum-based roles with @PreAuthorize annotations**

### Rationale
- Simple, clear role model (USER, ADMIN)
- Enum provides type safety
- @PreAuthorize integrates with Spring Security
- Roles stored as comma-separated string in User entity (simple, no join tables for MVP)

### Role Model
```java
public enum Role {
    USER,   // Default role for registered users
    ADMIN   // Administrative access
}
```

### Authorization Pattern
```java
@PreAuthorize("hasRole('ADMIN')")
public void adminOnlyMethod() { ... }

@PreAuthorize("hasAnyRole('USER', 'ADMIN')")
public void authenticatedMethod() { ... }
```

---

## 6. DDD Layer Placement

### Decision: **Authentication as cross-cutting bounded context within existing layers**

### Rationale
- Follows existing java-ddd template structure
- Authentication is infrastructure concern but has domain entities
- Clear separation maintains DDD principles

### Layer Assignment

| Component | Layer | Rationale |
|-----------|-------|-----------|
| User, Role, RefreshToken | Domain | Core domain entities |
| UserRepository (interface) | Domain | Domain port (hexagonal) |
| AuthService, TokenService | Application | Use cases |
| RegisterUserCommand, LoginCommand | Application | Commands/DTOs |
| JpaUserRepository | Infrastructure | Adapter implementation |
| JwtTokenProvider | Infrastructure | Technical infrastructure |
| SecurityConfig | Infrastructure | Framework configuration |
| AuthController | Interface | REST API adapter |
| DTOs (Request/Response) | Interface | API contracts |
| application.yml | Bootstrap | Configuration |

---

## 7. Error Handling

### Decision: **Structured error responses with HTTP status codes**

### Error Codes and Responses

| Scenario | HTTP Status | Error Code | Message |
|----------|-------------|------------|---------|
| Missing/malformed token | 401 | UNAUTHORIZED | "Authentication required" |
| Invalid/expired token | 401 | INVALID_TOKEN | "Invalid or expired token" |
| Invalid credentials | 401 | INVALID_CREDENTIALS | "Invalid username or password" |
| Insufficient permissions | 403 | ACCESS_DENIED | "Access denied" |
| User already exists | 409 | USER_EXISTS | "Email already registered" |
| Validation error | 400 | VALIDATION_ERROR | Field-specific messages |

### Response Format
```json
{
  "error": "INVALID_TOKEN",
  "message": "Invalid or expired token",
  "timestamp": "2026-01-05T10:30:00Z"
}
```

---

## 8. Configuration Properties

### Decision: **External configuration via application.yml**

### Properties
```yaml
jwt:
  secret: ${JWT_SECRET:default-dev-secret-change-in-production}
  access-token-expiration: 900000    # 15 minutes in ms
  refresh-token-expiration: 604800000 # 7 days in ms
  issuer: ${spring.application.name}
```

### Security Considerations
- Secret must be externalized in production (environment variable or secrets manager)
- Default development secret clearly marked as insecure
- Token expiration configurable per environment

---

## 9. Testing Strategy

### Decision: **Unit + Integration tests with MockMvc**

### Test Categories

| Category | Scope | Tools |
|----------|-------|-------|
| Unit | Service logic, token validation | JUnit 5, Mockito |
| Integration | Full auth flow | Spring Boot Test, MockMvc |
| Contract | API structure verification | Custom contract tests |

### Key Test Scenarios
1. Registration success/failure
2. Login success/failure
3. Token validation (valid, expired, malformed)
4. Token refresh flow
5. Protected endpoint access (with/without token)
6. Role-based access control
7. Logout and token invalidation

---

## 10. Dependencies

### Maven Dependencies to Add

```xml
<!-- Spring Security -->
<dependency>
    <groupId>org.springframework.boot</groupId>
    <artifactId>spring-boot-starter-security</artifactId>
</dependency>

<!-- JWT -->
<dependency>
    <groupId>io.jsonwebtoken</groupId>
    <artifactId>jjwt-api</artifactId>
    <version>0.12.3</version>
</dependency>
<dependency>
    <groupId>io.jsonwebtoken</groupId>
    <artifactId>jjwt-impl</artifactId>
    <version>0.12.3</version>
    <scope>runtime</scope>
</dependency>
<dependency>
    <groupId>io.jsonwebtoken</groupId>
    <artifactId>jjwt-jackson</artifactId>
    <version>0.12.3</version>
    <scope>runtime</scope>
</dependency>

<!-- Validation -->
<dependency>
    <groupId>org.springframework.boot</groupId>
    <artifactId>spring-boot-starter-validation</artifactId>
</dependency>
```

---

## Research Complete

All technical decisions documented. No outstanding NEEDS CLARIFICATION items. Ready for Phase 1 (data-model.md, contracts).
