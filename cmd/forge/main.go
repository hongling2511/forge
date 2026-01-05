package main

import (
	"os"

	"github.com/hongling2511/forge/internal/cli"
)

func main() {
	if err := cli.Execute(); err != nil {
		os.Exit(1)
	}
}
