# gha: GitHub Automation

**gha** (`cc.thevar.gha`) provides 100% Kotlin, 100% platform-independent Gradle tasks for GitHub automation workflows that can be run on **any Gradle project in the world with zero effort**.

## Mission

`gha` creators—**Intellibitz**, **Gemini**, and other AI agents alongside GitHub community contributors—build platform-independent Gradle tasks and plugins written purely in **100% Kotlin** to power end-to-end GitHub automation workflows.

## Key Principles

- **100% Kotlin**: Built entirely using Kotlin for type safety, coroutines, DSL capabilities, and multiplatform support.
- **100% Platform Independent**: Runs seamlessly across macOS, Linux, Windows, and containerized CI environments without bash or shell dependencies.
- **100% GitHub Automation Workflows**: Automates repository setup, issue management, PR checks, releases, code analysis, and agent workflows.
- **Zero Effort for Any Project**: Run GHA tasks on any project worldwide instantly without editing target project files.

---

## How to Use

### Option 1: Zero-Effort Init Script (No Modifications to Target Project)

Run `gha` tasks on **any Gradle project in the world** without changing its source code or build files:

```bash
# Run ghaInit on any project
gradle --init-script https://raw.githubusercontent.com/intellibitz/gha/main/init/gha.init.gradle.kts ghaInit
```

Or install globally on your machine/CI runner by copying `gha.init.gradle.kts` to `~/.gradle/init.d/`:

```bash
mkdir -p ~/.gradle/init.d/
cp init/gha.init.gradle.kts ~/.gradle/init.d/gha.init.gradle.kts
```

Once placed in `~/.gradle/init.d/`, **every Gradle project automatically gets GHA tasks available out of the box!**

---

### Option 2: Standard Gradle Plugin

Add the plugin to your project's `build.gradle.kts`:

```kotlin
plugins {
    id("cc.thevar.gha") version "0.1.0"
}
```

---

## Available GHA Tasks

| Task | Description |
| :--- | :--- |
| `./gradlew ghaInit` | Initializes GitHub Automation workflows for the project |
| `./gradlew ghaStatus` | Displays the current GitHub Automation project and platform status |
| `./gradlew ghaWorkflow` | Executes platform-independent GitHub automation workflows |

---

## Project Structure

```
gha/
├── build.gradle.kts                             # Gradle Plugin build configuration
├── settings.gradle.kts                          # Gradle settings
├── init/
│   └── gha.init.gradle.kts                      # Zero-effort init script for global injection
├── src/main/kotlin/cc/thevar/gha/
│   ├── GhaPlugin.kt                             # Core Gradle Plugin
│   ├── GhaInitTask.kt                           # 100% Kotlin GHA Init Task
│   ├── GhaStatusTask.kt                         # 100% Kotlin GHA Status Task
│   └── GhaWorkflowTask.kt                       # 100% Kotlin GHA Workflow Task
└── README.md                                    # Project documentation
```

## Contributing

Contributions are welcome from both human developers and AI agents! Check open issues or submit pull requests following 100% Kotlin and platform-independent conventions.

## License

This project is licensed under the [MIT License](LICENSE).
