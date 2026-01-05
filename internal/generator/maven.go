package generator

import (
	"bytes"
	"context"
	"fmt"
	"os"
	"os/exec"
	"path/filepath"
	"strings"
)

// Maven wraps Maven command execution
type Maven struct {
	workDir string
	quiet   bool
}

// NewMaven creates a new Maven wrapper
func NewMaven(workDir string) *Maven {
	return &Maven{
		workDir: workDir,
		quiet:   false,
	}
}

// SetQuiet enables quiet mode for Maven output
func (m *Maven) SetQuiet(quiet bool) {
	m.quiet = quiet
}

// Install runs mvn install in the specified directory
func (m *Maven) Install(ctx context.Context, dir string) error {
	// Skip tests and integration tests to avoid running archetype IT during install
	args := []string{"install", "-DskipTests", "-DskipITs", "-q"}

	cmd := exec.CommandContext(ctx, "mvn", args...)
	cmd.Dir = dir

	var stderr bytes.Buffer
	cmd.Stderr = &stderr

	if !m.quiet {
		cmd.Stdout = os.Stdout
	}

	if err := cmd.Run(); err != nil {
		return fmt.Errorf("maven install failed: %s", strings.TrimSpace(stderr.String()))
	}

	return nil
}

// Generate runs mvn archetype:generate with the specified parameters
func (m *Maven) Generate(ctx context.Context, params *GenerateParams) error {
	args := []string{
		"archetype:generate",
		"-q", // Quiet mode to reduce output noise
		fmt.Sprintf("-DarchetypeGroupId=%s", params.ArchetypeGroupID),
		fmt.Sprintf("-DarchetypeArtifactId=%s", params.ArchetypeArtifactID),
		fmt.Sprintf("-DarchetypeVersion=%s", params.ArchetypeVersion),
		fmt.Sprintf("-DgroupId=%s", params.GroupID),
		fmt.Sprintf("-DartifactId=%s", params.ArtifactID),
		fmt.Sprintf("-Dversion=%s", params.Version),
		fmt.Sprintf("-Dpackage=%s", params.Package),
		fmt.Sprintf("-DforgeArchetypeVersion=%s", params.ArchetypeVersion),
		"-DforgeTemplateVersion=1.0.0",
		"-DinteractiveMode=false",
	}

	if params.OutputDir != "" {
		args = append(args, fmt.Sprintf("-DoutputDirectory=%s", params.OutputDir))
	}

	cmd := exec.CommandContext(ctx, "mvn", args...)
	cmd.Dir = m.workDir

	var stdout, stderr bytes.Buffer
	cmd.Stdout = &stdout
	cmd.Stderr = &stderr

	if err := cmd.Run(); err != nil {
		errMsg := strings.TrimSpace(stderr.String())
		if errMsg == "" {
			errMsg = strings.TrimSpace(stdout.String())
		}
		return fmt.Errorf("archetype generation failed: %s", errMsg)
	}

	return nil
}

// GenerateParams holds parameters for archetype generation
type GenerateParams struct {
	ArchetypeGroupID    string
	ArchetypeArtifactID string
	ArchetypeVersion    string
	GroupID             string
	ArtifactID          string
	Version             string
	Package             string
	OutputDir           string
}

// CheckMaven verifies Maven is available
func CheckMaven() error {
	cmd := exec.Command("mvn", "-version")
	if err := cmd.Run(); err != nil {
		return fmt.Errorf("Maven is not installed or not in PATH")
	}
	return nil
}

// ResolveOutputDir resolves and validates the output directory
func ResolveOutputDir(outputDir, artifactID string) (string, error) {
	// Resolve to absolute path
	absDir, err := filepath.Abs(outputDir)
	if err != nil {
		return "", fmt.Errorf("failed to resolve output directory: %w", err)
	}

	// Create directory if it doesn't exist
	if err := os.MkdirAll(absDir, 0755); err != nil {
		return "", fmt.Errorf("failed to create output directory: %w", err)
	}

	// Check if target directory already exists and is not empty
	targetDir := filepath.Join(absDir, artifactID)
	if info, err := os.Stat(targetDir); err == nil && info.IsDir() {
		entries, _ := os.ReadDir(targetDir)
		if len(entries) > 0 {
			return "", fmt.Errorf("target directory '%s' already exists and is not empty", targetDir)
		}
	}

	return absDir, nil
}
