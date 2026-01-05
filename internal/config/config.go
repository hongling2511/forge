package config

import (
	"os"
	"path/filepath"
)

// Config holds the global configuration for forge
type Config struct {
	ForgeHome    string
	TemplatesDir string
	Version      string
}

// DefaultConfig returns the default configuration
func DefaultConfig() *Config {
	forgeHome := getForgeHome()
	return &Config{
		ForgeHome:    forgeHome,
		TemplatesDir: filepath.Join(forgeHome, "templates"),
		Version:      Version,
	}
}

// getForgeHome determines the FORGE_HOME directory
// Priority: FORGE_HOME env var > executable directory
func getForgeHome() string {
	// Check environment variable first
	if home := os.Getenv("FORGE_HOME"); home != "" {
		return home
	}

	// Use executable directory
	exe, err := os.Executable()
	if err != nil {
		// Fallback to current directory
		return "."
	}

	// Resolve symlinks
	exe, err = filepath.EvalSymlinks(exe)
	if err != nil {
		return filepath.Dir(exe)
	}

	// Go up from cmd/forge to project root
	dir := filepath.Dir(exe)

	// Check if we're in a typical Go build location (cmd/forge)
	if filepath.Base(filepath.Dir(dir)) == "cmd" {
		return filepath.Dir(filepath.Dir(dir))
	}

	// Check if templates dir exists at this level
	if _, err := os.Stat(filepath.Join(dir, "templates")); err == nil {
		return dir
	}

	// Go up one level and check
	parent := filepath.Dir(dir)
	if _, err := os.Stat(filepath.Join(parent, "templates")); err == nil {
		return parent
	}

	return dir
}

// Global config instance
var globalConfig *Config

// Get returns the global configuration
func Get() *Config {
	if globalConfig == nil {
		globalConfig = DefaultConfig()
	}
	return globalConfig
}
