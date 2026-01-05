#!/usr/bin/env bash
#
# new.sh - Create a new project from template
#
# Usage:
#   forge new [options]
#
# Options:
#   -t, --template    Template to use (default: java-ddd)
#   -g, --group-id    Maven groupId (required for java templates)
#   -a, --artifact-id Project name (required)
#   -v, --version     Project version (default: 1.0.0-SNAPSHOT)
#   -p, --package     Java package name (default: groupId)
#   -o, --output      Output directory (default: current directory)
#   -h, --help        Show this help message
#

# Default values
TEMPLATE="java-ddd"
GROUP_ID=""
ARTIFACT_ID=""
VERSION="1.0.0-SNAPSHOT"
PACKAGE=""
OUTPUT_DIR="."

# Show help for new command (T024)
show_new_help() {
    cat << 'EOF'
forge new - Create a new project from template

Usage:
  forge new [options]

Options:
  -t, --template <name>     Template to use (default: java-ddd)
  -g, --group-id <id>       Maven groupId (required for java templates)
  -a, --artifact-id <id>    Project name (required)
  -v, --version <version>   Project version (default: 1.0.0-SNAPSHOT)
  -p, --package <name>      Java package name (default: groupId)
  -o, --output <dir>        Output directory (default: current directory)
  -h, --help                Show this help message

Examples:
  # Create a new DDD project
  forge new -g com.example -a my-service

  # Create with all options
  forge new -t java-ddd -g com.example -a my-service -v 2.0.0 -p com.example.myservice

  # Create in specific directory
  forge new -g com.example -a my-service -o ./projects

Available Templates:
  Run 'forge templates' to see all available templates.
EOF
}

# Parse command line arguments (T019-T022)
parse_new_args() {
    while [[ $# -gt 0 ]]; do
        case "$1" in
            -h|--help)
                show_new_help
                exit 0
                ;;
            -t|--template)
                TEMPLATE="$2"
                shift 2
                ;;
            --template=*)
                TEMPLATE="${1#*=}"
                shift
                ;;
            -g|--group-id)
                GROUP_ID="$2"
                shift 2
                ;;
            --group-id=*)
                GROUP_ID="${1#*=}"
                shift
                ;;
            -a|--artifact-id)
                ARTIFACT_ID="$2"
                shift 2
                ;;
            --artifact-id=*)
                ARTIFACT_ID="${1#*=}"
                shift
                ;;
            -v|--version)
                VERSION="$2"
                shift 2
                ;;
            --version=*)
                VERSION="${1#*=}"
                shift
                ;;
            -p|--package)
                PACKAGE="$2"
                shift 2
                ;;
            --package=*)
                PACKAGE="${1#*=}"
                shift
                ;;
            -o|--output)
                OUTPUT_DIR="$2"
                shift 2
                ;;
            --output=*)
                OUTPUT_DIR="${1#*=}"
                shift
                ;;
            *)
                echo "Error: Unknown option '$1'" >&2
                echo "Run 'forge new --help' for usage." >&2
                exit 1
                ;;
        esac
    done
}

# Discover template and get archetype coordinates (T023)
discover_template() {
    local template_name="$1"
    local template_dir="${FORGE_HOME}/templates/${template_name}"
    local template_yaml="${template_dir}/template.yaml"

    if [[ ! -d "$template_dir" ]]; then
        echo "Error: Template '${template_name}' not found." >&2
        echo "" >&2
        echo "Available templates:" >&2
        for dir in "${FORGE_HOME}/templates"/*/; do
            if [[ -d "$dir" ]]; then
                echo "  - $(basename "$dir")" >&2
            fi
        done
        exit 1
    fi

    if [[ ! -f "$template_yaml" ]]; then
        echo "Error: Template '${template_name}' is missing template.yaml" >&2
        exit 1
    fi

    # Parse archetype coordinates from template.yaml
    ARCHETYPE_GROUP_ID=$(grep "groupId:" "$template_yaml" | tail -1 | sed 's/.*groupId:[[:space:]]*//' | tr -d '"')
    ARCHETYPE_ARTIFACT_ID=$(grep "artifactId:" "$template_yaml" | tail -1 | sed 's/.*artifactId:[[:space:]]*//' | tr -d '"')
    ARCHETYPE_VERSION=$(grep -A3 "^archetype:" "$template_yaml" | grep "version:" | sed 's/.*version:[[:space:]]*//' | tr -d '"')

    # Fallback defaults if parsing fails
    ARCHETYPE_GROUP_ID="${ARCHETYPE_GROUP_ID:-com.forge.templates}"
    ARCHETYPE_ARTIFACT_ID="${ARCHETYPE_ARTIFACT_ID:-${template_name}-archetype}"
    ARCHETYPE_VERSION="${ARCHETYPE_VERSION:-1.0.0-SNAPSHOT}"
}

