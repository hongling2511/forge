# Tasks: Go Service Template

**Input**: Design documents from `/specs/003-go-service-template/`
**Prerequisites**: plan.md ✅, spec.md ✅, research.md ✅, data-model.md ✅, contracts/ ✅, quickstart.md ✅

**Tests**: Not explicitly requested - test tasks excluded per specification.

**Organization**: Tasks are grouped by user story to enable independent implementation and testing of each story.

## Format: `[ID] [P?] [Story] Description`

- **[P]**: Can run in parallel (different files, no dependencies)
- **[Story]**: Which user story this task belongs to (e.g., US1, US2, US3)
- Include exact file paths in descriptions

## Path Conventions

- **Single project**: Extending existing CLI at repository root
- **Template files**: `templates/go-service/`
- **Generator code**: `internal/generator/`
- **CLI code**: `internal/cli/`, `internal/interactive/`, `internal/validation/`

---

## Phase 1: Setup (Shared Infrastructure)

**Purpose**: Template directory structure and base configuration

- [ ] T001 Create go-service template directory structure at templates/go-service/
- [ ] T002 [P] Create template.yaml metadata file in templates/go-service/template.yaml
- [ ] T003 [P] Create .gitignore for generated projects in templates/go-service/files/.gitignore
- [ ] T004 [P] Create LICENSE file for generated projects in templates/go-service/files/LICENSE

---

## Phase 2: Foundational (Blocking Prerequisites)

**Purpose**: Core infrastructure that MUST be complete before ANY user story can be implemented

**⚠️ CRITICAL**: No user story work can begin until this phase is complete

- [ ] T005 Define Executor interface in internal/generator/executor.go
- [ ] T006 Define ExecuteParams struct in internal/generator/executor.go
- [ ] T007 Extend Template struct with GoConfig in internal/template/template.go
- [ ] T008 Add IsGoTemplate() method to Template in internal/template/template.go
- [ ] T009 [P] Add Go module validation pattern GoModulePattern in internal/validation/validators.go
- [ ] T010 [P] Add ValidateGoModule function in internal/validation/validators.go

**Checkpoint**: Foundation ready - user story implementation can now begin

---

## Phase 3: User Story 1 - Create Go Service Project via CLI (Priority: P1) 🎯 MVP

**Goal**: Enable developers to create a new Go service project using `forge new -t go-service` command

**Independent Test**: Run `forge new -t go-service -a my-service -m github.com/example/my-service` and verify:
1. Project directory created with correct structure
2. `go build ./...` compiles without errors
3. `go test ./...` passes
4. `forge templates` lists `go-service`

### Go Template Files for User Story 1

- [ ] T011 [P] [US1] Create go.mod.tmpl template in templates/go-service/files/go.mod.tmpl
- [ ] T012 [P] [US1] Create go.sum.tmpl template (empty) in templates/go-service/files/go.sum.tmpl
- [ ] T013 [P] [US1] Create main.go.tmpl in templates/go-service/files/cmd/{{.ProjectName}}/main.go.tmpl
- [ ] T014 [P] [US1] Create config.go.tmpl in templates/go-service/files/internal/{{.ProjectName}}/config/config.go.tmpl
- [ ] T015 [P] [US1] Create health.go.tmpl in templates/go-service/files/internal/{{.ProjectName}}/handler/health.go.tmpl
- [ ] T016 [P] [US1] Create code.go.tmpl in templates/go-service/files/internal/pkg/code/code.go.tmpl
- [ ] T017 [P] [US1] Create version.go.tmpl in templates/go-service/files/pkg/version/version.go.tmpl
- [ ] T018 [P] [US1] Create Makefile.tmpl in templates/go-service/files/Makefile.tmpl
- [ ] T019 [P] [US1] Create README.md.tmpl in templates/go-service/files/README.md.tmpl
- [ ] T020 [P] [US1] Create Dockerfile.tmpl in templates/go-service/files/build/docker/Dockerfile.tmpl
- [ ] T021 [P] [US1] Create config.yaml.example in templates/go-service/files/configs/config.yaml.example
- [ ] T022 [P] [US1] Create docs/README.md.tmpl in templates/go-service/files/docs/README.md.tmpl
- [ ] T023 [P] [US1] Create common.mk in templates/go-service/files/scripts/make-rules/common.mk
- [ ] T024 [P] [US1] Create .gitkeep files for empty directories in templates/go-service/files/

