package cli

import (
	"github.com/hongling2511/forge/internal/output"
	"github.com/spf13/cobra"
)

var (
	// Global flags
	noColor bool
	quiet   bool
)

// rootCmd represents the base command
var rootCmd = &cobra.Command{
	Use:   "forge",
	Short: "Engineering scaffold CLI",
	Long: `Forge - Engineering scaffold CLI for generating project templates.

Forge helps you quickly scaffold new projects using predefined templates.
It wraps Maven Archetype Plugin with a simpler interface.

Examples:
  # Create a new DDD project
  forge new -g com.example -a my-service

  # List available templates
  forge templates

  # Interactive mode (wizard)
  forge new`,
	SilenceUsage:  true,
	SilenceErrors: true,
}

// Execute runs the root command
func Execute() error {
	return rootCmd.Execute()
}

func init() {
	cobra.OnInitialize(initConfig)

	// Global flags
	rootCmd.PersistentFlags().BoolVar(&noColor, "no-color", false, "Disable colored output")
	rootCmd.PersistentFlags().BoolVarP(&quiet, "quiet", "q", false, "Suppress non-essential output")
}

func initConfig() {
	printer := output.Default()
	printer.SetNoColor(noColor)
	printer.SetQuiet(quiet)
}
