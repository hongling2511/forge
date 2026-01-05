# Data Model: Go Service Template

**Feature**: 003-go-service-template
**Date**: 2026-01-05

## Overview

This document defines the data structures and entities for the Go service template feature.

---

## 1. Template Configuration Entities

### Template (Extended)

The existing Template struct in `internal/template/template.go` needs extension:

```go
type Template struct {
    Name        string          `yaml:"name"`
    Version     string          `yaml:"version"`
    Description string          `yaml:"description"`
    Type        string          `yaml:"type"`      // "maven-archetype" | "go-template"
    Archetype   ArchetypeConfig `yaml:"archetype"` // Maven-specific (optional)
    GoConfig    GoConfig        `yaml:"goConfig"`  // Go-specific (NEW)
    Parameters  ParameterConfig `yaml:"parameters"`
    Modules     []ModuleConfig  `yaml:"modules"`
    Stack       StackConfig     `yaml:"stack"`
    Path        string          `yaml:"-"`
}
```

### GoConfig (NEW)

Go-specific configuration stored in template.yaml:

```go
type GoConfig struct {
    MinGoVersion string `yaml:"minGoVersion"` // e.g., "1.21"
    FilesDir     string `yaml:"filesDir"`     // Template files directory, default "files"
}
```

### StackConfig (Extended)

```go
type StackConfig struct {
    Language         string `yaml:"language"`         // "java" | "go"
    JDK              string `yaml:"jdk"`              // Java only
    GoVersion        string `yaml:"goVersion"`        // Go only (NEW)
    Framework        string `yaml:"framework"`
    FrameworkVersion string `yaml:"frameworkVersion"`
    BuildTool        string `yaml:"buildTool"`        // "maven" | "go"
}
```

---

## 2. Generator Entities

### Executor Interface (NEW)

```go
// Executor defines the interface for template generators
type Executor interface {
    // Validate checks if prerequisites are available (Go/Maven installed)
    Validate(ctx context.Context) error

    // Execute performs the project generation
    Execute(ctx context.Context, params *ExecuteParams) error
}
```

### ExecuteParams (NEW)

```go
// ExecuteParams holds generation parameters
type ExecuteParams struct {
    OutputDir    string            // Target directory
    ArtifactID   string            // Project name
    Version      string            // Project version
    TemplateData map[string]string // Template-specific variables
}
```

### GoExecutor (NEW)

```go
type GoExecutor struct {
    templatePath string           // Path to template directory
    filesDir     string           // Subdirectory containing template files
    quiet        bool
}
```

### TemplateData (NEW)

Data passed to Go text/template:

```go
type TemplateData struct {
    ModuleName  string // Go module path
    ProjectName string // Directory/artifact name
    Version     string // Project version
    GoVersion   string // Minimum Go version
}
```

---

## 3. Validation Entities

### ValidationError (Existing - Extended)

```go
type ValidationError struct {
    Field   string // "artifact-id" | "group-id" | "module" | "version"
    Value   string
    Message string
    Help    string
}
```

### New Validation Pattern

```go
// GoModulePattern validates Go module paths
var GoModulePattern = regexp.MustCompile(`^[a-z0-9][a-z0-9.-]*(/[a-z0-9][a-z0-9._-]*)*$`)
```

---

## 4. CLI Configuration Entities

### NewConfig (Extended)

```go
type NewConfig struct {
    Template    string
    GroupID     string // Java only
    ArtifactID  string // Shared (project name)
    Module      string // Go only (NEW)
    Version     string // Shared
    Package     string // Java only
    OutputDir   string // Shared
    Interactive bool
}
```

### WizardConfig (Extended)

```go
type WizardConfig struct {
    Template   string
    GroupID    string // Java
    ArtifactID string // Shared
    Module     string // Go (NEW)
    Version    string // Shared
    Package    string // Java
    OutputDir  string // Shared
}
```

---

## 5. Generated Project Entities

### Directory Structure

```
{artifactId}/
├── cmd/                          # 组件入口目录
│   └── {artifactId}/             # 主程序入口
│       └── main.go
├── internal/                     # 私有应用代码
│   ├── {artifactId}/             # 应用实现代码
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
│   └── version/
│       └── version.go
├── configs/
│   └── config.yaml.example
├── scripts/
│   └── make-rules/
│       └── common.mk
├── build/
│   └── docker/
│       └── Dockerfile
├── api/
│   └── openapi/
│       └── .gitkeep
├── docs/
│   └── README.md
├── test/
│   └── testdata/
│       └── .gitkeep
├── go.mod
├── go.sum
├── Makefile
├── README.md
├── .gitignore
└── LICENSE
```

### go.mod Template

```
module {{.ModuleName}}

go {{.GoVersion}}
```

### main.go Template (Simplified)

```go
package main

import (
    "fmt"
    "net/http"
)

func main() {
    http.HandleFunc("/health", healthHandler)
    fmt.Println("Server starting on :8080")
    http.ListenAndServe(":8080", nil)
}

func healthHandler(w http.ResponseWriter, r *http.Request) {
    w.WriteHeader(http.StatusOK)
    w.Write([]byte(`{"status":"ok"}`))
}
```

---

## Entity Relationships

```
Template 1──1 GoConfig (when Type="go-template")
Template 1──1 ArchetypeConfig (when Type="maven-archetype")
Template 1──* ParameterDef
Template 1──1 StackConfig

Generator 1──1 Executor (interface)
    ├── MavenExecutor (implements)
    └── GoExecutor (implements)

GoExecutor ──> TemplateData ──> Generated Files
```

---

## Validation Rules

| Field | Pattern | Required When |
|-------|---------|---------------|
| artifactId | `^[a-z][a-z0-9-]*$` | Always |
| groupId | `^[a-z][a-z0-9]*(\.[a-z][a-z0-9]*)*$` | Java templates |
| module | `^[a-z0-9][a-z0-9.-]*(/[a-z0-9][a-z0-9._-]*)*$` | Go templates |
| version | `^[0-9]+\.[0-9]+\.[0-9]+(-[a-zA-Z0-9]+)?$` | Optional (has default) |
