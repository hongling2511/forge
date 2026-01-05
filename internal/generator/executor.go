package generator

import "context"

// Executor defines the interface for template generators
type Executor interface {
	// Validate checks if prerequisites are available (Go/Maven installed)
	Validate(ctx context.Context) error

	// Execute performs the project generation
	Execute(ctx context.Context, params *ExecuteParams) error
}

// ExecuteParams holds generation parameters for all template types
type ExecuteParams struct {
	OutputDir    string            // Target directory
	ArtifactID   string            // Project name
	Version      string            // Project version
	TemplateData map[string]string // Template-specific variables (module, groupId, etc.)
}
