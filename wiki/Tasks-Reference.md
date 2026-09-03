# GHA Tasks Reference

## Core Tasks
- `./gradlew ghaInit`: Initializes sandboxed GitHub Automation environment (`.gha/`).
- `./gradlew ghaStatus`: Displays current project status and platform details.
- `./gradlew ghaDependencies`: Prints trusted vendors and dependency versions.
- `./gradlew ghaWorkflow`: Executes automated workflows.

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

## Git Operations
- `./gradlew ghaGitStatus`: Displays Git repository status.
- `./gradlew ghaGitCommit`: Stages and commits working tree changes.
- `./gradlew ghaGitPush`: Pushes current branch to origin.
- `./gradlew ghaGitPull`: Pulls remote changes with rebase.
- `./gradlew ghaGitTag`: Tagging and pushing.
- `./gradlew ghaGitLog`: Displays commit log.