package cc.thevar.gha

import cc.thevar.gha.git.GhaGitCommitTask
import cc.thevar.gha.git.GhaGitLogTask
import cc.thevar.gha.git.GhaGitPullTask
import cc.thevar.gha.git.GhaGitPushTask
import cc.thevar.gha.git.GhaGitStatusTask
import cc.thevar.gha.git.GhaGitTagTask
import cc.thevar.gha.github.GhaIssueCloseTask
import cc.thevar.gha.github.GhaIssueCommentTask
import cc.thevar.gha.github.GhaIssueCreateTask
import cc.thevar.gha.github.GhaIssueEditTask
import cc.thevar.gha.github.GhaIssueListTask
import cc.thevar.gha.github.GhaIssueReopenTask
import cc.thevar.gha.github.GhaIssueViewTask
import cc.thevar.gha.github.GhaPrCheckoutTask
import cc.thevar.gha.github.GhaPrCloseTask
import cc.thevar.gha.github.GhaPrCreateTask
import cc.thevar.gha.github.GhaPrEditTask
import cc.thevar.gha.github.GhaPrListTask
import cc.thevar.gha.github.GhaPrMergeTask
import cc.thevar.gha.github.GhaPrReopenTask
import cc.thevar.gha.github.GhaPrReviewTask
import cc.thevar.gha.github.GhaPrViewTask
import cc.thevar.gha.github.GhaReleaseCreateTask
import cc.thevar.gha.insights.GhaContributorsTask
import cc.thevar.gha.insights.GhaInsightsTask
import cc.thevar.gha.insights.GhaTrafficTask
import cc.thevar.gha.projects.GhaProjectAddItemTask
import cc.thevar.gha.projects.GhaProjectCloseTask
import cc.thevar.gha.projects.GhaProjectCreateTask
import cc.thevar.gha.projects.GhaProjectInitTask
import cc.thevar.gha.projects.GhaProjectListTask
import cc.thevar.gha.projects.GhaProjectViewTask
import cc.thevar.gha.security.GhaCodeScanningInitTask
import cc.thevar.gha.security.GhaDependabotCloseTask
import cc.thevar.gha.security.GhaDependabotInitTask
import cc.thevar.gha.security.GhaDependabotListTask
import cc.thevar.gha.security.GhaDependabotMergeTask
import cc.thevar.gha.security.GhaDependabotRebaseTask
import cc.thevar.gha.security.GhaSecurityInitTask
import cc.thevar.gha.security.GhaSecurityStatusTask
import cc.thevar.gha.wiki.GhaWikiInitTask
import cc.thevar.gha.wiki.GhaWikiPublishTask
import cc.thevar.gha.wiki.GhaWikiStatusTask
import cc.thevar.gha.wiki.GhaWikiSyncTask
import cc.thevar.gha.workflow.GhaWorkflowCancelTask
import cc.thevar.gha.workflow.GhaWorkflowCleanupTask
import cc.thevar.gha.workflow.GhaWorkflowListTask
import org.gradle.api.Plugin
import org.gradle.api.Project

