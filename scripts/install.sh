#!/bin/bash
set -euo pipefail

# Forge CLI Installation Script
# Downloads and installs the forge binary for the current platform

FORGE_VERSION="${FORGE_VERSION:-latest}"
INSTALL_DIR="${INSTALL_DIR:-/usr/local/bin}"
REPO="hongling/forge"

# Detect OS and architecture
detect_platform() {
    OS="$(uname -s | tr '[:upper:]' '[:lower:]')"
    ARCH="$(uname -m)"

    case "$ARCH" in
        x86_64) ARCH="amd64" ;;
        aarch64|arm64) ARCH="arm64" ;;
        *)
            echo "Error: Unsupported architecture: $ARCH"
            exit 1
            ;;
    esac

    case "$OS" in
        linux|darwin) ;;
        mingw*|msys*|cygwin*) OS="windows" ;;
        *)
            echo "Error: Unsupported OS: $OS"
            exit 1
            ;;
    esac
}

# Get the download URL
get_download_url() {
    BINARY="forge-${OS}-${ARCH}"
    if [ "$OS" = "windows" ]; then
        BINARY="${BINARY}.exe"
    fi

    if [ "$FORGE_VERSION" = "latest" ]; then
        DOWNLOAD_URL="https://github.com/${REPO}/releases/latest/download/${BINARY}"
    else
        DOWNLOAD_URL="https://github.com/${REPO}/releases/download/${FORGE_VERSION}/${BINARY}"
    fi
}

# Download and install
install_forge() {
    echo "Installing forge ${FORGE_VERSION} for ${OS}/${ARCH}..."

    # Create install directory if needed
    if [ ! -d "$INSTALL_DIR" ]; then
        echo "Creating directory: $INSTALL_DIR"
        sudo mkdir -p "$INSTALL_DIR"
    fi

    # Download binary
    TEMP_FILE=$(mktemp)
    echo "Downloading from: $DOWNLOAD_URL"

    if command -v curl &> /dev/null; then
        curl -fsSL "$DOWNLOAD_URL" -o "$TEMP_FILE"
    elif command -v wget &> /dev/null; then
        wget -q "$DOWNLOAD_URL" -O "$TEMP_FILE"
    else
        echo "Error: curl or wget is required"
        exit 1
    fi

    # Install binary
    INSTALL_PATH="${INSTALL_DIR}/forge"
    if [ "$OS" = "windows" ]; then
        INSTALL_PATH="${INSTALL_DIR}/forge.exe"
    fi

    if [ -w "$INSTALL_DIR" ]; then
        mv "$TEMP_FILE" "$INSTALL_PATH"
        chmod +x "$INSTALL_PATH"
    else
        sudo mv "$TEMP_FILE" "$INSTALL_PATH"
        sudo chmod +x "$INSTALL_PATH"
    fi

    echo ""
    echo "forge installed successfully to $INSTALL_PATH"
    echo ""

    # Verify installation
    if command -v forge &> /dev/null; then
        forge --version
    else
        echo "Note: $INSTALL_DIR may not be in your PATH"
        echo "Add it with: export PATH=\"\$PATH:$INSTALL_DIR\""
    fi
}

# Main
main() {
    detect_platform
    get_download_url
    install_forge
}

main "$@"
