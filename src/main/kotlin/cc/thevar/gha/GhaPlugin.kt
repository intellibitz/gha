package cc.thevar.gha

import cc.thevar.gha.git.GhaGitCommitTask
import cc.thevar.gha.git.GhaGitPushTask
import cc.thevar.gha.git.GhaGitStatusTask
import cc.thevar.gha.git.GhaGitTagTask
import org.gradle.api.Plugin
import org.gradle.api.Project

class GhaPlugin : Plugin<Project> {
    override fun apply(project: Project) {
        // GitHub Automation Tasks
        project.tasks.register("ghaInit", GhaInitTask::class.java) {
            group = "GitHub Automation"
            description = "Initializes GitHub Automation workflows for any project automatically"
        }

        project.tasks.register("ghaStatus", GhaStatusTask::class.java) {
            group = "GitHub Automation"
            description = "Displays the current GitHub Automation workflow status"
        }

        project.tasks.register("ghaWorkflow", GhaWorkflowTask::class.java) {
            group = "GitHub Automation"
            description = "Executes platform-independent GitHub automation workflows"
        }

        // Git Automation Tasks
        project.tasks.register("ghaGitStatus", GhaGitStatusTask::class.java) {
            group = "Git Automation"
            description = "Displays the current Git repository status"
        }

        project.tasks.register("ghaGitCommit", GhaGitCommitTask::class.java) {
            group = "Git Automation"
            description = "Stages and commits working tree changes"
        }

        project.tasks.register("ghaGitPush", GhaGitPushTask::class.java) {
            group = "Git Automation"
            description = "Pushes the current branch to origin remote"
        }

        project.tasks.register("ghaGitTag", GhaGitTagTask::class.java) {
            group = "Git Automation"
            description = "Creates and pushes an annotated Git tag"
        }
    }
}
