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

@DisableCachingByDefault(because = "Creates Pull Requests on GitHub")
abstract class GhaPrCreateTask : GhaTask() {

    @get:InputDirectory
    @get:PathSensitive(PathSensitivity.RELATIVE)
    abstract val repoDir: DirectoryProperty

    @get:Input
    abstract val prTitle: Property<String>

    @get:Input
    @get:Optional
    abstract val prBody: Property<String>

    init {
        repoDir.convention(project.layout.projectDirectory)
        prBody.convention("Automated Pull Request created by GHA")
    }

    @TaskAction
    fun execute() {
        val dir = repoDir.get().asFile
        val title = prTitle.get()
        val body = prBody.get()
        val token = gitHubToken.orNull ?: ""

        logger.lifecycle("🔀 [GHA PR Create] Creating Pull Request: \"$title\"...")

        val env = if (token.isNotBlank()) mapOf("GITHUB_TOKEN" to token) else emptyMap()
        val result = GhaProcessRunner.exec(
            workingDir = dir,
            command = listOf("gh", "pr", "create", "--title", title, "--body", body),
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
