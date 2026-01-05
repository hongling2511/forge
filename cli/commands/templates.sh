#!/usr/bin/env bash
#
# templates.sh - List available forge templates
#
# Usage:
#   forge templates [options]
#
# Options:
#   -h, --help  Show this help message
#

# Show help for templates command
show_templates_help() {
    cat << EOF
forge templates - List available templates

Usage:
  forge templates [options]

Options:
  -h, --help  Show this help message

Output:
  Lists all available templates with their descriptions.
EOF
}

# Scan templates directory and display available templates
list_templates() {
    local templates_dir="${FORGE_HOME}/templates"

    echo "Available templates:"
    echo ""

    # Find all template.yaml files
    local found=0
    for template_dir in "${templates_dir}"/*/; do
        if [[ -d "$template_dir" ]]; then
            local template_name=$(basename "$template_dir")
            local template_yaml="${template_dir}/template.yaml"

            if [[ -f "$template_yaml" ]]; then
                # Parse description from template.yaml
                local description=$(grep "^description:" "$template_yaml" 2>/dev/null | sed 's/^description:[[:space:]]*//' | sed 's/^"//' | sed 's/"$//')
                if [[ -z "$description" ]]; then
                    description="No description available"
                fi
                printf "  %-15s %s\n" "$template_name" "$description"
                found=1
            fi
        fi
    done

    if [[ $found -eq 0 ]]; then
        echo "  No templates found."
        echo ""
        echo "Templates should be installed in: ${templates_dir}"
    fi

    echo ""
}

# Main entry point for templates command
cmd_templates() {
    while [[ $# -gt 0 ]]; do
        case "$1" in
            -h|--help)
                show_templates_help
                exit 0
                ;;
            *)
                echo "Error: Unknown option '$1'" >&2
                echo "Run 'forge templates --help' for usage." >&2
                exit 1
                ;;
        esac
        shift
    done

    list_templates
}
