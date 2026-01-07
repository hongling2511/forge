# Forge

Engineering scaffold CLI for generating project templates.

## Quick Start

```bash
# Install
./install.sh

# Create a new Go service project
forge new -t go-service -a my-api -m github.com/example/my-api
cd my-api
make run
curl http://localhost:8080/health

# Create a new Java DDD project
forge new -g com.example -a my-service
cd my-service
mvn clean package
java -jar my-service-bootstrap/target/my-service-bootstrap-1.0.0-SNAPSHOT.jar
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
  -m, --module <path>       Go module path (required for Go templates)
  -v, --version <version>   Project version (default: 1.0.0-SNAPSHOT / 0.1.0)
  -p, --package <name>      Java package name (default: groupId)
  -o, --output <dir>        Output directory (default: current directory)
  --interactive             Enable interactive wizard mode
  -h, --help                Show help
```

**Examples:**

```bash
# Go service project
forge new -t go-service -a my-api -m github.com/example/my-api

# Java DDD project
forge new -g com.example -a my-service

# With all options (Java)
forge new -t java-ddd \
  -g com.example \
  -a my-service \
  -v 2.0.0-SNAPSHOT \
  -p com.example.myservice \
  -o ./projects

# Interactive wizard mode
forge new --interactive
```

### `forge templates`

List available templates.

```bash
forge templates

# Output:
# Available templates:
#
# NAME        DESCRIPTION
# ----------  --------------------------------------------------
# go-service  Go microservice with clean architecture (Go 1.21+)
# java-ddd    Java DDD 多模块工程 (Spring Boot 3.x)
```

### `forge --help`

Show general help information.

### `forge --version`

Show version information.

## Available Templates

| Template | Description | Stack |
|----------|-------------|-------|
| `go-service` | Go microservice with clean architecture | Go 1.21+, Gin, Cobra, Viper |
| `java-ddd` | Java DDD multi-module project with JWT auth | Java 17+, Spring Boot 3.x, Spring Security 6.x |

## Generated Project Structure

### go-service Template

```
my-api/
├── cmd/my-api/              # Application entry point (Cobra CLI)
├── internal/                # Private application code
│   ├── my-api/              # Main application
│   │   ├── config/          # Viper configuration
│   │   ├── handler/         # Gin HTTP handlers
│   │   ├── middleware/      # Gin middleware
│   │   ├── service/         # Business logic
│   │   └── store/           # Data access
│   └── pkg/                 # Internal shared packages
├── pkg/                     # Public packages
├── configs/                 # Configuration files
├── build/                   # Dockerfile
├── Makefile                 # Build automation
└── README.md
```

### java-ddd Template

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

### JWT Authentication (java-ddd)

The `java-ddd` template includes a complete JWT authentication system:

**API Endpoints:**

| Method | Endpoint | Description | Auth |
|--------|----------|-------------|------|
| POST | `/api/auth/register` | Register new user | Public |
| POST | `/api/auth/login` | Login and get tokens | Public |
| POST | `/api/auth/refresh` | Refresh access token | Public |
| POST | `/api/auth/logout` | Logout and revoke token | Required |
| GET | `/api/users/me` | Get current user profile | Required |
| GET | `/api/admin/users` | List all users | ADMIN |

**Quick Test:**

```bash
# Register a user
curl -X POST http://localhost:8080/api/auth/register \
  -H "Content-Type: application/json" \
  -d '{"username":"testuser","email":"test@example.com","password":"Test@1234"}'

# Login
curl -X POST http://localhost:8080/api/auth/login \
  -H "Content-Type: application/json" \
  -d '{"email":"test@example.com","password":"Test@1234"}'

# Access protected endpoint (use token from login response)
curl http://localhost:8080/api/users/me \
  -H "Authorization: Bearer <access_token>"
```

**Token Configuration** (in `application.yml`):
- Access token: 15 minutes
- Refresh token: 7 days

## Requirements

### For Go Templates
- **Go 1.21+**: `go version`

### For Java Templates
- **JDK 17+**: `java -version`
- **Maven 3.6+**: `mvn -version`

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
├── cmd/forge/               # CLI entry point
├── internal/
│   ├── cli/                 # Cobra commands
│   ├── config/              # Configuration
│   ├── generator/           # Template generators (Go + Maven)
│   ├── interactive/         # Wizard mode
│   ├── output/              # Colored output
│   ├── template/            # Template discovery
│   └── validation/          # Input validation
├── templates/
│   ├── go-service/          # Go microservice template
│   └── java-ddd/            # Java DDD template
├── install.sh
└── Makefile
```

## License

MIT
