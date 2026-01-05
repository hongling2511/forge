# Quickstart: Maven DDD 多模块工程脚手架

**Date**: 2026-01-05
**Feature**: [spec.md](./spec.md) | [plan.md](./plan.md)

## 前置条件

- **JDK 17+**: `java -version` 应显示 17 或更高版本
- **Maven 3.6+**: `mvn -version` 应显示 3.6 或更高版本

## 快速开始

### 1. 安装 Forge CLI (首次使用)

```bash
# 克隆并安装
git clone https://github.com/your-org/forge.git
cd forge
./install.sh

# 验证安装
forge --version
```

### 2. 生成 DDD 工程

**交互模式**:
```bash
forge new
# 按提示输入项目名称、包名等
```

**非交互模式** (推荐用于 CI/CD):
```bash
forge new \
  --group-id com.example \
  --artifact-id my-service \
  --version 1.0.0-SNAPSHOT \
  --package com.example.myservice
```

**指定输出目录**:
```bash
forge new \
  --group-id com.example \
  --artifact-id my-service \
  --output ./projects/
```

### 3. 构建生成的工程

```bash
cd my-service
mvn clean package
```

### 4. 启动应用

```bash
java -jar my-service-bootstrap/target/my-service-bootstrap-1.0.0-SNAPSHOT.jar
```

### 5. 验证健康检查

```bash
curl http://localhost:8080/actuator/health
# 期望输出: {"status":"UP"}
```

---

## Forge CLI 命令

```bash
forge --help                    # 显示帮助
forge --version                 # 显示版本
forge templates                 # 列出所有可用模板
forge new                       # 交互式创建新工程
forge new --help                # 显示 new 命令参数
forge new -t java-ddd -g <groupId> -a <artifactId>  # 非交互式创建
```

### 可用模板

```bash
forge templates
# 输出:
# java-ddd     Java DDD 多模块工程 (Spring Boot 3.x)
# (更多模板将在后续版本添加)
```

### `forge new` 参数

| 参数 | 短参数 | 必需 | 默认值 | 说明 |
|------|--------|------|--------|------|
| --template | -t | ❌ | java-ddd | 模板类型 |
| --group-id | -g | ✅* | - | Maven groupId (Java 模板必需) |
| --artifact-id | -a | ✅ | - | 项目名称 |
| --version | -v | ❌ | 1.0.0-SNAPSHOT | 工程版本 |
| --package | -p | ❌ | = groupId | 包名 (Java 模板) |
| --output | -o | ❌ | 当前目录 | 输出目录 |

*注：不同模板可能有不同的必需参数

---

## 生成的工程结构

```text
my-service/
├── pom.xml                          # 父 POM
├── my-service-domain/               # 领域层
│   ├── pom.xml
│   └── src/main/java/com/example/myservice/domain/
├── my-service-application/          # 应用层
│   ├── pom.xml
│   └── src/main/java/com/example/myservice/application/
├── my-service-infrastructure/       # 基础设施层
│   ├── pom.xml
│   └── src/main/java/com/example/myservice/infrastructure/
├── my-service-interface/            # 接口层
│   ├── pom.xml
│   └── src/main/java/com/example/myservice/interfaces/
└── my-service-bootstrap/            # 启动模块
    ├── pom.xml
    ├── src/main/java/com/example/myservice/Application.java
    └── src/main/resources/application.yml
```

---

## 模块依赖关系

```text
bootstrap → interface → application → domain
              ↓              ↓
          infrastructure ────┘
```

- **domain**: 无依赖，纯领域模型
- **application**: 依赖 domain
- **infrastructure**: 依赖 domain, application
- **interface**: 依赖 application
- **bootstrap**: 依赖全部模块

---

## 常见问题

### Q: 目标目录已存在怎么办？

Forge 会拒绝在非空目录生成工程。请选择一个空目录或不存在的目录。

```bash
# 错误示例
forge new -g com.example -a existing-folder
# ERROR: 目标目录已存在且非空

# 正确做法
rm -rf existing-folder  # 或选择新目录名
forge new -g com.example -a new-folder
```

### Q: 如何查看生成元信息？

查看生成工程的父 POM 中的 properties:

```bash
grep -A3 "forge.archetype.version" my-service/pom.xml
```

### Q: 如何修改默认端口？

编辑 `my-service-bootstrap/src/main/resources/application.yml`:

```yaml
server:
  port: 9090  # 修改为所需端口
```

### Q: Forge CLI 底层是什么？

Forge CLI 是对 Maven Archetype 的封装，底层执行：

```bash
mvn archetype:generate \
  -DarchetypeGroupId=com.forge \
  -DarchetypeArtifactId=ddd-archetype \
  -DarchetypeVersion=${FORGE_VERSION} \
  -DgroupId=${groupId} \
  -DartifactId=${artifactId} \
  -Dversion=${version} \
  -Dpackage=${package} \
  -B
```

---

## 验证清单

生成工程后，执行以下验证：

- [ ] `mvn clean package` 构建成功
- [ ] `java -jar ...bootstrap...jar` 启动成功
- [ ] `curl localhost:8080/actuator/health` 返回 `{"status":"UP"}`
- [ ] 检查 POM 中 `forge.archetype.version` 属性存在
