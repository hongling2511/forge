# Feature Specification: JWT Authentication for Java DDD Template

**Feature Branch**: `004-jwt-auth`
**Created**: 2026-01-05
**Status**: Draft
**Input**: User description: "feature004: 支持用户服务和业务接口的jwt鉴权，先完善java-ddd模版"

## User Scenarios & Testing *(mandatory)*

### User Story 1 - Generate Project with JWT Authentication (Priority: P1)

As a developer using the forge CLI, I want to generate a new Java DDD project that includes pre-configured JWT authentication, so that I can immediately start building secured business APIs without setting up authentication infrastructure from scratch.

**Why this priority**: This is the core value proposition - developers should get a working, secured project scaffold out of the box. Without this, the template provides no authentication capability.

**Independent Test**: Can be fully tested by running `forge new -t java-ddd -g com.example -a demo-service` and verifying the generated project contains JWT authentication configuration, dependencies, and basic user service components.

**Acceptance Scenarios**:

1. **Given** a developer runs forge CLI with java-ddd template, **When** the project is generated, **Then** the project includes JWT authentication dependencies and configuration files
2. **Given** a generated project, **When** the developer builds the project without modifications, **Then** the build succeeds and the application starts with authentication enabled
3. **Given** a generated project, **When** the developer examines the project structure, **Then** they find clearly organized authentication components in the appropriate DDD layers

---

### User Story 2 - User Registration and Login (Priority: P1)

As a developer using the generated project, I want the template to include a working user registration and login flow with JWT token issuance, so that I can test authentication immediately and understand the implementation pattern.

**Why this priority**: Registration and login are fundamental authentication operations. Without working examples, developers cannot verify the authentication system works or learn from the patterns.

**Independent Test**: Can be fully tested by starting the generated application, calling the registration endpoint to create a user, then calling the login endpoint to receive a JWT token.

**Acceptance Scenarios**:

1. **Given** a running generated application, **When** a new user submits registration with username, email, and password, **Then** the user account is created and a success response is returned
2. **Given** a registered user, **When** they submit valid credentials to the login endpoint, **Then** they receive a JWT access token and refresh token
3. **Given** a user submits invalid credentials, **When** they attempt to login, **Then** they receive an appropriate error message without exposing sensitive information
4. **Given** a user submits registration with an existing email, **When** the registration is processed, **Then** they receive an error indicating the email is already registered

---

### User Story 3 - Protect Business API Endpoints (Priority: P1)

As a developer building business features, I want to easily protect my API endpoints with JWT authentication using annotations or configuration, so that I can secure my APIs without writing boilerplate security code.

**Why this priority**: The primary purpose of JWT authentication is to protect business APIs. Developers need a clear, simple pattern to secure their endpoints.

**Independent Test**: Can be fully tested by creating a sample protected endpoint, making an unauthenticated request (should fail), then making an authenticated request with valid JWT (should succeed).

**Acceptance Scenarios**:

1. **Given** a protected API endpoint, **When** a request is made without a JWT token, **Then** the request is rejected with 401 Unauthorized
2. **Given** a protected API endpoint, **When** a request is made with a valid JWT token in the Authorization header, **Then** the request is processed and the user context is available
3. **Given** a protected API endpoint, **When** a request is made with an expired JWT token, **Then** the request is rejected with an appropriate error indicating token expiration
4. **Given** a protected API endpoint, **When** a request is made with a malformed JWT token, **Then** the request is rejected with 401 Unauthorized

---

### User Story 4 - Token Refresh Flow (Priority: P2)

As a developer, I want the template to include token refresh functionality, so that users can maintain their session without re-entering credentials when access tokens expire.

**Why this priority**: Token refresh is important for user experience but not critical for basic authentication functionality. Applications can work with just access tokens initially.

**Independent Test**: Can be fully tested by obtaining tokens via login, waiting for access token expiration (or using a short-lived token), then using the refresh token to obtain new access tokens.

**Acceptance Scenarios**:

1. **Given** a user with a valid refresh token, **When** they request token refresh, **Then** they receive a new access token
2. **Given** a user with an expired refresh token, **When** they request token refresh, **Then** the request is rejected and they must re-authenticate
3. **Given** a user with an invalidated refresh token (e.g., after logout), **When** they request token refresh, **Then** the request is rejected

---

### User Story 5 - Role-Based Access Control (Priority: P2)

As a developer, I want the template to include role-based access control (RBAC) examples, so that I can restrict certain endpoints to specific user roles.

