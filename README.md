# gha: Git, GitHub & Gradle Automation

**gha** (`cc.thevar.gha`) is a **100% self-contained, 100% Kotlin** Git, GitHub, and Gradle automation plugin. **gha runs on gha**—automating its own development, testing, dependencies, commits, pulls, PRs, releases, security, issues, wikis, and insights.

GitHub users and developers across any IDE or terminal can clone this project and expect **0% system modifications**. All dependencies, Kotlin libraries, Gradle caches, JDK toolchains, and execution state are strictly sandboxed inside the local repository folder (`.gha/`).

## Mission

`gha` creators—**Intellibitz**, **Gemini**, and other AI agents alongside GitHub community contributors—build platform-independent Gradle tasks and plugins written purely in **100% Kotlin** to power end-to-end Git and GitHub automation workflows.

## Key Principles

- **100% IDE-Agnostic & CLI-First**: Supports Android Studio, IntelliJ IDEA, VS Code, Eclipse, Vim/Emacs, and CI pipelines equally. IDE-specific files (`.idea/`, `.vscode/`) are git-ignored.
- **Strict Official Stable Versions & Trusted Vendors**: `gha` strictly uses official stable releases from verified trusted vendors (`JetBrains`, `Gradle Inc.`, `Eclipse Adoptium`, `Oracle`, `GitHub Inc.`). No alpha, beta, rc, or untrusted third-party repositories.
- **gha Runs on gha**: Self-testing and self-automating via `.github/workflows/gha.yml`.
- **100% Sandboxed & 0 Side Effects**: `gha` **never modifies existing project files** (`settings.gradle.kts`, `build.gradle.kts`). It integrates via a self-contained initialization script, ensuring a perfect sandbox environment.
- **Centralized Version Catalog ([`GhaConfig.kt`](file:///home/ramadoss/Projects/AI/gha/src/main/kotlin/cc/thevar/gha/config/GhaConfig.kt))**: Single source of truth for tools, SDKs, frameworks, and plugin versions.
- **Infinite Loop Guard & Timeout Protection**: `GhaProcessRunner` enforces strict execution timeouts (30s) and non-interactive flags (`GIT_TERMINAL_PROMPT=0`, `GH_NO_PROMPT=1`), preventing infinite hangs or recursion loops.
- **100% Sandboxed Dependencies & JDKs**: All external libraries, Kotlin DSL plugins, and JDK toolchains are downloaded into `.gha/gradle-user-home/`.
- **0% System Modifications**: Zero changes to `~/.gradle/`, user system settings, shell configurations, or global user directories.
- **100% Self-Contained**: No third-party system dependencies or external tooling installers required.
- **100% Kotlin**: Built entirely using Kotlin for type safety, coroutines, DSL capabilities, and multiplatform support.
- **100% Platform Independent**: Runs seamlessly across macOS, Linux, Windows, and containerized CI environments without bash or shell dependencies.
- **100% Secure & Zero Secret Leakage**: Enforces strict security rules to prevent accidental token exposure in logs, task inputs, build reports, or configuration cache.

---

## 🚀 0 Effort, 100% Gain — Autonomous AI Workflow (`ghai`)

For maximum productivity with zero cognitive overhead, developers can run:

```bash
./ghai           # Short launcher shortcut
# OR
./gradlew ghai   # Standard Gradle task
```

`ghai` (and aliases `ghaAI`, `ghaAuto`, `ghaSync`, `ghaSave`) adapts autonomously to your repository and GitHub state:

### 📦 Mode A: Dirty Working Tree (Local Changes Exist)
1. **Analyzes Diffs & Generates Smart Messages**: Inspects modified/added files and auto-generates semantic commit messages.
2. **Guards Protected Branches**: If on `main`/`master`, auto-creates or switches to a clean feature branch (`gha-auto/...`).
3. **Zero-Loss Staging & Commit**: Stages all changes (`git add -A`) and commits local work.
4. **Rebase-Syncs with Upstream**: Rebase-pulls from `origin/main` (`git pull --rebase`) to keep local code 100% in sync.
5. **Remote Push & PR Creation**: Pushes branch to GitHub, opens/updates PR against `main`, and enables GitHub auto-merge!

### ✨ Mode B: Clean Working Tree (Post-Push or Synced State)
1. **Rebase Sync**: Syncs local branch with `origin/main`.
2. **Queries Active GitHub PR**: Inspects active PR details and combined CI check statuses.
3. **CI PASSED**: Automatically merges PR into `main`, auto-deletes temporary auto-branches, and rebase-pulls `main`.
4. **CI PENDING**: Enables GitHub `--auto` merge flag.
5. **CI FAILED**: Displays diagnostic report pointing directly to build failure logs.

---

## ⚡ Ridiculously Easy 0-Effort Installation (1 Second)

Install `gha` into **any repository** instantly with a single command:

```bash
# Linux, macOS, & WSL (1-Second One-Liner):
curl -sSL https://raw.githubusercontent.com/intellibitz/gha/main/init/install.sh | bash

# Windows PowerShell (1-Second One-Liner):
iwr -useb https://raw.githubusercontent.com/intellibitz/gha/main/init/install.ps1 | iex
```

### 🔄 Clone, Version, Update & Uninstall Subcommands

Once `ghai` is installed, managing `gha` is completely zero-effort:

```bash
# Smart clone repository (e.g. ./ghai clone intellibitz -> https://github.com/intellibitz/intellibitz):
./ghai clone intellibitz

# Clone specific owner/repo:
./ghai clone intellibitz/gha

# Print version report & verify engine stability (100% sandboxed):
./ghai version

# Update gha & ghai to the latest release version:
./ghai update

# Completely uninstall and remove gha (0 lingering system files, 0 user file changes):
./ghai uninstall
```

---

## How it Works: The True Sandbox

`gha` is designed to be completely non-intrusive for developers:

1. **Existing Projects (Scenario 2)**: `gha` **never touches** your project's `settings.gradle.kts` or `build.gradle.kts`. It extends your build environment purely in memory via Gradle's `--init-script` mechanism.
2. **Empty Folders (Scenario 1)**: `gha` scaffolds minimal files with a unique header (`// Generated by gha`). These files are safely removed during uninstallation.
3. **Isolated Cache**: All Gradle dependencies and JDK toolchains used by `gha` are isolated in `.gha/gradle-user-home`, keeping your global `~/.gradle` folder pristine.

---

## Contributors

`gha` is co-created and maintained by **IntelliBitz**, **Muthu Ramadoss**, and **Gemini (Google AI)**. See [CONTRIBUTORS.md](file:///home/ramadoss/Projects/AI/gha/CONTRIBUTORS.md) for the full list of project creators and contributors.

## License

This project is licensed under the [MIT License](LICENSE).
