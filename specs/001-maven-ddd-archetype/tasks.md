# Tasks: Maven DDD 多模块工程脚手架

**Input**: Design documents from `/specs/001-maven-ddd-archetype/`
**Prerequisites**: plan.md (required), spec.md (required), research.md, data-model.md, contracts/

**Tests**: 根据宪法第四条"生成结果即契约"，本 feature 需要契约测试验证生成结果。

**Organization**: Tasks are grouped by user story to enable independent implementation and testing of each story.

## Format: `[ID] [P?] [Story] Description`

- **[P]**: Can run in parallel (different files, no dependencies)
- **[Story]**: Which user story this task belongs to (e.g., US1, US2, US3)
- Include exact file paths in descriptions

## Path Conventions

本项目采用可扩展的多模板结构：
- **Forge CLI**: `cli/` 目录，Shell 脚本封装
- **模板目录**: `templates/` 目录，每个子目录是一个模板
- 契约测试: `templates/<template-name>/src/test/resources/projects/`

```text
forge/
├── cli/
│   ├── forge                    # 主入口脚本
│   ├── commands/
│   │   ├── new.sh               # new 子命令
│   │   └── templates.sh         # templates 子命令
│   └── lib/
│       ├── args.sh              # 参数解析
│       └── validation.sh        # 参数校验
├── templates/
│   └── java-ddd/                # Java DDD 模板 (Feature 001)
│       ├── pom.xml              # Archetype POM
│       ├── template.yaml        # 模板元数据
│       └── src/main/resources/  # Archetype 模板文件
├── install.sh                   # 安装脚本
└── pom.xml                      # 根 POM
```

---

## Phase 1: Setup (项目初始化)

**Purpose**: 初始化 Forge 项目结构（CLI + 模板目录）

- [x] T001 Create root project structure with pom.xml at repository root
- [x] T002 [P] Create cli/ directory structure with forge entry script at cli/forge
- [x] T003 [P] Create templates/ directory for template storage
- [x] T004 [P] Create templates/java-ddd/ directory with Maven Archetype project structure
- [x] T005 [P] Create install.sh script at repository root for PATH setup

**Checkpoint**: 基础项目结构已就绪

---

## Phase 2: Foundational (CLI 框架 + 模板系统)

**Purpose**: 实现 CLI 框架和模板加载机制，为所有用户故事提供基础

**⚠️ CRITICAL**: No user story work can begin until this phase is complete

### CLI 框架

- [x] T006 Create main forge script with subcommand dispatch at cli/forge
- [x] T007 [P] Create argument parsing library at cli/lib/args.sh
- [x] T008 [P] Create validation library at cli/lib/validation.sh
- [x] T009 Create --help and --version handlers in cli/forge
- [x] T010 [P] Create templates.sh subcommand to list available templates at cli/commands/templates.sh

### 模板系统

- [x] T011 Create template.yaml metadata file for java-ddd at templates/java-ddd/template.yaml
- [x] T012 Configure archetype plugin dependencies in templates/java-ddd/pom.xml (maven-archetype-plugin 3.2+)
- [x] T013 Create archetype-metadata.xml with required properties at templates/java-ddd/src/main/resources/META-INF/maven/archetype-metadata.xml
- [x] T014 [P] Define optional properties (version, package) with defaults in archetype-metadata.xml
- [x] T015 [P] Configure fileSet declarations for 5 DDD modules in archetype-metadata.xml
- [x] T016 Create parent POM template with module declarations at templates/java-ddd/src/main/resources/archetype-resources/pom.xml
- [x] T017 Add version tracking properties (forge.archetype.version, forge.template.version) to parent POM template

**Checkpoint**: CLI 框架和模板系统配置完成，可开始模块模板开发

---

## Phase 3: User Story 1 - 生成最小可运行 DDD 工程 (Priority: P1) 🎯 MVP

**Goal**: 通过 `forge new -t java-ddd` 生成可构建、可启动、有健康检查的 5 模块 DDD 工程

**Independent Test**: 执行 `forge new -t java-ddd -g com.example -a test` 后，生成的工程可以 `mvn package` 构建、`java -jar` 启动、`curl /actuator/health` 返回 200

### CLI `forge new` 命令

- [x] T018 [US1] Create new.sh subcommand script at cli/commands/new.sh
- [x] T019 [US1] Implement --template/-t parameter parsing with default java-ddd in cli/commands/new.sh
- [x] T020 [US1] Implement --group-id/-g parameter parsing in cli/commands/new.sh
- [x] T021 [US1] Implement --artifact-id/-a parameter parsing in cli/commands/new.sh
- [x] T022 [US1] Implement optional parameters (--version, --package, --output) in cli/commands/new.sh
- [x] T023 [US1] Implement template discovery and Maven Archetype invocation in cli/commands/new.sh
- [x] T024 [US1] Add --help handler for new subcommand in cli/commands/new.sh

### 契约测试 for User Story 1

