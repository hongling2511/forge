# Tasks: JWT Authentication for Java DDD Template

**Input**: Design documents from `/specs/004-jwt-auth/`
**Prerequisites**: plan.md, spec.md, research.md, data-model.md, contracts/auth-api.yaml

**Tests**: Tests are NOT explicitly requested in the feature specification. This task list focuses on implementation only.

**Organization**: Tasks are grouped by user story to enable independent implementation and testing of each story.

## Format: `[ID] [P?] [Story] Description`

- **[P]**: Can run in parallel (different files, no dependencies)
- **[Story]**: Which user story this task belongs to (e.g., US1, US2, US3)
- Include exact file paths in descriptions

## Path Conventions

This is a Maven archetype template enhancement. All paths are relative to:
`templates/java-ddd/src/main/resources/archetype-resources/`

Module abbreviations used:
- `domain/` = `__rootArtifactId__-domain/src/main/java/`
- `app/` = `__rootArtifactId__-application/src/main/java/`
- `infra/` = `__rootArtifactId__-infrastructure/src/main/java/`
- `iface/` = `__rootArtifactId__-interface/src/main/java/`
- `boot/` = `__rootArtifactId__-bootstrap/src/main/resources/`

---

## Phase 1: Setup (Shared Infrastructure)

**Purpose**: Add authentication dependencies and configuration to the archetype template

- [ ] T001 Add Spring Security and jjwt dependencies to parent pom.xml in `templates/java-ddd/src/main/resources/archetype-resources/pom.xml`
- [ ] T002 Add Spring Boot Validation dependency to parent pom.xml
- [ ] T003 [P] Add security dependencies to infrastructure module pom.xml in `__rootArtifactId__-infrastructure/pom.xml`
- [ ] T004 [P] Add validation dependencies to interface module pom.xml in `__rootArtifactId__-interface/pom.xml`
- [ ] T005 Create JWT configuration properties in `boot/application.yml`

---

## Phase 2: Foundational (Blocking Prerequisites)

**Purpose**: Core domain entities and infrastructure that MUST be complete before ANY user story can be implemented

**⚠️ CRITICAL**: No user story work can begin until this phase is complete

### Domain Layer (Entities)

- [ ] T006 [P] Create Role enum in `domain/auth/Role.java`
- [ ] T007 [P] Create User entity in `domain/auth/User.java` (with id, username, email, passwordHash, roles, enabled, timestamps)
- [ ] T008 [P] Create RefreshToken entity in `domain/auth/RefreshToken.java` (with id, token, userId, expiresAt, revoked, createdAt)
- [ ] T009 Create UserRepository interface (domain port) in `domain/auth/UserRepository.java`
- [ ] T010 Create RefreshTokenRepository interface (domain port) in `domain/auth/RefreshTokenRepository.java`

### Infrastructure Layer (Core Security)

- [ ] T011 Implement JwtTokenProvider for JWT creation/validation in `infra/auth/JwtTokenProvider.java`
- [ ] T012 Implement JpaUserRepository (adapter) in `infra/auth/JpaUserRepository.java`
- [ ] T013 Implement JpaRefreshTokenRepository (adapter) in `infra/auth/JpaRefreshTokenRepository.java`
- [ ] T014 Create JwtAuthenticationFilter for token validation in `infra/auth/JwtAuthenticationFilter.java`
- [ ] T015 Create CustomUserDetailsService for Spring Security in `infra/auth/CustomUserDetailsService.java`
- [ ] T016 Configure SecurityConfig with SecurityFilterChain in `infra/auth/SecurityConfig.java`

### Application Layer (Core Services)

- [ ] T017 Create TokenService for token operations in `app/auth/TokenService.java`
- [ ] T018 Create PasswordService for password hashing in `app/auth/PasswordService.java`

### Interface Layer (DTOs and Error Handling)

- [ ] T019 [P] Create ErrorResponse DTO in `iface/auth/ErrorResponse.java`
- [ ] T020 [P] Create MessageResponse DTO in `iface/auth/MessageResponse.java`
- [ ] T021 Create GlobalExceptionHandler for auth errors in `iface/auth/GlobalExceptionHandler.java`

