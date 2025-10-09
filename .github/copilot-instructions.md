# GitHub Copilot Instructions for Promoted Builds Plugin

## Project Overview

This is a Jenkins plugin that allows you to distinguish good builds from bad builds by introducing the notion of 'promotion'. A promoted build is a successful build that passed additional criteria such as comprehensive tests, manual approval, or other quality gates.

### Key Concepts

- **Build Promotion**: The mechanism to differentiate good builds from bad builds based on configurable criteria
- **Promotion Process**: A set of conditions and actions that define when and how a build gets promoted
- **Promotion Conditions**: Criteria that must be met for a build to be promoted (e.g., downstream builds passing, manual approval)
- **Promotion Actions**: Build steps executed when a build is promoted (e.g., copying artifacts, triggering downstream jobs)

## Project Structure

### Source Code Organization

```
src/
├── main/
│   ├── java/hudson/plugins/promoted_builds/     # Core plugin implementation
│   │   ├── PromotionProcess.java                # Defines promotion process configuration
│   │   ├── Promotion.java                       # Represents a single promotion execution
│   │   ├── PromotedProjectAction.java           # UI customization for promoted builds
│   │   ├── JobPropertyImpl.java                 # Job configuration property
│   │   ├── conditions/                          # Promotion condition implementations
│   │   └── integrations/jobdsl/                 # Job DSL plugin integration
│   ├── resources/                               # Jelly templates and configuration
│   └── webapp/                                  # Static web resources
└── test/
    ├── java/hudson/plugins/promoted_builds/     # JUnit tests
    └── resources/                               # Test resources and DSL examples
```

## Building and Testing

### Build System

This project uses **Maven** for building and dependency management.

#### Key Maven Commands

```bash
# Compile the plugin
mvn clean compile

# Run tests
mvn test

# Package the plugin (creates .hpi file)
mvn package

# Skip tests during packaging
mvn package -DskipTests

# Run specific test
mvn test -Dtest=PromotionTest

# Run with Jenkins test harness
mvn hpi:run
```

### Testing Framework

- **JUnit 5** (Jupiter) is used for unit and integration tests
- **JenkinsRule** from `org.jvnet.hudson.test` provides Jenkins test harness
- Test classes use `@WithJenkins` annotation to set up Jenkins environment
- Tests verify promotion conditions, actions, environment variables, and UI behavior

#### Test Patterns

```java
@WithJenkins
class MyTest {
    @Test
    void testSomething(JenkinsRule j) throws Exception {
        // Test implementation using Jenkins test harness
    }
}
```

## Code Style and Conventions

### Java Code

- Follow standard Java naming conventions (PascalCase for classes, camelCase for methods)
- Use meaningful variable names that reflect the promoted builds domain
- Add Javadoc for public APIs unless annotated with `@Restricted`
- Use Jenkins plugin patterns and APIs (e.g., `AbstractProject`, `Descriptor`)
- Keep backward compatibility in mind - this plugin has been around since 2009

### Important Annotations

- `@Restricted`: Marks internal APIs that should not be used externally
- `@CheckForNull` / `@Nonnull`: Document nullability contracts
- `@WithJenkins`: Required for integration tests
- `@Issue`: Link tests to JIRA issues

## Environment Variables

The plugin exposes several environment variables during promotion execution:

- `PROMOTED_URL` - URL of the build being promoted
- `PROMOTED_JOB_NAME` - Name of the job being promoted
- `PROMOTED_NUMBER` - Build number being promoted
- `PROMOTED_ID` - Build ID (timestamp format)
- `PROMOTED_USER_NAME` - User who triggered the promotion
- `PROMOTED_JOB_FULL_NAME` - Full name of the promoted job
- `PROMOTED_TIMESTAMP` - Timestamp (format configurable via global settings)

## Key Plugin Components

### Core Classes

- **PromotionProcess**: Represents the configuration of a promotion process (conditions, actions, name)
- **Promotion**: A single execution/build of a promotion process
- **JobPropertyImpl**: The job property that holds promotion configurations
- **PromotedProjectAction**: Provides UI elements for the project page
- **PromotionCondition**: Base class for conditions that trigger promotions

### Promotion Conditions

Located in `hudson.plugins.promoted_builds.conditions`:
- `SelfPromotionCondition` - Promotes when the build itself succeeds
- `DownstreamPassCondition` - Promotes when downstream builds pass
- `ManualCondition` - Requires manual approval from specified users
- `UpstreamPromotionCondition` - Promotes when upstream build is promoted
- `ReleasePromotionCondition` - For Maven release builds

