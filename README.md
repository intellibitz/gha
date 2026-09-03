# gha: Git & GitHub Automation

**gha** (`cc.thevar.gha`) is a **100% self-contained, 100% Kotlin** Git and GitHub automation plugin. **gha runs on gha**—automating its own development, testing, dependencies, commits, pulls, PRs, and releases.

GitHub users and developers across any IDE or terminal can clone this project and expect **0% system modifications**. All dependencies, Kotlin libraries, Gradle caches, JDK toolchains, and execution state are strictly sandboxed inside the local repository folder (`.gha/`).

## Mission

`gha` creators—**Intellibitz**, **Gemini**, and other AI agents alongside GitHub community contributors—build platform-independent Gradle tasks and plugins written purely in **100% Kotlin** to power end-to-end Git and GitHub automation workflows.

## Key Principles

- **100% IDE-Agnostic & CLI-First**: Supports Android Studio, IntelliJ IDEA, VS Code, Eclipse, Vim/Emacs, and CI pipelines equally. IDE-specific files (`.idea/`, `.vscode/`) are git-ignored.
- **Strict Official Stable Versions & Trusted Vendors**: `gha` strictly uses official stable releases from verified trusted vendors (`JetBrains`, `Gradle Inc.`, `Eclipse Adoptium`, `Oracle`, `GitHub Inc.`). No alpha, beta, rc, or untrusted third-party repositories.
- **gha Runs on gha**: Self-testing and self-automating via `.github/workflows/gha.yml`.
- **Centralized Version Catalog ([`GhaConfig.kt`](file:///home/ramadoss/Projects/AI/gha/src/main/kotlin/cc/thevar/gha/config/GhaConfig.kt))**: Single source of truth for tools, SDKs, frameworks, and plugin versions.
- **Infinite Loop Guard & Timeout Protection**: `GhaProcessRunner` enforces strict execution timeouts (30s) and non-interactive flags (`GIT_TERMINAL_PROMPT=0`, `GH_NO_PROMPT=1`), preventing infinite hangs or recursion loops.
- **100% Sandboxed Dependencies & JDKs**: All external libraries, Kotlin DSL plugins, and JDK toolchains are downloaded into `.gha/gradle-user-home/`.
- **0% System Modifications**: Zero changes to `~/.gradle/`, user system settings, shell configurations, or global user directories.
- **100% Self-Contained**: No third-party system dependencies or external tooling installers required.
- **100% Kotlin**: Built entirely using Kotlin for type safety, coroutines, DSL capabilities, and multiplatform support.
- **100% Platform Independent**: Runs seamlessly across macOS, Linux, Windows, and containerized CI environments without bash or shell dependencies.
- **100% Secure & Zero Secret Leakage**: Enforces strict security rules to prevent accidental token exposure in logs, task inputs, build reports, or configuration cache.

---

## IDE Independence & Collaboration

`gha` is **100% IDE-agnostic and CLI-first**. Developers can use any editor or IDE:

- **Android Studio / IntelliJ IDEA:** Reads `gradle.properties` (`org.gradle.user.home=.gha/gradle-user-home`) and `settings.gradle.kts` (Foojay toolchain resolver) automatically.
- **VS Code / Eclipse / Terminal / Vim / Emacs:** Runs seamlessly via standard `./gradlew` commands.
- **IDE Metadata (`.idea/` & `.vscode/`):** Git-ignored to ensure clean collaboration across different OS environments, IDE versions, and developer preferences without merge conflicts.

---

## Trusted Vendors & Official Stable Version Policy

All tools, SDKs, frameworks, and plugins are defined centrally in `gradle/libs.versions.toml` and [`GhaConfig.kt`](file:///home/ramadoss/Projects/AI/gha/src/main/kotlin/cc/thevar/gha/config/GhaConfig.kt):

| Tool / Dependency | Category | Trusted Vendor | Configured Version | Status |
| :--- | :--- | :--- | :--- | :--- |
| **Java / JDK Toolchain** | SDK | Eclipse Adoptium (Temurin) / Oracle | `25` | ✅ Official Stable |
| **Kotlin Language & DSL** | Framework | JetBrains | `2.1.0` | ✅ Official Stable |
| **Gradle Build Engine** | Build Tool | Gradle Inc. | `9.7.1` | ✅ Official Stable |
| **Gradle Plugin Publish** | Plugin | Gradle Inc. | `1.3.1` | ✅ Official Stable |
| **Foojay JDK Resolver** | Plugin | Foojay / Gradle Inc. | `0.9.0` | ✅ Official Stable |
| **Git VCS Engine** | CLI Tool | Software Freedom Conservancy | Latest Compatible | ✅ Official Stable |
| **GitHub CLI (`gh`)** | CLI Tool | GitHub Inc. | Latest Compatible | ✅ Official Stable |

Run `./gradlew --init-script init/gha.init.gradle.kts ghaDependencies` to inspect real-time version status and vendor verification in a structured table.

---

## How to Use

### Option 1: Self-Contained Init Script (Zero Modifications)

Run `gha` tasks on any cloned project without changing system or build files:

```bash
# Initialize sandbox, check status, and verify dependencies
./gradlew --init-script init/gha.init.gradle.kts ghaInit ghaStatus ghaDependencies
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
| `./gradlew ghaDependencies` | Prints all GHA dependencies, trusted vendors, and active runtime versions |
| `./gradlew ghaWorkflow` | Executes platform-independent GitHub automation workflows |

### GitHub Wiki Tasks
| Task | Description |
| :--- | :--- |
| `./gradlew ghaWikiInit` | Creates local `wiki/` documentation directory and template pages (`Home.md`, `_Sidebar.md`, etc.) |
| `./gradlew ghaWikiStatus` | Displays local wiki pages, sizes, remote wiki URL, and sync status |
| `./gradlew ghaWikiSync` | Pulls latest remote GitHub Wiki pages into local `wiki/` directory |
| `./gradlew ghaWikiPublish` | Commits and pushes local `wiki/` pages to remote GitHub Wiki repository |

### GitHub Issue Operations Tasks
| Task | Description | Usage Example |
| :--- | :--- | :--- |
| `./gradlew ghaIssueCreate` | Creates a new Issue on GitHub | `./gradlew ghaIssueCreate -PissueTitle="Bug title" -PissueBody="Details"` |
| `./gradlew ghaIssueList` | Lists Issues (supports open/closed/all filtering) | `./gradlew ghaIssueList [-PissueState=open\|closed\|all]` |
| `./gradlew ghaIssueView` | Displays details and comments for an Issue | `./gradlew ghaIssueView -PissueNumber=1` |
| `./gradlew ghaIssueComment` | Adds a comment to an Issue | `./gradlew ghaIssueComment -PissueNumber=1 -PcommentBody="Comment text"` |
| `./gradlew ghaIssueEdit` | Edits title, body, labels, or assignees | `./gradlew ghaIssueEdit -PissueNumber=1 -PissueTitle="Updated title"` |
| `./gradlew ghaIssueClose` | Closes an Issue (reason: completed or not_planned) | `./gradlew ghaIssueClose -PissueNumber=1 [-PcloseComment="Fixed"]` |
| `./gradlew ghaIssueReopen` | Reopens a closed Issue | `./gradlew ghaIssueReopen -PissueNumber=1` |

### GitHub PR & Release Operations Tasks
| Task | Description |
| :--- | :--- |
| `./gradlew ghaPrCreate` | Creates a Pull Request on GitHub |
| `./gradlew ghaPrList` | Lists open Pull Requests on GitHub |
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
├── gradle/
│   └── libs.versions.toml                       # Centralized Version Catalog
├── init/
│   └── gha.init.gradle.kts                      # Self-contained init script
├── src/main/kotlin/cc/thevar/gha/
│   ├── GhaPlugin.kt                             # Core Gradle Plugin registering Git & GitHub tasks
│   ├── GhaTask.kt                               # Base Task with secret handling
│   ├── GhaInitTask.kt                           # Sandboxed GHA Init Task (.gha/)
│   ├── GhaStatusTask.kt                         # GHA Status Task
│   ├── GhaDependenciesTask.kt                   # Prints trusted vendors, dependency table, and versions
│   ├── GhaWorkflowTask.kt                       # GHA Workflow Task
│   ├── config/
│   │   └── GhaConfig.kt                         # Centralized tools, SDK, and vendor stability rules
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
