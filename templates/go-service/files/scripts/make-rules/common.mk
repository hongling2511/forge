# Common Makefile rules

.PHONY: tools
tools:
	@echo "Installing tools..."
	go install github.com/golangci/golangci-lint/cmd/golangci-lint@latest

.PHONY: mod
mod:
	go mod tidy
	go mod verify

.PHONY: vet
vet:
	go vet ./...
