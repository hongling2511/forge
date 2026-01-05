package template

// Template represents a forge template
type Template struct {
	Name        string             `yaml:"name"`
	Version     string             `yaml:"version"`
	Description string             `yaml:"description"`
	Type        string             `yaml:"type"`
	Archetype   ArchetypeConfig    `yaml:"archetype"`
	Parameters  ParameterConfig    `yaml:"parameters"`
	Modules     []ModuleConfig     `yaml:"modules"`
	Stack       StackConfig        `yaml:"stack"`
	Path        string             `yaml:"-"` // Directory path (not from YAML)
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
	Framework        string `yaml:"framework"`
	FrameworkVersion string `yaml:"frameworkVersion"`
	BuildTool        string `yaml:"buildTool"`
}

// IsJavaTemplate returns true if this is a Java-based template
func (t *Template) IsJavaTemplate() bool {
	return t.Type == "maven-archetype" || t.Stack.Language == "java"
}
