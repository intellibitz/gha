# GHA Tasks Reference

## Core Tasks
- `./gradlew ghaInit`: Initializes sandboxed GitHub Automation environment (`.gha/`).
- `./gradlew ghaStatus`: Displays current project status and platform details.
- `./gradlew ghaSandbox`: Displays real-time sandbox status and environment health checks.
- `./gradlew ghaDependencies`: Prints trusted vendors and dependency versions.
- `./gradlew ghaWorkflow`: Executes automated workflows.
- `./gradlew ghaWorkflowList`: Lists GitHub Actions workflow runs.
- `./gradlew ghaWorkflowCleanup`: Cleans up old or failed workflow runs.
- `./gradlew ghaWorkflowCancel`: Cancels active workflow runs.
- `./gradlew ghaHelp`: Displays registered GHA tasks and CLI usage instructions.

## GitHub Wiki Tasks
- `./gradlew ghaWikiInit`: Creates local `wiki/` documentation directory and template pages.
- `./gradlew ghaWikiStatus`: Inspects local wiki pages and remote wiki status.
- `./gradlew ghaWikiSync`: Pulls remote wiki changes into local `wiki/` directory.
- `./gradlew ghaWikiPublish`: Commits and pushes local `wiki/` pages to remote GitHub Wiki.

## GitHub Operations
- `./gradlew ghaPrCreate`: Creates Pull Requests.
- `./gradlew ghaPrList`: Lists open Pull Requests.
- `./gradlew ghaIssueCreate`: Creates Issues.
- `./gradlew ghaIssueList`: Lists open Issues.
- `./gradlew ghaReleaseCreate`: Creates Releases.
- `./gradlew ghaDependabotCleanup`: Cleans up Dependabot branches.

## Git Operations
- `./gradlew ghaGitStatus`: Displays Git repository status.
- `./gradlew ghaGitBranch`: Displays current Git branches and branch status.
- `./gradlew ghaGitCommit`: Stages and commits working tree changes.
- `./gradlew ghaGitPush`: Pushes current branch to origin.
- `./gradlew ghaGitPull`: Pulls remote changes with rebase.
- `./gradlew ghaGitTag`: Tagging and pushing.
- `./gradlew ghaGitLog`: Displays commit log.
