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

## [Unreleased]

### Planned

- Additional templates (e.g., `kotlin-ddd`, `go-service`)
- Interactive mode with prompts
- Template versioning and updates
