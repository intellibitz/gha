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

@DisableCachingByDefault(because = "Edits title, body, base branch, or reviewers of a Pull Request on GitHub")
abstract class GhaPrEditTask : GhaTask() {

    @get:InputDirectory
    @get:PathSensitive(PathSensitivity.RELATIVE)
    abstract val repoDir: DirectoryProperty

    @get:Input
    @get:Optional
    abstract val prNumber: Property<String>

    @get:Input
    @get:Optional
    abstract val prTitle: Property<String>

    @get:Input
    @get:Optional
    abstract val prBody: Property<String>

    @get:Input
    @get:Optional
    abstract val prBase: Property<String>

    @get:Input
    @get:Optional
    abstract val addLabels: Property<String>

    @get:Input
    @get:Optional
    abstract val addReviewers: Property<String>

    init {
        repoDir.convention(project.layout.projectDirectory)
        prNumber.convention(project.providers.gradleProperty("prNumber"))
        prTitle.convention(project.providers.gradleProperty("prTitle"))
        prBody.convention(project.providers.gradleProperty("prBody"))
        prBase.convention(project.providers.gradleProperty("prBase"))
        addLabels.convention(project.providers.gradleProperty("addLabels"))
        addReviewers.convention(project.providers.gradleProperty("addReviewers"))
    }

    @TaskAction
    fun execute() {
        val dir = repoDir.get().asFile
        val num = prNumber.orNull

        val token = gitHubToken.orNull ?: ""
        logger.lifecycle("✏️ [GHA PR Edit] Updating Pull Request ${num?.let { "#$it" } ?: "current branch"}...")

        val cmd = mutableListOf("gh", "pr", "edit")
        if (!num.isNullOrBlank()) {
            cmd.add(num)
        }

        prTitle.orNull?.takeIf { it.isNotBlank() }?.let { title ->
            cmd.add("--title")
            cmd.add(title)
        }

        prBody.orNull?.takeIf { it.isNotBlank() }?.let { body ->
            cmd.add("--body")
            cmd.add(body)
        }

        prBase.orNull?.takeIf { it.isNotBlank() }?.let { base ->
            cmd.add("--base")
            cmd.add(base)
        }

        addLabels.orNull?.takeIf { it.isNotBlank() }?.let { labels ->
            cmd.add("--add-label")
            cmd.add(labels)
        }

        addReviewers.orNull?.takeIf { it.isNotBlank() }?.let { reviewers ->
            cmd.add("--add-reviewer")
            cmd.add(reviewers)
        }

        val initialMinSize = if (!num.isNullOrBlank()) 4 else 3
        if (cmd.size <= initialMinSize) {
            logger.error("❌ No edit fields specified. Provide -PprTitle, -PprBody, -PprBase, -PaddLabels, or -PaddReviewers.")
            return
        }

        val env = if (token.isNotBlank()) mapOf("GITHUB_TOKEN" to token, "GH_TOKEN" to token) else emptyMap()
        val result = GhaProcessRunner.exec(
            workingDir = dir,
            command = cmd,
            extraEnv = env,
            timeoutSeconds = 30L
        )

        if (result.isSuccess) {
            logger.lifecycle("✅ ${result.stdout.ifEmpty { "Pull Request updated successfully." }}")
        } else {
            logger.error("❌ ${result.stderr.ifEmpty { result.stdout }}")
        }
    }
}
