package template

// Template represents a forge template
type Template struct {
	Name        string             `yaml:"name"`
	Version     string             `yaml:"version"`
	Description string             `yaml:"description"`
	Type        string             `yaml:"type"`
	Archetype   ArchetypeConfig    `yaml:"archetype"`
	GoConfig    GoConfig           `yaml:"goConfig"`  // Go-specific configuration
	Parameters  ParameterConfig    `yaml:"parameters"`
	Modules     []ModuleConfig     `yaml:"modules"`
	Stack       StackConfig        `yaml:"stack"`
	Path        string             `yaml:"-"` // Directory path (not from YAML)
}

// GoConfig holds Go-specific template configuration
type GoConfig struct {
	MinGoVersion string `yaml:"minGoVersion"` // e.g., "1.21"
	FilesDir     string `yaml:"filesDir"`     // Template files directory, default "files"
}

// ArchetypeConfig holds Maven archetype coordinates
type ArchetypeConfig struct {
	GroupID    string `yaml:"groupId"`
	ArtifactID string `yaml:"artifactId"`
	Version    string `yaml:"version"`
}

// ParameterConfig holds required and optional parameters
type ParameterConfig struct {
	Required []ParameterDef `yaml:"required"`
	Optional []ParameterDef `yaml:"optional"`
}

// ParameterDef defines a single parameter
type ParameterDef struct {
	Name        string `yaml:"name"`
	Description string `yaml:"description"`
	Pattern     string `yaml:"pattern"`
	Default     string `yaml:"default"`
}

// ModuleConfig describes a generated module
type ModuleConfig struct {
	Name        string `yaml:"name"`
	Description string `yaml:"description"`
}

// StackConfig describes the technology stack
type StackConfig struct {
	Language         string `yaml:"language"`
	JDK              string `yaml:"jdk"`
	GoVersion        string `yaml:"goVersion"` // Go only
	Framework        string `yaml:"framework"`
	FrameworkVersion string `yaml:"frameworkVersion"`
	BuildTool        string `yaml:"buildTool"`
}

// IsJavaTemplate returns true if this is a Java-based template
func (t *Template) IsJavaTemplate() bool {
	return t.Type == "maven-archetype" || t.Stack.Language == "java"
}

// IsGoTemplate returns true if this is a Go-based template
func (t *Template) IsGoTemplate() bool {
	return t.Type == "go-template" || t.Stack.Language == "go"
}
