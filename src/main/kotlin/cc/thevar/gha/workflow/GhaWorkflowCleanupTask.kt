package cc.thevar.gha.workflow

import cc.thevar.gha.GhaTask
import org.gradle.api.provider.Property
import org.gradle.api.tasks.Input
import org.gradle.api.tasks.Optional
import org.gradle.api.tasks.TaskAction
import org.gradle.work.DisableCachingByDefault
import java.io.File

@DisableCachingByDefault(because = "Cleans up and deletes old/failed/cancelled GitHub Actions workflow runs")
abstract class GhaWorkflowCleanupTask : GhaTask() {

    @get:Input
    @get:Optional
    abstract val deleteRunId: Property<String>

    @get:Input
    @get:Optional
    abstract val statusFilter: Property<String>

    @get:Input
    @get:Optional
    abstract val cleanupAll: Property<String>

    init {
        deleteRunId.convention(project.providers.gradleProperty("deleteRunId"))
        statusFilter.convention(project.providers.gradleProperty("statusFilter").orElse("all"))
        cleanupAll.convention(project.providers.gradleProperty("cleanupAll").orElse("false"))
    }

    @TaskAction
    fun execute() {
        val rootDir = projectRootDir.get().asFile
        val token = gitHubToken.orNull
        val runId = deleteRunId.orNull
        val filter = statusFilter.get()
        val isCleanupAll = cleanupAll.get().lowercase() == "true"

        val localWorkflowFile = File(rootDir, ".github/workflows/gha.yml")
        if (localWorkflowFile.exists()) {
            localWorkflowFile.delete()
            logger.lifecycle("🧹 [GHA Workflow Cleanup] Deleted local .github/workflows/gha.yml")
        }

        if (!runId.isNullOrBlank()) {
            logger.lifecycle("🧹 [GHA Workflow Cleanup] Deleting workflow run #$runId...")
            val result = GhaWorkflowManager.deleteWorkflowRun(rootDir, token, runId)
            if (result.isSuccess) {
                logger.lifecycle("✅ Workflow run #$runId deleted successfully.")
            } else {
                logger.error("❌ Failed to delete workflow run #$runId: ${result.stderr.ifEmpty { result.stdout }}")
            }
        } else if (isCleanupAll) {
            logger.lifecycle("🧹 [GHA Workflow Cleanup] Cleaning up workflow runs (filter: $filter)...")
            val (deleted, failed) = GhaWorkflowManager.cleanupWorkflowRuns(rootDir, token, filter)
            logger.lifecycle("✅ Workflow runs cleanup completed: $deleted deleted, $failed failed.")
        } else {
            logger.error("❌ Specify workflow run ID (-PdeleteRunId=123) or cleanup all (-PcleanupAll=true).")
        }
    }
}
