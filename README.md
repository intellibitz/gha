# gha: Git & GitHub Automation

**gha** (`cc.thevar.gha`) is a **100% self-contained, 100% Kotlin** Git and GitHub automation plugin.

GitHub users can clone this project and expect **0% system modifications**. All dependencies, Kotlin libraries, Gradle caches, JDK toolchains, and execution state are strictly sandboxed inside the local repository folder (`.gha/`).

## Mission

`gha` creators—**Intellibitz**, **Gemini**, and other AI agents alongside GitHub community contributors—build platform-independent Gradle tasks and plugins written purely in **100% Kotlin** to power end-to-end Git and GitHub automation workflows.

## Key Principles

- **100% Sandboxed Dependencies & JDKs**: All external libraries, Kotlin DSL plugins, and JDK toolchains are downloaded into `.gha/gradle-user-home/`.
- **0% System Modifications**: Zero changes to `~/.gradle/`, user system settings, shell configurations, or global user directories.
- **100% Self-Contained**: No third-party system dependencies or external tooling installers required.
- **100% Kotlin**: Built entirely using Kotlin for type safety, coroutines, DSL capabilities, and multiplatform support.
- **100% Platform Independent**: Runs seamlessly across macOS, Linux, Windows, and containerized CI environments without bash or shell dependencies.
- **100% Secure & Zero Secret Leakage**: Enforces strict security rules to prevent accidental token exposure in logs, task inputs, build reports, or configuration cache.

---

## Sandboxed Dependency & Toolchain Architecture

All project dependencies (Kotlin stdlib, Gradle plugins) and JDK toolchains are downloaded and executed **100% inside the local repository sandbox** (`.gha/gradle-user-home/`):

- **Sandboxed Gradle User Home:** `.gha/gradle-user-home/`
- **Sandboxed Dependency Caches:** `.gha/gradle-user-home/caches/`
- **Sandboxed JDK Toolchains:** `.gha/gradle-user-home/jdks/`

### Impact on User Experience (UX Analysis)

| Aspect | Behavior & UX Impact |
| :--- | :--- |
| **System Cleanliness** | **0% System Pollution:** No files, dependencies, or JDKs are stored in `~/.gradle/` or global system paths. |
| **Portability & Isolation** | **100% Deterministic:** Every developer and CI machine runs in complete isolation without host dependency conflicts. |
| **Initial Build Download** | **Higher Initial Download:** The very first build downloads Kotlin libraries (~50MB) and JDK toolchain (~200MB) directly into `.gha/gradle-user-home/`. |
| **Subsequent Build Speed** | **Fast & Cached:** Subsequent builds in the cloned repo reuse the sandboxed cache and run in < 1 second. |
| **Clean Removal** | **Instant Clean:** Running `rm -rf gha` removes 100% of the project, dependencies, caches, and downloaded JDKs. |

---

## Security & Sandboxing Guarantees

1. **Local Project Sandbox:** Configuration is written exclusively to `.gha/gha.json` inside the local repository root.
2. **Non-Leaking Task Inputs:** All token properties are annotated with `@get:Internal` so secrets are **never recorded in Gradle task cache, build scans, or reports**.
3. **Masked Logging:** Any console or log output automatically masks sensitive tokens (e.g., `ghp_...JReW`).
4. **Configuration-Cache Safe:** Uses Gradle `ValueSource` to securely query system credentials (`gh auth token`) without violating configuration cache constraints.

---

## How to Use

### Option 1: Self-Contained Init Script (Zero Modifications)

Run `gha` tasks on any cloned project without changing system or build files:

```bash
# Run ghaInit or ghaGitStatus in local sandbox
./gradlew --init-script init/gha.init.gradle.kts ghaGitStatus
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
├── settings.gradle.kts                          # Gradle settings with Foojay sandboxed JDK resolver
├── gradle.properties                            # Sandboxed org.gradle.user.home=.gha/gradle-user-home
├── gradlew / gradlew.bat                        # Wrapper configuring sandboxed GRADLE_USER_HOME
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
