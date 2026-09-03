package cc.thevar.gha.projects

import cc.thevar.gha.GhaTask
import org.gradle.api.provider.Property
import org.gradle.api.tasks.Input
import org.gradle.api.tasks.Optional
import org.gradle.api.tasks.TaskAction
import org.gradle.work.DisableCachingByDefault

@DisableCachingByDefault(because = "Displays details and items of a GitHub Project board")
abstract class GhaProjectViewTask : GhaTask() {

    @get:Input
    @get:Optional
    abstract val projectNumber: Property<String>

    @get:Input
    @get:Optional
    abstract val projectOwner: Property<String>

    init {
        projectNumber.convention(project.providers.gradleProperty("projectNumber"))
        projectOwner.convention(project.providers.gradleProperty("projectOwner"))
    }

    @TaskAction
    fun execute() {
        val rootDir = projectRootDir.get().asFile
        val token = gitHubToken.orNull
        val num = projectNumber.orNull?.toIntOrNull()
        val owner = projectOwner.orNull

        if (num == null) {
            logger.error("❌ Project board number required. Usage: ./gradlew ghaProjectView -PprojectNumber=1")
            return
        }

        logger.lifecycle("🔍 [GHA Project View] Viewing GitHub Project board #$num...")

        val result = GhaProjectManager.viewProject(rootDir, token, num, owner)

        if (result.isSuccess) {
            logger.lifecycle(result.stdout.prependIndent("   "))
        } else {
            logger.error("❌ ${result.stderr.ifEmpty { result.stdout }}")
        }
    }
}
