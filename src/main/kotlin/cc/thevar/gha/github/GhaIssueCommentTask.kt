package cc.thevar.gha.github

import cc.thevar.gha.GhaTask
import cc.thevar.gha.safety.GhaProcessRunner
import org.gradle.api.provider.Property
import org.gradle.api.tasks.Input
import org.gradle.api.tasks.Optional
import org.gradle.api.tasks.TaskAction
import org.gradle.work.DisableCachingByDefault

@DisableCachingByDefault(because = "Adds a comment to an Issue on GitHub")
abstract class GhaIssueCommentTask : GhaTask() {

    @get:Input
    @get:Optional
    abstract val issueNumber: Property<String>

    @get:Input
    @get:Optional
    abstract val commentBody: Property<String>

    init {
        issueNumber.convention(project.providers.gradleProperty("issueNumber"))
        commentBody.convention(project.providers.gradleProperty("commentBody"))
    }

    @TaskAction
    fun execute() {
        val dir = projectRootDir.get().asFile
        val num = issueNumber.orNull
        val body = commentBody.orNull

        if (num.isNullOrBlank() || body.isNullOrBlank()) {
            logger.error("❌ Issue number and comment body required. Usage: ./gradlew ghaIssueComment -PissueNumber=1 -PcommentBody=\"My comment text\"")
            return
        }

        val token = gitHubToken.orNull ?: ""
        logger.lifecycle("💬 [GHA Issue Comment] Adding comment to Issue #$num...")

        val env = if (token.isNotBlank()) mapOf("GITHUB_TOKEN" to token, "GH_TOKEN" to token) else emptyMap()
        val result = GhaProcessRunner.exec(
            workingDir = dir,
            command = listOf("gh", "issue", "comment", num, "--body", body),
            extraEnv = env,
            timeoutSeconds = 30L
        )

        if (result.isSuccess) {
            logger.lifecycle("✅ ${result.stdout.ifEmpty { "Comment added to Issue #$num successfully." }}")
        } else {
            logger.error("❌ ${result.stderr.ifEmpty { result.stdout }}")
        }
    }
}
