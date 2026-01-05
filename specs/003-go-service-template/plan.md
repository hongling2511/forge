# Implementation Plan: Go Service Template

**Branch**: `003-go-service-template` | **Date**: 2026-01-05 | **Spec**: [spec.md](./spec.md)
**Input**: Feature specification from `/specs/003-go-service-template/spec.md`

## Summary

Add support for a `go-service` template to the forge CLI, enabling developers to generate well-structured Go microservice projects. This requires:
1. Creating a new template type (`go-template`) with Go-specific generator
2. Implementing Go file generation using `text/template`
3. Extending validation for Go module paths
4. Updating CLI and interactive wizard for Go-specific parameters

## Technical Context

**Language/Version**: Go 1.21+ (CLI implementation), Go 1.21+ (generated projects)
**Primary Dependencies**: Cobra (CLI), text/template (Go file generation), survey (interactive prompts)
**Storage**: N/A (file system operations only)
**Testing**: go test with table-driven tests
**Target Platform**: macOS, Linux, Windows (cross-platform CLI)
**Project Type**: Single project - extending existing CLI
**Performance Goals**: Project generation under 5 seconds
**Constraints**: Must not require Maven for Go templates, deterministic output per constitution
**Scale/Scope**: Single new template type, ~10-15 new/modified files

## Constitution Check

*GATE: Must pass before Phase 0 research. Re-check after Phase 1 design.*

| Article | Requirement | Status | Notes |
|---------|-------------|--------|-------|
| Art. 1 - Scaffold is Product | Feature driven by spec | ✅ PASS | spec.md created with acceptance criteria |
| Art. 2 - Minimal Defaults | Default = minimal runnable | ✅ PASS | Generated project: build, run, health check |
| Art. 3 - No Framework Invention | Use mainstream tools | ✅ PASS | Standard Go project layout, no custom frameworks |
| Art. 4 - Generated Results as Contracts | Contract tests required | ⚠️ PENDING | Need to add contract tests for go-service |
| Art. 5 - CLI First | CLI must be scriptable | ✅ PASS | forge new -t go-service -a name -m module |
| Art. 6 - Deterministic | Same input = same output | ✅ PASS | No random elements in generation |
| Art. 7 - Spec-Driven | Changes from spec | ✅ PASS | This plan follows spec.md |
| Art. 8 - Capability Isolation | Independent feature | ✅ PASS | go-service isolated from java-ddd |
| Art. 9 - Template Versioning | Templates versioned | ✅ PASS | template.yaml includes version field |
| Art. 10 - Fail Early | Clear error messages | ✅ PASS | Validation before generation |
| Art. 11 - No Implicit Global State | Document env vars | ✅ PASS | Only uses FORGE_HOME (documented) |

**Gate Status**: PASS (Art. 4 to be addressed in implementation)

## Project Structure

### Documentation (this feature)

```text
specs/003-go-service-template/
├── plan.md              # This file
├── research.md          # Phase 0 output
├── data-model.md        # Phase 1 output
├── quickstart.md        # Phase 1 output
├── contracts/           # Phase 1 output
└── tasks.md             # Phase 2 output (via /speckit.tasks)
```

### Source Code (repository root)

```text
# Existing structure - files to modify
internal/
├── generator/
│   ├── generator.go     # MODIFY: Add generator interface, type routing
│   ├── maven.go         # KEEP: Existing Maven generator
│   └── go.go            # NEW: Go template generator
├── template/
│   └── template.go      # MODIFY: Add IsGoTemplate(), GoConfig
├── validation/
│   └── validators.go    # MODIFY: Add Go module validation
├── interactive/
│   └── wizard.go        # MODIFY: Support Go-specific prompts
└── cli/
    └── new.go           # MODIFY: Update parameter handling

# New template directory
templates/
├── java-ddd/            # KEEP: Existing Java DDD template
└── go-service/          # NEW: Go service template
    ├── template.yaml    # Template metadata
    └── files/           # Template source files
        ├── cmd/
        │   └── {{.ProjectName}}/
        │       └── main.go.tmpl
        ├── internal/
        │   ├── {{.ProjectName}}/
        │   │   ├── config/
        │   │   │   └── config.go.tmpl
        │   │   ├── handler/
        │   │   │   └── health.go.tmpl
        │   │   ├── service/
        │   │   │   └── .gitkeep
        │   │   └── store/
        │   │       └── .gitkeep
        │   └── pkg/
        │       ├── code/
        │       │   └── code.go.tmpl
        │       └── middleware/
        │           └── .gitkeep
        ├── pkg/
        │   └── version/
        │       └── version.go.tmpl
        ├── configs/
        │   └── config.yaml.example
        ├── scripts/
        │   └── make-rules/
        │       └── common.mk
        ├── build/
        │   └── docker/
        │       └── Dockerfile.tmpl
        ├── api/
        │   └── openapi/
        │       └── .gitkeep
        ├── docs/
        │   └── README.md.tmpl
        ├── test/
        │   └── testdata/
        │       └── .gitkeep
        ├── go.mod.tmpl
        ├── go.sum.tmpl
        ├── Makefile.tmpl
        ├── README.md.tmpl
        ├── .gitignore
        └── LICENSE
```

**Structure Decision**: Extending existing single-project CLI structure. New generator type added alongside Maven generator with shared interface.

## Complexity Tracking

> No constitution violations requiring justification.

| Violation | Why Needed | Simpler Alternative Rejected Because |
|-----------|------------|-------------------------------------|
| N/A | N/A | N/A |
