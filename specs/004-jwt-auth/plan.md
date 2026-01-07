# Implementation Plan: JWT Authentication for Java DDD Template

**Branch**: `004-jwt-auth` | **Date**: 2026-01-05 | **Spec**: [spec.md](./spec.md)
**Input**: Feature specification from `/specs/004-jwt-auth/spec.md`

**Note**: This template is filled in by the `/speckit.plan` command. See `.specify/templates/commands/plan.md` for the execution workflow.

## Summary

Add JWT authentication infrastructure to the java-ddd Maven archetype template. The generated projects will include:
- User registration and login endpoints with JWT token issuance
- Access token + refresh token mechanism
- Endpoint protection via Spring Security annotations
- Role-based access control (USER, ADMIN roles)
- Token refresh and logout functionality
- Complete working examples following DDD layering conventions

## Technical Context

**Language/Version**: Java 17+
**Primary Dependencies**: Spring Boot 3.2.x, Spring Security 6.x, jjwt (io.jsonwebtoken) 0.12.x, Spring Data JPA
**Storage**: Relational database via JPA (H2 for development, configurable for production)
**Testing**: JUnit 5, Spring Boot Test, MockMvc
**Target Platform**: Java server applications (JVM)
**Project Type**: Maven multi-module archetype (DDD layers: domain, application, infrastructure, interface, bootstrap)
**Performance Goals**: 100 concurrent authentication requests without errors (per spec SC-006)
**Constraints**: Stateless JWT authentication (refresh tokens stored in database for invalidation)
**Scale/Scope**: Template enhancement for generated projects

## Constitution Check

*GATE: Must pass before Phase 0 research. Re-check after Phase 1 design.*

| Article | Requirement | Status | Notes |
|---------|-------------|--------|-------|
| Art 1: Scaffold is Product | All changes spec-driven, testable | ✅ PASS | Feature spec exists with acceptance criteria |
| Art 2: Minimal Defaults | Generated project must build/start, complexity explicit | ✅ PASS | JWT auth is explicit addition to template, project remains buildable |
| Art 3: No Framework Invention | Use mainstream tools, no private abstractions | ✅ PASS | Uses Spring Security + jjwt (industry standard) |
| Art 4: Generated Results = Contract | Contract tests for structure/config/commands | ✅ PASS | Will add contract tests for auth endpoints and config files |
| Art 5: CLI First | All capabilities via CLI, scriptable | ✅ PASS | Forge CLI generates project with auth included |
| Art 6: Deterministic Reproducibility | Same inputs = same outputs | ✅ PASS | Template generation is deterministic |
| Art 7: Spec-Driven Evolution | New capability has spec + acceptance criteria | ✅ PASS | spec.md defines all requirements |
| Art 8: Capability Isolation | Independent feature with own spec/plan/tasks | ✅ PASS | Separate feature branch 004-jwt-auth |
| Art 9: Template Versioning | Version tracked, breaking changes bump version | ✅ PASS | Template version in template.yaml |
| Art 10: Fail Early | Invalid input fails immediately with clear message | ✅ PASS | Spring validation with clear error responses |
| Art 11: No Implicit Global State | No hidden state, env vars documented | ✅ PASS | JWT secret configurable via application.yml |

**Gate Result**: ✅ PASS - Proceed to Phase 0

## Project Structure

### Documentation (this feature)

```text
specs/004-jwt-auth/
├── plan.md              # This file (/speckit.plan command output)
├── research.md          # Phase 0 output (/speckit.plan command)
├── data-model.md        # Phase 1 output (/speckit.plan command)
├── quickstart.md        # Phase 1 output (/speckit.plan command)
├── contracts/           # Phase 1 output (/speckit.plan command)
│   └── auth-api.yaml    # OpenAPI spec for auth endpoints
└── tasks.md             # Phase 2 output (/speckit.tasks command)
```

### Source Code (template enhancement)

```text
templates/java-ddd/src/main/resources/archetype-resources/
├── pom.xml                                    # Add Spring Security + jjwt dependencies
├── __rootArtifactId__-domain/
│   └── src/main/java/
│       └── auth/
│           ├── User.java                      # User entity
│           ├── Role.java                      # Role enum
│           ├── RefreshToken.java              # RefreshToken entity
│           └── UserRepository.java            # Repository interface (domain port)
├── __rootArtifactId__-application/
│   └── src/main/java/
│       └── auth/
│           ├── AuthService.java               # Authentication use case
│           ├── RegisterUserCommand.java       # Registration command
│           ├── LoginCommand.java              # Login command
│           └── TokenService.java              # Token management
├── __rootArtifactId__-infrastructure/
│   └── src/main/java/
│       └── auth/
│           ├── JpaUserRepository.java         # JPA implementation
│           ├── JpaRefreshTokenRepository.java # Refresh token persistence
│           ├── JwtTokenProvider.java          # JWT creation/validation
│           └── SecurityConfig.java            # Spring Security configuration
├── __rootArtifactId__-interface/
│   └── src/main/java/
│       └── auth/
│           ├── AuthController.java            # REST endpoints
│           ├── RegisterRequest.java           # DTO
│           ├── LoginRequest.java              # DTO
│           ├── TokenResponse.java             # DTO
│           └── ProtectedController.java       # Example protected endpoint
└── __rootArtifactId__-bootstrap/
    └── src/main/resources/
        └── application.yml                    # JWT configuration properties
```

**Structure Decision**: Follows existing DDD multi-module structure. Authentication components are placed in appropriate layers:
- **Domain**: User, Role, RefreshToken entities + repository interface
- **Application**: Authentication use cases (login, register, token management)
- **Infrastructure**: JPA implementations, JWT provider, Security config
- **Interface**: REST controllers and DTOs
- **Bootstrap**: Configuration properties

## Complexity Tracking

> No constitution violations - table not needed.
