# File Ref Tags - JetBrains IDE Code Reference Management Plugin

[中文文档](README_zh.md)

<!-- Plugin description -->
**File Ref Tags** is a JetBrains IDE plugin for managing and quickly accessing code references. It allows users to add files, code snippets, and comments to a reference panel, enabling fast navigation to corresponding locations and improving development efficiency for large projects.

## Features

- Add files, code snippets, and comments to a reference panel
- Quick navigation to code locations
- Drag and drop sorting
- Group management
<!-- Plugin description end -->

## Overview

File Ref Tags is a JetBrains IDE plugin that helps developers manage and quickly access code references in large projects. It provides a convenient tool window where you can organize files, code snippets, and comments for easy navigation.

### Core Features

- **Add Current File**: Add the current open file to the reference panel
- **Add File + Snippet**: Add the current file and selected code snippet to the reference panel
- **Add Global Snippet**: Add the selected globally unique snippet to the reference panel
- **Add User Comment**: Add custom comments to the reference panel
- **Group Management**: Organize reference items into groups for better organization
  - Create groups to categorize references
  - Drag and drop items into groups
  - Expand/collapse groups
  - Reorder groups by dragging
- **Type Color Differentiation**: Different colors for different types of reference items
  - File: Light yellow background
  - File + Snippet: Light green background
  - Global Snippet: Light pink background
  - Comment: Light gray background
  - Groups: Light blue background
- **Drag and Drop Sorting**: Support dragging to reorder reference items and groups
- **Quick Jump**: Click reference items to quickly jump to corresponding locations
- **Edit Title**: Right-click to edit reference item titles
- **Delete Reference**: Right-click to delete reference items
- **Refresh References**: Click the refresh button in the tool window title bar to reload data from storage
- **View Storage Location**: Show the storage location of reference data

## Installation

### From JetBrains Plugin Repository

1. Open your JetBrains IDE (IntelliJ IDEA, WebStorm, PyCharm, etc.)
2. Go to **File** → **Settings** (or **Preferences** on macOS)
3. Navigate to **Plugins**
4. Click **Marketplace** tab
5. Search for "File Ref Tags"
6. Click **Install**
7. Restart the IDE when prompted

### Manual Installation

1. Download the plugin ZIP file from the releases page
2. Go to **File** → **Settings** → **Plugins**
3. Click the gear icon → **Install Plugin from Disk...**
4. Select the downloaded ZIP file
5. Restart the IDE

## Usage

### 1. Open Reference Panel

Go to **View** → **Tool Windows** → **File Ref Tags**, or use the tool window button in the IDE's tool window bar to open the reference panel.

### 2. Add Reference Items

#### 2.1 Add Current File

- Open the file you want to add
- Right-click in the editor, select **FileRefTags** → **Add Current File to Panel**
- Or use Find Action (`Ctrl+Shift+A` / `Cmd+Shift+A`) and search for "Add Current File to Panel"

#### 2.2 Add Current File + Selected Snippet

- Open a file and select the code snippet you want to add
- Right-click in the editor, select **FileRefTags** → **Add Current File + Selected Snippet to Panel**
- Or use Find Action (`Ctrl+Shift+A` / `Cmd+Shift+A`) and search for "Add Current File + Selected Snippet to Panel"

#### 2.3 Add Selected Globally Unique Snippet

- Open a file and select the code snippet you want to add
- Right-click in the editor, select **FileRefTags** → **Add Current Selected Globally Unique Snippet to Panel**
- Or use Find Action (`Ctrl+Shift+A` / `Cmd+Shift+A`) and search for "Add Current Selected Globally Unique Snippet to Panel"
- The plugin will automatically search the project to ensure the snippet is unique

#### 2.4 Add User Comment

- Right-click in the editor, select **FileRefTags** → **Add User Comment to Panel**
- Or use Find Action (`Ctrl+Shift+A` / `Cmd+Shift+A`) and search for "Add User Comment to Panel"
- Enter the comment content in the popup input box and click OK

### 3. Manage Reference Items

#### 3.1 Edit Reference Item Title

- Right-click on a reference item in the panel
- Select **Edit** from the context menu
- Modify the title in the popup window
- Click **Save** or press `Enter` to save
- Click **Cancel** or press `Escape` to cancel

#### 3.2 Delete Reference Item

- Right-click on a reference item in the panel
- Select **Delete** from the context menu
- The reference item will be deleted

#### 3.3 Drag and Drop Sorting

- Click and drag the reference item to the target position
- Release the mouse, the reference item will be moved to the new position
- You can also drag reference items onto groups to organize them
- Groups can also be dragged to reorder them

#### 3.4 Jump to Reference Location

- Click the reference item, the plugin will automatically jump to the corresponding file or code snippet
- For file + snippet and global snippet types, the corresponding code snippet will be automatically selected
- Click on a group header to expand or collapse the group

#### 3.5 Group Management

##### 3.5.1 Add Group

- Click the **Add Group** button in the toolbar at the top of the reference panel
- Enter the group name in the popup dialog
- Click **OK** to create the group

