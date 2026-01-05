#!/usr/bin/env bash
#
# install.sh - Forge CLI installation script
#
# This script installs the forge CLI by:
# 1. Making the forge script executable
# 2. Adding forge to PATH (optional)
#

set -euo pipefail

SCRIPT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"
FORGE_CLI="${SCRIPT_DIR}/cli/forge"

echo "Installing Forge CLI..."

# Make forge executable
chmod +x "${FORGE_CLI}"
echo "✓ Made forge executable"

# Make library scripts executable
chmod +x "${SCRIPT_DIR}/cli/lib/"*.sh 2>/dev/null || true
echo "✓ Made library scripts executable"

# Detect shell configuration file based on user's default shell
detect_shell_config() {
    local user_shell="${SHELL:-/bin/bash}"

    case "$user_shell" in
        */zsh)
            echo "${HOME}/.zshrc"
            ;;
        */bash)
            if [[ -f "${HOME}/.bash_profile" ]]; then
                echo "${HOME}/.bash_profile"
            else
                echo "${HOME}/.bashrc"
            fi
            ;;
        */fish)
            echo "${HOME}/.config/fish/config.fish"
            ;;
        *)
            echo "${HOME}/.profile"
            ;;
    esac
}

SHELL_CONFIG=$(detect_shell_config)
FORGE_PATH_EXPORT="export PATH=\"${SCRIPT_DIR}/cli:\$PATH\""

# Check if PATH already contains forge
if [[ ":$PATH:" == *":${SCRIPT_DIR}/cli:"* ]]; then
    echo "✓ Forge is already in PATH"
else
    echo ""
    echo "To add forge to your PATH, add this line to ${SHELL_CONFIG}:"
    echo ""
    echo "  ${FORGE_PATH_EXPORT}"
    echo ""
    read -p "Add to ${SHELL_CONFIG} now? [y/N] " -n 1 -r
    echo ""

    if [[ $REPLY =~ ^[Yy]$ ]]; then
        echo "" >> "${SHELL_CONFIG}"
        echo "# Forge CLI" >> "${SHELL_CONFIG}"
        echo "${FORGE_PATH_EXPORT}" >> "${SHELL_CONFIG}"
        echo "✓ Added forge to ${SHELL_CONFIG}"
        echo ""
        echo "Run 'source ${SHELL_CONFIG}' or restart your terminal to use forge"
    else
        echo "Skipped PATH modification"
        echo ""
        echo "You can run forge directly with:"
        echo "  ${FORGE_CLI}"
    fi
fi

echo ""
echo "Installation complete!"
echo ""
echo "Verify installation:"
echo "  forge --version"
