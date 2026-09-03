package cc.thevar.gha

import cc.thevar.gha.git.GhaGitCommitTask
import cc.thevar.gha.git.GhaGitLogTask
import cc.thevar.gha.git.GhaGitPullTask
import cc.thevar.gha.git.GhaGitPushTask
import cc.thevar.gha.git.GhaGitStatusTask
import cc.thevar.gha.git.GhaGitTagTask
import cc.thevar.gha.github.GhaIssueCreateTask
import cc.thevar.gha.github.GhaIssueListTask
import cc.thevar.gha.github.GhaPrCreateTask
import cc.thevar.gha.github.GhaPrListTask
import cc.thevar.gha.github.GhaReleaseCreateTask
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

        // GitHub Operations
        project.tasks.register("ghaPrCreate", GhaPrCreateTask::class.java) {
            group = "GitHub Operations"
            description = "Creates a Pull Request on GitHub"
        }

        project.tasks.register("ghaPrList", GhaPrListTask::class.java) {
            group = "GitHub Operations"
            description = "Lists open Pull Requests on GitHub"
        }

        project.tasks.register("ghaIssueCreate", GhaIssueCreateTask::class.java) {
            group = "GitHub Operations"
            description = "Creates an Issue on GitHub"
        }

        project.tasks.register("ghaIssueList", GhaIssueListTask::class.java) {
            group = "GitHub Operations"
            description = "Lists open Issues on GitHub"
        }

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
