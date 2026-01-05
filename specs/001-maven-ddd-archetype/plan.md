# Implementation Plan: Maven DDD 多模块工程脚手架

**Branch**: `001-maven-ddd-archetype` | **Date**: 2026-01-05 | **Spec**: [spec.md](./spec.md)
**Input**: Feature specification from `/specs/001-maven-ddd-archetype/spec.md`

## Summary

通过 Maven Archetype 机制实现一个 DDD 多模块工程生成器。用户执行单一 CLI 命令，指定项目名称和包名后，即可生成一个可构建、可启动、包含健康检查端点的最小可运行工程。生成结果遵循经典 DDD 四层架构（Domain、Application、Infrastructure、Interface）加启动模块的结构。

## Technical Context

**Language/Version**: Java 17+ (生成的工程), Maven 3.6+ (脚手架和构建工具)
**Primary Dependencies**: Maven Archetype Plugin 3.2+, Spring Boot 3.x (生成工程的运行时框架)
**Storage**: N/A (脚手架本身无持久化需求)
**Testing**: Maven Archetype Integration Tests, JUnit 5 (生成工程的测试框架)
**Target Platform**: 跨平台 (任何安装了 Maven 和 JDK 的环境)
**Project Type**: Single project (Maven Archetype 项目)
**Performance Goals**: 生成时间 <60s, 构建时间 <120s, 启动时间 <30s
**Constraints**: 无外部网络依赖生成 (离线友好), 无隐式全局状态
**Scale/Scope**: 单一 Archetype，生成 5 模块 DDD 工程

## Constitution Check

*GATE: Must pass before Phase 0 research. Re-check after Phase 1 design.*

| 宪法条款 | 检查项 | 状态 | 设计决策 |
|----------|--------|------|----------|
| 第一条：脚手架是产品 | 功能由规格驱动，行为可测试 | ✅ | spec.md 驱动，契约测试验证 |
| 第二条：默认最小化 | 生成结果可构建、启动、有健康检查 | ✅ | FR-005/006/007 明确要求 |
| 第三条：不发明框架 | 使用主流工具，无私有封装 | ✅ | 直接使用 Spring Boot，无自定义框架 |
| 第四条：生成结果即契约 | 提供契约测试验证结构 | ✅ | 需实现 Archetype IT 测试 |
| 第五条：CLI 优先 | 单一入口，支持非交互 | ✅ | Maven Archetype CLI 原生支持 |
| 第六条：确定性可复现 | 相同输入相同输出 | ✅ | 无随机行为，无本地状态依赖 |
| 第九条：模板版本化 | 记录脚手架和模板版本 | ✅ | 在生成工程 POM 中记录 |
| 第十条：尽早失败 | 非法输入立即报错 | ✅ | Maven Archetype 原生校验 + 自定义校验 |
| 第十一条：禁止隐式全局状态 | 不依赖隐式环境变量 | ✅ | 所有配置显式传参 |

**Gate Result**: ✅ PASSED - 所有条款合规，可进入 Phase 0

## Project Structure

### Documentation (this feature)

```text
specs/001-maven-ddd-archetype/
├── plan.md              # This file
├── research.md          # Phase 0 output
├── data-model.md        # Phase 1 output
├── quickstart.md        # Phase 1 output
├── contracts/           # Phase 1 output
│   └── archetype-contract.yaml
└── tasks.md             # Phase 2 output (/speckit.tasks command)
```

### Source Code (repository root)

```text
src/
├── main/
│   └── resources/
│       ├── META-INF/
│       │   └── maven/
│       │       └── archetype-metadata.xml    # Archetype 描述符
│       └── archetype-resources/              # 模板文件
│           ├── pom.xml                       # 父 POM 模板
│           ├── __rootArtifactId__-domain/
│           │   ├── pom.xml
│           │   └── src/main/java/__packagePath__/domain/
│           ├── __rootArtifactId__-application/
│           │   ├── pom.xml
│           │   └── src/main/java/__packagePath__/application/
│           ├── __rootArtifactId__-infrastructure/
│           │   ├── pom.xml
│           │   └── src/main/java/__packagePath__/infrastructure/
│           ├── __rootArtifactId__-interface/
│           │   ├── pom.xml
│           │   └── src/main/java/__packagePath__/interfaces/
│           └── __rootArtifactId__-bootstrap/
│               ├── pom.xml
│               └── src/main/java/__packagePath__/
│                   └── Application.java      # 启动类
└── test/
    └── resources/
        └── projects/
            └── basic/                        # 契约测试项目
                ├── archetype.properties
                └── goal.txt

pom.xml                                       # Archetype 项目 POM
```

**Structure Decision**: 采用 Maven Archetype 标准结构。`archetype-resources/` 目录包含生成工程的模板，使用 `__rootArtifactId__` 和 `__packagePath__` 占位符实现动态替换。

## Complexity Tracking

> 无违规项，不需要复杂度辩护。
