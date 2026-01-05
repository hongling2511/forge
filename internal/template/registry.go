package template

import (
	"fmt"
	"os"
	"path/filepath"

	"github.com/hongling2511/forge/internal/config"
	"gopkg.in/yaml.v3"
)

// Registry manages template discovery and lookup
type Registry struct {
	templatesDir string
}

// NewRegistry creates a new template registry
func NewRegistry() *Registry {
	return &Registry{
		templatesDir: config.Get().TemplatesDir,
	}
}

// List returns all available templates
func (r *Registry) List() ([]*Template, error) {
	entries, err := os.ReadDir(r.templatesDir)
	if err != nil {
		return nil, fmt.Errorf("failed to read templates directory: %w", err)
	}

	var templates []*Template
	for _, entry := range entries {
		if !entry.IsDir() {
			continue
		}

		tmpl, err := r.loadTemplate(entry.Name())
		if err != nil {
			// Skip invalid templates
			continue
		}
		templates = append(templates, tmpl)
	}

	return templates, nil
}

// Get returns a template by name
func (r *Registry) Get(name string) (*Template, error) {
	tmpl, err := r.loadTemplate(name)
	if err != nil {
		return nil, err
	}
	return tmpl, nil
}

// Exists checks if a template exists
func (r *Registry) Exists(name string) bool {
	templateDir := filepath.Join(r.templatesDir, name)
	templateYAML := filepath.Join(templateDir, "template.yaml")

	if _, err := os.Stat(templateDir); os.IsNotExist(err) {
		return false
	}
	if _, err := os.Stat(templateYAML); os.IsNotExist(err) {
		return false
	}
	return true
}

// ListNames returns just the names of available templates
func (r *Registry) ListNames() ([]string, error) {
	templates, err := r.List()
	if err != nil {
		return nil, err
	}

	names := make([]string, len(templates))
	for i, t := range templates {
		names[i] = t.Name
	}
	return names, nil
}

// loadTemplate loads a template from disk
func (r *Registry) loadTemplate(name string) (*Template, error) {
	templateDir := filepath.Join(r.templatesDir, name)
	templateYAML := filepath.Join(templateDir, "template.yaml")

	// Check directory exists
	if _, err := os.Stat(templateDir); os.IsNotExist(err) {
		return nil, &TemplateNotFoundError{Name: name, Available: r.getAvailableNames()}
	}

	// Check template.yaml exists
	if _, err := os.Stat(templateYAML); os.IsNotExist(err) {
		return nil, fmt.Errorf("template '%s' is invalid (missing template.yaml)", name)
	}

	// Read and parse template.yaml
	data, err := os.ReadFile(templateYAML)
	if err != nil {
		return nil, fmt.Errorf("failed to read template.yaml: %w", err)
	}

	var tmpl Template
	if err := yaml.Unmarshal(data, &tmpl); err != nil {
		return nil, fmt.Errorf("failed to parse template.yaml: %w", err)
	}

	tmpl.Path = templateDir
	return &tmpl, nil
}

// getAvailableNames returns names of available templates for error messages
func (r *Registry) getAvailableNames() []string {
	entries, err := os.ReadDir(r.templatesDir)
	if err != nil {
		return nil
	}

	var names []string
	for _, entry := range entries {
		if entry.IsDir() {
			templateYAML := filepath.Join(r.templatesDir, entry.Name(), "template.yaml")
			if _, err := os.Stat(templateYAML); err == nil {
				names = append(names, entry.Name())
			}
		}
	}
	return names
}

// TemplateNotFoundError indicates a template was not found
type TemplateNotFoundError struct {
	Name      string
	Available []string
}

func (e *TemplateNotFoundError) Error() string {
	return fmt.Sprintf("template '%s' not found", e.Name)
}
