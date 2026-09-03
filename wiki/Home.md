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

## 🚀 0 Effort, 100% Gain — Autonomous AI Workflow (`ghai`)

For maximum productivity with zero cognitive overhead, developers can run:

```bash
./ghai           # Short launcher shortcut
# OR
./gradlew ghai   # Standard Gradle task
```

> 💡 **Note:** `./ghai` is simply an executable wrapper script that delegates directly to `./gradlew ghai`. Both execute the exact same autonomous AI workflow task!

`ghai` (and aliases `ghaAI`, `ghaAuto`, `ghaSync`, `ghaSave`) adapts autonomously to your repository and GitHub state:

### 📦 Mode A: Dirty Working Tree (Local Changes Exist)
1. **Analyzes Diffs & Generates Smart Messages**: Inspects modified/added files and auto-generates semantic commit messages (e.g. `feat(github): update ghai engine`, `docs: update README`, `build: update gradle configuration`).
2. **Guards Protected Branches**: If on `main`/`master`, auto-creates or switches to a clean feature branch (`gha-auto/...`).
3. **Zero-Loss Staging & Commit**: Stages all changes (`git add -A`) and commits local work.
4. **Rebase-Syncs with Upstream**: Rebase-pulls from `origin/main` (`git pull --rebase`) to keep local code 100% in sync.
5. **Remote Push & PR Creation**: Pushes branch to GitHub (`git push -u origin <head>`), opens/updates PR against `main`, and enables GitHub auto-merge!

### ✨ Mode B: Clean Working Tree (Post-Push or Synced State)
1. **Rebase Sync**: Syncs local branch with `origin/main`.
2. **Queries Active GitHub PR**: Inspects active PR details and combined CI check statuses using `gh pr view --json statusCheckRollup`.
3. **CI PASSED**: Automatically merges PR into `main`, auto-deletes temporary auto-branches, and rebase-pulls `main` so local repository is **100% synced with merged `main`**.
4. **CI PENDING**: Enables GitHub `--auto` merge flag so GitHub merges the PR automatically once CI passes.
5. **CI FAILED**: Displays diagnostic report pointing directly to build failure logs.

### 📋 Structured Summary & Actionable One-Line Tip
Every run of `ghai` prints a clear execution summary block and an actionable tip for what to do next:
```text
════════════════════════════════════════════════════════════════════════════════
📋 [ghai Execution Summary]
   • Working Branch : main (User Branch)
   • Commit Status  : Committed: "docs: update ghai docs and summary output"
   • Remote Push    : Pushed to origin/main
   • GitHub PR      : PR #12 active (https://github.com/owner/repo/pull/12)
   • CI/CD Status   : PENDING / AUTO-MERGE
   • Local Sync     : 100% Synced with origin/main
────────────────────────────────────────────────────────────────────────────────
💡 Tip: Run './ghai' after GitHub CI builds finish to verify and complete auto-merge into main.
════════════════════════════════════════════════════════════════════════════════
```

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

### 1. Git Automation Tasks
| Task | Description | Usage Example |
| :--- | :--- | :--- |
| `./gradlew ghaGitInit` | Initializes a local Git repository | `./gradlew ghaGitInit` |
| `./gradlew ghaGitStatus` | Displays current Git repository status, branch, and working tree changes | `./gradlew ghaGitStatus` |
| `./gradlew ghaGitBranch` | Lists, creates (`-PcreateBranch=...`), or deletes (`-PdeleteBranch=...`) Git branches | `./gradlew ghaGitBranch` |
| `./gradlew ghaGitCheckout` | Checks out or creates a Git branch safely with stash support | `./gradlew ghaGitCheckout -PbranchName="feature/login"` |
| `./gradlew ghaGitCommit` | Stages and commits working tree changes platform-independently | `./gradlew ghaGitCommit -PcommitMessage="Message"` |
| `./gradlew ghaGitCheckin` | Stages and checks in/commits working tree changes safely | `./gradlew ghaGitCheckin -PcommitMessage="Message"` |
| `./gradlew ghaGitPush` | Pushes current branch to origin remote | `./gradlew ghaGitPush` |
| `./gradlew ghaGitPull` | Pulls latest changes from remote with rebase | `./gradlew ghaGitPull` |
| `./gradlew ghaGitTag` | Creates and pushes an annotated Git tag | `./gradlew ghaGitTag -PtagName="v1.0.0"` |
| `./gradlew ghaGitLog` | Displays recent Git commits | `./gradlew ghaGitLog` |
| `./gradlew ghaGitReset` | Resets working tree changes (`--hard` / `--soft` / `--mixed`) | `./gradlew ghaGitReset [-PresetMode="hard"]` |
| `./gradlew ghaGitStash` | Stashes working tree changes (`push` / `pop` / `list` / `drop`) | `./gradlew ghaGitStash [-PstashAction="pop"]` |
| `./gradlew ghaGitDiff` | Inspects working tree changes and diffs | `./gradlew ghaGitDiff` |

---

### 2. Enterprise Parallel Collaboration & GitHub Automation Tasks

#### Enterprise Parallel Workflow (Protected Branch Guarded)
| Task | Description | Usage Example |
| :--- | :--- | :--- |
| `./gradlew ghaParallelWorkflow` | Executes end-to-end enterprise workflow (`pull` -> `branch` -> `checkin` -> `push` -> `PR` -> `merge` -> `auto-cleanup`) on protected branches | `./gradlew ghaParallelWorkflow -PbaseBranch=main -PcommitMessage="Feature contribution"` |
| `./gradlew ghaDevWorkflow` | Alias for `ghaParallelWorkflow`: Parallel developer contribution workflow with PR creation and smart branch preservation | `./gradlew ghaDevWorkflow -PautoMerge=true` |

#### Actions & Workflow Management
| Task | Description | Usage Example |
| :--- | :--- | :--- |
| `./gradlew ghaWorkflow` | Executes platform-independent GitHub automation workflows | `./gradlew ghaWorkflow` |
| `./gradlew ghaWorkflowList` | Lists GitHub Actions workflow runs | `./gradlew ghaWorkflowList` |
| `./gradlew ghaWorkflowCleanup` | Cleans up old or failed workflow runs | `./gradlew ghaWorkflowCleanup` |
| `./gradlew ghaWorkflowCancel` | Cancels active or queued workflow runs | `./gradlew ghaWorkflowCancel -PrunId=123` |

#### Pull Requests & Code Reviews
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
