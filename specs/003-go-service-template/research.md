# Research: Go Service Template

**Feature**: 003-go-service-template
**Date**: 2026-01-05
**Reference**: 孔令飞《Go 语言项目开发实战》最佳实践

## Overview

This document consolidates research findings for implementing Go service template support in the forge CLI.

## 0. Go 项目最佳实践原则

基于参考文档，生成的项目应遵循以下原则：

### 代码结构
- **按功能拆分**（而非按层拆分）：避免循环引用，实现高内聚低耦合
- 每个模块功能单一，职责明确

### 代码规范
- 遵循 [Uber Go Style Guide](https://github.com/uber-go/guide/blob/master/style.md)
- 使用 golangci-lint 进行静态代码检查
- 遵循 Effective Go 和 Go Code Review Comments

### 代码质量
- **可测试性**：通过接口解耦依赖，便于 Mock
- **高覆盖率**：使用 gotests 自动生成测试用例
- **Code Review**：建立代码审查机制

### 编程哲学
- **面向接口编程**：依赖抽象而非具体实现
- **面向对象**：通过组合实现继承，通过接口实现多态

### 软件设计
- **设计模式**：根据场景选用 GOF 设计模式
- **SOLID 原则**：单一职责、开闭、里氏替换、接口隔离、依赖倒置

---

## 1. Generator Architecture Approach

### Decision: Option 2 - Executor Interface Abstraction

**Rationale**:
- Current generator (`generator.go`) is tightly coupled to Maven
- Need clean separation to support multiple template types
- Interface-based approach allows testing and future extensibility

**Alternatives Considered**:
1. **Minimal changes (parallel struct)**: Quick but duplicates code patterns, no proper abstraction
2. **Executor interface** ✅: Clean separation, testable, follows Go idioms
3. **Plugin architecture**: Over-engineered for current needs (only 2 template types)

**Implementation**:
```go
// internal/generator/executor.go
type Executor interface {
    Validate(ctx context.Context) error
    Execute(ctx context.Context, params *ExecuteParams) error
}
```

---

## 2. Go Project Structure

### Decision: Standard Go Project Layout (cmd/internal/pkg)

**Rationale**:
- Follows widely-adopted community conventions
- Clean architecture principles built-in
- Familiar to Go developers (Art. 3 compliance)

**Alternatives Considered**:
1. **Flat structure**: Too simple, doesn't scale for services
2. **cmd/internal/pkg** ✅: Standard, scalable, clean architecture compatible
3. **Hexagonal/ports-adapters**: Good but adds complexity, can evolve from standard layout

**Generated Structure**:
```
my-api/
├── cmd/                          # 组件入口目录
│   └── my-api/                   # 主程序入口（目录名与artifactId一致）
│       └── main.go               # Application entry point
├── internal/                     # 私有应用代码（不可被外部导入）
│   ├── my-api/                   # 应用实现代码
│   │   ├── config/               # 配置加载
│   │   │   └── config.go
│   │   ├── handler/              # HTTP 处理器
│   │   │   └── health.go
│   │   ├── service/              # 业务逻辑层
│   │   │   └── .gitkeep
│   │   └── store/                # 数据访问层
│   │       └── .gitkeep
│   └── pkg/                      # 内部共享包
│       ├── code/                 # 业务错误码
│       │   └── code.go
│       └── middleware/           # HTTP 中间件
│           └── .gitkeep
├── pkg/                          # 公共包（可被外部导入）
│   └── version/                  # 版本信息
│       └── version.go
├── configs/                      # 配置文件模板
│   └── config.yaml.example
├── scripts/                      # 脚本目录
│   └── make-rules/
│       └── common.mk
├── build/                        # 构建相关
│   └── docker/
│       └── Dockerfile
├── api/                          # API 定义文件
│   └── openapi/
│       └── .gitkeep
├── docs/                         # 文档目录
│   └── README.md
├── test/                         # 测试目录
│   └── testdata/
│       └── .gitkeep
├── go.mod
├── go.sum
├── Makefile
├── README.md
├── .gitignore
└── LICENSE
```

---

## 3. Template File Processing

### Decision: Go text/template with embed.FS

**Rationale**:
- Standard library, no external dependencies
- Familiar Go template syntax
- embed.FS allows bundling templates in binary

**Alternatives Considered**:
1. **text/template + embed.FS** ✅: Standard, efficient, single binary
2. **External template files**: Requires runtime file access, harder to distribute
3. **Code generation (go generate)**: More complex, overkill for file templating

**Template Variables**:
```go
type TemplateData struct {
    ModuleName  string // e.g., "github.com/example/my-service"
    ProjectName string // e.g., "my-service"
    Version     string // e.g., "0.1.0"
}
```

---

## 4. Go Module Path Validation

### Decision: Regex validation matching Go module conventions

**Rationale**:
- Go module paths have specific rules
- Must start with domain, allow nested paths
- No uppercase in module paths (convention)

**Pattern**:
```go
// GoModulePattern validates Go module paths
// Valid: github.com/user/project, example.com/pkg/v2
// Invalid: My-Project, /invalid/path
var GoModulePattern = regexp.MustCompile(`^[a-z0-9][a-z0-9.-]*(/[a-z0-9][a-z0-9._-]*)*$`)
```

---

## 5. CLI Parameter Handling

### Decision: Add `-m/--module` flag for Go templates

**Rationale**:
- Go templates need module path (different from Java groupId)
- Reuse artifactId pattern for project name
- Version reusable across template types

**New Flags**:
```
-m, --module    Go module path (required for Go templates)
```

**Wizard Flow for Go**:
1. Select template → go-service
2. Enter project name (artifactId equivalent)
3. Enter module path (Go-specific)
4. Enter version
5. Confirm

---

## 6. Go Toolchain Detection

### Decision: Check `go version` command availability

**Rationale**:
- Simple, reliable check
- Provides version info for error messages
- Consistent with Maven check pattern

**Implementation**:
```go
func CheckGo() error {
    cmd := exec.Command("go", "version")
    output, err := cmd.Output()
    if err != nil {
        return fmt.Errorf("Go toolchain not found. Please install Go 1.21+")
    }
    // Optionally parse version from output
    return nil
}
```

---

## 7. Contract Tests for Go Template

### Decision: Shell-based contract tests similar to java-ddd

**Rationale**:
- Consistent with existing archetype contract tests
- Verifies structure, files, and commands
- Art. 4 compliance

**Test Cases**:
1. Directory structure exists (cmd/, internal/, go.mod)
2. `go build ./...` succeeds
3. `go test ./...` succeeds
4. Health check endpoint responds

---

## Summary of Decisions

| Area | Decision | Key Benefit |
|------|----------|-------------|
| Generator Architecture | Executor Interface | Clean abstraction, testable |
| Project Structure | cmd/internal/pkg | Standard Go conventions |
| Template Processing | text/template + embed.FS | Standard library, single binary |
| Module Validation | Regex pattern | Strict, follows Go rules |
| CLI Parameters | Add -m/--module flag | Go-specific, clear semantics |
| Go Detection | go version check | Simple, informative errors |
| Contract Tests | Shell-based | Consistent, comprehensive |
