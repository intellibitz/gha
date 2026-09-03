# gha: Git & GitHub Automation

**gha** (`cc.thevar.gha`) is a **100% self-contained, 100% Kotlin** Git and GitHub automation plugin. **gha runs on gha**—automating its own development, testing, commits, pulls, PRs, and releases.

GitHub users can clone this project and expect **0% system modifications**. All dependencies, Kotlin libraries, Gradle caches, JDK toolchains, and execution state are strictly sandboxed inside the local repository folder (`.gha/`).

## Mission

`gha` creators—**Intellibitz**, **Gemini**, and other AI agents alongside GitHub community contributors—build platform-independent Gradle tasks and plugins written purely in **100% Kotlin** to power end-to-end Git and GitHub automation workflows.

## Key Principles

- **gha Runs on gha**: Self-testing and self-automating via `.github/workflows/gha.yml`.
- **Infinite Loop Guard & Timeout Protection**: `GhaProcessRunner` enforces strict execution timeouts (30s) and non-interactive flags (`GIT_TERMINAL_PROMPT=0`, `GH_NO_PROMPT=1`), preventing infinite hangs or recursion loops.
- **100% Sandboxed Dependencies & JDKs**: All external libraries, Kotlin DSL plugins, and JDK toolchains are downloaded into `.gha/gradle-user-home/`.
- **0% System Modifications**: Zero changes to `~/.gradle/`, user system settings, shell configurations, or global user directories.
- **100% Self-Contained**: No third-party system dependencies or external tooling installers required.
- **100% Kotlin**: Built entirely using Kotlin for type safety, coroutines, DSL capabilities, and multiplatform support.
- **100% Platform Independent**: Runs seamlessly across macOS, Linux, Windows, and containerized CI environments without bash or shell dependencies.
- **100% Secure & Zero Secret Leakage**: Enforces strict security rules to prevent accidental token exposure in logs, task inputs, build reports, or configuration cache.

---

## Safety & Loop Prevention Engine ([`GhaProcessRunner.kt`](file:///home/ramadoss/Projects/AI/gha/src/main/kotlin/cc/thevar/gha/safety/GhaProcessRunner.kt))

To guarantee `gha` never hangs or enters an infinite execution loop:

1. **Strict Execution Timeouts:** Every process call enforces a 30–45 second maximum timeout. If exceeded, the process is killed forcibly.
2. **Non-Interactive Enforcements:** Environment variables `GIT_TERMINAL_PROMPT=0` and `GH_NO_PROMPT=1` prevent processes from waiting for interactive keyboard input.
3. **Recursion Guard:** `GHA_RECURSION_DEPTH` tracking limits nested task calls to a maximum depth of 3, aborting re-entry loops automatically.

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

## Complete Suite of GHA Tasks

### GitHub Automation Tasks
| Task | Description |
| :--- | :--- |
| `./gradlew ghaInit` | Initializes sandboxed GitHub Automation environment (`.gha/`) |
| `./gradlew ghaStatus` | Displays current GitHub Automation project and platform status |
| `./gradlew ghaWorkflow` | Executes platform-independent GitHub automation workflows |

### GitHub Operations Tasks
| Task | Description |
| :--- | :--- |
| `./gradlew ghaPrCreate` | Creates a Pull Request on GitHub |
| `./gradlew ghaPrList` | Lists open Pull Requests on GitHub |
| `./gradlew ghaIssueCreate` | Creates an Issue on GitHub |
| `./gradlew ghaIssueList` | Lists open Issues on GitHub |
| `./gradlew ghaReleaseCreate` | Creates a Release on GitHub |

### Git Operations Tasks
| Task | Description |
| :--- | :--- |
| `./gradlew ghaGitStatus` | Displays current Git repository status, branch, and working tree changes |
| `./gradlew ghaGitCommit` | Stages and commits working tree changes platform-independently |
| `./gradlew ghaGitPush` | Pushes current branch to origin remote |
| `./gradlew ghaGitPull` | Pulls latest changes from remote with rebase |
| `./gradlew ghaGitTag` | Creates and pushes an annotated Git tag |
| `./gradlew ghaGitLog` | Displays recent Git commits |

---

## Project Structure

```
gha/
├── .github/workflows/
│   └── gha.yml                                  # Self-testing GHA CI automation workflow
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
│   ├── safety/
│   │   └── GhaProcessRunner.kt                  # Timeouts, non-interactive flags, recursion guard
│   ├── git/
│   │   ├── GhaGitExec.kt                        # 100% Kotlin Git execution engine
│   │   ├── GhaGitStatusTask.kt                  # Git status task
│   │   ├── GhaGitCommitTask.kt                  # Git stage & commit task
│   │   ├── GhaGitPushTask.kt                    # Git push task
│   │   ├── GhaGitPullTask.kt                    # Git pull task
│   │   ├── GhaGitTagTask.kt                     # Git tag & push task
│   │   └── GhaGitLogTask.kt                     # Git log task
│   ├── github/
│   │   ├── GhaPrCreateTask.kt                   # PR create task
│   │   ├── GhaPrListTask.kt                     # PR list task
│   │   ├── GhaIssueCreateTask.kt                # Issue create task
│   │   ├── GhaIssueListTask.kt                  # Issue list task
│   │   └── GhaReleaseCreateTask.kt              # Release create task
│   └── security/
│       └── GhaCredentialsResolver.kt            # Secure GhAuthTokenValueSource & maskToken
└── README.md                                    # Project documentation
```

## Contributing

Contributions are welcome from both human developers and AI agents! Check open issues or submit pull requests following 100% Kotlin and platform-independent conventions.

## License

This project is licensed under the [MIT License](LICENSE).
