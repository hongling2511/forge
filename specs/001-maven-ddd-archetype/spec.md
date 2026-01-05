# Feature Specification: Maven DDD 多模块工程脚手架

**Feature Branch**: `001-maven-ddd-archetype`
**Created**: 2026-01-05
**Status**: Draft
**Input**: User description: "通过maven的模版功能初始化一个多模块DDD架构工程模版"

## User Scenarios & Testing *(mandatory)*

### User Story 1 - 生成最小可运行 DDD 工程 (Priority: P1)

开发者希望通过一条命令生成一个符合 DDD 分层架构的多模块工程，生成后无需任何修改即可构建和启动。

**Why this priority**: 这是脚手架的核心价值。根据宪法第二条，默认生成结果必须可构建、可启动、有可验证执行路径。

**Independent Test**: 执行生成命令后，直接运行构建和启动命令，验证工程可以成功运行并响应健康检查请求。

**Acceptance Scenarios**:

1. **Given** 开发者已安装脚手架, **When** 执行生成命令并指定项目名称和包名, **Then** 在指定目录生成完整的多模块 DDD 工程结构
2. **Given** 生成的工程目录, **When** 执行构建命令, **Then** 构建成功且无错误
3. **Given** 构建成功的工程, **When** 执行启动命令, **Then** 应用成功启动并监听指定端口
4. **Given** 应用已启动, **When** 访问健康检查端点, **Then** 返回成功状态

---

### User Story 2 - 非交互式批量生成 (Priority: P2)

开发者希望在 CI/CD 流水线或脚本中批量生成工程，无需人工干预。

**Why this priority**: 根据宪法第五条，CLI 必须支持非交互执行。这是规模化使用的前提。

**Independent Test**: 在无 TTY 环境下执行生成命令，验证可以完全通过命令行参数完成生成。

**Acceptance Scenarios**:

1. **Given** 所有必需参数通过命令行提供, **When** 在非交互环境执行生成命令, **Then** 生成成功且无任何交互提示
2. **Given** 缺少必需参数, **When** 执行生成命令, **Then** 立即失败并明确提示缺少哪些参数

---

### User Story 3 - 确定性可复现生成 (Priority: P3)

开发者希望在相同输入条件下，多次生成的工程结构完全一致，便于团队协作和问题排查。

**Why this priority**: 根据宪法第六条，生成结果必须确定性可复现。这是工程基础设施的基本要求。

**Independent Test**: 使用相同参数在不同机器或不同时间执行两次生成，比对生成结果是否完全一致。

**Acceptance Scenarios**:

1. **Given** 相同的脚手架版本、输入参数和模板版本, **When** 在不同机器上执行生成命令, **Then** 生成的工程结构和内容完全一致
2. **Given** 生成的工程, **When** 查看工程元信息, **Then** 可以看到脚手架版本和模板版本记录

---

### Edge Cases

- 当指定的目标目录已存在同名文件夹时，系统如何处理？
  - 必须立即失败并提示，禁止覆盖或合并
- 当指定的包名格式不合法时，系统如何处理？
  - 必须立即失败并提示合法格式要求
- 当磁盘空间不足时，系统如何处理？
  - 必须在生成前检测并提示，而非生成到一半失败

## Requirements *(mandatory)*

### Functional Requirements

**生成能力**:

- **FR-001**: 系统 MUST 通过单一 CLI 入口接受生成请求
- **FR-002**: 系统 MUST 生成包含以下模块的多模块工程结构：
  - 领域层（Domain）：存放领域模型和业务规则
  - 应用层（Application）：存放应用服务和用例编排
  - 基础设施层（Infrastructure）：存放技术实现和外部集成
  - 接口层（Interface）：存放对外暴露的 API 入口
  - 启动模块（Bootstrap）：存放应用启动配置
- **FR-003**: 系统 MUST 支持以下必需参数：
  - 项目名称（project name）
  - 基础包名（base package）
- **FR-004**: 系统 MUST 支持以下可选参数：
  - 目标目录（默认为当前目录）
  - 项目描述（默认为空）

**最小可运行要求**:

- **FR-005**: 生成的工程 MUST 可以成功执行构建命令
- **FR-006**: 生成的工程 MUST 可以成功启动应用
- **FR-007**: 生成的工程 MUST 包含健康检查端点，返回应用运行状态

**CLI 要求**:

- **FR-008**: 系统 MUST 通过 `forge` 命令作为统一入口
- **FR-009**: `forge --help` MUST 显示使用说明
- **FR-010**: `forge new` MUST 支持以下参数的命令行方式传入：
  - `--template` / `-t`: 可选，模板类型（默认 `java-ddd`），支持扩展
  - `--group-id` / `-g`: 必需，Maven groupId（Java 模板）
  - `--artifact-id` / `-a`: 必需，项目名称
  - `--version` / `-v`: 可选，工程版本
  - `--package` / `-p`: 可选，包名（Java 模板）
  - `--output` / `-o`: 可选，输出目录
- **FR-011**: `forge new` MAY 支持交互式提示，但必须有等价的命令行参数
- **FR-012**: `forge templates` MUST 列出所有可用模板

**错误处理**:

- **FR-013**: 当必需参数缺失时，系统 MUST 立即失败并列出缺失参数
- **FR-014**: 当参数格式非法时，系统 MUST 立即失败并说明合法格式
- **FR-015**: 当目标目录已存在时，系统 MUST 立即失败并提示
- **FR-016**: 当指定的模板不存在时，系统 MUST 立即失败并列出可用模板

**可追溯性**:

- **FR-017**: 生成的工程 MUST 在某处记录脚手架版本号
- **FR-018**: 生成的工程 MUST 在某处记录模板版本号

### Key Entities

- **工程（Project）**: 生成的目标产物，包含多个模块、配置文件和元信息
- **模块（Module）**: 工程的组成部分，代表 DDD 分层架构中的一个层
- **模板（Template）**: 脚手架用于生成工程的蓝图，具有独立版本
- **参数（Parameter）**: 用户输入的配置项，决定生成结果的差异

## Success Criteria *(mandatory)*

### Measurable Outcomes

- **SC-001**: 开发者可以在 60 秒内完成从执行命令到生成完整工程
- **SC-002**: 生成的工程可以在 120 秒内完成首次构建
- **SC-003**: 生成的工程可以在 30 秒内完成应用启动
- **SC-004**: 健康检查端点响应时间小于 1 秒
- **SC-005**: 相同输入参数生成的工程，文件内容 100% 一致（排除时间戳等动态元素）
- **SC-006**: 100% 的错误场景返回明确的错误信息和解决建议

## Assumptions

- 目标用户已安装 Maven 3.6+ 和 JDK 17+
- 生成的工程使用 Maven 作为构建工具
- DDD 分层采用经典四层架构（Domain、Application、Infrastructure、Interface）加启动模块
- 健康检查端点采用行业标准路径 `/actuator/health` 或 `/health`
- 默认生成的是 Web 应用（HTTP 服务）

## Constitution Compliance

| 宪法条款 | 合规设计 |
|----------|----------|
| 第二条：默认最小化 | 仅生成最小可运行工程，无预埋设计 |
| 第四条：生成结果即契约 | 需配套契约测试验证生成结果 |
| 第五条：CLI 优先 | 单一 CLI 入口，支持非交互执行 |
| 第六条：确定性可复现 | 相同输入产生相同输出 |
| 第九条：模板版本化 | 生成工程记录脚手架和模板版本 |
| 第十条：尽早失败 | 参数非法立即失败并提示 |
| 第十一条：禁止隐式全局状态 | 不依赖隐式环境变量 |
