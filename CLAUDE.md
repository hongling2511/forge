# forge Development Guidelines

Auto-generated from all feature plans. Last updated: 2026-01-05

## Active Technologies

- Go 1.21+ (CLI implementation)
- Cobra (CLI framework)
- Java 17+ (generated projects)
- Maven 3.6+ (archetype and build tool)
- Spring Boot 3.x (generated project runtime)

## Project Structure

```text
cmd/forge/              # CLI entry point
internal/
  cli/                  # Cobra commands (root, new, templates, version)
  config/               # Configuration and version info
  generator/            # Maven archetype invocation
  interactive/          # Wizard mode
  output/               # Colored output and spinners
  template/             # Template discovery and metadata
  validation/           # Input validation with regex
templates/
  java-ddd/             # Java DDD Maven archetype
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

# CLI Usage
forge new -g <groupId> -a <artifactId>  # Create project
forge new --interactive                  # Wizard mode
forge templates                          # List templates
forge version                            # Show version
```

## Code Style

- Go: Follow standard Go conventions (`go fmt`)
- Java (generated): Follow standard Maven conventions

## Recent Changes

- 002-go-cli: Rewrote CLI in Go with Cobra framework, added interactive mode
- 001-maven-ddd-archetype: Java DDD multi-module template with Spring Boot 3.x

<!-- MANUAL ADDITIONS START -->
<!-- MANUAL ADDITIONS END -->
