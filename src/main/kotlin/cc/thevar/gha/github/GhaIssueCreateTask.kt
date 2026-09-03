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

@DisableCachingByDefault(because = "Creates Issues on GitHub")
abstract class GhaIssueCreateTask : GhaTask() {

    @get:InputDirectory
    @get:PathSensitive(PathSensitivity.RELATIVE)
    abstract val repoDir: DirectoryProperty

    @get:Input
    abstract val issueTitle: Property<String>

    @get:Input
    @get:Optional
    abstract val issueBody: Property<String>

    init {
        repoDir.convention(project.layout.projectDirectory)
        issueBody.convention("Automated Issue created by GHA")
    }

    @TaskAction
    fun execute() {
        val dir = repoDir.get().asFile
        val title = issueTitle.get()
        val body = issueBody.get()
        val token = gitHubToken.orNull ?: ""

        logger.lifecycle("📌 [GHA Issue Create] Creating Issue: \"$title\"...")

        val env = if (token.isNotBlank()) mapOf("GITHUB_TOKEN" to token) else emptyMap()
        val result = GhaProcessRunner.exec(
            workingDir = dir,
            command = listOf("gh", "issue", "create", "--title", title, "--body", body),
            extraEnv = env,
            timeoutSeconds = 30L
        )

        if (result.isSuccess) {
            logger.lifecycle("✅ Issue created successfully:\n${result.stdout.prependIndent("   ")}")
        } else {
            logger.lifecycle("ℹ️ ${result.stdout.ifEmpty { result.stderr }}")
        }
    }
}
