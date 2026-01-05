# Quickstart: Go Service Template

**Feature**: 003-go-service-template
**Date**: 2026-01-05

## Prerequisites

- Go 1.21 or later
- Make (optional, for build automation)
- Docker (optional, for containerized builds)

## Quick Start

### 1. Create a New Project

```bash
# Using CLI flags
forge new -t go-service -a my-api -m github.com/yourname/my-api

# Or with interactive wizard
forge new --interactive
```

### 2. Navigate to Project

```bash
cd my-api
```

### 3. Initialize Dependencies

```bash
go mod tidy
```

### 4. Run the Application

```bash
# Using Make
make run

# Or directly with Go
go run ./cmd/my-api
```

### 5. Verify It Works

```bash
# In another terminal
curl http://localhost:8080/health
# Expected: {"status":"ok"}
```

## Project Commands

| Command | Description |
|---------|-------------|
| `make run` | Run the application |
| `make build` | Build binary to `bin/` |
| `make test` | Run all tests |
| `make lint` | Run linter (requires golangci-lint) |
| `make fmt` | Format code |
| `make clean` | Remove build artifacts |
| `make all` | Format, lint, test, and build |

## Directory Structure Overview

```
my-api/
├── cmd/                          # 组件入口目录
│   └── my-api/                   # 主程序入口
│       └── main.go
├── internal/                     # 私有应用代码
│   ├── my-api/                   # 应用实现代码
│   │   ├── config/               # 配置加载
│   │   ├── handler/              # HTTP 处理器
│   │   ├── service/              # 业务逻辑层
│   │   └── store/                # 数据访问层
│   └── pkg/                      # 内部共享包
│       ├── code/                 # 业务错误码
│       └── middleware/           # HTTP 中间件
├── pkg/                          # 公共包（可被外部导入）
│   └── version/                  # 版本信息
├── configs/                      # 配置文件模板
├── scripts/                      # 脚本目录
├── build/docker/                 # Dockerfile
├── api/openapi/                  # API 定义文件
├── docs/                         # 文档目录
├── test/testdata/                # 测试数据
├── go.mod
├── go.sum
├── Makefile
├── README.md
├── .gitignore
└── LICENSE
```

## Configuration

The application reads configuration from environment variables:

| Variable | Default | Description |
|----------|---------|-------------|
| `PORT` | `8080` | HTTP server port |

Example:

```bash
PORT=3000 make run
```

## Docker

Build and run with Docker:

```bash
# Build image
docker build -f build/docker/Dockerfile -t my-api .

# Run container
docker run -p 8080:8080 my-api

# With custom port
docker run -p 3000:3000 -e PORT=3000 my-api
```

## Next Steps

1. **Add Business Logic**: Implement your domain logic in `internal/my-api/service/`
2. **Add Data Access**: Implement repositories in `internal/my-api/store/`
3. **Add API Endpoints**: Add handlers in `internal/my-api/handler/`
4. **Add Tests**: Write tests alongside your code (`*_test.go`)
5. **Add Shared Utilities**: Add internal shared code in `internal/pkg/`
6. **Add Public Packages**: Add reusable libraries in `pkg/`

## Troubleshooting

### "go: command not found"
Install Go from https://go.dev/dl/

### "make: command not found"
Use `go run ./cmd/my-api` directly, or install Make for your platform.

### Port already in use
Set a different port: `PORT=3001 make run`
