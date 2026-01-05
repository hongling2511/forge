# Research: Maven DDD 多模块工程脚手架

**Date**: 2026-01-05
**Feature**: [spec.md](./spec.md) | [plan.md](./plan.md)

## Research Topics

### 1. Maven Archetype 机制

**Decision**: 使用 Maven Archetype Plugin 3.2.x 创建多模块项目模板

**Rationale**:
- Maven Archetype 是 Java 生态标准的项目生成机制
- 原生支持非交互式执行 (`-B` batch mode)
- 内置参数校验机制
- 支持多模块项目生成
- 符合宪法第三条"不发明框架"原则

**Alternatives Considered**:
| 方案 | 优点 | 缺点 | 排除原因 |
|------|------|------|----------|
| JHipster | 功能丰富 | 过于复杂，有私有框架 | 违反宪法第三条 |
| Spring Initializr | Spring 官方 | 单模块，需二次开发 | 不支持多模块 DDD |
| 自定义脚本 | 灵活 | 维护成本高，非标准 | 违反宪法第三条 |
| Yeoman | 跨语言 | Java 生态不主流 | 不符合最小化原则 |

---

### 2. DDD 多模块结构

**Decision**: 采用经典 5 模块结构

**Rationale**:
- 符合 DDD 分层架构共识
- 依赖方向清晰：domain ← application ← infrastructure/interface ← bootstrap
- 每层职责单一，便于测试
- 业界广泛采用的模式

**Module Structure**:

| 模块 | 职责 | 依赖 |
|------|------|------|
| domain | 领域模型、值对象、领域服务、仓储接口 | 无 |
| application | 应用服务、DTO、用例编排 | domain |
| infrastructure | 仓储实现、外部服务适配器 | domain, application |
| interface | REST Controller、API 入口 | application |
| bootstrap | 启动配置、依赖注入组装 | 全部 |

**Alternatives Considered**:
| 方案 | 模块数 | 缺点 | 排除原因 |
|------|--------|------|----------|
| 3 层 (domain/app/infra) | 3 | interface 和 bootstrap 混杂 | 职责不清 |
| 6 层 (加 common) | 6 | common 往往成垃圾桶 | 违反最小化原则 |
| 单体 | 1 | 无分层约束 | 不是 DDD |

---

### 3. Spring Boot 版本选择

**Decision**: Spring Boot 3.2.x (当前稳定版)

**Rationale**:
- Java 17+ 基准，符合现代 Java 生态
- 原生支持 GraalVM Native Image (可选)
- Spring Boot Actuator 提供健康检查
- 长期支持版本

**Key Dependencies**:
```xml
spring-boot-starter-web       <!-- REST API -->
spring-boot-starter-actuator  <!-- 健康检查 /actuator/health -->
spring-boot-starter-test      <!-- 测试框架 -->
```

---

### 4. 健康检查实现

**Decision**: 使用 Spring Boot Actuator `/actuator/health`

**Rationale**:
- 行业标准路径
- 零代码实现
- 支持自定义健康指示器扩展
- 符合宪法第二条"最小可运行"

**Configuration**:
```yaml
management:
  endpoints:
    web:
      exposure:
        include: health
  endpoint:
    health:
      show-details: never  # 最小化，不暴露细节
```

---

### 5. Archetype 参数设计

**Decision**: 2 个必需参数 + 2 个可选参数

**Parameters**:

| 参数 | 必需 | 默认值 | 校验规则 |
|------|------|--------|----------|
| groupId | ✅ | - | Maven groupId 格式 |
| artifactId | ✅ | - | Maven artifactId 格式 |
| version | ❌ | 1.0.0-SNAPSHOT | SemVer 格式 |
| package | ❌ | = groupId | Java 包名格式 |

**CLI Usage**:
```bash
mvn archetype:generate \
  -DarchetypeGroupId=com.example.forge \
  -DarchetypeArtifactId=ddd-archetype \
  -DarchetypeVersion=1.0.0 \
  -DgroupId=com.mycompany \
  -DartifactId=my-project \
  -Dversion=1.0.0-SNAPSHOT \
  -Dpackage=com.mycompany.myproject \
  -B  # 非交互模式
```

---

### 6. 版本追溯机制

**Decision**: 在生成工程的 parent POM 中记录元信息

**Rationale**:
- POM 是 Maven 项目的权威元数据位置
- 不需要额外文件
- 构建时可读取

**Implementation**:
```xml
<properties>
  <!-- Forge Scaffold Metadata -->
  <forge.archetype.version>1.0.0</forge.archetype.version>
  <forge.template.version>1.0.0</forge.template.version>
  <forge.generated.date>${timestamp}</forge.generated.date>
</properties>
```

---

### 7. 契约测试策略

**Decision**: 使用 Maven Archetype Plugin 的 Integration Test 机制

**Rationale**:
- 官方推荐方式
- 在 `src/test/resources/projects/` 定义测试用例
- 自动验证生成结果可构建

**Test Structure**:
```text
src/test/resources/projects/basic/
├── archetype.properties     # 测试参数
├── goal.txt                 # 验证目标 (verify)
└── reference/               # 可选：期望结果参照
```

**archetype.properties**:
```properties
groupId=com.example.test
artifactId=test-project
version=1.0.0-SNAPSHOT
package=com.example.test
```

**goal.txt**:
```text
verify
```

---

## Research Summary

| 领域 | 决策 | 符合宪法 |
|------|------|----------|
| 生成机制 | Maven Archetype | ✅ 第三条、第五条 |
| 模块结构 | 5 模块 DDD | ✅ 第二条 |
| 运行时框架 | Spring Boot 3.2.x | ✅ 第三条 |
| 健康检查 | Actuator | ✅ 第二条 |
| 参数设计 | 2 必需 + 2 可选 | ✅ 第十条 |
| 版本追溯 | POM properties | ✅ 第九条 |
| 契约测试 | Archetype IT | ✅ 第四条 |

所有技术决策均已确定，无 NEEDS CLARIFICATION 项。
