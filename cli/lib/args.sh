#!/usr/bin/env bash
#
# args.sh - Argument parsing library for forge CLI
#

# Parse a single argument value
# Usage: parse_arg_value "$1" "$2"
# Returns the value and sets SHIFT_COUNT
parse_arg_value() {
    local arg="$1"
    local next="${2:-}"

    if [[ "$arg" == *"="* ]]; then
        # Format: --key=value
        echo "${arg#*=}"
        SHIFT_COUNT=1
    elif [[ -n "$next" && ! "$next" =~ ^- ]]; then
        # Format: --key value
        echo "$next"
        SHIFT_COUNT=2
    else
        echo ""
        SHIFT_COUNT=1
    fi
}

# Check if argument is a flag (starts with -)
is_flag() {
    [[ "${1:-}" =~ ^- ]]
}

# Check if argument is a long option (starts with --)
is_long_opt() {
    [[ "${1:-}" =~ ^-- ]]
}

# Check if argument is a short option (starts with single -)
is_short_opt() {
    [[ "${1:-}" =~ ^-[^-] ]]
}

# Extract option name from --option=value format
get_option_name() {
    local arg="$1"
    if [[ "$arg" == *"="* ]]; then
        echo "${arg%%=*}"
    else
        echo "$arg"
    fi
}
