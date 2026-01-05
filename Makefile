VERSION ?= $(shell git describe --tags --always --dirty 2>/dev/null || echo "dev")
COMMIT ?= $(shell git rev-parse --short HEAD 2>/dev/null || echo "unknown")
BUILD_DATE ?= $(shell date -u +"%Y-%m-%dT%H:%M:%SZ")
LDFLAGS := -ldflags "-s -w \
	-X github.com/hongling2511/forge/internal/config.Version=$(VERSION) \
	-X github.com/hongling2511/forge/internal/config.Commit=$(COMMIT) \
	-X github.com/hongling2511/forge/internal/config.BuildDate=$(BUILD_DATE)"

.PHONY: build build-all test test-integration clean install lint fmt

# Build for current platform
build:
	go build $(LDFLAGS) -o bin/forge ./cmd/forge

# Cross-compile for all platforms
build-all: clean
	@mkdir -p dist
	GOOS=linux GOARCH=amd64 go build $(LDFLAGS) -o dist/forge-linux-amd64 ./cmd/forge
	GOOS=linux GOARCH=arm64 go build $(LDFLAGS) -o dist/forge-linux-arm64 ./cmd/forge
	GOOS=darwin GOARCH=amd64 go build $(LDFLAGS) -o dist/forge-darwin-amd64 ./cmd/forge
	GOOS=darwin GOARCH=arm64 go build $(LDFLAGS) -o dist/forge-darwin-arm64 ./cmd/forge
	GOOS=windows GOARCH=amd64 go build $(LDFLAGS) -o dist/forge-windows-amd64.exe ./cmd/forge

# Run tests
test:
	go test -v -race ./...

# Run integration tests
test-integration:
	go test -v -tags=integration ./...

# Clean build artifacts
clean:
	rm -rf bin/ dist/

# Install to /usr/local/bin
install: build
	cp bin/forge /usr/local/bin/

# Run linter
lint:
	golangci-lint run ./...

# Format code
fmt:
	go fmt ./...

# Tidy dependencies
tidy:
	go mod tidy

# Run the CLI
run: build
	./bin/forge $(ARGS)

# Development helpers
dev: fmt lint test build

# Show help
help:
	@echo "Forge CLI - Available targets:"
	@echo ""
	@echo "  build          Build for current platform"
	@echo "  build-all      Cross-compile for all platforms"
	@echo "  test           Run unit tests"
	@echo "  test-integration Run integration tests"
	@echo "  clean          Clean build artifacts"
	@echo "  install        Install to /usr/local/bin"
	@echo "  lint           Run linter"
	@echo "  fmt            Format code"
	@echo "  tidy           Tidy dependencies"
	@echo "  run            Build and run CLI"
	@echo "  dev            Format, lint, test, and build"
	@echo ""
	@echo "Variables:"
	@echo "  VERSION=$(VERSION)"
	@echo "  COMMIT=$(COMMIT)"
