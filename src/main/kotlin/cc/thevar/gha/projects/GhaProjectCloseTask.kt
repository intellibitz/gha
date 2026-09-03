package cc.thevar.gha.projects

import cc.thevar.gha.GhaTask
import org.gradle.api.file.DirectoryProperty
import org.gradle.api.provider.Property
import org.gradle.api.tasks.Input
import org.gradle.api.tasks.InputDirectory
import org.gradle.api.tasks.Optional
import org.gradle.api.tasks.PathSensitive
import org.gradle.api.tasks.PathSensitivity
import org.gradle.api.tasks.TaskAction
import org.gradle.work.DisableCachingByDefault

@DisableCachingByDefault(because = "Closes or archives a GitHub Project board")
abstract class GhaProjectCloseTask : GhaTask() {

    @get:InputDirectory
    @get:PathSensitive(PathSensitivity.RELATIVE)
    abstract val projectRootDir: DirectoryProperty

    @get:Input
    @get:Optional
    abstract val projectNumber: Property<String>

    @get:Input
    @get:Optional
    abstract val projectOwner: Property<String>

    init {
        projectRootDir.convention(project.layout.projectDirectory)
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
            logger.error("❌ Project board number required. Usage: ./gradlew ghaProjectClose -PprojectNumber=1")
            return
        }

        logger.lifecycle("🔒 [GHA Project Close] Closing GitHub Project board #$num...")

        val result = GhaProjectManager.closeProject(rootDir, token, num, owner)

        if (result.isSuccess) {
            logger.lifecycle("✅ ${result.stdout.ifEmpty { "GitHub Project board #$num closed successfully." }}")
        } else {
            logger.error("❌ ${result.stderr.ifEmpty { result.stdout }}")
        }
    }
}
