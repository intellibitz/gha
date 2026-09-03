package cc.thevar.gha.projects

import cc.thevar.gha.GhaTask
import org.gradle.api.provider.Property
import org.gradle.api.tasks.Input
import org.gradle.api.tasks.Optional
import org.gradle.api.tasks.TaskAction
import org.gradle.work.DisableCachingByDefault

@DisableCachingByDefault(because = "Creates a new GitHub Project board")
abstract class GhaProjectCreateTask : GhaTask() {

    @get:Input
    @get:Optional
    abstract val projectTitle: Property<String>

    @get:Input
    @get:Optional
    abstract val projectOwner: Property<String>

    init {
        projectTitle.convention(project.providers.gradleProperty("projectTitle").orElse("${project.name} Project Board"))
        projectOwner.convention(project.providers.gradleProperty("projectOwner"))
    }

    @TaskAction
    fun execute() {
        val rootDir = projectRootDir.get().asFile
        val token = gitHubToken.orNull
        val title = projectTitle.get()
        val owner = projectOwner.orNull

        logger.lifecycle("📌 [GHA Project Create] Creating GitHub Project board: \"$title\"...")

        val result = GhaProjectManager.createProject(rootDir, token, title, owner)

        if (result.isSuccess) {
            logger.lifecycle("✅ ${result.stdout.ifEmpty { "GitHub Project board created successfully." }}")
        } else {
            logger.error("❌ ${result.stderr.ifEmpty { result.stdout }}")
        }
    }
}