class GhaPlugin : Plugin<Project> {
    override fun apply(project: Project) {
        // GitHub Core Tasks
        project.tasks.register("ghaInit", GhaInitTask::class.java) {
            group = "GitHub Automation"
            description = "Initializes sandboxed GitHub Automation workflows for any project automatically"
        }

        project.tasks.register("ghaStatus", GhaStatusTask::class.java) {
            group = "GitHub Automation"
            description = "Displays the current GitHub Automation workflow status"
        }

        project.tasks.register("ghaWorkflow", GhaWorkflowTask::class.java) {
            group = "GitHub Automation"
            description = "Executes platform-independent GitHub automation workflows"
        }

        project.tasks.register("ghaDependencies", GhaDependenciesTask::class.java) {
            group = "GitHub Automation"
            description = "Prints all GHA dependencies, tools, SDKs, and their current versions"
        }

        // GitHub Actions Workflow Management & Cleanup
        project.tasks.register("ghaWorkflowList", GhaWorkflowListTask::class.java) {
            group = "GitHub Actions"
            description = "Lists recent GitHub Actions workflow runs and their status"
        }

        project.tasks.register("ghaWorkflowCleanup", GhaWorkflowCleanupTask::class.java) {
            group = "GitHub Actions"
            description = "Cleans up and deletes old, failed, or cancelled GitHub Actions workflow runs"
        }

        project.tasks.register("ghaWorkflowCancel", GhaWorkflowCancelTask::class.java) {
            group = "GitHub Actions"
            description = "Cancels in-progress GitHub Actions workflow runs"
        }

        // GitHub Projects Tasks
        project.tasks.register("ghaProjectInit", GhaProjectInitTask::class.java) {
            group = "GitHub Projects"
            description = "Initializes default GitHub Project boards (Roadmap, Issue Tracker, Releases) automatically"
        }

        project.tasks.register("ghaProjectCreate", GhaProjectCreateTask::class.java) {
            group = "GitHub Projects"
            description = "Creates a new GitHub Project board"
        }

        project.tasks.register("ghaProjectList", GhaProjectListTask::class.java) {
            group = "GitHub Projects"
            description = "Lists GitHub Project boards for an owner or repository"
        }

        project.tasks.register("ghaProjectView", GhaProjectViewTask::class.java) {
            group = "GitHub Projects"
            description = "Displays details and items of a GitHub Project board"
        }

        project.tasks.register("ghaProjectAddItem", GhaProjectAddItemTask::class.java) {
            group = "GitHub Projects"
            description = "Adds an Issue or Pull Request URL to a GitHub Project board"
        }

        project.tasks.register("ghaProjectClose", GhaProjectCloseTask::class.java) {
            group = "GitHub Projects"
            description = "Closes or archives a GitHub Project board"
        }

        // GitHub Insights Tasks
        project.tasks.register("ghaInsights", GhaInsightsTask::class.java) {
            group = "GitHub Insights"
            description = "Displays GitHub repository overview, stars, forks, issues, and commit metrics"
        }

        project.tasks.register("ghaContributors", GhaContributorsTask::class.java) {
            group = "GitHub Insights"
            description = "Displays repository contributors and commit breakdowns"
        }

        project.tasks.register("ghaTraffic", GhaTrafficTask::class.java) {
            group = "GitHub Insights"
            description = "Displays repository traffic, page views, and clone statistics"
        }

        // GitHub Security & Vulnerability Tasks
        project.tasks.register("ghaSecurityInit", GhaSecurityInitTask::class.java) {
            group = "GitHub Security"
            description = "Generates default GitHub security workflows, Dependabot, CodeQL, and SECURITY.md"
        }

        project.tasks.register("ghaSecurityStatus", GhaSecurityStatusTask::class.java) {
            group = "GitHub Security"
            description = "Displays current GitHub security, scanning, and Dependabot status"
        }

        project.tasks.register("ghaDependabotInit", GhaDependabotInitTask::class.java) {
            group = "GitHub Security"
            description = "Generates .github/dependabot.yml for automated dependency updates"
        }

        project.tasks.register("ghaDependabotList", GhaDependabotListTask::class.java) {
            group = "GitHub Security"
            description = "Lists active Dependabot pull requests and remote branches"
        }

        project.tasks.register("ghaDependabotMerge", GhaDependabotMergeTask::class.java) {
            group = "GitHub Security"
            description = "Merges open Dependabot pull requests and deletes remote branches"
        }

        project.tasks.register("ghaDependabotClose", GhaDependabotCloseTask::class.java) {
            group = "GitHub Security"
            description = "Closes Dependabot pull requests and deletes remote dependabot/ branches"
        }

        project.tasks.register("ghaDependabotRebase", GhaDependabotRebaseTask::class.java) {
            group = "GitHub Security"
            description = "Requests Dependabot to rebase or recreate pull requests to resolve conflicts"
        }

        project.tasks.register("ghaCodeScanningInit", GhaCodeScanningInitTask::class.java) {
            group = "GitHub Security"
            description = "Generates CodeQL code scanning workflow .github/workflows/codeql.yml"
        }

        // GitHub Wiki Tasks
        project.tasks.register("ghaWikiInit", GhaWikiInitTask::class.java) {
            group = "GitHub Wiki"
            description = "Initializes local Wiki directory structure and template pages"
        }

        project.tasks.register("ghaWikiStatus", GhaWikiStatusTask::class.java) {
            group = "GitHub Wiki"
            description = "Displays current GitHub Wiki pages and sync status"
        }

        project.tasks.register("ghaWikiSync", GhaWikiSyncTask::class.java) {
            group = "GitHub Wiki"
            description = "Pulls latest changes from remote GitHub Wiki repository"
        }

        project.tasks.register("ghaWikiPublish", GhaWikiPublishTask::class.java) {
            group = "GitHub Wiki"
            description = "Publishes local wiki/ directory pages to remote GitHub Wiki repository"
        }

        // GitHub Issue Operations
        project.tasks.register("ghaIssueCreate", GhaIssueCreateTask::class.java) {
            group = "GitHub Operations"
            description = "Creates an Issue on GitHub"
        }

        project.tasks.register("ghaIssueList", GhaIssueListTask::class.java) {
            group = "GitHub Operations"
            description = "Lists Issues on GitHub"
        }

        project.tasks.register("ghaIssueView", GhaIssueViewTask::class.java) {
            group = "GitHub Operations"
            description = "Displays details and comments for a GitHub Issue"
        }

        project.tasks.register("ghaIssueClose", GhaIssueCloseTask::class.java) {
            group = "GitHub Operations"
            description = "Closes an Issue on GitHub"
        }

        project.tasks.register("ghaIssueReopen", GhaIssueReopenTask::class.java) {
            group = "GitHub Operations"
            description = "Reopens a closed Issue on GitHub"
        }

        project.tasks.register("ghaIssueComment", GhaIssueCommentTask::class.java) {
            group = "GitHub Operations"
            description = "Adds a comment to an Issue on GitHub"
        }

        project.tasks.register("ghaIssueEdit", GhaIssueEditTask::class.java) {
            group = "GitHub Operations"
            description = "Edits title, body, labels, or assignees of an Issue on GitHub"
        }

        // GitHub PR Operations
        project.tasks.register("ghaPrCreate", GhaPrCreateTask::class.java) {
            group = "GitHub Operations"
            description = "Creates a Pull Request on GitHub"
        }

        project.tasks.register("ghaPrList", GhaPrListTask::class.java) {
            group = "GitHub Operations"
            description = "Lists Pull Requests on GitHub"
        }

        project.tasks.register("ghaPrView", GhaPrViewTask::class.java) {
            group = "GitHub Operations"
            description = "Displays details and comments for a Pull Request on GitHub"
        }

        project.tasks.register("ghaPrMerge", GhaPrMergeTask::class.java) {
            group = "GitHub Operations"
            description = "Merges a Pull Request on GitHub"
        }

        project.tasks.register("ghaPrClose", GhaPrCloseTask::class.java) {
            group = "GitHub Operations"
            description = "Closes a Pull Request on GitHub"
        }

        project.tasks.register("ghaPrReopen", GhaPrReopenTask::class.java) {
            group = "GitHub Operations"
            description = "Reopens a closed Pull Request on GitHub"
        }

        project.tasks.register("ghaPrEdit", GhaPrEditTask::class.java) {
            group = "GitHub Operations"
            description = "Edits title, body, base branch, or reviewers of a Pull Request on GitHub"
        }

        project.tasks.register("ghaPrCheckout", GhaPrCheckoutTask::class.java) {
            group = "GitHub Operations"
            description = "Checks out a Pull Request branch locally"
        }

        project.tasks.register("ghaPrReview", GhaPrReviewTask::class.java) {
            group = "GitHub Operations"
            description = "Submits a review on a Pull Request on GitHub"
        }

        // GitHub Release Operations
        project.tasks.register("ghaReleaseCreate", GhaReleaseCreateTask::class.java) {
            group = "GitHub Operations"
            description = "Creates a Release on GitHub"
        }

        // Git Operations
        project.tasks.register("ghaGitStatus", GhaGitStatusTask::class.java) {
            group = "Git Operations"
            description = "Displays current Git repository status"
        }

        project.tasks.register("ghaGitCommit", GhaGitCommitTask::class.java) {
            group = "Git Operations"
            description = "Stages and commits working tree changes"
        }

        project.tasks.register("ghaGitPush", GhaGitPushTask::class.java) {
            group = "Git Operations"
            description = "Pushes current branch to origin remote"
        }

        project.tasks.register("ghaGitPull", GhaGitPullTask::class.java) {
            group = "Git Operations"
            description = "Pulls latest changes from remote with rebase"
        }

        project.tasks.register("ghaGitTag", GhaGitTagTask::class.java) {
            group = "Git Operations"
            description = "Creates and pushes an annotated Git tag"
        }

        project.tasks.register("ghaGitLog", GhaGitLogTask::class.java) {
            group = "Git Operations"
            description = "Displays recent Git commits"
        }
    }
}
