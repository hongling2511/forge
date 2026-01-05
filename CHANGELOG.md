# Changelog

All notable changes to this project will be documented in this file.

The format is based on [Keep a Changelog](https://keepachangelog.com/en/1.0.0/),
and this project adheres to [Semantic Versioning](https://semver.org/spec/v2.0.0.html).

## [1.0.0] - 2026-01-05

### Added

- **Forge CLI** - Unified command-line interface for project generation
  - `forge new` - Create new projects from templates
  - `forge templates` - List available templates
  - `forge --help` - Show usage information
  - `forge --version` - Show version information

- **Java DDD Template** (`java-ddd`)
  - 5-module DDD architecture (domain, application, infrastructure, interface, bootstrap)
  - Spring Boot 3.2.x with Actuator health checks
  - Maven multi-module project structure
  - Configurable groupId, artifactId, version, and package

- **CLI Features**
  - Non-interactive batch mode for CI/CD environments
  - Parameter validation with clear error messages
  - Template discovery and listing
  - Exit codes: 0 for success, 1 for validation errors

- **Quality Assurance**
  - Contract tests for generated project validation
  - Reproducibility verification (identical inputs → identical outputs)
  - Version tracking in generated projects (`forge.archetype.version`, `forge.template.version`)

- **Documentation**
  - README with installation and usage instructions
  - Quickstart guide
  - Contract specifications

### Technical Details

- **Requirements**: JDK 17+, Maven 3.6+, Bash
- **Generated Project**: Spring Boot 3.2.1, Java 17
- **Health Check**: `/actuator/health` endpoint enabled by default

## [2.0.0] - 2026-01-05

### Changed

- **CLI Rewritten in Go** - Complete rewrite from Bash to Go using Cobra framework
  - Cross-platform support (Linux, macOS, Windows on amd64/arm64)
  - Faster execution and better error handling
  - Module path: `github.com/hongling2511/forge`

### Added

- **Interactive Mode** - Guided wizard when required parameters are missing
  - Template selection from available options
  - Step-by-step parameter input with validation
  - Confirmation summary before generation
  - Auto-triggers when `--artifact-id` or `--group-id` missing

- **Improved Output**
  - Colored success/error messages
  - Quiet Maven execution (no verbose build logs)
  - Clear next steps after project creation

- **Build System**
  - Makefile for build automation
  - Cross-platform compilation (`make build-all`)
  - Binary installation script (`scripts/install.sh`)

- **Testing**
  - Unit tests for validation package
  - Regex pattern tests for groupId, artifactId, version, package

### Technical Details

- **Go Version**: 1.21+
- **Dependencies**: Cobra, Survey, yaml.v3, color, spinner
- **Build Output**: Single static binary (~10MB)

## [Unreleased]

### Planned

- Shell completion (bash, zsh, fish)
- Additional templates (e.g., `kotlin-ddd`, `go-service`)
- Template versioning and updates
- Self-update command
