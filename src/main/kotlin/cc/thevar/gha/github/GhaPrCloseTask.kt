package cc.thevar.gha.github

import cc.thevar.gha.GhaTask
import cc.thevar.gha.safety.GhaProcessRunner
import org.gradle.api.provider.Property
import org.gradle.api.tasks.Input
import org.gradle.api.tasks.Optional
import org.gradle.api.tasks.TaskAction
import org.gradle.work.DisableCachingByDefault

@DisableCachingByDefault(because = "Closes a Pull Request on GitHub")
abstract class GhaPrCloseTask : GhaTask() {

    @get:Input
    @get:Optional
    abstract val prNumber: Property<String>

    @get:Input
    @get:Optional
    abstract val closeComment: Property<String>

    @get:Input
    @get:Optional
    abstract val deleteBranch: Property<String>

    init {
        prNumber.convention(project.providers.gradleProperty("prNumber"))
        closeComment.convention(project.providers.gradleProperty("closeComment"))
        deleteBranch.convention(project.providers.gradleProperty("deleteBranch"))
    }

    @TaskAction
    fun execute() {
        val dir = projectRootDir.get().asFile
        val num = prNumber.orNull

        val token = gitHubToken.orNull ?: ""
        logger.lifecycle("🔒 [GHA PR Close] Closing Pull Request ${num?.let { "#$it" } ?: "current branch"}...")

        val cmd = mutableListOf("gh", "pr", "close")
        if (!num.isNullOrBlank()) {
            cmd.add(num)
        }

        closeComment.orNull?.takeIf { it.isNotBlank() }?.let { comment ->
            cmd.add("--comment")
            cmd.add(comment)
        }

        if (deleteBranch.orNull?.lowercase() == "true") {
            cmd.add("--delete-branch")
        }

        val env = if (token.isNotBlank()) mapOf("GITHUB_TOKEN" to token, "GH_TOKEN" to token) else emptyMap()
        val result = GhaProcessRunner.exec(
            workingDir = dir,
            command = cmd,
            extraEnv = env,
            timeoutSeconds = 30L
        )

        if (result.isSuccess) {
            logger.lifecycle("✅ ${result.stdout.ifEmpty { "Pull Request closed successfully." }}")
        } else {
            logger.error("❌ ${result.stderr.ifEmpty { result.stdout }}")
        }
    }
}
