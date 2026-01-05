package interactive

import (
	"errors"
	"fmt"
	"strings"

	"github.com/AlecAivazis/survey/v2"
	"github.com/hongling2511/forge/internal/template"
	"github.com/hongling2511/forge/internal/validation"
)

// WizardConfig holds wizard configuration and results
type WizardConfig struct {
	Template   string
	GroupID    string
	ArtifactID string
	Version    string
	Package    string
	OutputDir  string
}

// Wizard handles interactive project setup
type Wizard struct {
	registry *template.Registry
	defaults WizardConfig
}

// NewWizard creates a new wizard
func NewWizard(registry *template.Registry) *Wizard {
	return &Wizard{
		registry: registry,
		defaults: WizardConfig{
			Template:  "java-ddd",
			Version:   "1.0.0-SNAPSHOT",
			OutputDir: ".",
		},
	}
}

// SetDefaults sets default values for the wizard
func (w *Wizard) SetDefaults(cfg WizardConfig) {
	if cfg.Template != "" {
		w.defaults.Template = cfg.Template
	}
	if cfg.GroupID != "" {
		w.defaults.GroupID = cfg.GroupID
	}
	if cfg.ArtifactID != "" {
		w.defaults.ArtifactID = cfg.ArtifactID
	}
	if cfg.Version != "" {
		w.defaults.Version = cfg.Version
	}
	if cfg.Package != "" {
		w.defaults.Package = cfg.Package
	}
	if cfg.OutputDir != "" {
		w.defaults.OutputDir = cfg.OutputDir
	}
}

// Run executes the wizard and returns the configuration
func (w *Wizard) Run() (*WizardConfig, error) {
	result := &WizardConfig{}

	// Step 1: Select template
	if err := w.promptTemplate(result); err != nil {
		return nil, err
	}

	// Get template to determine required parameters
	tmpl, err := w.registry.Get(result.Template)
	if err != nil {
		return nil, err
	}

	// Step 2: Artifact ID (project name)
	if err := w.promptArtifactID(result); err != nil {
		return nil, err
	}

	// Step 3: Group ID (for Java templates)
	if tmpl.IsJavaTemplate() {
		if err := w.promptGroupID(result); err != nil {
			return nil, err
		}
	}

	// Step 4: Version
	if err := w.promptVersion(result); err != nil {
		return nil, err
	}

	// Step 5: Package (for Java templates)
	if tmpl.IsJavaTemplate() {
		if err := w.promptPackage(result); err != nil {
			return nil, err
		}
	}

	// Step 6: Output directory
	if err := w.promptOutputDir(result); err != nil {
		return nil, err
	}

	// Step 7: Confirmation
	if err := w.promptConfirmation(result, tmpl); err != nil {
		return nil, err
	}

	return result, nil
}

func (w *Wizard) promptTemplate(result *WizardConfig) error {
	templates, err := w.registry.List()
	if err != nil {
		return err
	}

	if len(templates) == 0 {
		return errors.New("no templates available")
	}

	// Build options
	options := make([]string, len(templates))
	defaultIndex := 0
	for i, t := range templates {
		options[i] = fmt.Sprintf("%s - %s", t.Name, t.Description)
		if t.Name == w.defaults.Template {
			defaultIndex = i
		}
	}

	var selection string
	prompt := &survey.Select{
		Message: "Select a template:",
		Options: options,
		Default: options[defaultIndex],
	}

	if err := survey.AskOne(prompt, &selection); err != nil {
		return err
	}

	// Extract template name
	result.Template = strings.Split(selection, " - ")[0]
	return nil
}

func (w *Wizard) promptArtifactID(result *WizardConfig) error {
	prompt := &survey.Input{
		Message: "Enter project name (artifact-id):",
		Default: w.defaults.ArtifactID,
		Help:    "Lowercase letters, numbers, and hyphens. Must start with a letter.",
	}

	validator := func(val interface{}) error {
		str, ok := val.(string)
		if !ok {
			return errors.New("invalid input")
		}
		return validation.ValidateArtifactID(str)
	}

	return survey.AskOne(prompt, &result.ArtifactID, survey.WithValidator(validator))
}

func (w *Wizard) promptGroupID(result *WizardConfig) error {
	prompt := &survey.Input{
		Message: "Enter group ID:",
		Default: w.defaults.GroupID,
		Help:    "Maven groupId (e.g., com.example)",
	}

	validator := func(val interface{}) error {
		str, ok := val.(string)
		if !ok {
			return errors.New("invalid input")
		}
		return validation.ValidateGroupID(str)
	}

	return survey.AskOne(prompt, &result.GroupID, survey.WithValidator(validator))
}

func (w *Wizard) promptVersion(result *WizardConfig) error {
	prompt := &survey.Input{
		Message: "Enter version:",
		Default: w.defaults.Version,
		Help:    "SemVer format (e.g., 1.0.0-SNAPSHOT)",
	}

	validator := func(val interface{}) error {
		str, ok := val.(string)
		if !ok {
			return errors.New("invalid input")
		}
		if str == "" {
			return nil // Use default
		}
		return validation.ValidateVersion(str)
	}

	if err := survey.AskOne(prompt, &result.Version, survey.WithValidator(validator)); err != nil {
		return err
	}

	// Use default if empty
	if result.Version == "" {
		result.Version = w.defaults.Version
	}

	return nil
}

func (w *Wizard) promptPackage(result *WizardConfig) error {
	defaultPkg := w.defaults.Package
	if defaultPkg == "" {
		defaultPkg = result.GroupID // Default to groupId
	}

	prompt := &survey.Input{
		Message: "Enter package:",
		Default: defaultPkg,
		Help:    "Java package name (defaults to groupId)",
	}

	validator := func(val interface{}) error {
		str, ok := val.(string)
		if !ok {
			return errors.New("invalid input")
		}
		if str == "" {
			return nil // Use default
		}
		return validation.ValidatePackage(str)
	}

	if err := survey.AskOne(prompt, &result.Package, survey.WithValidator(validator)); err != nil {
		return err
	}

	// Use default if empty
	if result.Package == "" {
		result.Package = result.GroupID
	}

	return nil
}

func (w *Wizard) promptOutputDir(result *WizardConfig) error {
	prompt := &survey.Input{
		Message: "Enter output directory:",
		Default: w.defaults.OutputDir,
		Help:    "Directory where project will be created",
	}

	if err := survey.AskOne(prompt, &result.OutputDir); err != nil {
		return err
	}

	// Use default if empty
	if result.OutputDir == "" {
		result.OutputDir = w.defaults.OutputDir
	}

	return nil
}

func (w *Wizard) promptConfirmation(result *WizardConfig, tmpl *template.Template) error {
	fmt.Println()
	fmt.Println("Summary:")
	fmt.Printf("  Template:    %s\n", result.Template)
	fmt.Printf("  Project:     %s\n", result.ArtifactID)
	if tmpl.IsJavaTemplate() {
		fmt.Printf("  Group ID:    %s\n", result.GroupID)
	}
	fmt.Printf("  Version:     %s\n", result.Version)
	if tmpl.IsJavaTemplate() {
		fmt.Printf("  Package:     %s\n", result.Package)
	}
	fmt.Printf("  Output:      %s/%s\n", result.OutputDir, result.ArtifactID)
	fmt.Println()

	var confirm bool
	prompt := &survey.Confirm{
		Message: "Proceed with project creation?",
		Default: true,
	}

	if err := survey.AskOne(prompt, &confirm); err != nil {
		return err
	}

	if !confirm {
		return errors.New("project creation cancelled")
	}

	return nil
}
