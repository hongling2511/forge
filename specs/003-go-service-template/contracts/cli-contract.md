# CLI Contract: Go Service Template

**Feature**: 003-go-service-template
**Version**: 1.0.0

## Command: `forge new`

### Synopsis

```bash
forge new [flags]
```

### Go Template Flags

| Flag | Short | Type | Required | Default | Description |
|------|-------|------|----------|---------|-------------|
| `--template` | `-t` | string | No | `java-ddd` | Template to use |
| `--artifact-id` | `-a` | string | Yes | - | Project name |
| `--module` | `-m` | string | When Go | - | Go module path |
| `--version` | `-v` | string | No | `0.1.0` | Project version |
| `--output` | `-o` | string | No | `.` | Output directory |
| `--interactive` | - | bool | No | `false` | Enable wizard mode |

### Examples

```bash
# Create Go service with explicit flags
forge new -t go-service -a my-api -m github.com/example/my-api

# Create with all options
forge new -t go-service -a my-api -m github.com/example/my-api -v 1.0.0 -o ./projects

# Interactive mode
forge new --interactive
forge new -t go-service --interactive
```

### Exit Codes

| Code | Meaning |
|------|---------|
| 0 | Success |
| 1 | Validation error |
| 2 | Go toolchain not found |
| 3 | Template not found |
| 4 | Output directory error |
| 5 | Generation error |

### Output Format

**Success**:
```
Creating project 'my-api' from template 'go-service'...

Initializing Go module...
Generating project structure...

✅ Project 'my-api' created successfully!
   Version: 0.1.0
   Module:  github.com/example/my-api

Next steps:
   cd my-api
   go mod tidy
   make run
```

**Validation Error**:
```
❌ Validation error: module

   Invalid module path 'My-Module'
   Module path must be lowercase and follow Go conventions
   Example: github.com/username/project
```

**Go Not Found**:
```
❌ Go toolchain not found

   Please install Go 1.21 or later
   Visit: https://go.dev/dl/
```

---

## Command: `forge templates`

### Go Template Entry

```
Available templates:

  java-ddd     Java DDD 多模块工程 (Spring Boot 3.x)
  go-service   Go microservice with clean architecture (Go 1.21+)
```

---

## Validation Contracts

### Module Path Validation

| Input | Valid | Reason |
|-------|-------|--------|
| `github.com/user/project` | ✅ | Standard GitHub path |
| `example.com/pkg` | ✅ | Custom domain |
| `my-project` | ✅ | Simple local module |
| `github.com/user/my-pkg/v2` | ✅ | Versioned path |
| `My-Project` | ❌ | Uppercase not allowed |
| `/invalid/path` | ❌ | Cannot start with slash |
| `github.com/` | ❌ | Trailing slash incomplete |
| `` (empty) | ❌ | Required field |

### Artifact ID Validation

| Input | Valid | Reason |
|-------|-------|--------|
| `my-service` | ✅ | Standard format |
| `api` | ✅ | Simple name |
| `my-api-v2` | ✅ | With version suffix |
| `My-Service` | ❌ | Uppercase not allowed |
| `123-service` | ❌ | Must start with letter |
| `my_service` | ❌ | Underscores not allowed |
