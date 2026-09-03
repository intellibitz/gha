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

@DisableCachingByDefault(because = "Lists Issues on GitHub")
abstract class GhaIssueListTask : GhaTask() {

    @get:InputDirectory
    @get:PathSensitive(PathSensitivity.RELATIVE)
    abstract val repoDir: DirectoryProperty

    @get:Input
    @get:Optional
    abstract val issueState: Property<String>

    @get:Input
    @get:Optional
    abstract val issueLimit: Property<String>

    @get:Input
    @get:Optional
    abstract val issueLabels: Property<String>

    init {
        repoDir.convention(project.layout.projectDirectory)
        issueState.convention(project.providers.gradleProperty("issueState").orElse("open"))
        issueLimit.convention(project.providers.gradleProperty("issueLimit").orElse("30"))
        issueLabels.convention(project.providers.gradleProperty("issueLabels"))
    }

    @TaskAction
    fun execute() {
        val dir = repoDir.get().asFile
        val token = gitHubToken.orNull ?: ""
        val state = issueState.get()
        val limit = issueLimit.get()

        logger.lifecycle("📌 [GHA Issue List] Listing $state Issues (limit $limit)...")

        val cmd = mutableListOf("gh", "issue", "list", "--state", state, "--limit", limit)

        issueLabels.orNull?.takeIf { it.isNotBlank() }?.let { labels ->
            cmd.add("--label")
            cmd.add(labels)
        }

        val env = if (token.isNotBlank()) mapOf("GITHUB_TOKEN" to token, "GH_TOKEN" to token) else emptyMap()
        val result = GhaProcessRunner.exec(
            workingDir = dir,
            command = cmd,
            extraEnv = env,
            timeoutSeconds = 30L
        )

        if (result.isSuccess) {
            if (result.stdout.isNotBlank()) {
                logger.lifecycle(result.stdout.prependIndent("   "))
            } else {
                logger.lifecycle("   No $state Issues found.")
            }
        } else {
            logger.lifecycle("ℹ️ ${result.stdout.ifEmpty { result.stderr }}")
        }
    }
}
