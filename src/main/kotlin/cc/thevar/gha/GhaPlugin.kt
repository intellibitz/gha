package cc.thevar.gha

import cc.thevar.gha.ai.GhaAiContextTask
import cc.thevar.gha.ai.GhaAiTask
import cc.thevar.gha.ai.mcp.GhaMcpTask
import cc.thevar.gha.ai.orchestrator.GhaAiOrchestratorTask
import cc.thevar.gha.git.GhaGitBranchTask
import cc.thevar.gha.git.GhaGitCheckinTask
import cc.thevar.gha.git.GhaGitCheckoutTask
import cc.thevar.gha.git.GhaGitCloneTask
import cc.thevar.gha.git.GhaGitCommitTask
import cc.thevar.gha.git.GhaGitDiffTask
import cc.thevar.gha.git.GhaGitInitTask
import cc.thevar.gha.git.GhaGitLogTask
import cc.thevar.gha.git.GhaGitPullTask
import cc.thevar.gha.git.GhaGitPushTask
import cc.thevar.gha.git.GhaGitResetTask
import cc.thevar.gha.git.GhaGitStashTask
import cc.thevar.gha.git.GhaGitStatusTask
import cc.thevar.gha.git.GhaGitTagTask
import cc.thevar.gha.github.GhaGistCreateTask
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
import cc.thevar.gha.github.GhaRepoViewTask
import cc.thevar.gha.github.GhaSecretSetTask
import cc.thevar.gha.insights.GhaContributorsTask
import cc.thevar.gha.insights.GhaInsightsTask
import cc.thevar.gha.insights.GhaTrafficTask
import cc.thevar.gha.projects.GhaProjectAddItemTask
import cc.thevar.gha.projects.GhaProjectCloseTask
import cc.thevar.gha.projects.GhaProjectCreateTask
import cc.thevar.gha.projects.GhaProjectInitTask
import cc.thevar.gha.projects.GhaProjectListTask
import cc.thevar.gha.projects.GhaProjectViewTask
import cc.thevar.gha.safety.GhaBumpVersionTask
import cc.thevar.gha.security.GhaCodeScanningInitTask
import cc.thevar.gha.security.GhaDependabotCleanupTask
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
import cc.thevar.gha.workflow.GhaParallelWorkflowTask
import cc.thevar.gha.workflow.GhaWorkflowCancelTask
import cc.thevar.gha.workflow.GhaWorkflowCleanupTask
import cc.thevar.gha.workflow.GhaWorkflowInitTask
import cc.thevar.gha.workflow.GhaWorkflowListTask
import org.gradle.api.Plugin
import org.gradle.api.Project

