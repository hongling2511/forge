package generator

import (
	"bytes"
	"context"
	"fmt"
	"io/fs"
	"os"
	"os/exec"
	"path/filepath"
	"strings"
	"text/template"
	"unicode"

	"github.com/hongling2511/forge/internal/output"
)

// templateFuncs provides custom functions for Go templates
var templateFuncs = template.FuncMap{
	"upper": strings.ToUpper,
	"lower": strings.ToLower,
	"title": strings.Title,
	"replace": strings.ReplaceAll,
	"contains": strings.Contains,
	"hasPrefix": strings.HasPrefix,
	"hasSuffix": strings.HasSuffix,
	"trimPrefix": strings.TrimPrefix,
	"trimSuffix": strings.TrimSuffix,
	"snakeCase": toSnakeCase,
	"kebabCase": toKebabCase,
	"camelCase": toCamelCase,
	"pascalCase": toPascalCase,
	"envPrefix": toEnvPrefix,
}

// toSnakeCase converts string to snake_case
func toSnakeCase(s string) string {
	var result []rune
	for i, r := range s {
		if unicode.IsUpper(r) {
			if i > 0 {
				result = append(result, '_')
			}
			result = append(result, unicode.ToLower(r))
		} else if r == '-' {
			result = append(result, '_')
		} else {
			result = append(result, r)
		}
	}
	return string(result)
}

// toKebabCase converts string to kebab-case
func toKebabCase(s string) string {
	var result []rune
	for i, r := range s {
		if unicode.IsUpper(r) {
			if i > 0 {
				result = append(result, '-')
			}
			result = append(result, unicode.ToLower(r))
		} else if r == '_' {
			result = append(result, '-')
		} else {
			result = append(result, r)
		}
	}
	return string(result)
}

// toCamelCase converts string to camelCase
func toCamelCase(s string) string {
	words := splitWords(s)
	for i, w := range words {
		if i == 0 {
			words[i] = strings.ToLower(w)
		} else {
			words[i] = strings.Title(strings.ToLower(w))
		}
	}
	return strings.Join(words, "")
}

// toPascalCase converts string to PascalCase
func toPascalCase(s string) string {
	words := splitWords(s)
	for i, w := range words {
		words[i] = strings.Title(strings.ToLower(w))
	}
	return strings.Join(words, "")
}

// splitWords splits a string into words by common delimiters
func splitWords(s string) []string {
	// Replace common delimiters with space
	s = strings.ReplaceAll(s, "-", " ")
	s = strings.ReplaceAll(s, "_", " ")

	// Split on uppercase letters
	var words []string
	var current []rune
	for _, r := range s {
		if r == ' ' {
			if len(current) > 0 {
				words = append(words, string(current))
				current = nil
			}
		} else if unicode.IsUpper(r) && len(current) > 0 {
			words = append(words, string(current))
			current = []rune{r}
		} else {
			current = append(current, r)
		}
	}
	if len(current) > 0 {
		words = append(words, string(current))
	}
	return words
}

// toEnvPrefix converts string to UPPER_SNAKE_CASE for environment variable prefix
// e.g., "my-api" -> "MY_API"
func toEnvPrefix(s string) string {
	s = strings.ReplaceAll(s, "-", "_")
	s = strings.ReplaceAll(s, ".", "_")
	return strings.ToUpper(s)
}

// TemplateData holds data passed to Go templates
type TemplateData struct {
	ModuleName  string // Go module path (e.g., github.com/example/my-service)
	ProjectName string // Directory/artifact name (e.g., my-service)
	Version     string // Project version (e.g., 0.1.0)
	GoVersion   string // Minimum Go version (e.g., 1.21)
}

// GoExecutor implements Executor for Go templates
type GoExecutor struct {
	templatePath string         // Path to template directory
	filesDir     string         // Subdirectory containing template files (default: "files")
	printer      *output.Printer
	quiet        bool
}

// NewGoExecutor creates a new Go template executor
func NewGoExecutor(templatePath, filesDir string) *GoExecutor {
	if filesDir == "" {
		filesDir = "files"
	}
	return &GoExecutor{
		templatePath: templatePath,
		filesDir:     filesDir,
		printer:      output.Default(),
		quiet:        false,
	}
}

