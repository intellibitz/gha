# GHA Tasks Reference

## 🤖 GHA Autonomous AI Tasks (0 Effort, 100% Gain)
- `./ghai`: Primary zero-effort AI task: auto-detects diffs, commits with AI messages, rebase-syncs, pushes, opens PRs, checks CI status, auto-merges into main, and auto-prunes old CI logs!
- `./ghai :clone <repo>`: Smart Git clone (e.g. `./ghai :clone intellibitz` -> `https://github.com/intellibitz/intellibitz`). Supports Scenario 1 (sync into `.`) with safety guards.
- `./ghai :version`: Displays `ghai` Version Report and verifies 100% engine stability and sandbox health.
- `./ghai :status`: Displays current project status, platform details, and sandbox health.
- `./ghai :help`: Displays all registered GHA tasks grouped by domain and CLI usage instructions.
- `./ghai :update`: Fetches and updates `gha` & `ghai` to the latest release in 1 second.
- `./ghai :uninstall`: Completely removes `.gha` sandbox, runner scripts, and CI workflows with 0 lingering system files and 0 modifications to original project files.
- `./gradlew ghaAI`: Alias for `ghai`.
- `./gradlew ghaAuto`: Alias for `ghai`.
- `./gradlew ghaSync`: Alias for `ghai`: Keeps local working branch 100% synced with remote base branch via rebase and push.
- `./gradlew ghaSave`: Alias for `ghai`: Saves and pushes all local work to GitHub automatically.

## Gradle Automation Tasks
- `./gradlew ghaInit`: Initializes sandboxed GitHub Automation environment (`.gha/`).
- `./gradlew ghaStatus`: Displays current project status and platform details.
- `./gradlew ghaSandbox`: Displays real-time sandbox status and environment health checks.
- `./gradlew ghaDependencies`: Prints trusted vendors and dependency versions.
- `./gradlew ghaClean`: Cleans build directory and temporary caches.
- `./gradlew ghaBuild`: Executes sandboxed Gradle build.
- `./gradlew ghaTest`: Executes project test suite.
- `./gradlew ghaKotlinInit`: Scaffolds a new 100% Kotlin project structure with Gradle DSL, version catalog, and sandboxed GHA.
- `./gradlew ghaKotlinProjectCreate`: Creates a new 100% Kotlin project structure platform-independently.
- `./gradlew ghaAndroidRemove`: Removes Android manifests, resources, plugins, and dependencies for pure Kotlin development.
- `./gradlew ghaAndroidProjectRemove`: Converts an Android project into a pure 100% Kotlin project.
- `./gradlew ghaHelp`: Displays registered GHA tasks and CLI usage instructions.

## Git Automation Tasks
- `./gradlew ghaGitInit`: Initializes a local Git repository.
- `./gradlew ghaGitStatus`: Displays current Git repository status, branch, and working tree changes.
- `./gradlew ghaGitBranch`: Lists, creates (`-PcreateBranch=...`), or deletes (`-PdeleteBranch=...`) Git branches.
- `./gradlew ghaGitCheckout`: Checks out or creates a Git branch safely with stash support (`-PbranchName=...`).
- `./gradlew ghaGitClone`: Clones or syncs a Git repository from GitHub (`-PtargetRepo=...` `-Pdir=...`). Supports smart shorthand resolution.
- `./gradlew ghaGitCommit`: Stages and commits working tree changes platform-independently.
- `./gradlew ghaGitCheckin`: Stages and checks in/commits working tree changes safely (`-PcommitMessage=...`).
- `./gradlew ghaGitPush`: Pushes current branch to origin remote.
- `./gradlew ghaGitPull`: Pulls latest changes from remote with rebase.
- `./gradlew ghaGitTag`: Creates and pushes an annotated Git tag (`-PtagName=...`).
- `./gradlew ghaGitLog`: Displays recent Git commits for a target reference (`-PtargetRef=...` `-PmaxCount=...`).
- `./gradlew ghaGitReset`: Resets working tree changes (`--hard` / `--soft` / `--mixed`).
- `./gradlew ghaGitStash`: Stashes working tree changes (push / pop / list / drop).
- `./gradlew ghaGitDiff`: Inspects working tree changes and diffs.

## GitHub & Enterprise Parallel Automation Tasks
- `./gradlew ghaParallelWorkflow`: Executes enterprise parallel collaboration workflow (pull -> branch -> checkin -> push -> PR -> merge -> auto-cleanup).
- `./gradlew ghaDevWorkflow`: Alias for `ghaParallelWorkflow`: End-to-end parallel developer contribution workflow on protected branches.
- `./gradlew ghaRepoView`: Displays GitHub repository details and metadata.
- `./gradlew ghaGistCreate`: Creates a GitHub Gist from a local file.
- `./gradlew ghaSecretSet`: Configures repository secrets safely.
- `./gradlew ghaWorkflow`: Executes automated workflows.
- `./gradlew ghaWorkflowList`: Lists GitHub Actions workflow runs.
- `./gradlew ghaWorkflowCleanup`: Cleans up old or failed workflow runs.
- `./gradlew ghaWorkflowCancel`: Cancels active workflow runs.

## GitHub Wiki Tasks
- `./gradlew ghaWikiInit`: Creates local `wiki/` documentation directory and template pages.
- `./gradlew ghaWikiStatus`: Inspects local wiki pages and remote wiki status.
- `./gradlew ghaWikiSync`: Pulls remote wiki changes into local `wiki/` directory.
- `./gradlew ghaWikiPublish`: Commits and pushes local `wiki/` pages to remote GitHub Wiki.

## GitHub Operations
- `./gradlew ghaPrCreate`: Creates Pull Requests.
- `./gradlew ghaPrList`: Lists open Pull Requests.
- `./gradlew ghaPrCheckout`: Checks out a Pull Request branch locally (`-PprNumber=...`).
- `./gradlew ghaPrMerge`: Merges Pull Requests (`-PprNumber=...` `-PautoMerge=true`).
- `./gradlew ghaIssueCreate`: Creates Issues.
- `./gradlew ghaIssueList`: Lists open Issues.
- `./gradlew ghaReleaseCreate`: Creates Releases.
- `./gradlew ghaDependabotCleanup`: Cleans up Dependabot branches.
