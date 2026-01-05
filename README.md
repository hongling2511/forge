# Forge

Engineering scaffold CLI for generating project templates.

## Quick Start

```bash
# Install
./install.sh

# Create a new DDD project
forge new -g com.example -a my-service

# Build and run
cd my-service
mvn clean package
java -jar my-service-bootstrap/target/my-service-bootstrap-1.0.0-SNAPSHOT.jar

# Verify health check
curl http://localhost:8080/actuator/health
```

## Installation

```bash
git clone https://github.com/your-org/forge.git
cd forge
./install.sh
```

The installer will:
1. Make the `forge` script executable
2. Optionally add `forge` to your PATH

## Commands

### `forge new`

Create a new project from a template.

```bash
forge new [options]

Options:
  -t, --template <name>     Template to use (default: java-ddd)
  -g, --group-id <id>       Maven groupId (required for Java templates)
  -a, --artifact-id <id>    Project name (required)
  -v, --version <version>   Project version (default: 1.0.0-SNAPSHOT)
  -p, --package <name>      Java package name (default: groupId)
  -o, --output <dir>        Output directory (default: current directory)
  -h, --help                Show help
```

**Examples:**

```bash
# Basic usage
forge new -g com.example -a my-service

# With all options
forge new -t java-ddd \
  -g com.example \
  -a my-service \
  -v 2.0.0-SNAPSHOT \
  -p com.example.myservice \
  -o ./projects

# CI/CD batch mode
forge new -g com.example -a my-service && cd my-service && mvn package
```

### `forge templates`

List available templates.

```bash
forge templates

# Output:
# Available templates:
#
#   java-ddd        Java DDD 多模块工程 (Spring Boot 3.x)
```

### `forge --help`

Show general help information.

### `forge --version`

Show version information.

## Available Templates

| Template | Description |
|----------|-------------|
| `java-ddd` | Java DDD multi-module project (Spring Boot 3.x) |

## Generated Project Structure

When using `java-ddd` template:

```
my-service/
├── pom.xml                          # Parent POM
├── my-service-domain/               # Domain layer
├── my-service-application/          # Application layer
├── my-service-infrastructure/       # Infrastructure layer
├── my-service-interface/            # Interface layer (REST)
└── my-service-bootstrap/            # Bootstrap module
    ├── src/main/java/.../Application.java
    └── src/main/resources/application.yml
```

### Module Dependencies

```
bootstrap → interface → application → domain
              ↓              ↓
          infrastructure ────┘
```

## Requirements

- **JDK 17+**: `java -version`
- **Maven 3.6+**: `mvn -version`
- **Bash**: For the CLI

## Version Tracking

Generated projects include version metadata in `pom.xml`:

```xml
<properties>
    <forge.archetype.version>1.0.0-SNAPSHOT</forge.archetype.version>
    <forge.template.version>1.0.0</forge.template.version>
</properties>
```

## Development

### Run Tests

```bash
# Run all contract tests
cd templates/java-ddd
mvn clean verify

# Verify reproducibility
./verify-reproducibility.sh
```

### Project Structure

```
forge/
├── cli/
│   ├── forge                 # Main CLI entry point
│   ├── commands/
│   │   ├── new.sh            # forge new command
│   │   └── templates.sh      # forge templates command
│   └── lib/
│       ├── args.sh           # Argument parsing
│       └── validation.sh     # Parameter validation
├── templates/
│   └── java-ddd/             # Java DDD template
│       ├── pom.xml
│       ├── template.yaml
│       └── src/main/resources/
├── install.sh
├── verify-reproducibility.sh
└── pom.xml
```

## License

MIT
