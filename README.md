# gha: Git & GitHub Automation

**gha** (`cc.thevar.gha`) provides 100% Kotlin, 100% platform-independent Gradle tasks for Git and GitHub automation workflows that can be run on **any Gradle project in the world with zero effort**.

## Mission

`gha` creators—**Intellibitz**, **Gemini**, and other AI agents alongside GitHub community contributors—build platform-independent Gradle tasks and plugins written purely in **100% Kotlin** to power end-to-end Git and GitHub automation workflows.

## Key Principles

- **100% Kotlin**: Built entirely using Kotlin for type safety, coroutines, DSL capabilities, and multiplatform support.
- **100% Platform Independent**: Runs seamlessly across macOS, Linux, Windows, and containerized CI environments without bash or shell dependencies.
- **Git & GitHub Automation Workflows**: Automates working tree operations, commits, pushes, tags, repository setup, issue management, PR checks, releases, and agent workflows.
- **100% Secure & Zero Secret Leakage**: Enforces strict security rules to prevent accidental token exposure in logs, task inputs, build reports, or configuration cache.
- **Zero Effort for Any Project**: Run GHA tasks on any project worldwide instantly without editing target project files.

---

## Security & Credential Management

`gha` handles Git and GitHub credentials with zero secret leakage:

1. **Non-Leaking Task Inputs:** All token properties are annotated with `@get:Internal` so secrets are **never recorded in Gradle task cache, build scans, or reports**.
2. **Masked Logging:** Any console or log output automatically masks sensitive tokens (e.g., `ghp_...JReW`).
3. **Configuration-Cache Safe:** Uses Gradle `ValueSource` to securely query system credentials (`gh auth token`) without violating configuration cache constraints.
4. **Resolution Order:**
   - Environment Variable: `GITHUB_TOKEN`
   - Environment Variable: `GH_TOKEN`
   - Gradle Property: `gha.github.token` (in `~/.gradle/gradle.properties`)
   - System GitHub CLI: `gh auth token`

---

## How to Use

### Option 1: Zero-Effort Init Script (No Modifications to Target Project)

Run `gha` tasks on **any Gradle project in the world** without changing its source code or build files:

```bash
# Run ghaInit or ghaGitStatus on any project
gradle --init-script https://raw.githubusercontent.com/intellibitz/gha/main/init/gha.init.gradle.kts ghaGitStatus
```

Or install globally on your machine/CI runner by copying `gha.init.gradle.kts` to `~/.gradle/init.d/`:

```bash
mkdir -p ~/.gradle/init.d/
cp init/gha.init.gradle.kts ~/.gradle/init.d/gha.init.gradle.kts
```

Once placed in `~/.gradle/init.d/`, **every Gradle project automatically gets GHA Git & GitHub tasks available out of the box!**

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

### GitHub Automation Tasks
| Task | Description |
| :--- | :--- |
| `./gradlew ghaInit` | Initializes GitHub Automation workflows for the project |
| `./gradlew ghaStatus` | Displays the current GitHub Automation project and platform status |
| `./gradlew ghaWorkflow` | Executes platform-independent GitHub automation workflows |

### Git Automation Tasks
| Task | Description |
| :--- | :--- |
| `./gradlew ghaGitStatus` | Displays current Git repository status, branch, and working tree changes |
| `./gradlew ghaGitCommit` | Stages and commits working tree changes platform-independently |
| `./gradlew ghaGitPush` | Pushes current branch to origin remote |
| `./gradlew ghaGitTag` | Creates and pushes an annotated Git tag |

---

## Project Structure

```
gha/
├── build.gradle.kts                             # Gradle Plugin build configuration
├── settings.gradle.kts                          # Gradle settings
├── init/
│   └── gha.init.gradle.kts                      # Zero-effort init script for global injection
├── src/main/kotlin/cc/thevar/gha/
│   ├── GhaPlugin.kt                             # Core Gradle Plugin registering Git & GitHub tasks
│   ├── GhaTask.kt                               # Base Task with secret handling
│   ├── GhaInitTask.kt                           # 100% Kotlin GHA Init Task
│   ├── GhaStatusTask.kt                         # 100% Kotlin GHA Status Task
│   ├── GhaWorkflowTask.kt                       # 100% Kotlin GHA Workflow Task
│   ├── git/
│   │   ├── GhaGitExec.kt                        # 100% Kotlin Git execution engine
│   │   ├── GhaGitStatusTask.kt                  # Git status task
│   │   ├── GhaGitCommitTask.kt                  # Git stage & commit task
│   │   ├── GhaGitPushTask.kt                    # Git push task
│   │   └── GhaGitTagTask.kt                     # Git tag & push task
│   └── security/
│       └── GhaCredentialsResolver.kt            # Secure GhAuthTokenValueSource & maskToken
└── README.md                                    # Project documentation
```

## Contributing

Contributions are welcome from both human developers and AI agents! Check open issues or submit pull requests following 100% Kotlin and platform-independent conventions.

## License

This project is licensed under the [MIT License](LICENSE).
