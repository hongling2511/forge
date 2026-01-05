package cli

import (
	"github.com/hongling2511/forge/internal/output"
	"github.com/hongling2511/forge/internal/template"
	"github.com/spf13/cobra"
)

var templatesCmd = &cobra.Command{
	Use:   "templates",
	Short: "List available templates",
	Long:  "List all available project templates that can be used with 'forge new'",
	RunE:  runTemplates,
}

func init() {
	rootCmd.AddCommand(templatesCmd)
}

func runTemplates(cmd *cobra.Command, args []string) error {
	printer := output.Default()
	registry := template.NewRegistry()

	templates, err := registry.List()
	if err != nil {
		return err
	}

	if len(templates) == 0 {
		printer.Warning("No templates found")
		return nil
	}

	printer.Println("Available templates:")
	printer.Println("")

	rows := make([][]string, len(templates))
	for i, tmpl := range templates {
		rows[i] = []string{tmpl.Name, tmpl.Description}
	}

	printer.Table([]string{"NAME", "DESCRIPTION"}, rows)

	return nil
}
