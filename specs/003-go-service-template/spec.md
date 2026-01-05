# Feature Specification: Go Service Template

**Feature Branch**: `003-go-service-template`
**Created**: 2026-01-05
**Status**: Draft
**Input**: User description: "支持go-service模版"

## User Scenarios & Testing *(mandatory)*

### User Story 1 - Create Go Service Project via CLI (Priority: P1)

As a developer, I want to create a new Go service project using the `forge new` command with the `go-service` template, so that I can quickly bootstrap a well-structured Go microservice.

**Why this priority**: This is the core functionality - without the ability to generate a Go service project, the entire feature has no value.

**Independent Test**: Can be fully tested by running `forge new -t go-service -a my-service -m github.com/example/my-service` and verifying a working Go project is generated.

**Acceptance Scenarios**:

1. **Given** forge CLI is installed, **When** I run `forge new -t go-service -a my-api -m github.com/example/my-api`, **Then** a new Go project `my-api` is created with proper go.mod and directory structure
2. **Given** forge CLI is installed, **When** I run `forge templates`, **Then** `go-service` template is listed alongside `java-ddd`
3. **Given** a generated go-service project, **When** I run `go build ./...`, **Then** the project compiles without errors
4. **Given** a generated go-service project, **When** I run `go test ./...`, **Then** all tests pass

---

### User Story 2 - Interactive Mode for Go Template (Priority: P2)

As a developer, I want to use the interactive wizard mode with the `go-service` template, so that I can be guided through the project creation process.

**Why this priority**: Interactive mode improves UX for new users but is not essential for core functionality.

**Independent Test**: Can be tested by running `forge new --interactive`, selecting go-service template, and completing the wizard.

**Acceptance Scenarios**:

1. **Given** forge CLI in interactive mode, **When** I select `go-service` template, **Then** I am prompted for Go-specific parameters (module name, project name)
2. **Given** forge CLI in interactive mode with go-service, **When** I skip optional parameters, **Then** sensible defaults are applied
3. **Given** forge CLI in interactive mode, **When** I complete the wizard, **Then** a summary shows Go-specific configuration before confirmation

---

### User Story 3 - Go Project Structure with Clean Architecture (Priority: P2)

As a developer, I want the generated Go project to follow clean architecture principles, so that I have a maintainable and testable codebase from the start.

**Why this priority**: Good project structure is important but depends on core generation working first.

**Independent Test**: Can be verified by inspecting the generated project structure against expected layout.

**Acceptance Scenarios**:

1. **Given** a generated go-service project, **When** I inspect the structure, **Then** I see separate `cmd/`, `internal/`, and `pkg/` directories
2. **Given** a generated go-service project, **When** I check internal structure, **Then** I see clean separation of domain, service, and handler layers
3. **Given** a generated go-service project, **When** I review the code, **Then** dependencies flow inward (handlers depend on services, services depend on domain)

---

### Edge Cases

- What happens when Go is not installed on the system? → Clear error message with installation instructions
- What happens when module name is invalid? → Validation error with proper format examples
- What happens when target directory already exists? → Prompt for confirmation or error
- What happens when module name contains uppercase letters? → Validation error (Go convention)

## Requirements *(mandatory)*

### Functional Requirements

- **FR-001**: System MUST generate a valid Go module project with proper `go.mod` file
- **FR-002**: System MUST validate Go module path format (e.g., `github.com/user/project`)
- **FR-003**: System MUST validate project name format (lowercase letters, numbers, hyphens)
- **FR-004**: System MUST check if Go toolchain is available before generation
- **FR-005**: System MUST generate a compilable project that passes `go build ./...`
- **FR-006**: System MUST generate a project with passing tests via `go test ./...`
- **FR-007**: System MUST support both CLI flags and interactive wizard mode
- **FR-008**: System MUST list `go-service` in available templates via `forge templates`
- **FR-009**: Generated project MUST include a health check endpoint
- **FR-010**: Generated project MUST include basic Makefile for common operations

### Key Entities

- **Template**: Metadata describing the go-service template (name, version, parameters, stack info)
- **GoConfig**: Go-specific configuration (module path, Go version requirement)
- **GeneratedProject**: Output directory structure with Go source files, go.mod, Makefile

## Success Criteria *(mandatory)*

### Measurable Outcomes

- **SC-001**: User can generate a Go service project in under 5 seconds
- **SC-002**: Generated project compiles with zero errors on Go 1.21+
- **SC-003**: Generated project includes at least 80% test coverage for generated code
- **SC-004**: 100% of validation errors provide actionable guidance to the user
- **SC-005**: Interactive wizard completes in under 60 seconds with minimal prompts
