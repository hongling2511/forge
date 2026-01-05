package output

import (
	"fmt"
	"time"

	"github.com/briandowns/spinner"
)

// Spinner wraps the spinner library for progress indication
type Spinner struct {
	s       *spinner.Spinner
	message string
}

// NewSpinner creates a new spinner with a message
func NewSpinner(message string) *Spinner {
	s := spinner.New(spinner.CharSets[14], 100*time.Millisecond)
	s.Suffix = " " + message
	return &Spinner{s: s, message: message}
}

// Start starts the spinner
func (sp *Spinner) Start() {
	sp.s.Start()
}

// Stop stops the spinner
func (sp *Spinner) Stop() {
	sp.s.Stop()
}

// Success stops the spinner and shows success
func (sp *Spinner) Success(msg string) {
	sp.s.Stop()
	fmt.Printf("✓ %s\n", msg)
}

// Fail stops the spinner and shows failure
func (sp *Spinner) Fail(msg string) {
	sp.s.Stop()
	fmt.Printf("✗ %s\n", msg)
}

// UpdateMessage updates the spinner message
func (sp *Spinner) UpdateMessage(msg string) {
	sp.s.Suffix = " " + msg
}