**Why this priority**: RBAC is essential for most real applications but is an extension of basic authentication. The system should work without roles initially.

**Independent Test**: Can be fully tested by creating users with different roles, then verifying that role-restricted endpoints allow or deny access based on user roles.

**Acceptance Scenarios**:

1. **Given** an endpoint restricted to ADMIN role, **When** a user with ADMIN role accesses it, **Then** the request is allowed
2. **Given** an endpoint restricted to ADMIN role, **When** a user without ADMIN role accesses it, **Then** the request is rejected with 403 Forbidden
3. **Given** the generated project, **When** the developer examines role management, **Then** they find clear examples of assigning and checking roles

---

### User Story 6 - Logout and Token Invalidation (Priority: P3)

As a developer, I want the template to include logout functionality that invalidates tokens, so that users can securely end their sessions.

**Why this priority**: Logout is important for security but many applications can function initially without explicit logout (tokens simply expire).

**Independent Test**: Can be fully tested by logging in, calling the logout endpoint, then attempting to use the previous tokens (should fail).

**Acceptance Scenarios**:

1. **Given** an authenticated user, **When** they call the logout endpoint, **Then** their refresh token is invalidated
2. **Given** a logged-out user, **When** they attempt to use their previous refresh token, **Then** the request is rejected

---

### Edge Cases

- What happens when a user attempts to access a protected endpoint during token refresh? The request should wait or fail gracefully
- How does the system handle concurrent login sessions from the same user? By default, multiple sessions are allowed
- What happens when JWT signing key is changed? All existing tokens become invalid; users must re-authenticate
- How does the system handle clock skew between client and server for token expiration? A reasonable tolerance (e.g., 60 seconds) should be configured

## Requirements *(mandatory)*

### Functional Requirements

- **FR-001**: System MUST generate JWT authentication infrastructure as part of the java-ddd template
- **FR-002**: System MUST include user entity with username, email, password (hashed), and roles
- **FR-003**: System MUST provide user registration endpoint that validates input and creates user accounts
- **FR-004**: System MUST provide login endpoint that validates credentials and issues JWT tokens
- **FR-005**: System MUST issue both access tokens (short-lived) and refresh tokens (long-lived)
- **FR-006**: System MUST validate JWT tokens on protected endpoints and reject invalid/expired tokens
- **FR-007**: System MUST extract and provide user context from validated JWT tokens to business logic
- **FR-008**: System MUST support protecting endpoints via declarative configuration (annotations)
- **FR-009**: System MUST provide token refresh endpoint for obtaining new access tokens
- **FR-010**: System MUST support role-based access control with at least USER and ADMIN roles
- **FR-011**: System MUST provide logout functionality that invalidates refresh tokens
- **FR-012**: System MUST hash passwords before storage using secure algorithms
- **FR-013**: System MUST include example protected endpoint demonstrating authentication usage
- **FR-014**: System MUST return appropriate HTTP status codes (401, 403) for authentication failures
- **FR-015**: System MUST log authentication events (login success/failure, token validation failures)

### Key Entities

- **User**: Represents an authenticated user with unique identifier, username, email, hashed password, assigned roles, and account status
- **Role**: Represents a permission group (USER, ADMIN) that can be assigned to users
- **RefreshToken**: Represents a long-lived token associated with a user for obtaining new access tokens, with expiration and revocation status

## Success Criteria *(mandatory)*

### Measurable Outcomes

- **SC-001**: Developer can generate a new project with working JWT authentication in under 5 minutes
- **SC-002**: Generated project builds and starts successfully without any code modifications
- **SC-003**: Developer can complete user registration, login, and access protected endpoint in under 10 minutes
- **SC-004**: 100% of authentication-related endpoints return appropriate HTTP status codes for success and error cases
- **SC-005**: Developer can understand and extend the authentication system by examining included examples and documentation
- **SC-006**: Authentication system handles 100 concurrent authentication requests without errors

## Assumptions

- The java-ddd template uses Spring Boot 3.x as the application framework
- Passwords will be hashed using industry-standard algorithms (e.g., bcrypt)
- JWT tokens will follow standard JWT format (RFC 7519)
- Access token lifetime defaults to 15 minutes; refresh token lifetime defaults to 7 days
- The generated project targets Java 17+
- User data will be persisted to a relational database (configuration provided for common databases)
- The template generates example code that developers may modify; it is not a library
