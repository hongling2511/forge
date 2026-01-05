# Generated Project Contract: go-service

**Feature**: 003-go-service-template
**Version**: 1.0.0
**Reference**: 基于 project-layout 规范和 IAM 项目结构设计

## Directory Structure Contract

Given input:
- `artifactId`: `my-api`
- `module`: `github.com/example/my-api`
- `version`: `0.1.0`

The generated project MUST have this structure:

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
│   └── make-rules/               # Makefile 规则
│       └── common.mk
├── build/                        # 构建相关
│   └── docker/                   # Dockerfile
│       └── Dockerfile
├── api/                          # API 定义文件
│   └── openapi/
│       └── .gitkeep
├── docs/                         # 文档目录
│   └── README.md
├── test/                         # 测试目录
│   └── testdata/
│       └── .gitkeep
├── go.mod                        # Go module definition
├── go.sum                        # Dependencies checksum (empty initially)
├── Makefile                      # Build automation
├── README.md                     # Project documentation
├── .gitignore                    # Git ignore rules
└── LICENSE                       # License file
```

---

## Directory Purposes

| Directory | Purpose | Go Import Protection |
|-----------|---------|---------------------|
| `cmd/` | 组件 main 函数，每个组件一个子目录 | 可导入 |
| `internal/` | 私有应用代码，仅本项目可用 | **受保护** |
| `internal/{app}/` | 具体应用实现 | 受保护 |
| `internal/pkg/` | 项目内共享包 | 受保护 |
| `pkg/` | 公共库，可被其他项目导入 | 可导入 |
| `configs/` | 配置模板，不含敏感信息 | N/A |
| `scripts/` | 构建、安装脚本 | N/A |
| `build/` | CI/Docker 配置 | N/A |
| `api/` | API 定义文件 (OpenAPI/Swagger) | N/A |
| `docs/` | 项目文档 | N/A |
| `test/` | 测试数据和集成测试 | N/A |

---

## File Content Contracts

### go.mod

```
module github.com/example/my-api

go 1.21
```

### cmd/my-api/main.go

```go
package main

import (
	"log"
	"net/http"

	"github.com/example/my-api/internal/my-api/config"
	"github.com/example/my-api/internal/my-api/handler"
)

func main() {
	cfg := config.Load()

	mux := http.NewServeMux()
	mux.HandleFunc("/health", handler.Health)

	log.Printf("Server starting on :%s", cfg.Port)
	if err := http.ListenAndServe(":"+cfg.Port, mux); err != nil {
		log.Fatal(err)
	}
}
```

### internal/my-api/config/config.go

```go
package config

import "os"

// Config holds application configuration
type Config struct {
	Port string
}

// Load loads configuration from environment
func Load() *Config {
	port := os.Getenv("PORT")
	if port == "" {
		port = "8080"
	}
	return &Config{Port: port}
}
```

### internal/my-api/handler/health.go

```go
package handler

import (
	"encoding/json"
	"net/http"
)

// HealthResponse represents health check response
type HealthResponse struct {
	Status string `json:"status"`
}

// Health handles health check requests
func Health(w http.ResponseWriter, r *http.Request) {
	w.Header().Set("Content-Type", "application/json")
	w.WriteHeader(http.StatusOK)
	json.NewEncoder(w).Encode(HealthResponse{Status: "ok"})
}
```

### internal/pkg/code/code.go

```go
package code

// Common error codes
const (
	OK          = 0
	ErrUnknown  = 1
	ErrInternal = 2
)

// Message returns error message for code
func Message(code int) string {
	switch code {
	case OK:
		return "success"
	case ErrUnknown:
		return "unknown error"
	case ErrInternal:
		return "internal error"
	default:
		return "unknown"
	}
}
```

### pkg/version/version.go

```go
package version

var (
	// Version is the current version (set by ldflags)
	Version = "0.1.0"
	// GitCommit is the git commit hash (set by ldflags)
	GitCommit = "unknown"
	// BuildTime is the build timestamp (set by ldflags)
	BuildTime = "unknown"
)

// Info returns version information
func Info() string {
	return Version + " (" + GitCommit + ") built " + BuildTime
}
```

### Makefile

```makefile
.PHONY: all build run test clean lint fmt help

