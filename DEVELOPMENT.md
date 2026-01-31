# Development Documentation

This document will guide you on how to contribute to the development of the File Ref Tags plugin.

## Table of Contents

- [Development Environment Setup](#development-environment-setup)
- [Project Structure](#project-structure)
- [Core Module Overview](#core-module-overview)
- [Development Workflow](#development-workflow)
- [How to Add New Features](#how-to-add-new-features)
- [Code Standards](#code-standards)
- [Testing](#testing)
- [Building and Packaging](#building-and-packaging)
- [Frequently Asked Questions](#frequently-asked-questions)

## Development Environment Setup

### Prerequisites

- **JDK**: Version 17 or higher
- **IntelliJ IDEA**: 2023.1 or higher (recommended to use the latest version)
- **Gradle**: Project includes Gradle Wrapper, no separate installation needed
- **Git**: For version control

### Environment Setup Steps

1. **Clone the project**
   ```bash
   git clone https://github.com/your-username/file-ref-tags.git
   cd file-ref-tags/intellij-platform-plugin-template-main
   ```

2. **Open the project**
   - Open the project root directory in IntelliJ IDEA
   - Wait for Gradle synchronization to complete

3. **Configure JDK**
   - Open **File** → **Project Structure** → **Project Settings** → **Project**
   - Set **SDK** to Java 17 or higher
   - Set **Language level** to 17 or higher

4. **Verify environment**
   ```bash
   # Run tests to verify environment is correct
   ./gradlew test
   
   # Run plugin to verify it starts normally
   ./gradlew runIde
   ```

## Project Structure

```
intellij-platform-plugin-template-main/
├── src/
│   ├── main/
│   │   ├── kotlin/org/jetbrains/plugins/template/
│   │   │   ├── actions/          # Action definitions
│   │   │   │   ├── Actions.kt                    # Action group definitions
│   │   │   │   ├── AddCurrentFileAction.kt       # Add current file
│   │   │   │   ├── AddFileAndSnippetAction.kt     # Add file and code snippet
│   │   │   │   ├── AddGlobalUniqueSnippetAction.kt # Add globally unique code snippet
│   │   │   │   ├── AddCommentAction.kt           # Add comment
│   │   │   │   ├── AddGroupAction.kt              # Add group
│   │   │   │   ├── DeleteReferenceAction.kt       # Delete reference
│   │   │   │   ├── EditReferenceTitleAction.kt   # Edit reference title
│   │   │   │   ├── RefreshReferencesAction.kt    # Refresh references
│   │   │   │   └── ShowStorageLocationAction.kt   # Show storage location
│   │   │   ├── model/            # Data models
│   │   │   │   ├── ReferenceItem.kt    # Reference item data model
│   │   │   │   └── ReferenceGroup.kt   # Group data model
│   │   │   ├── services/         # Service layer
│   │   │   │   └── ReferenceDataService.kt  # Reference data management service
│   │   │   ├── ui/               # UI components
│   │   │   │   └── ReferenceListPanel.kt     # Reference list panel
│   │   │   ├── toolWindow/       # Tool window
│   │   │   │   └── MyToolWindowFactory.kt    # Tool window factory
│   │   │   ├── utils/            # Utility classes
│   │   │   │   ├── NotificationUtils.kt      # Notification utilities
│   │   │   │   └── ReferenceNavigationUtils.kt # Navigation utilities
│   │   │   ├── MyBundle.kt       # Internationalization support
│   │   │   └── UriHandler.kt     # URI handler
│   │   └── resources/
│   │       ├── META-INF/
│   │       │   └── plugin.xml     # Plugin configuration file
│   │       └── messages/
│   │           └── MyBundle.properties  # Internationalization resource file
│   └── test/                      # Test code
│       └── kotlin/
├── build.gradle.kts               # Gradle build configuration
├── gradle.properties             # Gradle properties configuration
├── settings.gradle.kts           # Gradle project settings
└── README.md                      # Project documentation
```

## Core Module Overview

### 1. Data Models (model/)

#### ReferenceItem.kt
Defines the data structure for reference items, including:
- `id`: Unique identifier
- `title`: Display title
- `type`: Reference type (FILE, FILE_SNIPPET, GLOBAL_SNIPPET, COMMENT)
- `filePath`: File path
- `snippet`: Code snippet
- `groupId`: Group ID

#### ReferenceGroup.kt
Defines the data structure for groups, including:
- `id`: Unique identifier
- `name`: Group name

### 2. Service Layer (services/)

#### ReferenceDataService.kt
Core data management service responsible for:
- Loading and saving reference data (JSON format)
- Adding, deleting, updating reference items and groups
- Managing storage paths (based on MD5 hash of project path)
- Lazy loading mechanism (only loads data when tool window is shown)

**Key methods:**
- `getReferences()`: Get all reference items
- `getGroups()`: Get all groups
- `addReference()`: Add reference item
- `deleteReference()`: Delete reference item
- `updateReferenceTitle()`: Update reference item title
- `addGroup()`: Add group
- `deleteGroup()`: Delete group
- `saveReferences()`: Save data to file

### 3. UI Components (ui/)

#### ReferenceListPanel.kt
Main panel of the tool window, responsible for:
- Displaying reference items and group lists
- Handling drag-and-drop sorting
- Handling click events (navigating to code locations)
- Handling right-click context menus
- Rendering different types of reference items (different background and foreground colors)
- Displaying empty state
- Showing storage location button

**Key features:**
- Adaptive width (no scrollbars)
- Automatic foreground color adjustment based on background color (white for dark themes, black for light themes)
- Support for group collapse/expand
- Support for drag-and-drop sorting

### 4. Actions (actions/)

All user operations are implemented through Actions:

- **AddCurrentFileAction**: Add currently opened file
- **AddFileAndSnippetAction**: Add current file and selected code snippet
- **AddGlobalUniqueSnippetAction**: Add globally unique code snippet (automatically searches project to ensure uniqueness)
- **AddCommentAction**: Add user comment
- **AddGroupAction**: Create new group
- **DeleteReferenceAction**: Delete reference item
- **EditReferenceTitleAction**: Edit reference item title
- **RefreshReferencesAction**: Refresh reference list
- **ShowStorageLocationAction**: Show storage file location

### 5. Tool Window (toolWindow/)

#### MyToolWindowFactory.kt
Responsible for creating and managing the tool window:
- Creating tool window content
- Registering toolbar actions (refresh button)
- Listening for tool window display events, triggering lazy loading

### 6. Utility Classes (utils/)

#### NotificationUtils.kt
Provides non-blocking notification functionality:
- `showInfo()`: Show information notification
- `showWarning()`: Show warning notification
- `showError()`: Show error notification

#### ReferenceNavigationUtils.kt
Provides code navigation functionality:
- `navigateToReference()`: Navigate to the code location corresponding to the reference item
- Automatically select code snippet (if exists)

## Development Workflow

### 1. Create Feature Branch

```bash
git checkout -b feature/your-feature-name
```

### 2. Develop New Feature

1. **Modify code**
   - Modify corresponding modules based on feature requirements
   - Follow code standards (see below)

2. **Add tests**
   - Add unit tests for new features
   - Ensure tests pass

3. **Test functionality**
   ```bash
   # Run plugin for testing
   ./gradlew runIde
   ```

4. **Commit code**
   ```bash
   git add .
   git commit -m "feat: add new feature description"
   ```

### 3. Submit Pull Request

1. Push branch to remote repository
2. Create Pull Request on GitHub
3. Wait for code review
4. Modify code based on feedback

## How to Add New Features

### Example: Adding New Reference Type

1. **Modify data model**
   
   Add new type to the `ReferenceType` enum in `ReferenceItem.kt`:
   ```kotlin
   enum class ReferenceType {
       FILE,
       FILE_SNIPPET,
       GLOBAL_SNIPPET,
       COMMENT,
       NEW_TYPE  // New type
   }
   ```

2. **Modify UI rendering**
   
   Add rendering logic for the new type in `ReferenceListPanel.kt`'s `ReferenceListCellRenderer`:
   ```kotlin
   ReferenceType.NEW_TYPE -> {
       if (!isSelected) {
           val bgColor = adjustColor(baseBg, ...)
           background = bgColor
           foreground = getForegroundColor(bgColor)
       }
       AllIcons.General.SomeIcon
   }
   ```

3. **Add corresponding Action**
   
   Create new Action class, e.g., `AddNewTypeAction.kt`:
   ```kotlin
   class AddNewTypeAction : AnAction() {
       override fun actionPerformed(e: AnActionEvent) {
           // Implement add logic
       }
   }
   ```

4. **Register Action**
   
   Register new Action in `plugin.xml`:
   ```xml
   <action id="FileRefTags.AddNewType" 
           class="org.jetbrains.plugins.template.actions.AddNewTypeAction"
           text="Add New Type">
       <add-to-group group-id="FileRefTags.EditorPopupMenu" anchor="first"/>
   </action>
   ```

5. **Add internationalization text**
   
   Add text in `MyBundle.properties`:
   ```properties
   action.addNewType.text=Add New Type
   ```

### Example: Adding New Tool Window Button

1. **Create Action**
   
   Create new Action class, e.g., `NewToolbarAction.kt`

2. **Register to toolbar**
   
   Register in `plugin.xml`:
   ```xml
   <action id="FileRefTags.NewToolbarAction" 
           class="org.jetbrains.plugins.template.actions.NewToolbarAction"
           icon="AllIcons.General.Add">
       <add-to-group group-id="FileRefTags.ToolWindowToolbar" anchor="last"/>
   </action>
   ```

## Code Standards

### Kotlin Code Standards

1. **Naming conventions**
   - Class names: PascalCase, e.g., `ReferenceItem`
   - Function names: camelCase, e.g., `getReferences()`
   - Constants: UPPER_SNAKE_CASE, e.g., `MAX_SIZE`
   - Private properties: camelCase, e.g., `private val dataService`

2. **Code formatting**
   - Use 4 spaces for indentation (not tabs)
   - Line length should not exceed 120 characters
   - Break and align function parameters when there are too many

3. **Comment standards**
   - Public APIs must have KDoc comments
   - Add inline comments for complex logic
   - Use Chinese comments (project uses Chinese uniformly)

4. **Exception handling**
   - Use try-catch to catch exceptions
   - Use `NotificationUtils` to display error messages to users
   - Log error messages (if needed)

### Example Code

```kotlin
/**
 * Add new reference item
 * 
 * @param item Reference item to add
 * @return Whether the addition was successful
 */
fun addReference(item: ReferenceItem): Boolean {
    return try {
        references.add(item)
        saveReferences()
        true
    } catch (e: Exception) {
        NotificationUtils.showError(
            project,
            "Add failed",
            "Cannot add reference item: ${e.message}"
        )
        false
    }
}
```

## Testing

### Running Tests

```bash
# Run all tests
./gradlew test

# Run specific test class
./gradlew test --tests "org.jetbrains.plugins.template.services.ReferenceDataServiceTest"
```

### Writing Tests

Test files should be placed in the `src/test/kotlin/` directory, example:

```kotlin
class ReferenceDataServiceTest {
    @Test
    fun testAddReference() {
        // Test code
    }
}
```

### Manual Testing

1. Run the plugin:
   ```bash
   ./gradlew runIde
   ```

2. Test functionality in the opened IDE:
   - Open tool window
   - Test various operations
   - Check if data is saved correctly

## Building and Packaging

### Building Plugin

```bash
# Build plugin (generates ZIP file)
./gradlew buildPlugin

# Build artifact location
# build/distributions/File Ref Tags-1.0.0.zip
```

### Verifying Plugin

```bash
# Verify plugin compatibility
./gradlew verifyPlugin
```

### Local Installation Testing

1. Build plugin: `./gradlew buildPlugin`
2. In IntelliJ IDEA:
   - **File** → **Settings** → **Plugins**
   - Click gear icon → **Install Plugin from Disk...**
   - Select `build/distributions/File Ref Tags-1.0.0.zip`
   - Restart IDE

## Frequently Asked Questions

### Q: How to debug the plugin?

A: 
1. Set breakpoints in code
2. Run `./gradlew runIde --debug-jvm`
3. Attach debugger in IntelliJ IDEA (port 5005)

Or use IntelliJ IDEA's Run Configuration:
1. Create new "Gradle" run configuration
2. Task: `runIde`
3. Run in Debug mode

### Q: How to view plugin logs?

A: 
- When running `./gradlew runIde`, logs are output to console
- In sandbox IDE: **Help** → **Show Log in Files**
- Log location: `build/idea-sandbox/IC-2024.1/log/idea.log`

### Q: How to modify plugin version?

A: 
Modify the `pluginVersion` property in `gradle.properties`:
```properties
pluginVersion=1.0.1
```

### Q: How to add new dependencies?

A: 
Add in the `dependencies` block in `build.gradle.kts`:
```kotlin
dependencies {
    implementation("com.example:library:1.0.0")
}
```

### Q: What to do if the plugin fails to load?

A: 
1. Check if `plugin.xml` configuration is correct
2. Check for compilation errors: `./gradlew build`
3. Check error messages in log files
4. Ensure IntelliJ Platform version compatibility

### Q: How to implement internationalization?

A: 
1. Add key-value pairs in `MyBundle.properties`:
   ```properties
   my.key=My text
   ```
2. Use in code:
   ```kotlin
   MyBundle.message("my.key")
   ```
3. Support multiple languages: create `MyBundle_zh_CN.properties` and other files

### Q: How to debug UI issues?

A: 
1. Use IntelliJ IDEA's UI Inspector (while running the plugin)
2. Add log output to check component state
3. Use breakpoints to check component properties

## Contribution Guidelines

### Pre-commit Checklist

- [ ] Code follows project code standards
- [ ] All tests pass
- [ ] Added necessary comments and documentation
- [ ] Updated relevant documentation (e.g., README)
- [ ] Commit message is clear and descriptive

### Commit Message Format

Use conventional commit format:
- `feat:` New feature
- `fix:` Bug fix
- `docs:` Documentation update
- `style:` Code formatting changes
- `refactor:` Code refactoring
- `test:` Test related
- `chore:` Build/tool related

Examples:
```
feat: Add support for new reference types
fix: Fix index error during drag-and-drop sorting
docs: Update development documentation
```

### Pull Request Requirements

1. **Clear description**: Explain what was changed and why
2. **Issue association**: If fixing an issue, reference it in the PR description
3. **Adequate testing**: Ensure new features are thoroughly tested
4. **Code review**: Wait for maintainer review before merging

## Getting Help

- **GitHub Issues**: Submit issues or feature requests
- **Discussions**: Discuss in GitHub Discussions
- **Documentation**: View [IntelliJ Platform SDK Documentation](https://plugins.jetbrains.com/docs/intellij/)

---

**Thank you for your contribution!** 🎉
