#!/usr/bin/env bash
#
# validation.sh - Validation library for forge CLI
#

# Validate Maven groupId format
# Pattern: ^[a-z][a-z0-9]*(\.[a-z][a-z0-9]*)*$
validate_group_id() {
    local group_id="$1"
    if [[ ! "$group_id" =~ ^[a-z][a-z0-9]*(\.[a-z][a-z0-9]*)*$ ]]; then
        echo "Error: Invalid groupId '${group_id}'" >&2
        echo "groupId must be a valid Maven groupId (e.g., com.example)" >&2
        return 1
    fi
    return 0
}

# Validate Maven artifactId format
# Pattern: ^[a-z][a-z0-9-]*$
validate_artifact_id() {
    local artifact_id="$1"
    if [[ ! "$artifact_id" =~ ^[a-z][a-z0-9-]*$ ]]; then
        echo "Error: Invalid artifactId '${artifact_id}'" >&2
        echo "artifactId must be a valid Maven artifactId (e.g., my-project)" >&2
        return 1
    fi
    return 0
}

# Validate version format (SemVer)
# Pattern: ^\d+\.\d+\.\d+(-[a-zA-Z0-9]+)?$
validate_version() {
    local version="$1"
    if [[ ! "$version" =~ ^[0-9]+\.[0-9]+\.[0-9]+(-[a-zA-Z0-9]+)?$ ]]; then
        echo "Error: Invalid version '${version}'" >&2
        echo "version must be in SemVer format (e.g., 1.0.0-SNAPSHOT)" >&2
        return 1
    fi
    return 0
}

# Validate Java package name format
validate_package() {
    local package="$1"
    if [[ ! "$package" =~ ^[a-z][a-z0-9]*(\.[a-z][a-z0-9]*)*$ ]]; then
        echo "Error: Invalid package '${package}'" >&2
        echo "package must be a valid Java package name (e.g., com.example.project)" >&2
        return 1
    fi
    return 0
}

# Check if directory exists and is not empty
validate_output_dir() {
    local output_dir="$1"
    local artifact_id="$2"
    local target_dir="${output_dir}/${artifact_id}"

    if [[ -d "$target_dir" ]]; then
        if [[ -n "$(ls -A "$target_dir" 2>/dev/null)" ]]; then
            echo "Error: Target directory '${target_dir}' already exists and is not empty" >&2
            echo "Please choose a different artifactId or remove the existing directory" >&2
            return 1
        fi
    fi
    return 0
}

# Check required parameters
check_required_params() {
    local missing=()

    for param in "$@"; do
        local name="${param%%=*}"
        local value="${param#*=}"
        if [[ -z "$value" ]]; then
            missing+=("$name")
        fi
    done

    if [[ ${#missing[@]} -gt 0 ]]; then
        echo "Error: Missing required parameters: ${missing[*]}" >&2
        return 1
    fi
    return 0
}

# Validate template exists
# Returns 0 if template exists, 1 otherwise
validate_template_exists() {
    local template_name="$1"
    local templates_dir="$2"
    local template_dir="${templates_dir}/${template_name}"

    if [[ ! -d "$template_dir" ]]; then
        echo "Error: Template '${template_name}' not found" >&2
        echo "" >&2
        echo "Available templates:" >&2
        for dir in "${templates_dir}"/*/; do
            if [[ -d "$dir" ]]; then
                local name=$(basename "$dir")
                echo "  - ${name}" >&2
            fi
        done
        return 1
    fi

    if [[ ! -f "${template_dir}/template.yaml" ]]; then
        echo "Error: Template '${template_name}' is invalid (missing template.yaml)" >&2
        return 1
    fi

    return 0
}

# Print validation error summary for batch mode
# Provides clear, actionable error messages
print_validation_summary() {
    local errors=("$@")

    echo "" >&2
    echo "Validation failed with ${#errors[@]} error(s):" >&2
    echo "" >&2
    for err in "${errors[@]}"; do
        echo "  ✗ ${err}" >&2
    done
    echo "" >&2
    echo "For usage information, run: forge new --help" >&2
}