### Generator Implementation for User Story 1

- [ ] T025 [US1] Implement GoExecutor struct in internal/generator/go.go
- [ ] T026 [US1] Implement GoExecutor.Validate() method (check Go toolchain) in internal/generator/go.go
- [ ] T027 [US1] Implement GoExecutor.Execute() method (file generation) in internal/generator/go.go
- [ ] T028 [US1] Implement template file processing with text/template in internal/generator/go.go
- [ ] T029 [US1] Implement TemplateData struct for template variables in internal/generator/go.go

### Generator Routing for User Story 1

- [ ] T030 [US1] Refactor Generator to use Executor interface in internal/generator/generator.go
- [ ] T031 [US1] Create MavenExecutor wrapper for existing Maven logic in internal/generator/maven.go
- [ ] T032 [US1] Add template type routing (maven-archetype vs go-template) in internal/generator/generator.go

### CLI Updates for User Story 1

- [ ] T033 [US1] Add --module/-m flag to new command in internal/cli/new.go
- [ ] T034 [US1] Update flag validation for Go templates (require module) in internal/cli/new.go
- [ ] T035 [US1] Update NewConfig struct with Module field in internal/cli/new.go
- [ ] T036 [US1] Pass module parameter to generator in internal/cli/new.go

### Template Discovery for User Story 1

- [ ] T037 [US1] Update template discovery to find go-service in internal/template/template.go
- [ ] T038 [US1] Update templates command output to show go-service in internal/cli/templates.go

**Checkpoint**: At this point, User Story 1 should be fully functional - CLI can generate Go projects

---

## Phase 4: User Story 2 - Interactive Mode for Go Template (Priority: P2)

**Goal**: Enable developers to use the interactive wizard mode with go-service template

**Independent Test**: Run `forge new --interactive`, select go-service, complete wizard, verify project created

### Implementation for User Story 2

- [ ] T039 [US2] Add Go template selection to wizard template prompt in internal/interactive/wizard.go
- [ ] T040 [US2] Add module path prompt for Go templates in internal/interactive/wizard.go
- [ ] T041 [US2] Add module path validation in wizard in internal/interactive/wizard.go
- [ ] T042 [US2] Update WizardConfig struct with Module field in internal/interactive/wizard.go
- [ ] T043 [US2] Update confirmation summary for Go templates in internal/interactive/wizard.go
- [ ] T044 [US2] Add sensible defaults for Go templates (version 0.1.0) in internal/interactive/wizard.go

**Checkpoint**: At this point, User Stories 1 AND 2 should both work independently

---

## Phase 5: User Story 3 - Go Project Structure with Clean Architecture (Priority: P2)

**Goal**: Ensure generated Go project follows clean architecture with proper directory separation

**Independent Test**: Generate project, verify:
1. `cmd/`, `internal/`, `pkg/` directories exist
2. Handler → Service → Store dependency flow
3. Internal packages protected from external import

### Implementation for User Story 3

*Note: Most of US3 is already addressed by template files in US1. These tasks ensure proper structure validation.*

- [ ] T045 [US3] Verify template structure matches clean architecture in templates/go-service/files/
- [ ] T046 [US3] Add service/.gitkeep placeholder in templates/go-service/files/internal/{{.ProjectName}}/service/.gitkeep
- [ ] T047 [US3] Add store/.gitkeep placeholder in templates/go-service/files/internal/{{.ProjectName}}/store/.gitkeep
- [ ] T048 [US3] Add middleware/.gitkeep placeholder in templates/go-service/files/internal/pkg/middleware/.gitkeep
- [ ] T049 [US3] Add api/openapi/.gitkeep placeholder in templates/go-service/files/api/openapi/.gitkeep
- [ ] T050 [US3] Add test/testdata/.gitkeep placeholder in templates/go-service/files/test/testdata/.gitkeep

**Checkpoint**: All user stories should now be independently functional

---

## Phase 6: Polish & Cross-Cutting Concerns

**Purpose**: Improvements that affect multiple user stories