- [x] T025 [US1] Create basic contract test project at templates/java-ddd/src/test/resources/projects/basic/archetype.properties
- [x] T026 [US1] Create goal.txt with verify target at templates/java-ddd/src/test/resources/projects/basic/goal.txt

### Domain 模块模板

- [x] T027 [P] [US1] Create domain module directory structure at templates/java-ddd/src/main/resources/archetype-resources/__rootArtifactId__-domain/
- [x] T028 [P] [US1] Create domain module pom.xml template at templates/java-ddd/src/main/resources/archetype-resources/__rootArtifactId__-domain/pom.xml
- [x] T029 [US1] Create domain package placeholder at templates/java-ddd/src/main/resources/archetype-resources/__rootArtifactId__-domain/src/main/java/__packagePath__/domain/.gitkeep

### Application 模块模板

- [x] T030 [P] [US1] Create application module directory structure at templates/java-ddd/src/main/resources/archetype-resources/__rootArtifactId__-application/
- [x] T031 [P] [US1] Create application module pom.xml template with domain dependency at templates/java-ddd/src/main/resources/archetype-resources/__rootArtifactId__-application/pom.xml
- [x] T032 [US1] Create application package placeholder at templates/java-ddd/src/main/resources/archetype-resources/__rootArtifactId__-application/src/main/java/__packagePath__/application/.gitkeep

### Infrastructure 模块模板

- [x] T033 [P] [US1] Create infrastructure module directory structure at templates/java-ddd/src/main/resources/archetype-resources/__rootArtifactId__-infrastructure/
- [x] T034 [P] [US1] Create infrastructure module pom.xml template with domain and application dependencies at templates/java-ddd/src/main/resources/archetype-resources/__rootArtifactId__-infrastructure/pom.xml
- [x] T035 [US1] Create infrastructure package placeholder at templates/java-ddd/src/main/resources/archetype-resources/__rootArtifactId__-infrastructure/src/main/java/__packagePath__/infrastructure/.gitkeep

### Interface 模块模板

- [x] T036 [P] [US1] Create interface module directory structure at templates/java-ddd/src/main/resources/archetype-resources/__rootArtifactId__-interface/
- [x] T037 [P] [US1] Create interface module pom.xml template with application dependency at templates/java-ddd/src/main/resources/archetype-resources/__rootArtifactId__-interface/pom.xml
- [x] T038 [US1] Create interfaces package placeholder at templates/java-ddd/src/main/resources/archetype-resources/__rootArtifactId__-interface/src/main/java/__packagePath__/interfaces/.gitkeep

### Bootstrap 模块模板

- [x] T039 [P] [US1] Create bootstrap module directory structure at templates/java-ddd/src/main/resources/archetype-resources/__rootArtifactId__-bootstrap/
- [x] T040 [P] [US1] Create bootstrap module pom.xml template with all module dependencies and Spring Boot starter at templates/java-ddd/src/main/resources/archetype-resources/__rootArtifactId__-bootstrap/pom.xml
- [x] T041 [US1] Create Application.java startup class template at templates/java-ddd/src/main/resources/archetype-resources/__rootArtifactId__-bootstrap/src/main/java/__packagePath__/Application.java
- [x] T042 [US1] Create application.yml with health endpoint config at templates/java-ddd/src/main/resources/archetype-resources/__rootArtifactId__-bootstrap/src/main/resources/application.yml

### 验证

- [x] T043 [US1] Run mvn verify in templates/java-ddd/ to execute contract test and validate generated project builds successfully
- [x] T044 [US1] Test forge new command end-to-end with generated project startup verification

**Checkpoint**: 用户故事 1 完成 - `forge new -t java-ddd` 可生成可构建、可启动、有健康检查的工程

---

## Phase 4: User Story 2 - 非交互式批量生成 (Priority: P2)

**Goal**: 支持 CI/CD 环境下的非交互式生成，所有参数通过 CLI 传入

**Independent Test**: 在无 TTY 环境下使用 `forge new -t java-ddd -g ... -a ...` 执行生成，验证无交互提示且生成成功

### 契约测试 for User Story 2

- [x] T045 [US2] Create batch mode contract test at templates/java-ddd/src/test/resources/projects/batch/archetype.properties with all required parameters
- [x] T046 [US2] Create goal.txt for batch test at templates/java-ddd/src/test/resources/projects/batch/goal.txt

### 参数校验增强

- [x] T047 [US2] Add required parameter validation (groupId, artifactId) in cli/lib/validation.sh
- [x] T048 [US2] Add clear error messages for missing parameters in cli/commands/new.sh
- [x] T049 [US2] Ensure CLI exits with non-zero code on validation failure
- [x] T050 [US2] Add template existence validation in cli/commands/new.sh

### 验证

- [x] T051 [US2] Test batch mode generation in CI-like environment (no TTY, all params via CLI)

**Checkpoint**: 用户故事 2 完成 - 支持非交互式批量生成

---

## Phase 5: User Story 3 - 确定性可复现生成 (Priority: P3)

