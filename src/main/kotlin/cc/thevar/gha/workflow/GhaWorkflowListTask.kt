package cc.thevar.gha.workflow

import cc.thevar.gha.GhaTask
import org.gradle.api.provider.Property
import org.gradle.api.tasks.Input
import org.gradle.api.tasks.Optional
import org.gradle.api.tasks.TaskAction
import org.gradle.work.DisableCachingByDefault

@DisableCachingByDefault(because = "Lists recent GitHub Actions workflow runs")
abstract class GhaWorkflowListTask : GhaTask() {

    @get:Input
    @get:Optional
    abstract val runLimit: Property<String>

    init {
        runLimit.convention(project.providers.gradleProperty("runLimit").orElse("30"))
    }

    @TaskAction
    fun execute() {
        val rootDir = projectRootDir.get().asFile
        val token = gitHubToken.orNull
        val limit = runLimit.get().toIntOrNull() ?: 30

        logger.lifecycle("⚙️ [GHA Workflow List] Listing recent $limit GitHub Actions workflow runs...")

        val result = GhaWorkflowManager.listWorkflowRuns(rootDir, token, limit)

        if (result.isSuccess) {
            if (result.stdout.isNotBlank()) {
                logger.lifecycle(result.stdout.prependIndent("   "))
            } else {
                logger.lifecycle("   No recent workflow runs found.")
            }
        } else {
            logger.error("❌ ${result.stderr.ifEmpty { result.stdout }}")
        }
    }
}
