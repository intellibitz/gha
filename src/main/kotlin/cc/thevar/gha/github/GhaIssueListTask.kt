package cc.thevar.gha.github

import cc.thevar.gha.GhaTask
import cc.thevar.gha.safety.GhaProcessRunner
import org.gradle.api.file.DirectoryProperty
import org.gradle.api.tasks.InputDirectory
import org.gradle.api.tasks.PathSensitive
import org.gradle.api.tasks.PathSensitivity
import org.gradle.api.tasks.TaskAction
import org.gradle.work.DisableCachingByDefault

@DisableCachingByDefault(because = "Lists open Issues on GitHub")
abstract class GhaIssueListTask : GhaTask() {

    @get:InputDirectory
    @get:PathSensitive(PathSensitivity.RELATIVE)
    abstract val repoDir: DirectoryProperty

    init {
        repoDir.convention(project.layout.projectDirectory)
    }

    @TaskAction
    fun execute() {
        val dir = repoDir.get().asFile
        val token = gitHubToken.orNull ?: ""

        logger.lifecycle("📌 [GHA Issue List] Listing open Issues...")

        val env = if (token.isNotBlank()) mapOf("GITHUB_TOKEN" to token) else emptyMap()
        val result = GhaProcessRunner.exec(
            workingDir = dir,
            command = listOf("gh", "issue", "list"),
            extraEnv = env,
            timeoutSeconds = 30L
        )

        if (result.isSuccess) {
            if (result.stdout.isNotBlank()) {
                logger.lifecycle(result.stdout.prependIndent("   "))
            } else {
                logger.lifecycle("   No open Issues found.")
            }
        } else {
            logger.lifecycle("ℹ️ ${result.stdout.ifEmpty { result.stderr }}")
        }
    }
}
