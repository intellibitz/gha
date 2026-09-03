# gha: Git & GitHub Automation

**gha** (`cc.thevar.gha`) is a **100% self-contained, 100% Kotlin** Git and GitHub automation plugin.

GitHub users can clone this project and expect **0% system modifications**. All GHA configuration and state are strictly sandboxed inside the local repository folder (`.gha/` and `build/`).

## Mission

`gha` creators—**Intellibitz**, **Gemini**, and other AI agents alongside GitHub community contributors—build platform-independent Gradle tasks and plugins written purely in **100% Kotlin** to power end-to-end Git and GitHub automation workflows.

## Key Principles

- **100% Self-Contained**: No third-party system dependencies or external tooling installers required.
- **0% System Modifications**: Zero changes to user system settings, shell configurations, or global user directories.
- **100% Sandboxed**: All execution state, logs, and configuration remain strictly inside `.gha/` and `build/` within the cloned repository.
- **100% Kotlin**: Built entirely using Kotlin for type safety, coroutines, DSL capabilities, and multiplatform support.
- **100% Platform Independent**: Runs seamlessly across macOS, Linux, Windows, and containerized CI environments without bash or shell dependencies.
- **100% Secure & Zero Secret Leakage**: Enforces strict security rules to prevent accidental token exposure in logs, task inputs, build reports, or configuration cache.

---

## Security & Sandboxing Architecture

`gha` operates in a local sandbox with complete security guarantees:

1. **Local Project Sandbox:** State is written exclusively to `.gha/gha.json` inside the local repository root.
2. **Non-Leaking Task Inputs:** All token properties are annotated with `@get:Internal` so secrets are **never recorded in Gradle task cache, build scans, or reports**.
3. **Masked Logging:** Any console or log output automatically masks sensitive tokens (e.g., `ghp_...JReW`).
4. **Configuration-Cache Safe:** Uses Gradle `ValueSource` to securely query system credentials (`gh auth token`) without violating configuration cache constraints.

---

## How to Use

### Option 1: Self-Contained Init Script (Zero Modifications)

Run `gha` tasks on any cloned project without changing system or build files:

```bash
# Run ghaInit in local sandbox
./gradlew --init-script init/gha.init.gradle.kts ghaInit
```

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
| `./gradlew ghaInit` | Initializes sandboxed GitHub Automation environment (`.gha/`) |
| `./gradlew ghaStatus` | Displays current GitHub Automation project and platform status |
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
│   └── gha.init.gradle.kts                      # Self-contained init script
├── src/main/kotlin/cc/thevar/gha/
│   ├── GhaPlugin.kt                             # Core Gradle Plugin registering Git & GitHub tasks
│   ├── GhaTask.kt                               # Base Task with secret handling
│   ├── GhaInitTask.kt                           # Sandboxed GHA Init Task (.gha/)
│   ├── GhaStatusTask.kt                         # GHA Status Task
│   ├── GhaWorkflowTask.kt                       # GHA Workflow Task
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