class GhaPlugin : Plugin<Project> {
    override fun apply(project: Project) {
        val GROUP_GIT = "Git Automation"
        val GROUP_GITHUB = "GitHub Automation"
        val GROUP_GRADLE = "Gradle Automation"
        val GROUP_AI = "GHA AI Automation"

        // ---------------------------------------------------------------------
        // 0. Autonomous AI Automation Tasks (0 Effort, 100% Gain)
        // ---------------------------------------------------------------------
        project.tasks.register("ghai", GhaAiTask::class.java) {
            group = GROUP_AI
            description =
                "0 Effort, 100% Gain: Autonomous AI task for instant local checkin or GitHub PR/CI auto-merge & sync"
        }

        project.tasks.register("ghaAI", GhaAiTask::class.java) {
            group = GROUP_AI
            description =
                "Alias for ghai: Autonomous AI task that auto-detects context, commits, syncs rebase, pushes, and manages PRs"
        }

        project.tasks.register("ghaAuto", GhaAiTask::class.java) {
            group = GROUP_AI
            description = "Alias for ghai: Autonomous zero-effort project automation"
        }

        project.tasks.register("ghaSync", GhaAiTask::class.java) {
            group = GROUP_AI
            description =
                "Alias for ghai: Syncs local working branch 100% with remote base via rebase and push"
        }

        project.tasks.register("ghaSave", GhaAiTask::class.java) {
            group = GROUP_AI
            description = "Alias for ghai: Saves and pushes all local work to GitHub automatically"
        }

        project.tasks.register("ghaAiContext", GhaAiContextTask::class.java) {
            group = GROUP_AI
            description = "Generates a comprehensive AI Context report for LLMs and Agents"
        }

        project.tasks.register("ghaMcp", GhaMcpTask::class.java) {
            group = GROUP_AI
            description = "Exposes gha tasks as Model Context Protocol (MCP) tools for AI Agents"
        }

        project.tasks.register("ghaAiVision", cc.thevar.gha.ai.vision.GhaAiTask::class.java) {
            group = GROUP_AI
            description = "GHA AI Vision: Execute autonomous agents or start an MCP bridge for AI models"
        }

        project.tasks.register("ghaAiOrchestrate", GhaAiOrchestratorTask::class.java) {
            group = GROUP_AI
            description = "GHA Agent of Agents & Universal AI Orchestrator: Manage models, engines, MCP hubs, and hardware-optimized AI"
        }

        project.tasks.register("ghaModels", GhaAiOrchestratorTask::class.java) {
            group = GROUP_AI
            description = "List or download AI models optimized for your hardware"
        }

        project.tasks.register("ghaEngines", GhaAiOrchestratorTask::class.java) {
            group = GROUP_AI
            description = "Detect and inspect local AI execution engines (Ollama, Hugging Face CLI, llama.cpp, PyTorch)"
        }

        project.tasks.register("ghaMcpHub", GhaAiOrchestratorTask::class.java) {
            group = GROUP_AI
            description = "List and discover registered MCP Tool Hub servers"
        }

        // ---------------------------------------------------------------------
        // 1. Gradle Automation Tasks
        // ---------------------------------------------------------------------
        project.tasks.register("ghaInit", GhaInitTask::class.java) {
            group = GROUP_GRADLE
            description =
                "Initializes sandboxed GitHub Automation environment for any project automatically"
        }

        project.tasks.register("ghaWorkflowInit", GhaWorkflowInitTask::class.java) {
            group = GROUP_GRADLE
            description =
                "Initializes GitHub Actions CI workflow files (.github/workflows/gha.yml) on demand"
        }

        project.tasks.register("ghaUpdate", GhaUpdateTask::class.java) {
            group = GROUP_GRADLE
            description =
                "Updates gha init scripts, version configurations, and runner wrappers (100% Kotlin)"
        }

        project.tasks.register("ghaBumpVersion", GhaBumpVersionTask::class.java) {
            group = GROUP_GRADLE
            description = "Bumps project version and commits changes prior to push"
        }

        project.tasks.register("ghaUninstall", GhaUninstallTask::class.java) {
            group = GROUP_GRADLE
            description =
                "Completely removes gha sandbox, runner scripts, and workflows (100% Kotlin)"
        }

        project.tasks.register("ghaStatus", GhaStatusTask::class.java) {
            group = GROUP_GRADLE
            description = "Displays the current GitHub Automation workflow status"
        }

        project.tasks.register("ghaSandbox", GhaSandboxTask::class.java) {
            group = GROUP_GRADLE
            description = "Displays a detailed GHA sandbox report"
        }

        project.tasks.register("ghaDependencies", GhaDependenciesTask::class.java) {
            group = GROUP_GRADLE
            description = "Prints all GHA dependencies, tools, SDKs, and their current versions"
        }

        project.tasks.register("ghaKotlinInit", GhaKotlinInitTask::class.java) {
            group = GROUP_GRADLE
            description =
                "Initializes a 100% Kotlin project structure with sandboxed GHA automation"
        }

        project.tasks.register("ghaKotlinProjectCreate", GhaKotlinInitTask::class.java) {
            group = GROUP_GRADLE
            description =
                "Creates a new 100% Kotlin project with Gradle DSL, version catalog, and sandboxed GHA"
        }

        project.tasks.register("ghaAndroidRemove", GhaAndroidRemoveTask::class.java) {
            group = GROUP_GRADLE
            description =
                "Removes Android project files, plugins, and dependencies to convert to a pure Kotlin project"
        }

        project.tasks.register("ghaAndroidProjectRemove", GhaAndroidRemoveTask::class.java) {
            group = GROUP_GRADLE
            description =
                "Alias for ghaAndroidRemove: Converts project to pure Kotlin by removing Android components"
        }

        project.tasks.register("ghaClean", GhaCleanTask::class.java) {
            group = GROUP_GRADLE
            description = "Cleans build directory and temporary caches"
        }

        project.tasks.register("ghaBuild", GhaBuildTask::class.java) {
            group = GROUP_GRADLE
            description = "Executes sandboxed Gradle build"
        }

        project.tasks.register("ghaTest", GhaTestTask::class.java) {
            group = GROUP_GRADLE
            description = "Executes project test suite"
        }

        // ---------------------------------------------------------------------
        // 2. Git Automation Tasks
        // ---------------------------------------------------------------------
        project.tasks.register("ghaGitInit", GhaGitInitTask::class.java) {
            group = GROUP_GIT
            description = "Initializes a local Git repository"
        }

        project.tasks.register("ghaGitStatus", GhaGitStatusTask::class.java) {
            group = GROUP_GIT
            description = "Displays current Git repository status"
        }

        project.tasks.register("ghaGitBranch", GhaGitBranchTask::class.java) {
            group = GROUP_GIT
            description = "Lists, creates, or deletes local and remote Git branches"
        }

        project.tasks.register("ghaGitCheckout", GhaGitCheckoutTask::class.java) {
            group = GROUP_GIT
            description = "Checks out or creates a Git branch safely with stash support"
        }

        project.tasks.register("ghaGitClone", GhaGitCloneTask::class.java) {
            group = GROUP_GIT
            description = "Clones a Git repository from GitHub (e.g., ./ghai clone intellibitz)"
        }

        project.tasks.register("ghaGitCommit", GhaGitCommitTask::class.java) {
            group = GROUP_GIT
            description = "Stages and commits working tree changes"
        }

        project.tasks.register("ghaGitCheckin", GhaGitCheckinTask::class.java) {
            group = GROUP_GIT
            description = "Stages and checks in/commits working tree changes safely"
        }

        project.tasks.register("ghaGitPush", GhaGitPushTask::class.java) {
            group = GROUP_GIT
            description = "Pushes current branch to origin remote"
        }

        project.tasks.register("ghaGitPull", GhaGitPullTask::class.java) {
            group = GROUP_GIT
            description = "Pulls latest changes from remote with rebase"
        }

        project.tasks.register("ghaGitTag", GhaGitTagTask::class.java) {
            group = GROUP_GIT
            description = "Creates and pushes an annotated Git tag"
        }

        project.tasks.register("ghaGitLog", GhaGitLogTask::class.java) {
            group = GROUP_GIT
            description = "Displays recent Git commits"
        }

        project.tasks.register("ghaGitReset", GhaGitResetTask::class.java) {
            group = GROUP_GIT
            description = "Resets working tree changes (--hard / --soft / --mixed)"
        }

        project.tasks.register("ghaGitStash", GhaGitStashTask::class.java) {
            group = GROUP_GIT
            description = "Stashes working tree changes (push / pop / list / drop)"
        }

        project.tasks.register("ghaGitDiff", GhaGitDiffTask::class.java) {
            group = GROUP_GIT
            description = "Inspects working tree changes"
        }

        // ---------------------------------------------------------------------
        // 3. GitHub Automation Tasks
        // ---------------------------------------------------------------------
        project.tasks.register("ghaParallelWorkflow", GhaParallelWorkflowTask::class.java) {
            group = GROUP_GITHUB
            description =
                "Executes enterprise parallel collaboration workflow (pull -> branch -> checkin -> push -> PR -> merge -> auto-cleanup)"
        }

        project.tasks.register("ghaDevWorkflow", GhaParallelWorkflowTask::class.java) {
            group = GROUP_GITHUB
            description =
                "Alias for ghaParallelWorkflow: Executes end-to-end parallel developer contribution workflow on protected branches"
        }

        project.tasks.register("ghaRepoView", GhaRepoViewTask::class.java) {
            group = GROUP_GITHUB
            description = "Displays GitHub repository details and metadata"
        }

        project.tasks.register("ghaGistCreate", GhaGistCreateTask::class.java) {
            group = GROUP_GITHUB
            description = "Creates a GitHub Gist from a local file"
        }

        project.tasks.register("ghaSecretSet", GhaSecretSetTask::class.java) {
            group = GROUP_GITHUB
            description = "Configures repository secrets safely"
        }

        project.tasks.register("ghaWorkflow", GhaWorkflowTask::class.java) {
            group = GROUP_GITHUB
            description = "Executes platform-independent GitHub automation workflows"
        }

        project.tasks.register("ghaWorkflowList", GhaWorkflowListTask::class.java) {
            group = GROUP_GITHUB
            description = "Lists recent GitHub Actions workflow runs and their status"
        }

        project.tasks.register("ghaWorkflowCleanup", GhaWorkflowCleanupTask::class.java) {
            group = GROUP_GITHUB
            description =
                "Cleans up and deletes old, failed, or cancelled GitHub Actions workflow runs"
        }

        project.tasks.register("ghaWorkflowCancel", GhaWorkflowCancelTask::class.java) {
            group = GROUP_GITHUB
            description = "Cancels in-progress GitHub Actions workflow runs"
        }

        project.tasks.register("ghaProjectInit", GhaProjectInitTask::class.java) {
            group = GROUP_GITHUB
            description =
                "Initializes default GitHub Project boards (Roadmap, Issue Tracker, Releases) automatically"
        }

        project.tasks.register("ghaProjectCreate", GhaProjectCreateTask::class.java) {
            group = GROUP_GITHUB
            description = "Creates a new GitHub Project board"
        }

        project.tasks.register("ghaProjectList", GhaProjectListTask::class.java) {
            group = GROUP_GITHUB
            description = "Lists GitHub Project boards for an owner or repository"
        }

        project.tasks.register("ghaProjectView", GhaProjectViewTask::class.java) {
            group = GROUP_GITHUB
            description = "Displays details and items of a GitHub Project board"
        }

        project.tasks.register("ghaProjectAddItem", GhaProjectAddItemTask::class.java) {
            group = GROUP_GITHUB
            description = "Adds an Issue or Pull Request URL to a GitHub Project board"
        }

        project.tasks.register("ghaProjectClose", GhaProjectCloseTask::class.java) {
            group = GROUP_GITHUB
            description = "Closes or archives a GitHub Project board"
        }

        project.tasks.register("ghaInsights", GhaInsightsTask::class.java) {
            group = GROUP_GITHUB
            description =
                "Displays GitHub repository overview, stars, forks, issues, and commit metrics"
        }

        project.tasks.register("ghaContributors", GhaContributorsTask::class.java) {
            group = GROUP_GITHUB
            description = "Displays repository contributors and commit breakdowns"
        }

        project.tasks.register("ghaTraffic", GhaTrafficTask::class.java) {
            group = GROUP_GITHUB
            description = "Displays repository traffic, page views, and clone statistics"
        }

        project.tasks.register("ghaSecurityInit", GhaSecurityInitTask::class.java) {
            group = GROUP_GITHUB
            description =
                "Generates default GitHub security workflows, Dependabot, CodeQL, and SECURITY.md"
        }

        project.tasks.register("ghaSecurityStatus", GhaSecurityStatusTask::class.java) {
            group = GROUP_GITHUB
            description = "Displays current GitHub security, scanning, and Dependabot status"
        }

        project.tasks.register("ghaDependabotInit", GhaDependabotInitTask::class.java) {
            group = GROUP_GITHUB
            description = "Generates .github/dependabot.yml for automated dependency updates"
        }

        project.tasks.register("ghaDependabotList", GhaDependabotListTask::class.java) {
            group = GROUP_GITHUB
            description = "Lists active Dependabot pull requests and remote branches"
        }

        project.tasks.register("ghaDependabotMerge", GhaDependabotMergeTask::class.java) {
            group = GROUP_GITHUB
            description = "Merges open Dependabot pull requests and deletes remote branches"
        }

        project.tasks.register("ghaDependabotClose", GhaDependabotCloseTask::class.java) {
            group = GROUP_GITHUB
            description = "Closes Dependabot pull requests and deletes remote dependabot/ branches"
        }

        project.tasks.register("ghaDependabotCleanup", GhaDependabotCleanupTask::class.java) {
            group = GROUP_GITHUB
            description =
                "Removes stale remote dependabot/ branches that have no open pull requests"
        }

        project.tasks.register("ghaDependabotRebase", GhaDependabotRebaseTask::class.java) {
            group = GROUP_GITHUB
            description =
                "Requests Dependabot to rebase or recreate pull requests to resolve conflicts"
        }

        project.tasks.register("ghaCodeScanningInit", GhaCodeScanningInitTask::class.java) {
            group = GROUP_GITHUB
            description = "Generates CodeQL code scanning workflow .github/workflows/codeql.yml"
        }

        project.tasks.register("ghaWikiInit", GhaWikiInitTask::class.java) {
            group = GROUP_GITHUB
            description = "Initializes local Wiki directory structure and template pages"
        }

        project.tasks.register("ghaWikiStatus", GhaWikiStatusTask::class.java) {
            group = GROUP_GITHUB
            description = "Displays current GitHub Wiki pages and sync status"
        }

        project.tasks.register("ghaWikiSync", GhaWikiSyncTask::class.java) {
            group = GROUP_GITHUB
            description = "Pulls latest changes from remote GitHub Wiki repository"
        }

        project.tasks.register("ghaWikiPublish", GhaWikiPublishTask::class.java) {
            group = GROUP_GITHUB
            description = "Publishes local wiki/ directory pages to remote GitHub Wiki repository"
        }

        project.tasks.register("ghaIssueCreate", GhaIssueCreateTask::class.java) {
            group = GROUP_GITHUB
            description = "Creates an Issue on GitHub"
        }

        project.tasks.register("ghaIssueList", GhaIssueListTask::class.java) {
            group = GROUP_GITHUB
            description = "Lists Issues on GitHub"
        }

        project.tasks.register("ghaIssueView", GhaIssueViewTask::class.java) {
            group = GROUP_GITHUB
            description = "Displays details and comments for a GitHub Issue"
        }

        project.tasks.register("ghaIssueClose", GhaIssueCloseTask::class.java) {
            group = GROUP_GITHUB
            description = "Closes an Issue on GitHub"
        }

        project.tasks.register("ghaIssueReopen", GhaIssueReopenTask::class.java) {
            group = GROUP_GITHUB
            description = "Reopens a closed Issue on GitHub"
        }

        project.tasks.register("ghaIssueComment", GhaIssueCommentTask::class.java) {
            group = GROUP_GITHUB
            description = "Adds a comment to an Issue on GitHub"
        }

        project.tasks.register("ghaIssueEdit", GhaIssueEditTask::class.java) {
            group = GROUP_GITHUB
            description = "Edits title, body, labels, or assignees of an Issue on GitHub"
        }

        project.tasks.register("ghaPrCreate", GhaPrCreateTask::class.java) {
            group = GROUP_GITHUB
            description = "Creates a Pull Request on GitHub"
        }

        project.tasks.register("ghaPrList", GhaPrListTask::class.java) {
            group = GROUP_GITHUB
            description = "Lists Pull Requests on GitHub"
        }

        project.tasks.register("ghaPrView", GhaPrViewTask::class.java) {
            group = GROUP_GITHUB
            description = "Displays details and comments for a Pull Request on GitHub"
        }

        project.tasks.register("ghaPrMerge", GhaPrMergeTask::class.java) {
            group = GROUP_GITHUB
            description = "Merges a Pull Request on GitHub"
        }

        project.tasks.register("ghaPrClose", GhaPrCloseTask::class.java) {
            group = GROUP_GITHUB
            description = "Closes a Pull Request on GitHub"
        }

        project.tasks.register("ghaPrReopen", GhaPrReopenTask::class.java) {
            group = GROUP_GITHUB
            description = "Reopens a closed Pull Request on GitHub"
        }

        project.tasks.register("ghaPrEdit", GhaPrEditTask::class.java) {
            group = GROUP_GITHUB
            description = "Edits title, body, base branch, or reviewers of a Pull Request on GitHub"
        }

        project.tasks.register("ghaPrCheckout", GhaPrCheckoutTask::class.java) {
            group = GROUP_GITHUB
            description = "Checks out a Pull Request branch locally"
        }

        project.tasks.register("ghaPrReview", GhaPrReviewTask::class.java) {
            group = GROUP_GITHUB
            description = "Submits a review on a Pull Request on GitHub"
        }

        project.tasks.register("ghaReleaseCreate", GhaReleaseCreateTask::class.java) {
            group = GROUP_GITHUB
            description = "Creates a Release on GitHub"
        }

        // ---------------------------------------------------------------------
        // 4. Help Task
        // ---------------------------------------------------------------------
        project.tasks.register("ghaHelp", GhaHelpTask::class.java) {
            group = GROUP_GRADLE
            description = "Displays help information for all GHA tasks"
            val staticDetails = project.tasks
                .filter { it.name.startsWith("gha") && it.name != "ghaHelp" }
                .map { "${it.group ?: "Other"}|${it.name}|${it.description ?: "No description"}" }
            taskDetails.set(staticDetails)
        }

        // Configure tasks and set properties at configuration time for Configuration Cache support
        project.tasks.withType(GhaTask::class.java).configureEach {
            taskRootDirFile = project.layout.projectDirectory.asFile
            taskProjectNameStr = project.name
            taskGradleUserHomeDirFile = project.gradle.gradleUserHomeDir
            taskGitHubToken = System.getenv("GITHUB_TOKEN") ?: System.getenv("GH_TOKEN") ?: ""
        }

        project.tasks.withType(GhaAiTask::class.java).configureEach {
            if (project.hasProperty("commitMessage") || project.hasProperty("message") || project.hasProperty("ghaAction")) {
                commitMessage.convention(
                    (project.findProperty("commitMessage") as? String)
                        ?: (project.findProperty("message") as? String)
                        ?: (project.findProperty("ghaAction") as? String)
                )
            }
        }
    }
}
