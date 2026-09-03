package cc.thevar.gha.github

import cc.thevar.gha.GhaTask
import cc.thevar.gha.safety.GhaProcessRunner
import org.gradle.api.file.DirectoryProperty
import org.gradle.api.provider.Property
import org.gradle.api.tasks.Input
import org.gradle.api.tasks.InputDirectory
import org.gradle.api.tasks.Optional
import org.gradle.api.tasks.PathSensitive
import org.gradle.api.tasks.PathSensitivity
import org.gradle.api.tasks.TaskAction
import org.gradle.work.DisableCachingByDefault

@DisableCachingByDefault(because = "Closes an Issue on GitHub")
abstract class GhaIssueCloseTask : GhaTask() {

    @get:InputDirectory
    @get:PathSensitive(PathSensitivity.RELATIVE)
    abstract val repoDir: DirectoryProperty

    @get:Input
    @get:Optional
    abstract val issueNumber: Property<String>

    @get:Input
    @get:Optional
    abstract val closeReason: Property<String>

    @get:Input
    @get:Optional
    abstract val closeComment: Property<String>

    init {
        repoDir.convention(project.layout.projectDirectory)
        issueNumber.convention(project.providers.gradleProperty("issueNumber"))
        closeReason.convention(project.providers.gradleProperty("closeReason").orElse("completed"))
        closeComment.convention(project.providers.gradleProperty("closeComment"))
    }

    @TaskAction
    fun execute() {
        val dir = repoDir.get().asFile
        val num = issueNumber.orNull

        if (num.isNullOrBlank()) {
            logger.error("❌ Issue number required. Usage: ./gradlew ghaIssueClose -PissueNumber=1 [-PcloseReason=completed] [-PcloseComment=\"Fixed in v1.0\"]")
            return
        }

        val token = gitHubToken.orNull ?: ""
        val reason = closeReason.get()
        logger.lifecycle("🔒 [GHA Issue Close] Closing Issue #$num (reason: $reason)...")

        val cmd = mutableListOf("gh", "issue", "close", num, "--reason", reason)

        closeComment.orNull?.takeIf { it.isNotBlank() }?.let { comment ->
            cmd.add("--comment")
            cmd.add(comment)
        }

        val env = if (token.isNotBlank()) mapOf("GITHUB_TOKEN" to token, "GH_TOKEN" to token) else emptyMap()
        val result = GhaProcessRunner.exec(
            workingDir = dir,
            command = cmd,
            extraEnv = env,
            timeoutSeconds = 30L
        )

        if (result.isSuccess) {
            logger.lifecycle("✅ ${result.stdout.ifEmpty { "Issue #$num closed successfully." }}")
        } else {
            logger.error("❌ ${result.stderr.ifEmpty { result.stdout }}")
        }
    }
}