**Checkpoint**: Foundation ready - user story implementation can now begin

---

## Phase 3: User Story 1 - Generate Project with JWT Authentication (Priority: P1) 🎯 MVP

**Goal**: Template generates project with JWT auth infrastructure that builds and starts successfully

**Independent Test**: Run `forge new -t java-ddd -g com.example -a demo-service`, build with `mvn clean install`, start application, verify auth configuration is loaded

### Implementation for User Story 1

- [ ] T022 [US1] Update archetype-metadata.xml to include new auth files in `templates/java-ddd/src/main/resources/META-INF/maven/archetype-metadata.xml`
- [ ] T023 [US1] Add H2 database configuration for development in `boot/application.yml`
- [ ] T024 [US1] Add JPA entity scanning configuration in `boot/application.yml`
- [ ] T025 [US1] Create schema.sql for H2 auto-initialization in `boot/schema.sql` (users and refresh_tokens tables)
- [ ] T026 [US1] Update Application.java to enable Spring Security in `__rootArtifactId__-bootstrap/src/main/java/Application.java`
- [ ] T027 [US1] Add contract test to verify generated project structure in `templates/java-ddd/contract-test/` (verify auth files exist)

**Checkpoint**: At this point, User Story 1 should be fully functional - generated project builds and starts with auth enabled

---

## Phase 4: User Story 2 - User Registration and Login (Priority: P1)

**Goal**: Template includes working registration and login endpoints with JWT token issuance

**Independent Test**: Start generated app, POST to /api/auth/register to create user, POST to /api/auth/login to receive JWT tokens

### Application Layer (Use Cases)

- [ ] T028 [P] [US2] Create RegisterUserCommand in `app/auth/RegisterUserCommand.java`
- [ ] T029 [P] [US2] Create LoginCommand in `app/auth/LoginCommand.java`
- [ ] T030 [US2] Create AuthService with register and login methods in `app/auth/AuthService.java`

### Interface Layer (DTOs and Controller)

- [ ] T031 [P] [US2] Create RegisterRequest DTO with validation in `iface/auth/RegisterRequest.java`
- [ ] T032 [P] [US2] Create LoginRequest DTO with validation in `iface/auth/LoginRequest.java`
- [ ] T033 [P] [US2] Create TokenResponse DTO in `iface/auth/TokenResponse.java`
- [ ] T034 [P] [US2] Create UserResponse DTO in `iface/auth/UserResponse.java`
- [ ] T035 [US2] Implement AuthController with /register and /login endpoints in `iface/auth/AuthController.java`

### Security Configuration

- [ ] T036 [US2] Update SecurityConfig to permit /api/auth/register and /api/auth/login in `infra/auth/SecurityConfig.java`
- [ ] T037 [US2] Add authentication event logging in `infra/auth/AuthenticationEventLogger.java`

**Checkpoint**: At this point, User Story 2 should be fully functional - registration and login work independently

---

## Phase 5: User Story 3 - Protect Business API Endpoints (Priority: P1)

**Goal**: Template includes example protected endpoints demonstrating JWT authentication

**Independent Test**: Access /api/protected/user without token (401), access with valid token (200)

### Implementation for User Story 3

