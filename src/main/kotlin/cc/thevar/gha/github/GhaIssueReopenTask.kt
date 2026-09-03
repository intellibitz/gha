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

@DisableCachingByDefault(because = "Reopens a closed Issue on GitHub")
abstract class GhaIssueReopenTask : GhaTask() {

    @get:InputDirectory
    @get:PathSensitive(PathSensitivity.RELATIVE)
    abstract val repoDir: DirectoryProperty

    @get:Input
    @get:Optional
    abstract val issueNumber: Property<String>

    @get:Input
    @get:Optional
    abstract val reopenComment: Property<String>

    init {
        repoDir.convention(project.layout.projectDirectory)
        issueNumber.convention(project.providers.gradleProperty("issueNumber"))
        reopenComment.convention(project.providers.gradleProperty("reopenComment"))
    }

    @TaskAction
    fun execute() {
        val dir = repoDir.get().asFile
        val num = issueNumber.orNull

        if (num.isNullOrBlank()) {
            logger.error("❌ Issue number required. Usage: ./gradlew ghaIssueReopen -PissueNumber=1 [-PreopenComment=\"Reopening for further investigation\"]")
            return
        }

        val token = gitHubToken.orNull ?: ""
        logger.lifecycle("🔓 [GHA Issue Reopen] Reopening Issue #$num...")

        val cmd = mutableListOf("gh", "issue", "reopen", num)

        reopenComment.orNull?.takeIf { it.isNotBlank() }?.let { comment ->
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
            logger.lifecycle("✅ ${result.stdout.ifEmpty { "Issue #$num reopened successfully." }}")
        } else {
            logger.error("❌ ${result.stderr.ifEmpty { result.stdout }}")
        }
    }
}
