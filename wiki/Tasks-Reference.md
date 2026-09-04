# GHA Tasks Reference

## 🤖 GHA Autonomous AI Tasks (0 Effort, 100% Gain)
- `./ghai`: Primary zero-effort AI task: auto-detects diffs, commits with AI messages, rebase-syncs, pushes, opens PRs, checks CI status, auto-merges into main, and auto-prunes old CI logs!
- `./ghai ai agent "<goal>"`: Triggers an autonomous GHA Agent to achieve a project goal (e.g., "fix build", "sync work").
- `./ghai ai mcp`: Exposes GHA tasks as Model Context Protocol (MCP) tools for external AI models.
- `./ghai :clone <repo>`: Smart Git clone (e.g. `./ghai :clone intellibitz` -> `https://github.com/intellibitz/intellibitz`). Supports Scenario 1 (sync into `.`) with safety guards.
- `./ghai :version`: Displays `ghai` Version Report and verifies 100% engine stability and sandbox health.
- `./ghai :status`: Displays current project status, platform details, and sandbox health.
- `./ghai :help`: Displays all registered GHA tasks grouped by domain and CLI usage instructions.
- `./ghai :install`: Initializes or restores the sandboxed GitHub Automation environment (`.gha/`).
- `./ghai :reinstall`: Completely wipes the GHA sandbox and restores it from the official source.
- `./ghai :update`: Fetches and updates `gha` & `ghai` to the latest release in 1 second.
- `./ghai :uninstall`: Completely removes `.gha` sandbox and restoration scripts with 0 lingering system files.

## ⚡ Universal Power Mapping
The `ghai` launcher intelligently routes commands to the right engine:
1. **GHA Subcommands**: `install`, `status`, `version`, `update`, `uninstall`, `ai`, `clone`.
2. **Gradle Tasks**: Prefix with `:` (e.g., `./ghai :assemble`, `./ghai :ghaBuild`).
3. **GitHub CLI (gh)**: Map directly to `gh` subcommands (e.g., `./ghai pr list`, `./ghai repo view`).
4. **Git Engine**: Universal fallback (e.g., `./ghai log`, `./ghai diff`, `./ghai checkout`).

## Gradle Automation Tasks
- `./gradlew ghaInit`: Initializes sandboxed GitHub Automation environment (`.gha/`).
- `./gradlew ghaStatus`: Displays current project status and platform details.
- `./gradlew ghaSandbox`: Displays real-time sandbox status and environment health checks.
- `./gradlew ghaDependencies`: Prints trusted vendors and dependency versions.
- `./gradlew ghaClean`: Cleans build directory and temporary caches.
- `./gradlew ghaBuild`: Executes sandboxed Gradle build.
- `./gradlew ghaTest`: Executes project test suite.
- `./gradlew ghaKotlinInit`: Scaffolds a new 100% Kotlin project structure with Gradle DSL, version catalog, and sandboxed GHA.
- `./gradlew ghaAndroidRemove`: Removes Android manifests, resources, plugins, and dependencies for pure Kotlin development.
- `./gradlew ghaHelp`: Displays registered GHA tasks and CLI usage instructions.
- `./gradlew ghaAiVision`: Core task for AI vision features (Agent/MCP).

## Git Automation Tasks
- `./gradlew ghaGitInit`: Initializes a local Git repository.
- `./gradlew ghaGitStatus`: Displays current Git repository status.
- `./gradlew ghaGitBranch`: Lists, creates, or deletes Git branches.
- `./gradlew ghaGitCheckout`: Checks out or creates a Git branch safely.
- `./gradlew ghaGitClone`: Clones or syncs a Git repository from GitHub.
- `./gradlew ghaGitCommit`: Stages and commits working tree changes.
- `./gradlew ghaGitPush`: Pushes current branch to origin remote.
- `./gradlew ghaGitPull`: Pulls latest changes from remote with rebase.
- `./gradlew ghaGitTag`: Creates and pushes an annotated Git tag.
- `./gradlew ghaGitLog`: Displays recent Git commits.
- `./gradlew ghaGitReset`: Resets working tree changes.
- `./gradlew ghaGitStash`: Stashes working tree changes.
- `./gradlew ghaGitDiff`: Inspects working tree changes and diffs.

## GitHub Operations
- `./gradlew ghaParallelWorkflow`: Executes enterprise parallel collaboration workflow.
- `./gradlew ghaRepoView`: Displays GitHub repository details and metadata.
- `./gradlew ghaGistCreate`: Creates a GitHub Gist from a local file.
- `./gradlew ghaSecretSet`: Configures repository secrets safely.
- `./gradlew ghaWorkflow`: Executes automated workflows.
- `./gradlew ghaWorkflowList`: Lists GitHub Actions workflow runs.
- `./gradlew ghaPrCreate`: Creates Pull Requests.
- `./gradlew ghaPrList`: Lists open Pull Requests.
- `./gradlew ghaIssueCreate`: Creates Issues.
- `./gradlew ghaIssueList`: Lists open Issues.
- `./gradlew ghaReleaseCreate`: Creates Releases.
- `./gradlew ghaDependabotCleanup`: Cleans up Dependabot branches.