# Invoke Maven Archetype (T023)
invoke_archetype() {
    # Set package to groupId if not specified
    if [[ -z "$PACKAGE" ]]; then
        PACKAGE="$GROUP_ID"
    fi

    # Resolve output directory to absolute path
    OUTPUT_DIR="$(cd "$OUTPUT_DIR" 2>/dev/null && pwd || echo "$OUTPUT_DIR")"

    # Create output directory if it doesn't exist
    if [[ ! -d "$OUTPUT_DIR" ]]; then
        mkdir -p "$OUTPUT_DIR"
    fi

    # Check if target directory already exists
    local target_dir="${OUTPUT_DIR}/${ARTIFACT_ID}"
    if [[ -d "$target_dir" && -n "$(ls -A "$target_dir" 2>/dev/null)" ]]; then
        echo "Error: Target directory '${target_dir}' already exists and is not empty." >&2
        echo "Please choose a different artifact-id or remove the existing directory." >&2
        exit 1
    fi

    echo "Creating project '${ARTIFACT_ID}' from template '${TEMPLATE}'..."
    echo ""

    # First, install the archetype to local repository
    local archetype_dir="${FORGE_HOME}/templates/${TEMPLATE}"
    echo "Installing archetype from ${archetype_dir}..."

    (cd "$archetype_dir" && mvn install -q -DskipTests) || {
        echo "Error: Failed to install archetype." >&2
        exit 1
    }

    # Generate project using Maven Archetype
    mvn archetype:generate \
        -DarchetypeGroupId="${ARCHETYPE_GROUP_ID}" \
        -DarchetypeArtifactId="${ARCHETYPE_ARTIFACT_ID}" \
        -DarchetypeVersion="${ARCHETYPE_VERSION}" \
        -DgroupId="${GROUP_ID}" \
        -DartifactId="${ARTIFACT_ID}" \
        -Dversion="${VERSION}" \
        -Dpackage="${PACKAGE}" \
        -DforgeArchetypeVersion="${ARCHETYPE_VERSION}" \
        -DforgeTemplateVersion="1.0.0" \
        -DinteractiveMode=false \
        -DoutputDirectory="${OUTPUT_DIR}" \
        || {
            echo "Error: Failed to generate project." >&2
            exit 1
        }

    echo ""
    echo "✓ Project created successfully!"
    echo ""
    echo "Next steps:"
    echo "  cd ${ARTIFACT_ID}"
    echo "  mvn clean package"
    echo "  java -jar ${ARTIFACT_ID}-bootstrap/target/${ARTIFACT_ID}-bootstrap-${VERSION}.jar"
}

# Validate required parameters
validate_params() {
    local errors=()

    # Check required parameters
    if [[ -z "$ARTIFACT_ID" ]]; then
        errors+=("--artifact-id/-a is required")
    fi

    # For java templates, groupId is required
    if [[ "$TEMPLATE" == java-* && -z "$GROUP_ID" ]]; then
        errors+=("--group-id/-g is required for Java templates")
    fi

    # Validate template exists (T050)
    if ! validate_template_exists "$TEMPLATE" "${FORGE_HOME}/templates" 2>/dev/null; then
        errors+=("Template '${TEMPLATE}' not found")
    fi

    # Validate formats if provided
    if [[ -n "$GROUP_ID" ]]; then
        if ! validate_group_id "$GROUP_ID" 2>/dev/null; then
            errors+=("Invalid groupId format '${GROUP_ID}' - must be like: com.example")
        fi
    fi

    if [[ -n "$ARTIFACT_ID" ]]; then
        if ! validate_artifact_id "$ARTIFACT_ID" 2>/dev/null; then
            errors+=("Invalid artifactId format '${ARTIFACT_ID}' - must be like: my-project")
        fi
    fi

    if [[ -n "$VERSION" ]]; then
        if ! validate_version "$VERSION" 2>/dev/null; then
            errors+=("Invalid version format '${VERSION}' - must be like: 1.0.0-SNAPSHOT")
        fi
    fi

    if [[ -n "$PACKAGE" ]]; then
        if ! validate_package "$PACKAGE" 2>/dev/null; then
            errors+=("Invalid package format '${PACKAGE}' - must be like: com.example.project")
        fi
    fi

    # Report errors with clear batch-friendly output (T048)
    if [[ ${#errors[@]} -gt 0 ]]; then
        print_validation_summary "${errors[@]}"
        exit 1
    fi
}

# Main entry point for new command (T018)
cmd_new() {
    parse_new_args "$@"
    validate_params
    discover_template "$TEMPLATE"
    invoke_archetype
}
