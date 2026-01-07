# Changelog

All notable changes to this project will be documented in this file.

The format is based on [Keep a Changelog](https://keepachangelog.com/en/1.0.0/),
and this project adheres to [Semantic Versioning](https://semver.org/spec/v2.0.0.html).

## [1.0.0] - 2026-01-05

### Added

- **Forge CLI** - Unified command-line interface for project generation
  - `forge new` - Create new projects from templates
  - `forge templates` - List available templates
  - `forge --help` - Show usage information
  - `forge --version` - Show version information

- **Java DDD Template** (`java-ddd`)
  - 5-module DDD architecture (domain, application, infrastructure, interface, bootstrap)
  - Spring Boot 3.2.x with Actuator health checks
  - Maven multi-module project structure
  - Configurable groupId, artifactId, version, and package

- **CLI Features**
  - Non-interactive batch mode for CI/CD environments
  - Parameter validation with clear error messages
  - Template discovery and listing
  - Exit codes: 0 for success, 1 for validation errors

- **Quality Assurance**
  - Contract tests for generated project validation
  - Reproducibility verification (identical inputs → identical outputs)
  - Version tracking in generated projects (`forge.archetype.version`, `forge.template.version`)

- **Documentation**
  - README with installation and usage instructions
  - Quickstart guide
  - Contract specifications

### Technical Details

- **Requirements**: JDK 17+, Maven 3.6+, Bash
- **Generated Project**: Spring Boot 3.2.1, Java 17
- **Health Check**: `/actuator/health` endpoint enabled by default

## [2.0.0] - 2026-01-05

### Changed

- **CLI Rewritten in Go** - Complete rewrite from Bash to Go using Cobra framework
  - Cross-platform support (Linux, macOS, Windows on amd64/arm64)
  - Faster execution and better error handling
  - Module path: `github.com/hongling2511/forge`

### Added

- **Interactive Mode** - Guided wizard when required parameters are missing
  - Template selection from available options
  - Step-by-step parameter input with validation
  - Confirmation summary before generation
  - Auto-triggers when `--artifact-id` or `--group-id` missing

- **Improved Output**
  - Colored success/error messages
  - Quiet Maven execution (no verbose build logs)
  - Clear next steps after project creation

- **Build System**
  - Makefile for build automation
  - Cross-platform compilation (`make build-all`)
  - Binary installation script (`scripts/install.sh`)

- **Testing**
  - Unit tests for validation package
  - Regex pattern tests for groupId, artifactId, version, package

### Technical Details

- **Go Version**: 1.21+
- **Dependencies**: Cobra, Survey, yaml.v3, color, spinner
- **Build Output**: Single static binary (~10MB)

## [Unreleased]

### Planned

- Shell completion (bash, zsh, fish)
- Additional templates (e.g., `kotlin-ddd`)
- Template versioning and updates
- Self-update command

## [2.2.0] - 2026-01-07

### Added

- **JWT Authentication** (`java-ddd` template v1.1.0)
  - Complete JWT authentication system following DDD architecture
  - User registration and login with JWT token issuance
  - Access token (15min) and refresh token (7 days) rotation
  - Role-based access control (USER, ADMIN roles)
  - Protected endpoints with Bearer token authentication

- **Domain Layer**
  - `User` entity with roles, password hash, and account status
  - `RefreshToken` entity with expiration and revocation
  - `Role` enum (USER, ADMIN)
  - Repository interfaces (UserRepository, RefreshTokenRepository)

- **Application Layer**
  - `AuthenticationService` for login/logout flows
  - `TokenService` for JWT token management
  - `UserRegistrationService` for new user registration
  - `CurrentUserService` for authenticated user context
  - `PasswordService` for BCrypt password hashing

- **Infrastructure Layer**
  - `JwtTokenProvider` using jjwt 0.12.x
  - `JwtAuthenticationFilter` for request authentication
  - `SecurityConfig` with Spring Security 6.x
  - JPA repositories for User and RefreshToken

- **Interface Layer (REST API)**
  - `POST /api/auth/register` - User registration
  - `POST /api/auth/login` - User login
  - `POST /api/auth/refresh` - Token refresh
  - `POST /api/auth/logout` - User logout
  - `GET /api/users/me` - Current user profile
  - `GET /api/admin/users` - List users (ADMIN only)
  - Request/Response DTOs with Jakarta validation
  - `GlobalExceptionHandler` for consistent error responses

### Technical Details

- **Spring Security**: 6.x with stateless session management
- **JWT Library**: io.jsonwebtoken (jjwt) 0.12.x
- **Password Hashing**: BCrypt with strength validation
- **Database**: H2 (development), JPA for production databases

## [2.1.0] - 2026-01-05

### Added

- **Go Service Template** (`go-service`)
  - Clean architecture with `cmd/`, `internal/`, `pkg/` structure
  - **Gin** - High-performance HTTP web framework
  - **Cobra** - CLI framework with subcommand support
  - **Viper** - Configuration management (file, env, flags)
  - Graceful shutdown with signal handling
  - Middleware: Logger, RequestID, Recovery
  - Health check endpoints: `/health`, `/health/live`, `/health/ready`
  - Version endpoint: `/version`
  - API routes: `/api/v1/ping`
  - Docker support with multi-stage build
  - Makefile for build automation

- **CLI Enhancements**
  - `-m, --module` flag for Go module path
  - Template-specific validation (Go module vs Maven groupId)
  - Template-specific default versions (0.1.0 for Go, 1.0.0-SNAPSHOT for Java)

- **Go Generator**
  - `GoExecutor` implementing `Executor` interface
  - Template function helpers: `envPrefix`, `snakeCase`, `camelCase`, etc.
  - Go toolchain validation

- **Interactive Mode Updates**
  - Go template selection in wizard
  - Module path prompt with validation
  - Go-specific confirmation summary

### Technical Details

- **Go Version**: 1.21+
- **Dependencies**: gin-gonic/gin v1.9.1, spf13/cobra v1.8.0, spf13/viper v1.18.2
- **Generated Project**: Compiles and runs out of the box
