package com.intellibitz.gha

import org.gradle.api.DefaultTask
import org.gradle.api.provider.Property
import org.gradle.api.tasks.Input
import org.gradle.api.tasks.TaskAction
import org.gradle.work.DisableCachingByDefault

@DisableCachingByDefault(because = "Initializes GitHub Automation workflows")
abstract class GhaInitTask : DefaultTask() {

    @get:Input
    abstract val projectName: Property<String>

    init {
        projectName.convention(project.name)
    }

    @TaskAction
    fun execute() {
        logger.lifecycle("🚀 [GHA] GitHub Automation initialized for project: ${projectName.get()}")
        logger.lifecycle("✅ 100% Kotlin | 100% Platform Independent | 100% GitHub Automation")
    }
}