// SetQuiet enables quiet mode
func (e *GoExecutor) SetQuiet(quiet bool) {
	e.quiet = quiet
	e.printer.SetQuiet(quiet)
}

// Validate checks if Go toolchain is available
func (e *GoExecutor) Validate(ctx context.Context) error {
	return CheckGo()
}

// Execute performs the Go project generation
func (e *GoExecutor) Execute(ctx context.Context, params *ExecuteParams) error {
	// Extract template data from params
	data := &TemplateData{
		ModuleName:  params.TemplateData["module"],
		ProjectName: params.ArtifactID,
		Version:     params.Version,
		GoVersion:   "1.21",
	}

	// Resolve output directory
	outputDir, err := ResolveOutputDir(params.OutputDir, params.ArtifactID)
	if err != nil {
		return err
	}

	// Create project directory
	projectDir := filepath.Join(outputDir, params.ArtifactID)
	if err := os.MkdirAll(projectDir, 0755); err != nil {
		return fmt.Errorf("failed to create project directory: %w", err)
	}

	// Process template files
	filesPath := filepath.Join(e.templatePath, e.filesDir)
	if err := e.processTemplateDir(filesPath, projectDir, data); err != nil {
		return fmt.Errorf("failed to process templates: %w", err)
	}

	return nil
}

// processTemplateDir walks the template directory and processes each file
func (e *GoExecutor) processTemplateDir(srcDir, dstDir string, data *TemplateData) error {
	return filepath.WalkDir(srcDir, func(path string, d fs.DirEntry, err error) error {
		if err != nil {
			return err
		}

		// Get relative path from source directory
		relPath, err := filepath.Rel(srcDir, path)
		if err != nil {
			return err
		}

		// Process path to replace placeholders
		dstPath := e.processPath(relPath, data)
		fullDstPath := filepath.Join(dstDir, dstPath)

		if d.IsDir() {
			// Create directory
			return os.MkdirAll(fullDstPath, 0755)
		}

		// Process file
		return e.processFile(path, fullDstPath, data)
	})
}

// processPath replaces placeholders in file/directory paths
func (e *GoExecutor) processPath(path string, data *TemplateData) string {
	// Remove .tmpl extension
	path = strings.TrimSuffix(path, ".tmpl")

	// Replace {{.ProjectName}} placeholder in paths
	path = strings.ReplaceAll(path, "{{.ProjectName}}", data.ProjectName)

	return path
}

// processFile processes a single template file
func (e *GoExecutor) processFile(srcPath, dstPath string, data *TemplateData) error {
	// Read source file
	content, err := os.ReadFile(srcPath)
	if err != nil {
		return fmt.Errorf("failed to read %s: %w", srcPath, err)
	}

	// Create destination directory
	if err := os.MkdirAll(filepath.Dir(dstPath), 0755); err != nil {
		return fmt.Errorf("failed to create directory: %w", err)
	}

	// Check if this is a template file
	if strings.HasSuffix(srcPath, ".tmpl") {
		// Parse and execute template with custom functions
		tmpl, err := template.New(filepath.Base(srcPath)).Funcs(templateFuncs).Parse(string(content))
		if err != nil {
			return fmt.Errorf("failed to parse template %s: %w", srcPath, err)
		}

		var buf bytes.Buffer
		if err := tmpl.Execute(&buf, data); err != nil {
			return fmt.Errorf("failed to execute template %s: %w", srcPath, err)
		}
		content = buf.Bytes()
	}

	// Write destination file
	if err := os.WriteFile(dstPath, content, 0644); err != nil {
		return fmt.Errorf("failed to write %s: %w", dstPath, err)
	}

	return nil
}

// CheckGo verifies Go is available and returns version info
func CheckGo() error {
	cmd := exec.Command("go", "version")
	_, err := cmd.Output()
	if err != nil {
		return &GoNotFoundError{}
	}
	return nil
}

// GoNotFoundError indicates Go toolchain is not installed
type GoNotFoundError struct{}

func (e *GoNotFoundError) Error() string {
	return "Go toolchain not found"
}
