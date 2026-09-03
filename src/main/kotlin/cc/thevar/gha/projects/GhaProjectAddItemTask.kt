package cc.thevar.gha.projects

import cc.thevar.gha.GhaTask
import org.gradle.api.provider.Property
import org.gradle.api.tasks.Input
import org.gradle.api.tasks.Optional
import org.gradle.api.tasks.TaskAction
import org.gradle.work.DisableCachingByDefault

@DisableCachingByDefault(because = "Adds an Issue or Pull Request to a GitHub Project board")
abstract class GhaProjectAddItemTask : GhaTask() {

    @get:Input
    @get:Optional
    abstract val projectNumber: Property<String>

    @get:Input
    @get:Optional
    abstract val itemUrl: Property<String>

    @get:Input
    @get:Optional
    abstract val projectOwner: Property<String>

    init {
        projectNumber.convention(project.providers.gradleProperty("projectNumber"))
        itemUrl.convention(project.providers.gradleProperty("itemUrl"))
        projectOwner.convention(project.providers.gradleProperty("projectOwner"))
    }

    @TaskAction
    fun execute() {
        val rootDir = projectRootDir.get().asFile
        val token = gitHubToken.orNull
        val num = projectNumber.orNull?.toIntOrNull()
        val url = itemUrl.orNull
        val owner = projectOwner.orNull

        if (num == null || url.isNullOrBlank()) {
            logger.error("❌ Project board number and item URL required. Usage: ./gradlew ghaProjectAddItem -PprojectNumber=1 -PitemUrl=\"https://github.com/owner/repo/issues/1\"")
            return
        }

        logger.lifecycle("➕ [GHA Project Add Item] Adding $url to GitHub Project board #$num...")

        val result = GhaProjectManager.addItemToProject(rootDir, token, num, url, owner)

        if (result.isSuccess) {
            logger.lifecycle("✅ ${result.stdout.ifEmpty { "Item added to project board successfully." }}")
        } else {
            logger.error("❌ ${result.stderr.ifEmpty { result.stdout }}")
        }
    }
}
