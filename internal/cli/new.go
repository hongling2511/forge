package cli

import (
	"os"

	"github.com/hongling2511/forge/internal/generator"
	"github.com/hongling2511/forge/internal/interactive"
	"github.com/hongling2511/forge/internal/output"
	"github.com/hongling2511/forge/internal/template"
	"github.com/hongling2511/forge/internal/validation"
	"github.com/spf13/cobra"
	"golang.org/x/term"
)

// NewConfig holds configuration for the new command
type NewConfig struct {
	Template    string
	GroupID     string
	ArtifactID  string
	Module      string // Go module path (for Go templates)
	Version     string
	Package     string
	OutputDir   string
	Interactive bool
}

var newConfig NewConfig

var newCmd = &cobra.Command{
	Use:   "new",
	Short: "Create a new project from template",
	Long: `Create a new project from a template.

The new command generates a project using Maven archetypes or Go templates.
It supports both command-line and interactive modes.

Examples:
  # Create a new Java DDD project
  forge new -g com.example -a my-service

  # Create a new Go service project
  forge new -t go-service -a my-api -m github.com/example/my-api

  # Create with all options
  forge new -t java-ddd -g com.example -a my-service -v 2.0.0 -p com.example.myservice

  # Create in specific directory
  forge new -g com.example -a my-service -o ./projects

  # Interactive mode (wizard)
  forge new --interactive
  forge new  # auto-triggers wizard if required params missing`,
	RunE: runNew,
}

func init() {
	rootCmd.AddCommand(newCmd)

	// Flags matching the Bash CLI exactly
	newCmd.Flags().StringVarP(&newConfig.Template, "template", "t", "java-ddd", "Template to use")
	newCmd.Flags().StringVarP(&newConfig.GroupID, "group-id", "g", "", "Maven groupId (required for Java templates)")
	newCmd.Flags().StringVarP(&newConfig.ArtifactID, "artifact-id", "a", "", "Project name (required)")
	newCmd.Flags().StringVarP(&newConfig.Module, "module", "m", "", "Go module path (required for Go templates)")
	newCmd.Flags().StringVarP(&newConfig.Version, "version", "v", "", "Project version")
	newCmd.Flags().StringVarP(&newConfig.Package, "package", "p", "", "Java package name (defaults to groupId)")
	newCmd.Flags().StringVarP(&newConfig.OutputDir, "output", "o", ".", "Output directory")
	newCmd.Flags().BoolVar(&newConfig.Interactive, "interactive", false, "Enable interactive mode (wizard)")
}

func runNew(cmd *cobra.Command, args []string) error {
	printer := output.Default()
	registry := template.NewRegistry()

	// Determine if we should run interactive mode
	shouldInteract := shouldRunInteractive(&newConfig, registry)

	if shouldInteract {
		// Run interactive wizard
		wizard := interactive.NewWizard(registry)

		// Pre-populate with any provided values
		wizard.SetDefaults(interactive.WizardConfig{
			Template:   newConfig.Template,
			GroupID:    newConfig.GroupID,
			ArtifactID: newConfig.ArtifactID,
			Module:     newConfig.Module,
			Version:    newConfig.Version,
			Package:    newConfig.Package,
			OutputDir:  newConfig.OutputDir,
		})

		result, err := wizard.Run()
		if err != nil {
			return err
		}

		// Update config with wizard results
		newConfig.Template = result.Template
		newConfig.GroupID = result.GroupID
		newConfig.ArtifactID = result.ArtifactID
		newConfig.Module = result.Module
		newConfig.Version = result.Version
		newConfig.Package = result.Package
		newConfig.OutputDir = result.OutputDir
	}

	// Get template
	tmpl, err := registry.Get(newConfig.Template)
	if err != nil {
		if notFound, ok := err.(*template.TemplateNotFoundError); ok {
			printer.Errorf("Template '%s' not found", notFound.Name)
			printer.Println("")
			printer.Println("Available templates:")
			for _, name := range notFound.Available {
				printer.Printf("  - %s\n", name)
			}
			return err
		}
		return err
	}

	// Set default version based on template type
	if newConfig.Version == "" {
		if tmpl.IsGoTemplate() {
			newConfig.Version = "0.1.0"
		} else {
			newConfig.Version = "1.0.0-SNAPSHOT"
		}
	}

	// Validate parameters based on template type
	var errors []error
	if tmpl.IsGoTemplate() {
		errors = validation.ValidateAllGo(
			newConfig.ArtifactID,
			newConfig.Module,
			newConfig.Version,
		)
	} else {
		errors = validation.ValidateAll(
			newConfig.ArtifactID,
			newConfig.GroupID,
			newConfig.Version,
			newConfig.Package,
			tmpl.IsJavaTemplate(),
		)
	}

	if len(errors) > 0 {
		printer.ValidationErrors(errors)
		return errors[0]
	}

	// Generate project
	gen := generator.New(tmpl, &generator.Config{
		GroupID:    newConfig.GroupID,
		ArtifactID: newConfig.ArtifactID,
		Module:     newConfig.Module,
		Version:    newConfig.Version,
		Package:    newConfig.Package,
		OutputDir:  newConfig.OutputDir,
	})
	gen.SetQuiet(quiet)

	return gen.Generate(cmd.Context())
}

// shouldRunInteractive determines if we should enter interactive mode
func shouldRunInteractive(cfg *NewConfig, registry *template.Registry) bool {
	// Explicit flag takes precedence
	if cfg.Interactive {
		return true
	}

	// Don't run in non-interactive terminals (pipes, CI)
	if !term.IsTerminal(int(os.Stdin.Fd())) {
		return false
	}

	// Check if required parameters are missing
	if cfg.ArtifactID == "" {
		return true
	}

	// Check template-specific requirements
	if tmpl, err := registry.Get(cfg.Template); err == nil {
		// For Java templates, check if groupId is missing
		if tmpl.IsJavaTemplate() && cfg.GroupID == "" {
			return true
		}
		// For Go templates, check if module is missing
		if tmpl.IsGoTemplate() && cfg.Module == "" {
			return true
		}
	}

	return false
}