- [ ] T051 [P] Add helpful error messages for Go toolchain not found in internal/generator/go.go
- [ ] T052 [P] Add helpful error messages for invalid module path in internal/validation/validators.go
- [ ] T053 Update output messages with Go-specific next steps in internal/output/output.go
- [ ] T054 [P] Run quickstart.md validation - verify documented commands work
- [ ] T055 Verify determinism - same inputs produce identical output
- [ ] T056 Update CLAUDE.md with Go template information

---

## Dependencies & Execution Order

### Phase Dependencies

- **Setup (Phase 1)**: No dependencies - can start immediately
- **Foundational (Phase 2)**: Depends on Setup completion - BLOCKS all user stories
- **User Stories (Phase 3+)**: All depend on Foundational phase completion
  - User stories can then proceed in priority order (P1 → P2)
  - US3 partially overlaps with US1 (template structure)
- **Polish (Final Phase)**: Depends on all user stories being complete

### User Story Dependencies

- **User Story 1 (P1)**: Can start after Foundational (Phase 2) - No dependencies on other stories
- **User Story 2 (P2)**: Can start after Foundational (Phase 2) - Uses generator from US1, but wizard code is independent
- **User Story 3 (P2)**: Mostly addressed by US1 template files - validation tasks can run in parallel

### Within Each User Story

- Template files before generator implementation
- Generator implementation before CLI updates
- Core functionality before polish
- Commit after each logical group

### Parallel Opportunities

**Phase 1 (Setup)**:
- T002, T003, T004 can run in parallel (different files)

**Phase 2 (Foundational)**:
- T009, T010 can run in parallel with T005-T008 (different files: validators.go vs executor.go/template.go)

**Phase 3 (User Story 1)**:
- All template files T011-T024 can run in parallel (different template files)
- Generator tasks T025-T029 are sequential (same file)
- CLI tasks T033-T036 are sequential (same file)

**Phase 4 (User Story 2)**:
- T039-T044 are sequential (same file: wizard.go)

**Phase 5 (User Story 3)**:
- T046-T050 can run in parallel (different .gitkeep files)

---

## Parallel Example: User Story 1 Template Files

```bash
# Launch all template files together (different files, no dependencies):
Task: "Create go.mod.tmpl template in templates/go-service/files/go.mod.tmpl"
Task: "Create main.go.tmpl in templates/go-service/files/cmd/{{.ProjectName}}/main.go.tmpl"
Task: "Create config.go.tmpl in templates/go-service/files/internal/{{.ProjectName}}/config/config.go.tmpl"
Task: "Create health.go.tmpl in templates/go-service/files/internal/{{.ProjectName}}/handler/health.go.tmpl"
Task: "Create Makefile.tmpl in templates/go-service/files/Makefile.tmpl"
# ... all T011-T024 tasks
```

---

## Implementation Strategy

### MVP First (User Story 1 Only)

1. Complete Phase 1: Setup (T001-T004)
2. Complete Phase 2: Foundational (T005-T010)
3. Complete Phase 3: User Story 1 (T011-T038)
4. **STOP and VALIDATE**:
   - `forge new -t go-service -a test-api -m github.com/test/test-api`
   - `cd test-api && go build ./... && go test ./...`
   - `forge templates` shows go-service
5. Deploy/demo if ready

### Incremental Delivery

1. Complete Setup + Foundational → Foundation ready
2. Add User Story 1 → Test independently → MVP Ready!
3. Add User Story 2 → Test interactive mode → Enhanced UX
4. Add User Story 3 → Validate structure → Complete feature
5. Each story adds value without breaking previous stories

### Recommended Task Sequence

For single developer:
1. T001 → T002-T004 (parallel) → T005-T010 → **Foundation complete**
2. T011-T024 (parallel templates) → T025-T029 (generator) → T030-T032 (routing) → T033-T038 (CLI)
3. Validate US1 works
4. T039-T044 (wizard)
5. T045-T050 (structure validation)
6. T051-T056 (polish)

---

## Notes

- [P] tasks = different files, no dependencies
- [Story] label maps task to specific user story for traceability
- Each user story should be independently completable and testable
- Commit after each task or logical group
- Stop at any checkpoint to validate story independently
- Template syntax uses `{{.Variable}}` for Go text/template
- All generated code must pass `go build ./...` and `go test ./...`
