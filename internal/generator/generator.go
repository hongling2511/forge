package generator

import (
	"context"
	"fmt"

	"github.com/hongling2511/forge/internal/output"
	"github.com/hongling2511/forge/internal/template"
)

// Generator orchestrates project generation
type Generator struct {
	template  *template.Template
	config    *Config
	printer   *output.Printer
	quiet     bool
}

// Config holds generation configuration
type Config struct {
	GroupID    string
	ArtifactID string
	Version    string
	Package    string
	OutputDir  string
}

// New creates a new generator
func New(tmpl *template.Template, cfg *Config) *Generator {
	return &Generator{
		template: tmpl,
		config:   cfg,
		printer:  output.Default(),
		quiet:    false,
	}
}

// SetQuiet enables quiet mode
func (g *Generator) SetQuiet(quiet bool) {
	g.quiet = quiet
	g.printer.SetQuiet(quiet)
}

// Generate creates a new project from the template
func (g *Generator) Generate(ctx context.Context) error {
	// Check Maven is available
	if err := CheckMaven(); err != nil {
		return err
	}

	// Resolve and validate output directory
	outputDir, err := ResolveOutputDir(g.config.OutputDir, g.config.ArtifactID)
	if err != nil {
		return err
	}

	// Set package to groupId if not specified
	pkg := g.config.Package
	if pkg == "" {
		pkg = g.config.GroupID
	}

	g.printer.Printf("Creating project '%s' from template '%s'...\n", g.config.ArtifactID, g.template.Name)
	g.printer.Println("")

	// Install archetype
	g.printer.Printf("Installing archetype from %s...\n", g.template.Path)

	maven := NewMaven(g.template.Path)
	maven.SetQuiet(g.quiet)

	if err := maven.Install(ctx, g.template.Path); err != nil {
		return fmt.Errorf("failed to install archetype: %w", err)
	}

	// Generate project
	params := &GenerateParams{
		ArchetypeGroupID:    g.template.Archetype.GroupID,
		ArchetypeArtifactID: g.template.Archetype.ArtifactID,
		ArchetypeVersion:    g.template.Archetype.Version,
		GroupID:             g.config.GroupID,
		ArtifactID:          g.config.ArtifactID,
		Version:             g.config.Version,
		Package:             pkg,
		OutputDir:           outputDir,
	}

	if err := maven.Generate(ctx, params); err != nil {
		return fmt.Errorf("failed to generate project: %w", err)
	}

	g.printer.ProjectCreated(g.config.ArtifactID, g.config.Version)

	return nil
}