# Build variables
BINARY_NAME := my-api
VERSION := 0.1.0
GIT_COMMIT := $(shell git rev-parse --short HEAD 2>/dev/null || echo "unknown")
BUILD_TIME := $(shell date -u '+%Y-%m-%d_%H:%M:%S')
LDFLAGS := -ldflags "-X github.com/example/my-api/pkg/version.Version=$(VERSION) \
	-X github.com/example/my-api/pkg/version.GitCommit=$(GIT_COMMIT) \
	-X github.com/example/my-api/pkg/version.BuildTime=$(BUILD_TIME)"

all: fmt lint test build

build:
	@echo "Building..."
	go build $(LDFLAGS) -o bin/$(BINARY_NAME) ./cmd/$(BINARY_NAME)

run:
	go run ./cmd/$(BINARY_NAME)

test:
	go test -v -race -cover ./...

clean:
	rm -rf bin/
	go clean

lint:
	@if command -v golangci-lint > /dev/null; then \
		golangci-lint run ./...; \
	else \
		echo "golangci-lint not installed, skipping"; \
	fi

fmt:
	go fmt ./...
	@echo "Code formatted"

help:
	@echo "Available targets:"
	@echo "  all    - Format, lint, test, and build"
	@echo "  build  - Build the binary"
	@echo "  run    - Run the application"
	@echo "  test   - Run tests"
	@echo "  clean  - Clean build artifacts"
	@echo "  lint   - Run linter"
	@echo "  fmt    - Format code"
```

### configs/config.yaml.example

```yaml
# Application Configuration
# Copy to config.yaml and modify as needed

server:
  port: ${PORT:-8080}

# Add more configuration as needed
```

### build/docker/Dockerfile

```dockerfile
# Build stage
FROM golang:1.21-alpine AS builder

WORKDIR /app
COPY go.mod go.sum ./
RUN go mod download

COPY . .
RUN CGO_ENABLED=0 GOOS=linux go build -o /bin/my-api ./cmd/my-api

# Runtime stage
FROM alpine:3.19

RUN apk --no-cache add ca-certificates
COPY --from=builder /bin/my-api /bin/my-api

EXPOSE 8080
ENTRYPOINT ["/bin/my-api"]
```

### .gitignore

```
# Binaries
bin/
*.exe
*.exe~
*.dll
*.so
*.dylib

# Test
*.test
coverage.out

# IDE
.idea/
.vscode/
*.swp
*.swo

# OS
.DS_Store
Thumbs.db

# Local config
config.yaml
.env
```

### README.md

```markdown
# my-api

Generated with forge CLI using go-service template v1.0.0

## Quick Start

```bash
# Run the server
make run

# Build binary
make build

# Run tests
make test

# Run all checks
make all
```

## Project Structure

```
├── cmd/my-api/      - Application entry point
├── internal/        - Private application code
│   ├── my-api/      - Main application
│   │   ├── config/  - Configuration
│   │   ├── handler/ - HTTP handlers
│   │   ├── service/ - Business logic
│   │   └── store/   - Data access
│   └── pkg/         - Internal shared packages
├── pkg/             - Public packages
├── configs/         - Configuration templates
├── scripts/         - Build scripts
├── build/           - CI/Docker files
├── api/             - API definitions
├── docs/            - Documentation
└── test/            - Test data
```

## Development

### Prerequisites

- Go 1.21+
- Make

### Building

```bash
make build
# Binary will be in bin/my-api
```

### Docker

```bash
docker build -f build/docker/Dockerfile -t my-api .
docker run -p 8080:8080 my-api
```
```

---

## Build Contract

The generated project MUST pass these commands:

```bash
# Must compile without errors
go build ./...

# Must pass tests
go test ./...

# Must format correctly (no changes)
go fmt ./...

# Must pass vet
go vet ./...

# Make targets must work
make build
make test
```

---

## Runtime Contract

When started, the server MUST:

1. Listen on port 8080 (or PORT env var)
2. Respond to GET /health with:
   - Status: 200 OK
   - Content-Type: application/json
   - Body: `{"status":"ok"}`

---

## Determinism Contract (Art. 6)

Given identical inputs (template version + parameters), output MUST be byte-for-byte identical.

```bash
# Verify determinism
forge new -t go-service -a test1 -m github.com/test/test1 -o /tmp/gen1
forge new -t go-service -a test1 -m github.com/test/test1 -o /tmp/gen2
diff -r /tmp/gen1/test1 /tmp/gen2/test1  # Should be empty
```
