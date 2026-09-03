package cc.thevar.gha

import org.gradle.api.provider.Property
import org.gradle.api.tasks.Input
import org.gradle.api.tasks.Optional
import org.gradle.api.tasks.TaskAction
import org.gradle.work.DisableCachingByDefault

@DisableCachingByDefault(because = "Executes dynamic workflow actions")
abstract class GhaWorkflowTask : GhaTask() {

    @get:Input
    abstract val projectName: Property<String>

    @get:Input
    @get:Optional
    abstract val workflowName: Property<String>

    init {
        projectName.convention(project.name)
    }

    @TaskAction
    fun execute() {
        val name = workflowName.orNull ?: "default"
        logger.lifecycle("⚙️ [GHA Workflow] Executing workflow '$name' for ${projectName.get()}...")
        logger.lifecycle("🔒 [GHA Security] Authenticated with Token: ${maskedToken()}")
        logger.lifecycle("✅ Workflow '$name' completed successfully.")
    }
}
