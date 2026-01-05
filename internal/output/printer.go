package output

import (
	"fmt"
	"io"
	"os"
	"strings"

	"github.com/fatih/color"
)

// Printer handles formatted output
type Printer struct {
	out     io.Writer
	err     io.Writer
	noColor bool
	quiet   bool
}

// NewPrinter creates a new printer with stdout/stderr
func NewPrinter() *Printer {
	return &Printer{
		out:     os.Stdout,
		err:     os.Stderr,
		noColor: false,
		quiet:   false,
	}
}

// SetNoColor disables colored output
func (p *Printer) SetNoColor(noColor bool) {
	p.noColor = noColor
	if noColor {
		color.NoColor = true
	}
}

// SetQuiet enables quiet mode
func (p *Printer) SetQuiet(quiet bool) {
	p.quiet = quiet
}

// SetOutput sets the output writer (useful for testing)
func (p *Printer) SetOutput(out io.Writer) {
	p.out = out
}

// Println prints a line to stdout
func (p *Printer) Println(msg string) {
	if p.quiet {
		return
	}
	fmt.Fprintln(p.out, msg)
}

// Printf prints formatted output to stdout
func (p *Printer) Printf(format string, args ...interface{}) {
	if p.quiet {
		return
	}
	fmt.Fprintf(p.out, format, args...)
}

// Success prints a success message with green checkmark
func (p *Printer) Success(msg string) {
	if p.quiet {
		return
	}
	green := color.New(color.FgGreen).SprintFunc()
	fmt.Fprintf(p.out, "%s %s\n", green("✓"), msg)
}

// Error prints an error message to stderr
func (p *Printer) Error(msg string) {
	red := color.New(color.FgRed).SprintFunc()
	fmt.Fprintf(p.err, "%s %s\n", red("Error:"), msg)
}

// Errorf prints a formatted error message to stderr
func (p *Printer) Errorf(format string, args ...interface{}) {
	red := color.New(color.FgRed).SprintFunc()
	fmt.Fprintf(p.err, "%s %s\n", red("Error:"), fmt.Sprintf(format, args...))
}

// Warning prints a warning message
func (p *Printer) Warning(msg string) {
	if p.quiet {
		return
	}
	yellow := color.New(color.FgYellow).SprintFunc()
	fmt.Fprintf(p.out, "%s %s\n", yellow("Warning:"), msg)
}

// Info prints an info message
func (p *Printer) Info(msg string) {
	if p.quiet {
		return
	}
	cyan := color.New(color.FgCyan).SprintFunc()
	fmt.Fprintf(p.out, "%s %s\n", cyan("ℹ"), msg)
}

// Bold prints bold text
func (p *Printer) Bold(msg string) string {
	return color.New(color.Bold).Sprint(msg)
}

// Table prints a formatted table
func (p *Printer) Table(headers []string, rows [][]string) {
	if p.quiet || len(rows) == 0 {
		return
	}

	// Calculate column widths
	widths := make([]int, len(headers))
	for i, h := range headers {
		widths[i] = len(h)
	}
	for _, row := range rows {
		for i, cell := range row {
			if i < len(widths) && len(cell) > widths[i] {
				widths[i] = len(cell)
			}
		}
	}

	// Print headers
	headerLine := ""
	for i, h := range headers {
		headerLine += fmt.Sprintf("%-*s  ", widths[i], h)
	}
	fmt.Fprintln(p.out, p.Bold(strings.TrimSpace(headerLine)))

	// Print separator
	sep := ""
	for _, w := range widths {
		sep += strings.Repeat("-", w) + "  "
	}
	fmt.Fprintln(p.out, strings.TrimSpace(sep))

	// Print rows
	for _, row := range rows {
		rowLine := ""
		for i, cell := range row {
			if i < len(widths) {
				rowLine += fmt.Sprintf("%-*s  ", widths[i], cell)
			}
		}
		fmt.Fprintln(p.out, strings.TrimSpace(rowLine))
	}
}

// ValidationErrors prints validation errors in a batch-friendly format
func (p *Printer) ValidationErrors(errors []error) {
	fmt.Fprintln(p.err)
	red := color.New(color.FgRed).SprintFunc()
	fmt.Fprintf(p.err, "%s Validation failed with %d error(s):\n", red("Error:"), len(errors))
	fmt.Fprintln(p.err)

	for _, err := range errors {
		fmt.Fprintf(p.err, "  %s %s\n", red("✗"), err.Error())
	}

	fmt.Fprintln(p.err)
	fmt.Fprintln(p.err, "For usage information, run: forge new --help")
}

// ProjectCreated prints the success message after project creation
func (p *Printer) ProjectCreated(artifactID, version string) {
	p.Println("")
	p.Success("Project created successfully!")
	p.Println("")
	p.Println("Next steps:")
	p.Printf("  cd %s\n", artifactID)
	p.Println("  mvn clean package")
	p.Printf("  java -jar %s-bootstrap/target/%s-bootstrap-%s.jar\n", artifactID, artifactID, version)
}

// Global printer instance
var defaultPrinter = NewPrinter()

// Default returns the default printer
func Default() *Printer {
	return defaultPrinter
}
