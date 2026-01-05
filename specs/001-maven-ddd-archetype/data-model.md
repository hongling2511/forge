# Data Model: Maven DDD 多模块工程脚手架

**Date**: 2026-01-05
**Feature**: [spec.md](./spec.md) | [plan.md](./plan.md)

## Overview

本文档定义脚手架涉及的核心实体模型。注意：这是**脚手架本身**的模型，不是生成工程的业务模型。

---

## Core Entities

### 1. ArchetypeProject (Archetype 项目)

脚手架本身作为一个 Maven Archetype 项目。

| 属性 | 类型 | 必需 | 说明 |
|------|------|------|------|
| groupId | String | ✅ | Archetype 的 Maven groupId |
| artifactId | String | ✅ | Archetype 的 Maven artifactId |
| version | String | ✅ | Archetype 版本 (SemVer) |
| templateVersion | String | ✅ | 模板版本，可独立于 archetype 版本 |

---

### 2. GenerationRequest (生成请求)

用户执行生成命令时传入的参数。

| 属性 | 类型 | 必需 | 默认值 | 校验规则 |
|------|------|------|--------|----------|
| groupId | String | ✅ | - | `^[a-z][a-z0-9]*(\.[a-z][a-z0-9]*)*$` |
| artifactId | String | ✅ | - | `^[a-z][a-z0-9-]*$` |
| version | String | ❌ | 1.0.0-SNAPSHOT | SemVer 格式 |
| package | String | ❌ | = groupId | Java 包名格式 |
| outputDirectory | Path | ❌ | 当前目录 | 目录不存在或为空 |

**Validation Rules**:
- groupId: 必须是合法的 Maven groupId，全小写，点分隔
- artifactId: 必须是合法的 Maven artifactId，全小写，可含连字符
- package: 如未指定，默认为 groupId + "." + artifactId（连字符转下划线）
- outputDirectory: 如目标目录已存在且非空，拒绝生成

---

### 3. GeneratedProject (生成的工程)

脚手架生成的目标产物。

| 属性 | 类型 | 说明 |
|------|------|------|
| rootDirectory | Path | 工程根目录 |
| modules | List[Module] | 包含的模块列表 |
| parentPom | File | 父 POM 文件 |
| metadata | ProjectMetadata | 元信息 |

---

### 4. Module (模块)

DDD 分层架构中的一个模块。

| 属性 | 类型 | 说明 |
|------|------|------|
| name | String | 模块名 (domain/application/infrastructure/interface/bootstrap) |
| artifactId | String | 模块的 Maven artifactId |
| directory | Path | 模块目录路径 |
| dependencies | List[String] | 依赖的其他模块 artifactId |
| layer | DddLayer | DDD 层级枚举 |

**DddLayer Enum**:
```
DOMAIN         - 领域层，无依赖
APPLICATION    - 应用层，依赖 domain
INFRASTRUCTURE - 基础设施层，依赖 domain, application
INTERFACE      - 接口层，依赖 application
BOOTSTRAP      - 启动模块，依赖全部
```

---

### 5. ProjectMetadata (工程元信息)

记录在生成工程中的追溯信息。

| 属性 | 类型 | 说明 |
|------|------|------|
| archetypeVersion | String | 使用的 Archetype 版本 |
| templateVersion | String | 使用的模板版本 |
| generatedDate | DateTime | 生成时间 (ISO 8601) |
| generatorName | String | 生成器名称 (forge-ddd-archetype) |

---

## Entity Relationships

```text
┌─────────────────────┐
│  ArchetypeProject   │
│  (脚手架项目)        │
└─────────────────────┘
          │
          │ generates
          ▼
┌─────────────────────┐      ┌─────────────────────┐
│  GenerationRequest  │ ───▶ │  GeneratedProject   │
│  (生成请求)          │      │  (生成的工程)        │
└─────────────────────┘      └─────────────────────┘
                                       │
                                       │ contains
                                       ▼
                             ┌─────────────────────┐
                             │      Module         │
                             │  (DDD 模块) × 5     │
                             └─────────────────────┘
                                       │
                                       │ has
                                       ▼
                             ┌─────────────────────┐
                             │  ProjectMetadata    │
                             │  (工程元信息)        │
                             └─────────────────────┘
```

---

## Module Dependency Graph

```text
                    ┌──────────────┐
                    │   bootstrap  │
                    └──────────────┘
                           │
           ┌───────────────┼───────────────┐
           ▼               ▼               ▼
    ┌────────────┐  ┌────────────┐  ┌──────────────┐
    │ interface  │  │infrastructure│  │              │
    └────────────┘  └────────────┘  │              │
           │               │         │              │
           │               ▼         │              │
           │        ┌────────────┐   │              │
           └──────▶ │ application│ ◀─┘              │
                    └────────────┘                  │
                           │                        │
                           ▼                        │
                    ┌────────────┐                  │
                    │   domain   │ ◀────────────────┘
                    └────────────┘

箭头方向: 依赖指向
```

---

## State Transitions

### GenerationRequest Lifecycle

```text
[Created] ──validate──▶ [Validated] ──generate──▶ [Completed]
    │                        │
    │                        │
    ▼                        ▼
[Invalid]              [Failed]
(参数校验失败)          (生成过程失败)
```

**State Descriptions**:
- **Created**: 请求已创建，参数未校验
- **Validated**: 参数校验通过，可执行生成
- **Invalid**: 参数校验失败，需修正参数
- **Completed**: 生成成功
- **Failed**: 生成过程中发生错误

---

## Validation Rules Summary

| 实体 | 字段 | 规则 | 错误消息 |
|------|------|------|----------|
| GenerationRequest | groupId | 非空，Maven groupId 格式 | "groupId 必须是合法的 Maven groupId，例如: com.example" |
| GenerationRequest | artifactId | 非空，Maven artifactId 格式 | "artifactId 必须是合法的 Maven artifactId，例如: my-project" |
| GenerationRequest | version | SemVer 格式 | "version 必须是 SemVer 格式，例如: 1.0.0-SNAPSHOT" |
| GenerationRequest | package | Java 包名格式 | "package 必须是合法的 Java 包名，例如: com.example.project" |
| GenerationRequest | outputDirectory | 不存在或为空目录 | "目标目录已存在且非空，请指定一个空目录或不存在的路径" |
