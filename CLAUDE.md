# forge Development Guidelines

Auto-generated from all feature plans. Last updated: 2026-01-05

## Active Technologies
- Go 1.21+ (CLI implementation)
- Cobra (CLI framework)
- text/template (Go template generation)
- survey (interactive prompts)
- Java 17+ (generated projects - java-ddd)
- Maven 3.6+ (archetype and build tool - java-ddd)
- Spring Boot 3.x (generated project runtime - java-ddd)
- Java 17+ + Spring Boot 3.2.x, Spring Security 6.x, jjwt (io.jsonwebtoken) 0.12.x, Spring Data JPA (004-jwt-auth)
- Relational database via JPA (H2 for development, configurable for production) (004-jwt-auth)
- Logback (structured logging with TraceId), Flyway 9.x (database migration), MySQL 8.0+/PostgreSQL 12+ (004-logging-database)

## Project Structure

```text
cmd/forge/              # CLI entry point
internal/
  cli/                  # Cobra commands (root, new, templates, version)
  config/               # Configuration and version info
  generator/            # Template generation (Maven + Go)
    executor.go         # Executor interface
    maven.go            # Maven archetype executor
    go.go               # Go template executor
    generator.go        # Main generator with routing
  interactive/          # Wizard mode
  output/               # Colored output and spinners
  template/             # Template discovery and metadata
  validation/           # Input validation with regex
templates/
  java-ddd/             # Java DDD Maven archetype
  go-service/           # Go microservice template
scripts/
  install.sh            # Binary installation script
```

## Commands

```bash
# Build
make build              # Build for current platform
make build-all          # Cross-compile all platforms
make test               # Run unit tests
make clean              # Clean build artifacts

# CLI Usage - Java DDD
forge new -g <groupId> -a <artifactId>  # Create Java project
forge new -t java-ddd -g com.example -a my-service

# CLI Usage - Go Service
forge new -t go-service -a <name> -m <module>  # Create Go project
forge new -t go-service -a my-api -m github.com/example/my-api

# Common
forge new --interactive                  # Wizard mode
forge templates                          # List templates
forge version                            # Show version
```

## Code Style

- Go: Follow standard Go conventions (`go fmt`)
- Java (generated): Follow standard Maven conventions

## Recent Changes
- 004-logging-database: Added Logback with TraceId, Flyway database migration, environment-specific configs (dev/qa/prod)
- 004-jwt-auth: Added Java 17+ + Spring Boot 3.2.x, Spring Security 6.x, jjwt (io.jsonwebtoken) 0.12.x, Spring Data JPA
- 003-go-service-template: Added go-service template with clean architecture, Go generator, and interactive wizard support

- 002-go-cli: Rewrote CLI in Go with Cobra framework, added interactive mode

<!-- MANUAL ADDITIONS START -->
<!-- MANUAL ADDITIONS END -->
