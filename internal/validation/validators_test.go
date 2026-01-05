package validation

import (
	"testing"
)

func TestValidateArtifactID(t *testing.T) {
	tests := []struct {
		input   string
		wantErr bool
	}{
		// Valid cases
		{"my-service", false},
		{"myservice", false},
		{"my-service-123", false},
		{"a", false},
		{"a1", false},
		{"my123service", false},

		// Invalid cases
		{"My-Service", true},    // uppercase
		{"my_service", true},    // underscore
		{"-my-service", true},   // starts with hyphen
		{"123-service", true},   // starts with number
		{"", true},              // empty
		{"my service", true},    // space
		{"my.service", true},    // dot
		{"MY-SERVICE", true},    // all uppercase
	}

	for _, tt := range tests {
		t.Run(tt.input, func(t *testing.T) {
			err := ValidateArtifactID(tt.input)
			if (err != nil) != tt.wantErr {
				t.Errorf("ValidateArtifactID(%q) error = %v, wantErr %v",
					tt.input, err, tt.wantErr)
			}
		})
	}
}

func TestValidateGroupID(t *testing.T) {
	tests := []struct {
		input   string
		wantErr bool
	}{
		// Valid cases
		{"com.example", false},
		{"com.example.project", false},
		{"org.test", false},
		{"a.b.c", false},
		{"com", false},
		{"com1.example2", false},

		// Invalid cases
		{"Com.Example", true},      // uppercase
		{"com.example.", true},     // trailing dot
		{".com.example", true},     // leading dot
		{"com..example", true},     // double dot
		{"com-example", true},      // hyphen (not allowed in groupId)
		{"com_example", true},      // underscore
		{"123.example", true},      // starts with number
		{"", true},                 // empty
		{"com.123", true},          // segment starts with number
	}

	for _, tt := range tests {
		t.Run(tt.input, func(t *testing.T) {
			err := ValidateGroupID(tt.input)
			if (err != nil) != tt.wantErr {
				t.Errorf("ValidateGroupID(%q) error = %v, wantErr %v",
					tt.input, err, tt.wantErr)
			}
		})
	}
}

func TestValidateVersion(t *testing.T) {
	tests := []struct {
		input   string
		wantErr bool
	}{
		// Valid cases
		{"1.0.0", false},
		{"1.0.0-SNAPSHOT", false},
		{"0.0.1", false},
		{"10.20.30", false},
		{"1.0.0-alpha", false},
		{"1.0.0-beta1", false},
		{"", false}, // empty is OK (has default)

		// Invalid cases
		{"1.0", true},           // missing patch
		{"1", true},             // only major
		{"v1.0.0", true},        // leading v
		{"1.0.0.0", true},       // too many parts
		{"1.0.0-", true},        // trailing hyphen
		{"1.0.0-SNAP_SHOT", true}, // underscore in suffix
		{"a.b.c", true},         // non-numeric
	}

	for _, tt := range tests {
		t.Run(tt.input, func(t *testing.T) {
			err := ValidateVersion(tt.input)
			if (err != nil) != tt.wantErr {
				t.Errorf("ValidateVersion(%q) error = %v, wantErr %v",
					tt.input, err, tt.wantErr)
			}
		})
	}
}

func TestValidatePackage(t *testing.T) {
	tests := []struct {
		input   string
		wantErr bool
	}{
		// Valid cases
		{"com.example", false},
		{"com.example.project", false},
		{"org.test.mypackage", false},
		{"", false}, // empty is OK (defaults to groupId)

		// Invalid cases
		{"Com.Example", true},  // uppercase
		{"com-example", true},  // hyphen
		{"com_example", true},  // underscore
		{"123.example", true},  // starts with number
	}

	for _, tt := range tests {
		t.Run(tt.input, func(t *testing.T) {
			err := ValidatePackage(tt.input)
			if (err != nil) != tt.wantErr {
				t.Errorf("ValidatePackage(%q) error = %v, wantErr %v",
					tt.input, err, tt.wantErr)
			}
		})
	}
}

func TestValidateAll(t *testing.T) {
	tests := []struct {
		name        string
		artifactID  string
		groupID     string
		version     string
		pkg         string
		isJava      bool
		wantErrCnt  int
	}{
		{
			name:       "all valid",
			artifactID: "my-service",
			groupID:    "com.example",
			version:    "1.0.0",
			pkg:        "com.example.myservice",
			isJava:     true,
			wantErrCnt: 0,
		},
		{
			name:       "missing artifact-id",
			artifactID: "",
			groupID:    "com.example",
			version:    "1.0.0",
			pkg:        "",
			isJava:     true,
			wantErrCnt: 1,
		},
		{
			name:       "missing group-id for java",
			artifactID: "my-service",
			groupID:    "",
			version:    "1.0.0",
			pkg:        "",
			isJava:     true,
			wantErrCnt: 1,
		},
		{
			name:       "missing group-id for non-java",
			artifactID: "my-service",
			groupID:    "",
			version:    "1.0.0",
			pkg:        "",
			isJava:     false,
			wantErrCnt: 0,
		},
		{
			name:       "multiple errors",
			artifactID: "",
			groupID:    "",
			version:    "invalid",
			pkg:        "Invalid",
			isJava:     true,
			wantErrCnt: 4,
		},
	}

	for _, tt := range tests {
		t.Run(tt.name, func(t *testing.T) {
			errors := ValidateAll(tt.artifactID, tt.groupID, tt.version, tt.pkg, tt.isJava)
			if len(errors) != tt.wantErrCnt {
				t.Errorf("ValidateAll() returned %d errors, want %d. Errors: %v",
					len(errors), tt.wantErrCnt, errors)
			}
		})
	}
}
