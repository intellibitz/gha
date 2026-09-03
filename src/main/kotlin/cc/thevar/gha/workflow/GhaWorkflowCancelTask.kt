package cc.thevar.gha.workflow

import cc.thevar.gha.GhaTask
import org.gradle.api.file.DirectoryProperty
import org.gradle.api.provider.Property
import org.gradle.api.tasks.Input
import org.gradle.api.tasks.InputDirectory
import org.gradle.api.tasks.Optional
import org.gradle.api.tasks.PathSensitive
import org.gradle.api.tasks.PathSensitivity
import org.gradle.api.tasks.TaskAction
import org.gradle.work.DisableCachingByDefault

@DisableCachingByDefault(because = "Cancels in-progress GitHub Actions workflow runs")
abstract class GhaWorkflowCancelTask : GhaTask() {

    @get:InputDirectory
    @get:PathSensitive(PathSensitivity.RELATIVE)
    abstract val projectRootDir: DirectoryProperty

    @get:Input
    @get:Optional
    abstract val runId: Property<String>

    init {
        projectRootDir.convention(project.layout.projectDirectory)
        runId.convention(project.providers.gradleProperty("runId"))
    }

    @TaskAction
    fun execute() {
        val rootDir = projectRootDir.get().asFile
        val token = gitHubToken.orNull
        val id = runId.orNull

        if (id.isNullOrBlank()) {
            logger.error("❌ Workflow run ID required. Usage: ./gradlew ghaWorkflowCancel -PrunId=12345")
            return
        }

        logger.lifecycle("🚫 [GHA Workflow Cancel] Cancelling in-progress workflow run #$id...")

        val result = GhaWorkflowManager.cancelWorkflowRun(rootDir, token, id)

        if (result.isSuccess) {
            logger.lifecycle("✅ ${result.stdout.ifEmpty { "Workflow run #$id cancelled successfully." }}")
        } else {
            logger.error("❌ ${result.stderr.ifEmpty { result.stdout }}")
        }
    }
}