## Integration Points

### Job DSL Support

The plugin integrates with Job DSL plugin. DSL examples are in `src/test/resources/`:
- `complex-example-dsl.groovy` - Full-featured example
- `copyartifacts-example-dsl.groovy` - Integration with Copy Artifact plugin

DSL integration classes are in `integrations/jobdsl/` package.

### Other Plugin Integrations

- **Copy Artifact Plugin**: For accessing promoted build artifacts
- **Parameterized Trigger Plugin**: For triggering downstream jobs with parameters
- **Token Macro Plugin**: For token expansion in promotion actions
- **Rebuild Plugin**: For rebuilding promoted builds
- **Config File Provider Plugin**: For managed configuration files

## Contributing Guidelines

### Pull Request Requirements

1. **Title**: Must include JIRA issue reference (e.g., "JENKINS-XXXXX: Fix promotion badge display")
2. **Description**: Explain what was changed and why
3. **Tests**: Add automated tests for new features or complex bugfixes
4. **Documentation**: Update README.md for user-facing changes
5. **Javadoc**: Document all public/protected methods (unless `@Restricted`)
6. **Screenshots**: Include for any UI changes

### Code Changes

- Make minimal, focused changes that address the specific issue
- Maintain backward compatibility where possible
- Follow existing code patterns and conventions in the codebase
- Use Jenkins plugin best practices and APIs

### Issue Tracking

- JIRA is used for issue tracking: https://issues.jenkins-ci.org/browse/JENKINS (component: promoted-builds-plugin)
- Reference JIRA issues in commits and pull requests

## Common Development Tasks

### Adding a New Promotion Condition

1. Create a new class extending `PromotionCondition` in the `conditions` package
2. Implement the `isMet()` method with your promotion logic
3. Add a `@Symbol` annotation for Job DSL/Pipeline support
4. Create a Descriptor inner class extending `PromotionConditionDescriptor`
5. Add Jelly views for configuration UI in `src/main/resources/`
6. Write tests in `src/test/java/`

### Adding a New Environment Variable

1. Modify the `buildEnvVars()` method in `Promotion.java`
2. Document the variable in README.md
3. Add tests in `PromotionEnvironmentVariablesTest.java`

### Modifying UI

1. Jelly files are in `src/main/resources/` (follow package structure)
2. Static resources (CSS, JS, images) go in `src/main/webapp/`
3. Follow Jenkins UI conventions and patterns
4. Test with `mvn hpi:run` to see changes in development Jenkins instance

## Important Notes

### Workspace Handling

**WARNING**: The promotion action uses the workspace of the job, but by the time promotion runs, this workspace can contain files from unrelated builds. Never rely on workspace files directly. Instead:
- Use the Copy Artifact Plugin with permalinks
- Access artifacts through Jenkins APIs
- Use fingerprinting to track artifacts

### Qualification and Downstream Promotions

A downstream build "qualifies" an upstream build through:
1. **Fingerprinting**: Downstream build uses artifacts from upstream build
2. **Build Triggers**: Upstream job triggers downstream job

This is important for the "downstream builds passed" promotion condition.

## Resources

- **Wiki**: https://wiki.jenkins-ci.org/display/JENKINS/Promoted+Builds+Plugin
- **JIRA**: https://issues.jenkins-ci.org/browse/JENKINS (component: promoted-builds-plugin)
- **Gitter Chat**: https://gitter.im/jenkinsci/promoted-builds-plugin
- **Plugin Site**: https://plugins.jenkins.io/promoted-builds

## Tips for Code Generation

When generating code for this plugin:

1. **Understand the context**: This is a mature plugin with many existing patterns - follow them
2. **Check for similar features**: Look at existing promotion conditions/actions before creating new ones
3. **Use Jenkins APIs properly**: Leverage Jenkins core APIs and plugin patterns
4. **Test with JenkinsRule**: Always write tests that use the Jenkins test harness
5. **Consider compatibility**: Changes should not break existing configurations
6. **Document thoroughly**: Users rely on clear documentation for promotion configuration
7. **Think about security**: Promotions can trigger powerful actions - validate permissions properly
8. **Handle errors gracefully**: Promotion failures should be clear and actionable

## License

This plugin is licensed under the MIT License. All contributions must be compatible with this license.
