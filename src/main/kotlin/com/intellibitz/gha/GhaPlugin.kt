package com.intellibitz.gha

import org.gradle.api.Plugin
import org.gradle.api.Project

class GhaPlugin : Plugin<Project> {
    override fun apply(project: Project) {
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
    }
}
