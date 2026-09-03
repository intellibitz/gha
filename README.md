# gha: Git, GitHub & Gradle Automation

**gha** (`cc.thevar.gha`) is a **100% self-contained, 100% Kotlin** Git, GitHub, and Gradle automation plugin. **gha runs on gha**—automating its own development, testing, dependencies, commits, pulls, PRs, releases, security, issues, wikis, and insights.

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
| **Java / JDK Toolchain** | SDK | Eclipse Adoptium (Temurin) / Oracle | `21` | ✅ Official Stable |
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

### GitHub Automation & Workflow Tasks
| Task | Description | Usage Example |
| :--- | :--- | :--- |
| `./gradlew ghaInit` | Initializes sandboxed GitHub Automation environment (`.gha/`) | `./gradlew ghaInit` |
| `./gradlew ghaKotlinInit` | Initializes a 100% Kotlin project with Gradle DSL, version catalog, and sandboxed GHA | `./gradlew ghaKotlinInit [-PprojectName="my-app"]` |
| `./gradlew ghaKotlinProjectCreate` | Creates a new 100% Kotlin project structure platform-independently | `./gradlew ghaKotlinProjectCreate [-PprojectName="my-app"]` |
| `./gradlew ghaStatus` | Displays current GitHub Automation project and platform status | `./gradlew ghaStatus` |
| `./gradlew ghaSandbox` | Displays real-time sandbox status and environment health checks | `./gradlew ghaSandbox` |
| `./gradlew ghaDependencies` | Prints all GHA dependencies, trusted vendors, and active runtime versions | `./gradlew ghaDependencies` |
| `./gradlew ghaWorkflow` | Executes platform-independent GitHub automation workflows | `./gradlew ghaWorkflow` |
| `./gradlew ghaWorkflowList` | Lists GitHub Actions workflow runs | `./gradlew ghaWorkflowList` |
| `./gradlew ghaWorkflowCleanup` | Cleans up old or failed workflow runs | `./gradlew ghaWorkflowCleanup` |
| `./gradlew ghaWorkflowCancel` | Cancels active or queued workflow runs | `./gradlew ghaWorkflowCancel -PrunId=123` |
| `./gradlew ghaHelp` | Displays all registered GHA tasks and CLI usage instructions | `./gradlew ghaHelp` |

### GitHub Projects Tasks
| Task | Description | Usage Example |
| :--- | :--- | :--- |
| `./gradlew ghaProjectInit` | Initializes default GitHub Project boards (Roadmap, Issue Tracker, Releases) | `./gradlew ghaProjectInit` |
| `./gradlew ghaProjectCreate` | Creates a new GitHub Project board | `./gradlew ghaProjectCreate -PprojectTitle="My Board"` |
| `./gradlew ghaProjectList` | Lists GitHub Project boards for an owner or repository | `./gradlew ghaProjectList [-PprojectOwner="owner"]` |
| `./gradlew ghaProjectView` | Displays details and items of a GitHub Project board | `./gradlew ghaProjectView -PprojectNumber=1` |
| `./gradlew ghaProjectAddItem` | Adds an Issue or Pull Request URL to a project board | `./gradlew ghaProjectAddItem -PprojectNumber=1 -PitemUrl="https://..."` |
| `./gradlew ghaProjectClose` | Closes or archives a GitHub Project board | `./gradlew ghaProjectClose -PprojectNumber=1` |

### GitHub Insights Tasks
| Task | Description |
| :--- | :--- |
| `./gradlew ghaInsights` | Displays repository overview, stars, forks, watchers, open issues, and commit counts |
| `./gradlew ghaContributors` | Displays contributor breakdown, commit counts, and percentage contributions |
| `./gradlew ghaTraffic` | Displays repository traffic, page views, and clone statistics |