- [ ] T038 [US3] Implement ProtectedController with example endpoints in `iface/auth/ProtectedController.java`
- [ ] T039 [US3] Create CurrentUser annotation for injecting user context in `infra/auth/CurrentUser.java`
- [ ] T040 [US3] Implement CurrentUserArgumentResolver for @CurrentUser in `infra/auth/CurrentUserArgumentResolver.java`
- [ ] T041 [US3] Configure argument resolver in WebMvcConfig in `infra/config/WebMvcConfig.java`
- [ ] T042 [US3] Update SecurityConfig to require authentication for /api/protected/** in `infra/auth/SecurityConfig.java`

**Checkpoint**: At this point, User Story 3 should be fully functional - protected endpoints work with JWT authentication

---

## Phase 6: User Story 4 - Token Refresh Flow (Priority: P2)

**Goal**: Template includes token refresh endpoint for obtaining new access tokens

**Independent Test**: Login to get tokens, use refresh token to get new access token via /api/auth/refresh

### Implementation for User Story 4

- [ ] T043 [P] [US4] Create RefreshRequest DTO in `iface/auth/RefreshRequest.java`
- [ ] T044 [US4] Add refreshToken method to AuthService in `app/auth/AuthService.java`
- [ ] T045 [US4] Add /refresh endpoint to AuthController in `iface/auth/AuthController.java`
- [ ] T046 [US4] Update SecurityConfig to permit /api/auth/refresh in `infra/auth/SecurityConfig.java`

**Checkpoint**: At this point, User Story 4 should be fully functional - token refresh works independently

---

## Phase 7: User Story 5 - Role-Based Access Control (Priority: P2)

**Goal**: Template includes RBAC examples with USER and ADMIN roles

**Independent Test**: Create admin user, access /api/protected/admin (200), access with regular user (403)

### Implementation for User Story 5

- [ ] T047 [US5] Add admin-only endpoint to ProtectedController in `iface/auth/ProtectedController.java`
- [ ] T048 [US5] Add @PreAuthorize annotations for role-based access in `iface/auth/ProtectedController.java`
- [ ] T049 [US5] Enable method security in SecurityConfig with @EnableMethodSecurity in `infra/auth/SecurityConfig.java`
- [ ] T050 [US5] Create data.sql with sample admin user for testing in `boot/data.sql`

**Checkpoint**: At this point, User Story 5 should be fully functional - RBAC works with different user roles

---

## Phase 8: User Story 6 - Logout and Token Invalidation (Priority: P3)

**Goal**: Template includes logout endpoint that invalidates refresh tokens

**Independent Test**: Login, call logout with refresh token, attempt to use refresh token (should fail)

### Implementation for User Story 6

- [ ] T051 [P] [US6] Create LogoutRequest DTO in `iface/auth/LogoutRequest.java`
- [ ] T052 [US6] Add logout method to AuthService (revoke refresh token) in `app/auth/AuthService.java`
- [ ] T053 [US6] Add /logout endpoint to AuthController in `iface/auth/AuthController.java`
- [ ] T054 [US6] Add /me endpoint to AuthController (get current user) in `iface/auth/AuthController.java`

**Checkpoint**: At this point, User Story 6 should be fully functional - logout invalidates tokens

---

## Phase 9: Polish & Cross-Cutting Concerns

**Purpose**: Final improvements and documentation

- [ ] T055 [P] Update template.yaml version to 1.1.0 in `templates/java-ddd/template.yaml`
- [ ] T056 [P] Add authentication section to generated README in `__rootArtifactId__-bootstrap/README.md`
- [ ] T057 Add comprehensive JavaDoc to all public auth classes
- [ ] T058 Add logging statements for authentication events (login, logout, token refresh)
- [ ] T059 Update archetype integration tests for auth functionality in `templates/java-ddd/contract-test/`
- [ ] T060 Run full template generation and verify quickstart.md scenarios work

---

## Dependencies & Execution Order

### Phase Dependencies

- **Setup (Phase 1)**: No dependencies - can start immediately
- **Foundational (Phase 2)**: Depends on Setup completion - BLOCKS all user stories
- **User Story 1 (Phase 3)**: Depends on Foundational - Template generation works
- **User Story 2 (Phase 4)**: Depends on Foundational - Registration/Login
- **User Story 3 (Phase 5)**: Depends on Foundational + US2 (needs login to get tokens) - Protected endpoints
- **User Story 4 (Phase 6)**: Depends on Foundational + US2 - Token refresh
- **User Story 5 (Phase 7)**: Depends on Foundational + US3 - RBAC
- **User Story 6 (Phase 8)**: Depends on Foundational + US2 + US4 - Logout
- **Polish (Phase 9)**: Depends on all user stories complete

### User Story Dependencies

```
                    ┌─────────────────┐
                    │  Phase 1: Setup  │
                    └────────┬────────┘
                             │
                    ┌────────▼────────┐
                    │Phase 2: Foundation│
                    └────────┬────────┘
                             │
         ┌───────────────────┼───────────────────┐
         │                   │                   │
  ┌──────▼──────┐    ┌───────▼───────┐    ┌──────▼──────┐
  │US1: Generate │    │US2: Reg/Login │    │US4: Refresh │
  │   (P1) 🎯    │    │    (P1)       │    │   (P2)      │
  └──────────────┘    └───────┬───────┘    └──────┬──────┘
                              │                   │
                       ┌──────▼──────┐            │
                       │US3: Protect │            │
                       │   (P1)      │            │
                       └──────┬──────┘            │
                              │                   │
         ┌────────────────────┤                   │
         │                    │                   │
  ┌──────▼──────┐      ┌──────▼──────┐           │
  │ US5: RBAC   │      │US6: Logout  │◄──────────┘
  │   (P2)      │      │   (P3)      │
  └─────────────┘      └─────────────┘
                              │
                    ┌─────────▼─────────┐
                    │  Phase 9: Polish   │
                    └───────────────────┘
```

### Within Each User Story

- Models/DTOs before services
- Services before controllers
- Core implementation before integration
- Story complete before moving to next priority

### Parallel Opportunities

**Phase 1 (Setup):**
- T003, T004 can run in parallel (different module pom.xml files)

**Phase 2 (Foundational):**
- T006, T007, T008 can run in parallel (different entity files)
- T019, T020 can run in parallel (different DTO files)

**Phase 4 (US2):**
- T028, T029 can run in parallel (different command files)
- T031, T032, T033, T034 can run in parallel (different DTO files)

**Phase 6 (US4):**
- T043 can run in parallel with other DTOs

**Phase 8 (US6):**
- T051 can run in parallel with other DTOs

**Phase 9 (Polish):**
- T055, T056 can run in parallel (different files)

---

## Parallel Example: Foundational Phase

```bash
# Launch domain entities in parallel:
Task: "Create Role enum in domain/auth/Role.java"
Task: "Create User entity in domain/auth/User.java"
Task: "Create RefreshToken entity in domain/auth/RefreshToken.java"

# Launch DTOs in parallel:
Task: "Create ErrorResponse DTO in iface/auth/ErrorResponse.java"
Task: "Create MessageResponse DTO in iface/auth/MessageResponse.java"
```

## Parallel Example: User Story 2

```bash
# Launch commands in parallel:
Task: "Create RegisterUserCommand in app/auth/RegisterUserCommand.java"
Task: "Create LoginCommand in app/auth/LoginCommand.java"

# Launch DTOs in parallel:
Task: "Create RegisterRequest DTO in iface/auth/RegisterRequest.java"
Task: "Create LoginRequest DTO in iface/auth/LoginRequest.java"
Task: "Create TokenResponse DTO in iface/auth/TokenResponse.java"
Task: "Create UserResponse DTO in iface/auth/UserResponse.java"
```

---

## Implementation Strategy

### MVP First (User Stories 1, 2, 3)

1. Complete Phase 1: Setup (dependencies)
2. Complete Phase 2: Foundational (entities, security config)
3. Complete Phase 3: US1 (template generates working project)
4. Complete Phase 4: US2 (registration and login work)
5. Complete Phase 5: US3 (protected endpoints work)
6. **STOP and VALIDATE**: Test all P1 stories independently
7. Deploy/demo if ready

### Incremental Delivery

1. Complete Setup + Foundational → Foundation ready
2. Add US1 → Template generates buildable auth project (MVP!)
3. Add US2 → Registration and login work
4. Add US3 → Protected endpoints work
5. Add US4 → Token refresh works
6. Add US5 → RBAC works
7. Add US6 → Logout works
8. Polish → Documentation and tests

---

## Notes

- [P] tasks = different files, no dependencies
- [Story] label maps task to specific user story for traceability
- Each user story should be independently completable and testable
- All paths use Maven archetype variable syntax (`__rootArtifactId__`)
- Commit after each task or logical group
- Stop at any checkpoint to validate story independently
- Template version should be bumped to 1.1.0 after completing all stories
