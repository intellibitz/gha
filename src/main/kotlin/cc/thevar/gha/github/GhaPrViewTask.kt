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

@DisableCachingByDefault(because = "Displays details for a Pull Request on GitHub")
abstract class GhaPrViewTask : GhaTask() {

    @get:InputDirectory
    @get:PathSensitive(PathSensitivity.RELATIVE)
    abstract val repoDir: DirectoryProperty

    @get:Input
    @get:Optional
    abstract val prNumber: Property<String>

    init {
        repoDir.convention(project.layout.projectDirectory)
        prNumber.convention(project.providers.gradleProperty("prNumber"))
    }

    @TaskAction
    fun execute() {
        val dir = repoDir.get().asFile
        val num = prNumber.orNull

        val token = gitHubToken.orNull ?: ""
        logger.lifecycle("🔍 [GHA PR View] Fetching Pull Request details...")

        val cmd = mutableListOf("gh", "pr", "view")
        if (!num.isNullOrBlank()) {
            cmd.add(num)
        }
        cmd.add("--comments")

        val env = if (token.isNotBlank()) mapOf("GITHUB_TOKEN" to token, "GH_TOKEN" to token) else emptyMap()
        val result = GhaProcessRunner.exec(
            workingDir = dir,
            command = cmd,
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
