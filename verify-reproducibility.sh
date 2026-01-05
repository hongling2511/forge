#!/usr/bin/env bash
#
# verify-reproducibility.sh - Verify that forge generates reproducible output
#
# This script generates a project twice with identical parameters and
# compares the output to ensure deterministic generation.
#

set -euo pipefail

SCRIPT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"
FORGE_CLI="${SCRIPT_DIR}/cli/forge"
TEST_DIR="/tmp/forge-reproducibility-test-$$"

# Test parameters
GROUP_ID="com.example.repro"
ARTIFACT_ID="repro-test"
VERSION="1.0.0-SNAPSHOT"
PACKAGE="com.example.repro"

cleanup() {
    rm -rf "$TEST_DIR"
}

trap cleanup EXIT

echo "=== Forge Reproducibility Verification ==="
echo ""

# Create test directories
mkdir -p "${TEST_DIR}/run1" "${TEST_DIR}/run2"

# Generate project - Run 1
echo "[1/4] Generating project (run 1)..."
"$FORGE_CLI" new \
    -t java-ddd \
    -g "$GROUP_ID" \
    -a "$ARTIFACT_ID" \
    -v "$VERSION" \
    -p "$PACKAGE" \
    -o "${TEST_DIR}/run1" \
    > /dev/null 2>&1

# Generate project - Run 2
echo "[2/4] Generating project (run 2)..."
"$FORGE_CLI" new \
    -t java-ddd \
    -g "$GROUP_ID" \
    -a "$ARTIFACT_ID" \
    -v "$VERSION" \
    -p "$PACKAGE" \
    -o "${TEST_DIR}/run2" \
    > /dev/null 2>&1

# Compare outputs
echo "[3/4] Comparing generated files..."

# Get list of all files in both runs
FILES1=$(cd "${TEST_DIR}/run1/${ARTIFACT_ID}" && find . -type f | sort)
FILES2=$(cd "${TEST_DIR}/run2/${ARTIFACT_ID}" && find . -type f | sort)

# Check file lists match
if [[ "$FILES1" != "$FILES2" ]]; then
    echo "ERROR: File lists do not match!"
    echo ""
    echo "Run 1 files:"
    echo "$FILES1"
    echo ""
    echo "Run 2 files:"
    echo "$FILES2"
    exit 1
fi

# Compare each file
DIFF_COUNT=0
while IFS= read -r file; do
    FILE1="${TEST_DIR}/run1/${ARTIFACT_ID}/${file}"
    FILE2="${TEST_DIR}/run2/${ARTIFACT_ID}/${file}"

    if ! diff -q "$FILE1" "$FILE2" > /dev/null 2>&1; then
        echo "  DIFF: $file"
        DIFF_COUNT=$((DIFF_COUNT + 1))
    fi
done <<< "$FILES1"

echo "[4/4] Verifying version metadata..."

# Check version properties exist in parent POM
PARENT_POM="${TEST_DIR}/run1/${ARTIFACT_ID}/pom.xml"
if grep -q "forge.archetype.version" "$PARENT_POM" && \
   grep -q "forge.template.version" "$PARENT_POM"; then
    echo "  ✓ Version metadata present in pom.xml"
else
    echo "  ✗ Missing version metadata in pom.xml"
    exit 1
fi

# Report results
echo ""
if [[ $DIFF_COUNT -eq 0 ]]; then
    echo "=== SUCCESS: All files are identical ==="
    echo ""
    echo "Verified:"
    echo "  - File structure is identical"
    echo "  - File contents are identical"
    echo "  - Version metadata is present"
    echo ""
    echo "Generation is deterministic and reproducible."
    exit 0
else
    echo "=== FAILURE: Found $DIFF_COUNT different file(s) ==="
    exit 1
fi