**Goal**: 确保相同输入产生相同输出，并记录版本元信息

**Independent Test**: 使用相同参数执行 `forge new -t java-ddd` 两次，diff 比对结果应完全一致（排除时间戳）

### 契约测试 for User Story 3

- [x] T052 [US3] Create reproducibility contract test at templates/java-ddd/src/test/resources/projects/reproducible/archetype.properties
- [x] T053 [US3] Create verification script for reproducibility check

### 版本追溯实现

- [x] T054 [US3] Add archetype version property injection to parent POM template
- [x] T055 [US3] Add template version property injection to parent POM template
- [x] T056 [US3] Ensure no random/timestamp elements in generated files (except designated metadata properties)

### 验证

- [x] T057 [US3] Verify generated project contains version metadata in pom.xml properties
- [x] T058 [US3] Verify two identical generations produce identical output

**Checkpoint**: 用户故事 3 完成 - 生成结果确定性可复现

---

## Phase 6: Polish & Cross-Cutting Concerns

**Purpose**: 文档、清理和最终验证

- [ ] T059 [P] Create README.md with forge CLI usage instructions at repository root
- [ ] T060 [P] Create CHANGELOG.md with v1.0.0 release notes at repository root
- [ ] T061 Update quickstart.md verification against actual forge new output
- [ ] T062 Run full contract test suite (mvn verify in templates/java-ddd/)
- [ ] T063 Validate generated project against archetype-contract.yaml
- [ ] T064 Test install.sh on clean environment
- [ ] T065 Test forge templates command lists java-ddd correctly

---

## Dependencies & Execution Order

### Phase Dependencies

- **Setup (Phase 1)**: No dependencies - can start immediately
- **Foundational (Phase 2)**: Depends on Setup completion - BLOCKS all user stories
- **User Stories (Phase 3+)**: All depend on Foundational phase completion
  - User stories can then proceed in parallel (if staffed)
  - Or sequentially in priority order (P1 → P2 → P3)
- **Polish (Final Phase)**: Depends on all desired user stories being complete

### User Story Dependencies

- **User Story 1 (P1)**: Can start after Foundational (Phase 2) - No dependencies on other stories
- **User Story 2 (P2)**: Can start after Foundational (Phase 2) - Builds on US1 but independently testable
- **User Story 3 (P3)**: Can start after Foundational (Phase 2) - Builds on US1 but independently testable

### Within Each User Story

- Contract tests FIRST, then implementation
- Module templates can be created in parallel ([P] marked)
- POM templates before Java/config files
- Story complete before moving to next priority

### Parallel Opportunities

- T002, T003, T004, T005 can run in parallel (Setup phase)
- T007, T008, T010 can run in parallel (Foundational - CLI framework)
- T014, T015 can run in parallel (Foundational - Template system)
- T027-T029 (Domain), T030-T032 (Application), T033-T035 (Infrastructure), T036-T038 (Interface), T039-T042 (Bootstrap) - all module templates can be developed in parallel by different team members
- T059, T060 can run in parallel (Polish phase)

---

## Parallel Example: User Story 1 Module Templates

```bash
# Launch all module directory structures in parallel:
Task: "Create domain module directory structure"
Task: "Create application module directory structure"
Task: "Create infrastructure module directory structure"
Task: "Create interface module directory structure"
Task: "Create bootstrap module directory structure"

# Then launch all module POMs in parallel:
Task: "Create domain module pom.xml template"
Task: "Create application module pom.xml template"
Task: "Create infrastructure module pom.xml template"
Task: "Create interface module pom.xml template"
Task: "Create bootstrap module pom.xml template"
```

---

## Implementation Strategy

### MVP First (User Story 1 Only)

1. Complete Phase 1: Setup (T001-T005)
2. Complete Phase 2: Foundational (T006-T017)
3. Complete Phase 3: User Story 1 (T018-T044)
4. **STOP and VALIDATE**: Run `mvn verify` to test generated project
5. Deploy/demo if ready - this is a fully functional DDD project generator!

### Incremental Delivery

1. Complete Setup + Foundational → Foundation ready
2. Add User Story 1 → Test independently → **MVP Ready!**
3. Add User Story 2 → Test batch mode → CI/CD Ready
4. Add User Story 3 → Test reproducibility → Enterprise Ready
5. Polish → Documentation complete → v1.0.0 Release

### Parallel Team Strategy

With multiple developers:

1. Team completes Setup + Foundational together
2. Once Foundational is done:
   - Developer A: Domain + Application module templates
   - Developer B: Infrastructure + Interface module templates
   - Developer C: Bootstrap module + contract tests
3. Stories complete and integrate independently

---

## Notes

- [P] tasks = different files, no dependencies
- [Story] label maps task to specific user story for traceability
- Each user story should be independently completable and testable
- Commit after each task or logical group
- Stop at any checkpoint to validate story independently
- All templates use `__rootArtifactId__` and `__packagePath__` placeholders for Maven Archetype variable substitution
