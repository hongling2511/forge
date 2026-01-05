package cli

import (
	"fmt"

	"github.com/hongling2511/forge/internal/config"
	"github.com/spf13/cobra"
)

var versionCmd = &cobra.Command{
	Use:   "version",
	Short: "Print the version number",
	Long:  "Print the version number of forge CLI",
	Run: func(cmd *cobra.Command, args []string) {
		fmt.Printf("forge version %s\n", config.Version)
		if config.Commit != "unknown" {
			fmt.Printf("  commit: %s\n", config.Commit)
		}
		if config.BuildDate != "unknown" {
			fmt.Printf("  built:  %s\n", config.BuildDate)
		}
	},
}

func init() {
	rootCmd.AddCommand(versionCmd)

	// Also add --version flag to root command
	rootCmd.Version = config.Version
	rootCmd.SetVersionTemplate("forge version {{.Version}}\n")
}
