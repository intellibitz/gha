package cc.thevar.gha.github

import cc.thevar.gha.GhaTask
import cc.thevar.gha.safety.GhaProcessRunner
import org.gradle.api.provider.Property
import org.gradle.api.tasks.Input
import org.gradle.api.tasks.Optional
import org.gradle.api.tasks.TaskAction
import org.gradle.work.DisableCachingByDefault

@DisableCachingByDefault(because = "Creates Issues on GitHub")
abstract class GhaIssueCreateTask : GhaTask() {

    @get:Input
    @get:Optional
    abstract val issueTitle: Property<String>

    @get:Input
    @get:Optional
    abstract val issueBody: Property<String>

    @get:Input
    @get:Optional
    abstract val issueLabels: Property<String>

    @get:Input
    @get:Optional
    abstract val issueAssignees: Property<String>

    init {
        issueTitle.convention(project.providers.gradleProperty("issueTitle").orElse("Automated Issue via GHA"))
        issueBody.convention(project.providers.gradleProperty("issueBody").orElse("Automated Issue created by GHA"))
        issueLabels.convention(project.providers.gradleProperty("issueLabels"))
        issueAssignees.convention(project.providers.gradleProperty("issueAssignees"))
    }

    @TaskAction
    fun execute() {
        val dir = projectRootDir.get().asFile
        val title = issueTitle.get()
        val body = issueBody.get()
        val token = gitHubToken.orNull ?: ""

        logger.lifecycle("📌 [GHA Issue Create] Creating Issue: \"$title\"...")

        val cmd = mutableListOf("gh", "issue", "create", "--title", title, "--body", body)

        issueLabels.orNull?.takeIf { it.isNotBlank() }?.let { labels ->
            cmd.add("--label")
            cmd.add(labels)
        }

        issueAssignees.orNull?.takeIf { it.isNotBlank() }?.let { assignees ->
            cmd.add("--assignee")
            cmd.add(assignees)
        }

        val env = if (token.isNotBlank()) mapOf("GITHUB_TOKEN" to token, "GH_TOKEN" to token) else emptyMap()
        val result = GhaProcessRunner.exec(
            workingDir = dir,
            command = cmd,
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
