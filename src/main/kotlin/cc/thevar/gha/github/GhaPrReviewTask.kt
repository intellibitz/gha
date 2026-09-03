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

@DisableCachingByDefault(because = "Submits a review on a Pull Request on GitHub")
abstract class GhaPrReviewTask : GhaTask() {

    @get:InputDirectory
    @get:PathSensitive(PathSensitivity.RELATIVE)
    abstract val repoDir: DirectoryProperty

    @get:Input
    @get:Optional
    abstract val prNumber: Property<String>

    @get:Input
    @get:Optional
    abstract val approve: Property<String>

    @get:Input
    @get:Optional
    abstract val requestChanges: Property<String>

    @get:Input
    @get:Optional
    abstract val reviewComment: Property<String>

    init {
        repoDir.convention(project.layout.projectDirectory)
        prNumber.convention(project.providers.gradleProperty("prNumber"))
        approve.convention(project.providers.gradleProperty("approve"))
        requestChanges.convention(project.providers.gradleProperty("requestChanges"))
        reviewComment.convention(project.providers.gradleProperty("reviewComment"))
    }

    @TaskAction
    fun execute() {
        val dir = repoDir.get().asFile
        val num = prNumber.orNull

        val token = gitHubToken.orNull ?: ""
        logger.lifecycle("👁️ [GHA PR Review] Reviewing Pull Request ${num?.let { "#$it" } ?: "current branch"}...")

        val cmd = mutableListOf("gh", "pr", "review")
        if (!num.isNullOrBlank()) {
            cmd.add(num)
        }

        when {
            approve.orNull?.lowercase() == "true" -> cmd.add("--approve")
            requestChanges.orNull?.lowercase() == "true" -> cmd.add("--request-changes")
            else -> cmd.add("--comment")
        }

        reviewComment.orNull?.takeIf { it.isNotBlank() }?.let { comment ->
            cmd.add("--body")
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
            logger.lifecycle("✅ ${result.stdout.ifEmpty { "Pull Request review submitted successfully." }}")
        } else {
            logger.error("❌ ${result.stderr.ifEmpty { result.stdout }}")
        }
    }
}
