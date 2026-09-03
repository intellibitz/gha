package cc.thevar.gha.github

import cc.thevar.gha.GhaTask
import cc.thevar.gha.safety.GhaProcessRunner
import org.gradle.api.provider.Property
import org.gradle.api.tasks.Input
import org.gradle.api.tasks.Optional
import org.gradle.api.tasks.TaskAction
import org.gradle.work.DisableCachingByDefault

@DisableCachingByDefault(because = "Edits title, body, labels, or assignees of an Issue on GitHub")
abstract class GhaIssueEditTask : GhaTask() {

    @get:Input
    @get:Optional
    abstract val issueNumber: Property<String>

    @get:Input
    @get:Optional
    abstract val issueTitle: Property<String>

    @get:Input
    @get:Optional
    abstract val issueBody: Property<String>

    @get:Input
    @get:Optional
    abstract val addLabels: Property<String>

    @get:Input
    @get:Optional
    abstract val addAssignees: Property<String>

    init {
        issueNumber.convention(project.providers.gradleProperty("issueNumber"))
        issueTitle.convention(project.providers.gradleProperty("issueTitle"))
        issueBody.convention(project.providers.gradleProperty("issueBody"))
        addLabels.convention(project.providers.gradleProperty("addLabels"))
        addAssignees.convention(project.providers.gradleProperty("addAssignees"))
    }

    @TaskAction
    fun execute() {
        val dir = projectRootDir.get().asFile
        val num = issueNumber.orNull

        if (num.isNullOrBlank()) {
            logger.error("❌ Issue number required. Usage: ./gradlew ghaIssueEdit -PissueNumber=1 [-PissueTitle=\"...\"] [-PissueBody=\"...\"] [-PaddLabels=\"bug\"]")
            return
        }

        val token = gitHubToken.orNull ?: ""
        logger.lifecycle("✏️ [GHA Issue Edit] Updating Issue #$num...")

        val cmd = mutableListOf("gh", "issue", "edit", num)

        issueTitle.orNull?.takeIf { it.isNotBlank() }?.let { title ->
            cmd.add("--title")
            cmd.add(title)
        }

        issueBody.orNull?.takeIf { it.isNotBlank() }?.let { body ->
            cmd.add("--body")
            cmd.add(body)
        }

        addLabels.orNull?.takeIf { it.isNotBlank() }?.let { labels ->
            cmd.add("--add-label")
            cmd.add(labels)
        }

        addAssignees.orNull?.takeIf { it.isNotBlank() }?.let { assignees ->
            cmd.add("--add-assignee")
            cmd.add(assignees)
        }

        if (cmd.size == 4) {
            logger.error("❌ No edit fields specified. Provide -PissueTitle, -PissueBody, -PaddLabels, or -PaddAssignees.")
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
            logger.lifecycle("✅ ${result.stdout.ifEmpty { "Issue #$num updated successfully." }}")
        } else {
            logger.error("❌ ${result.stderr.ifEmpty { result.stdout }}")
        }
    }
}
