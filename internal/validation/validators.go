package validation

import (
	"fmt"
	"regexp"
)

// Validation patterns ported from cli/lib/validation.sh
var (
	// ArtifactIDPattern: ^[a-z][a-z0-9-]*$
	ArtifactIDPattern = regexp.MustCompile(`^[a-z][a-z0-9-]*$`)

	// GroupIDPattern: ^[a-z][a-z0-9]*(\.[a-z][a-z0-9]*)*$
	GroupIDPattern = regexp.MustCompile(`^[a-z][a-z0-9]*(\.[a-z][a-z0-9]*)*$`)

	// VersionPattern: ^[0-9]+\.[0-9]+\.[0-9]+(-[a-zA-Z0-9]+)?$
	VersionPattern = regexp.MustCompile(`^[0-9]+\.[0-9]+\.[0-9]+(-[a-zA-Z0-9]+)?$`)

	// PackagePattern: same as GroupIDPattern
	PackagePattern = regexp.MustCompile(`^[a-z][a-z0-9]*(\.[a-z][a-z0-9]*)*$`)
)

// ValidationError represents a validation error with context
type ValidationError struct {
	Field   string
	Value   string
	Message string
	Help    string
}

func (e *ValidationError) Error() string {
	return fmt.Sprintf("%s: %s", e.Field, e.Message)
}

// ValidateArtifactID validates Maven artifactId format
func ValidateArtifactID(value string) error {
	if value == "" {
		return &ValidationError{
			Field:   "artifact-id",
			Value:   value,
			Message: "artifact-id is required",
			Help:    "Provide a project name using -a or --artifact-id",
		}
	}
	if !ArtifactIDPattern.MatchString(value) {
		return &ValidationError{
			Field:   "artifact-id",
			Value:   value,
			Message: fmt.Sprintf("invalid artifactId '%s'", value),
			Help:    "artifactId must be lowercase letters, numbers, and hyphens (e.g., my-project)",
		}
	}
	return nil
}

// ValidateGroupID validates Maven groupId format
func ValidateGroupID(value string) error {
	if value == "" {
		return &ValidationError{
			Field:   "group-id",
			Value:   value,
			Message: "group-id is required for Java templates",
			Help:    "Provide a Maven groupId using -g or --group-id",
		}
	}
	if !GroupIDPattern.MatchString(value) {
		return &ValidationError{
			Field:   "group-id",
			Value:   value,
			Message: fmt.Sprintf("invalid groupId '%s'", value),
			Help:    "groupId must be a valid Maven groupId (e.g., com.example)",
		}
	}
	return nil
}

// ValidateVersion validates SemVer format
func ValidateVersion(value string) error {
	if value == "" {
		return nil // Version has a default, so empty is ok
	}
	if !VersionPattern.MatchString(value) {
		return &ValidationError{
			Field:   "version",
			Value:   value,
			Message: fmt.Sprintf("invalid version '%s'", value),
			Help:    "version must be in SemVer format (e.g., 1.0.0-SNAPSHOT)",
		}
	}
	return nil
}

// ValidatePackage validates Java package name format
func ValidatePackage(value string) error {
	if value == "" {
		return nil // Package defaults to groupId, so empty is ok
	}
	if !PackagePattern.MatchString(value) {
		return &ValidationError{
			Field:   "package",
			Value:   value,
			Message: fmt.Sprintf("invalid package '%s'", value),
			Help:    "package must be a valid Java package name (e.g., com.example.project)",
		}
	}
	return nil
}

// ValidateAll runs all validations and returns a slice of errors
func ValidateAll(artifactID, groupID, version, pkg string, isJavaTemplate bool) []error {
	var errors []error

	if err := ValidateArtifactID(artifactID); err != nil {
		errors = append(errors, err)
	}

	if isJavaTemplate {
		if err := ValidateGroupID(groupID); err != nil {
			errors = append(errors, err)
		}
	}

	if err := ValidateVersion(version); err != nil {
		errors = append(errors, err)
	}

	if err := ValidatePackage(pkg); err != nil {
		errors = append(errors, err)
	}

	return errors
}