##### 3.5.2 Organize Items into Groups

- Drag and drop a reference item onto a group header to add it to that group
- Drag and drop a reference item to an empty area to remove it from its group

##### 3.5.3 Expand/Collapse Groups

- Click on a group header to expand or collapse the group
- Collapsed groups show a "▶" icon, expanded groups show a "▼" icon

##### 3.5.4 Reorder Groups

- Drag and drop group headers to reorder them
- The order is automatically saved

#### 3.6 Refresh References

- Click the **Refresh** button (🔄 icon) in the tool window title bar (next to the gear icon)
- The plugin will reload reference data from the storage file
- Useful when you've modified the storage file externally
- A message will show the number of references and groups loaded after refresh

#### 3.7 View Storage Location

- Click the **Show Storage Location** button in the toolbar at the top of the reference panel
- The plugin will open the JSON file that stores the reference data in the file explorer
- The file will also be opened in the editor for viewing and editing

## Data Storage

The plugin uses a multi-project storage architecture. Each project has its own isolated reference data:

### Storage Structure

```
~/.file-ref-tags/
├── projects.json          # Project path to folder mapping
├── <hash1>/              # Project 1 storage folder (MD5 hash of project path)
│   └── references.json   # Project 1 reference data
├── <hash2>/              # Project 2 storage folder
│   └── references.json   # Project 2 reference data
└── default/              # Default folder for projects without path
    └── references.json
```

### Storage Location

- **Base Directory**:
  - **Windows**: `%USERPROFILE%\.file-ref-tags\`
  - **macOS/Linux**: `~/.file-ref-tags/`

- **Project Mapping**: `projects.json` stores the mapping between project paths and their storage folders
- **Project Data**: Each project's `references.json` is stored in its own folder (identified by MD5 hash of the project path)

### Benefits

- **Isolated Data**: Each project has its own reference data, preventing conflicts
- **Automatic Management**: The plugin automatically creates and manages project folders
- **Path Normalization**: Project paths are normalized (case-insensitive, unified separators) for cross-platform compatibility

The storage location for the current project can be viewed through the "Show Storage Location" button in the tool window toolbar.

## System Requirements

- **IntelliJ Platform**: 2023.1 or later
- **Supported IDEs**: IntelliJ IDEA, WebStorm, PyCharm, PhpStorm, RubyMine, GoLand, CLion, Rider, DataGrip, and other JetBrains IDEs
- **Operating System**: Supports Windows, macOS, and Linux

## Development

This project is built using the [IntelliJ Platform Plugin Template][gh:template]. 

**For detailed development documentation, please see [DEVELOPMENT.md](DEVELOPMENT.md) (English) or [DEVELOPMENT_zh.md](DEVELOPMENT_zh.md) (中文).**

For quick start, see the sections below.

### Getting Started

1. Clone this repository
2. Open the project in IntelliJ IDEA
3. Set the SDK to Java 21 in **File** → **Project Structure** → **Project Settings** → **Project**
4. Review and update configuration in `gradle.properties`
5. Run `./gradlew buildPlugin` to build the plugin
6. Run `./gradlew runIde` to test the plugin in a sandbox IDE instance

### Gradle Configuration

The project uses [Gradle][gradle] with the [IntelliJ Platform Gradle Plugin][gh:intellij-platform-gradle-plugin] for building and publishing.

Key Gradle properties in `gradle.properties`:

| Property name            | Description                                                                                          |
|--------------------------|------------------------------------------------------------------------------------------------------|
| `pluginGroup`            | Package name: `lirentech.fileRefTags`                                                               |
| `pluginName`             | Plugin name displayed in JetBrains Marketplace: `File Ref Tags`                                      |
| `pluginRepositoryUrl`    | Repository URL: `https://github.com/LiRenTech/file-ref-tags`                                        |
| `pluginVersion`          | The current version of the plugin in [SemVer][semver] format: `1.0.0`                               |
| `pluginSinceBuild`       | The `since-build` attribute: `231` (2023.1)                                                           |
| `platformVersion`        | The version of the IntelliJ Platform IDE: `2023.1`                                                   |
| `gradleVersion`          | Version of Gradle used: `9.2.1`                                                                     |

### Project Structure

```
.
├── .github/                GitHub Actions workflows
├── .run/                   Predefined Run/Debug Configurations
├── gradle
│   ├── wrapper/            Gradle Wrapper
│   └── libs.versions.toml  Gradle version catalog
├── src                     Plugin sources
│   ├── main
│   │   ├── kotlin/         Kotlin production sources
│   │   │   └── org/jetbrains/plugins/template/
│   │   │       ├── model/          Data models (ReferenceItem, ReferenceGroup)
│   │   │       ├── services/       Services (ReferenceDataService)
│   │   │       ├── ui/             UI components (ReferenceListPanel)
│   │   │       ├── actions/       Actions (AddCurrentFileAction, etc.)
│   │   │       └── toolWindow/    Tool window factory
│   │   └── resources/      Resources - plugin.xml, icons, messages
│   └── test
│       ├── kotlin/         Kotlin test sources
│       └── testData/       Test data used by tests
├── build.gradle.kts        Gradle configuration
├── CHANGELOG.md            Full change history
├── gradle.properties       Gradle configuration properties
├── gradlew                 *nix Gradle Wrapper script
├── gradlew.bat             Windows Gradle Wrapper script
├── LICENSE                 License, MIT by default
├── qodana.yml              Qodana configuration file
└── settings.gradle.kts     Gradle project settings
```