### GitHub Security & Vulnerability Tasks
| Task | Description | Usage Example |
| :--- | :--- | :--- |
| `./gradlew ghaSecurityInit` | Generates default security workflows, Dependabot, CodeQL scanning, and `SECURITY.md` | `./gradlew ghaSecurityInit` |
| `./gradlew ghaSecurityStatus` | Inspects current GitHub security, scanning, Dependabot, and policy status | `./gradlew ghaSecurityStatus` |
| `./gradlew ghaDependabotInit` | Generates `.github/dependabot.yml` for automated dependency updates | `./gradlew ghaDependabotInit` |
| `./gradlew ghaDependabotList` | Lists active Dependabot pull requests and remote `dependabot/` branches | `./gradlew ghaDependabotList` |
| `./gradlew ghaDependabotMerge` | Merges Dependabot PRs and deletes remote branches | `./gradlew ghaDependabotMerge [-PprNumber=123] [-PmergeAll=true]` |
| `./gradlew ghaDependabotClose` | Closes Dependabot PRs and deletes remote `dependabot/` branches | `./gradlew ghaDependabotClose [-PprNumber=123] [-PcloseAll=true]` |
| `./gradlew ghaDependabotCleanup` | Cleans up closed or merged Dependabot branches | `./gradlew ghaDependabotCleanup` |
| `./gradlew ghaDependabotRebase` | Requests Dependabot to `@dependabot rebase` or `@dependabot recreate` | `./gradlew ghaDependabotRebase [-PprNumber=123] [-PrebaseAll=true] [-Precreate=true]` |
| `./gradlew ghaCodeScanningInit` | Generates `.github/workflows/codeql.yml` for CodeQL code scanning | `./gradlew ghaCodeScanningInit` |

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

### GitHub Pull Request Operations Tasks
| Task | Description | Usage Example |
| :--- | :--- | :--- |
| `./gradlew ghaPrCreate` | Creates a new Pull Request on GitHub | `./gradlew ghaPrCreate -PprTitle="Feature PR" -PprBody="Details"` |
| `./gradlew ghaPrList` | Lists Pull Requests (supports open/closed/merged/all filtering) | `./gradlew ghaPrList [-PprState=open\|closed\|merged\|all]` |
| `./gradlew ghaPrView` | Displays details, comments, and files for a PR | `./gradlew ghaPrView [-PprNumber=1]` |
| `./gradlew ghaPrCheckout` | Checks out a Pull Request branch locally | `./gradlew ghaPrCheckout -PprNumber=1` |
| `./gradlew ghaPrEdit` | Edits title, body, base branch, labels, or reviewers | `./gradlew ghaPrEdit -PprNumber=1 -PprTitle="Updated PR Title"` |
| `./gradlew ghaPrReview` | Submits a PR review (approve, request changes, or comment) | `./gradlew ghaPrReview [-PprNumber=1] -Papprove=true` |
| `./gradlew ghaPrMerge` | Merges a Pull Request (squash, merge, or rebase) | `./gradlew ghaPrMerge [-PprNumber=1] [-PmergeMethod=squash]` |
| `./gradlew ghaPrClose` | Closes a Pull Request without merging | `./gradlew ghaPrClose [-PprNumber=1] [-PcloseComment="Closed"]` |
| `./gradlew ghaPrReopen` | Reopens a closed Pull Request | `./gradlew ghaPrReopen [-PprNumber=1]` |

### GitHub Release Operations Tasks
| Task | Description | Usage Example |
| :--- | :--- | :--- |
| `./gradlew ghaReleaseCreate` | Creates a Release on GitHub | `./gradlew ghaReleaseCreate -PtagName="v1.0.0"` |

### Git Operations Tasks
| Task | Description |
| :--- | :--- |
| `./gradlew ghaGitStatus` | Displays current Git repository status, branch, and working tree changes |
| `./gradlew ghaGitBranch` | Displays current Git branches and branch status |
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
│   ├── GhaTask.kt                               # Base Task with secret handling & relative path input
│   ├── GhaInitTask.kt                           # Sandboxed GHA Init Task (.gha/)
│   ├── GhaKotlinInitTask.kt                     # 100% Kotlin Project Scaffolding Task
│   ├── GhaStatusTask.kt                         # GHA Status Task
│   ├── GhaSandboxTask.kt                        # GHA Sandbox Health Check Task
│   ├── GhaDependenciesTask.kt                   # Prints trusted vendors, dependency table, and versions
│   ├── GhaHelpTask.kt                           # Displays available tasks and usage examples
│   ├── GhaWorkflowTask.kt                       # GHA Workflow Task
│   ├── config/
│   │   └── GhaConfig.kt                         # Centralized tools, SDK, and vendor stability rules
│   ├── safety/
│   │   ├── GhaProcessRunner.kt                  # Timeouts, non-interactive flags, recursion guard
│   │   └── GhaSandboxManager.kt                 # Sandbox environment health checking
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
