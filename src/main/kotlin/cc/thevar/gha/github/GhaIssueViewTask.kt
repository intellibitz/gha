package cc.thevar.gha.github

import cc.thevar.gha.GhaTask
import cc.thevar.gha.safety.GhaProcessRunner
import org.gradle.api.provider.Property
import org.gradle.api.tasks.Input
import org.gradle.api.tasks.Optional
import org.gradle.api.tasks.TaskAction
import org.gradle.work.DisableCachingByDefault

@DisableCachingByDefault(because = "Displays details for a GitHub Issue")
abstract class GhaIssueViewTask : GhaTask() {

    @get:Input
    @get:Optional
    abstract val issueNumber: Property<String>

    init {
        issueNumber.convention(project.providers.gradleProperty("issueNumber"))
    }

    @TaskAction
    fun execute() {
        val dir = projectRootDir.get().asFile
        val num = issueNumber.orNull

        if (num.isNullOrBlank()) {
            logger.error("❌ Issue number required. Usage: ./gradlew ghaIssueView -PissueNumber=1")
            return
        }

        val token = gitHubToken.orNull ?: ""
        logger.lifecycle("🔍 [GHA Issue View] Fetching details for Issue #$num...")

        val env = if (token.isNotBlank()) mapOf("GITHUB_TOKEN" to token, "GH_TOKEN" to token) else emptyMap()
        val result = GhaProcessRunner.exec(
            workingDir = dir,
            command = listOf("gh", "issue", "view", num, "--comments"),
            extraEnv = env,
            timeoutSeconds = 30L
        )

        if (result.isSuccess) {
            logger.lifecycle(result.stdout.prependIndent("   "))
        } else {
            logger.error("❌ ${result.stderr.ifEmpty { result.stdout }}")
        }
    }
}
