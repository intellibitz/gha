package cc.thevar.gha.github

import cc.thevar.gha.GhaTask
import cc.thevar.gha.safety.GhaProcessRunner
import org.gradle.api.provider.Property
import org.gradle.api.tasks.Input
import org.gradle.api.tasks.Optional
import org.gradle.api.tasks.TaskAction
import org.gradle.work.DisableCachingByDefault

@DisableCachingByDefault(because = "Creates Pull Requests on GitHub")
abstract class GhaPrCreateTask : GhaTask() {

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
    abstract val prHead: Property<String>

    @get:Input
    @get:Optional
    abstract val prDraft: Property<String>

    @get:Input
    @get:Optional
    abstract val prReviewers: Property<String>

    @get:Input
    @get:Optional
    abstract val prLabels: Property<String>

    init {
        prTitle.convention(project.providers.gradleProperty("prTitle").orElse("Automated Pull Request via GHA"))
        prBody.convention(project.providers.gradleProperty("prBody").orElse("Automated Pull Request created by GHA"))
        prBase.convention(project.providers.gradleProperty("prBase"))
        prHead.convention(project.providers.gradleProperty("prHead"))
        prDraft.convention(project.providers.gradleProperty("prDraft"))
        prReviewers.convention(project.providers.gradleProperty("prReviewers"))
        prLabels.convention(project.providers.gradleProperty("prLabels"))
    }

    @TaskAction
    fun execute() {
        val dir = projectRootDir.get().asFile
        val title = prTitle.get()
        val body = prBody.get()
        val token = gitHubToken.orNull ?: ""

        logger.lifecycle("🔀 [GHA PR Create] Creating Pull Request: \"$title\"...")

        val cmd = mutableListOf("gh", "pr", "create", "--title", title, "--body", body)

        prBase.orNull?.takeIf { it.isNotBlank() }?.let { base ->
            cmd.add("--base")
            cmd.add(base)
        }

        prHead.orNull?.takeIf { it.isNotBlank() }?.let { head ->
            cmd.add("--head")
            cmd.add(head)
        }

        if (prDraft.orNull?.lowercase() == "true") {
            cmd.add("--draft")
        }

        prReviewers.orNull?.takeIf { it.isNotBlank() }?.let { reviewers ->
            cmd.add("--reviewer")
            cmd.add(reviewers)
        }

        prLabels.orNull?.takeIf { it.isNotBlank() }?.let { labels ->
            cmd.add("--label")
            cmd.add(labels)
        }

        val env = if (token.isNotBlank()) mapOf("GITHUB_TOKEN" to token, "GH_TOKEN" to token) else emptyMap()
        val result = GhaProcessRunner.exec(
            workingDir = dir,
            command = cmd,
            extraEnv = env,
            timeoutSeconds = 45L
        )

        if (result.isSuccess) {
            logger.lifecycle("✅ Pull Request created successfully:\n${result.stdout.prependIndent("   ")}")
        } else {
            logger.lifecycle("ℹ️ ${result.stdout.ifEmpty { result.stderr }}")
        }
    }
}