### Building the Plugin

```bash
# Build the plugin
./gradlew buildPlugin

# Run the plugin in a sandbox IDE
./gradlew runIde

# Run tests
./gradlew test

# Verify plugin compatibility
./gradlew verifyPlugin
```

### Testing

The project includes test infrastructure for both functional and UI tests.

#### Functional Tests

Run tests using:
```bash
./gradlew test
```

Or use the predefined *Run Tests* configuration in IntelliJ IDEA.

#### Code Coverage

Code coverage is provided by [Kover][gh:kover]. Run tests with coverage:
```bash
./gradlew test
```

Coverage reports are available in `build/reports/kover/`.

### Predefined Run/Debug Configurations

The project includes predefined Run/Debug configurations in the `.run` directory:

| Configuration name | Description                                                                                                         |
|--------------------|---------------------------------------------------------------------------------------------------------------------|
| Run Plugin         | Runs `:runIde` task. Use the *Debug* icon for plugin debugging.                                                    |
| Run Tests          | Runs `:test` Gradle task.                                                                                           |
| Run Verifications  | Runs `:verifyPlugin` task to check plugin compatibility against specified IntelliJ IDEs.                           |

### Continuous Integration

The project uses GitHub Actions for continuous integration. Workflows are defined in `.github/workflows/`:

- **Build**: Runs on push and pull requests, builds and verifies the plugin
- **Release**: Publishes the plugin to JetBrains Marketplace when a release is created
- **Run UI Tests**: Manual workflow for running UI tests on multiple platforms

### Publishing

To publish the plugin to JetBrains Marketplace:

1. Create a plugin entry in [JetBrains Marketplace](https://plugins.jetbrains.com)
2. Generate a publishing token in your Marketplace profile
3. Set the `PUBLISH_TOKEN` environment variable (or GitHub secret)
4. Create a GitHub release to trigger the publishing workflow

For more details, see the [Publishing a Plugin][docs:publishing] documentation.

## FAQ

### How do I add a new reference type?

Reference types are defined in the `ReferenceType` enum in `src/main/kotlin/org/jetbrains/plugins/template/model/ReferenceItem.kt`. Add new types there and update the UI rendering logic.

### How do I customize the storage location?

The storage location is determined in `ReferenceDataService.kt`. Modify the `init` block to change the storage path.

### Can I use this plugin with multiple projects?

Yes, the plugin stores references per project. Each project has its own `references.json` file.

## Feedback and Suggestions

If you encounter any issues or have suggestions, please:
- Open an issue on [GitHub](https://github.com/LiRenTech/file-ref-tags/issues)
- Submit a pull request
- Contact the maintainers

## License

This project uses the [MIT License](../LICENSE), copyright belongs to LiRenTech.

## Useful Links

- [IntelliJ Platform SDK Plugin SDK][docs]
- [IntelliJ Platform Gradle Plugin Documentation][gh:intellij-platform-gradle-plugin-docs]
- [IntelliJ Platform Explorer][jb:ipe]
- [JetBrains Marketplace Quality Guidelines][jb:quality-guidelines]
- [IntelliJ Platform UI Guidelines][jb:ui-guidelines]
- [IntelliJ SDK Code Samples][gh:code-samples]
- [JetBrains Platform Slack][jb:slack]
- [JetBrains Platform Twitter][jb:twitter]

---

**Enjoy coding with File Ref Tags!**

[docs]: https://plugins.jetbrains.com/docs/intellij?from=IJPluginTemplate
[docs:publishing]: https://plugins.jetbrains.com/docs/intellij/publishing-plugin.html?from=IJPluginTemplate
[gh:template]: https://github.com/JetBrains/intellij-platform-plugin-template
[gh:intellij-platform-gradle-plugin]: https://github.com/JetBrains/intellij-platform-gradle-plugin
[gh:intellij-platform-gradle-plugin-docs]: https://plugins.jetbrains.com/docs/intellij/tools-intellij-platform-gradle-plugin.html
[gh:code-samples]: https://github.com/JetBrains/intellij-sdk-code-samples
[gh:kover]: https://github.com/Kotlin/kotlinx-kover
[gradle]: https://gradle.org
[semver]: https://semver.org
[jb:ipe]: https://jb.gg/ipe
[jb:quality-guidelines]: https://plugins.jetbrains.com/docs/marketplace/quality-guidelines.html
[jb:ui-guidelines]: https://jetbrains.github.io/ui
[jb:slack]: https://plugins.jetbrains.com/slack
[jb:twitter]: https://twitter.com/JBPlatform
