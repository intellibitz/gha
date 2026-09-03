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

@DisableCachingByDefault(because = "Lists GitHub Project boards for an owner or repository")
abstract class GhaProjectListTask : GhaTask() {

    @get:InputDirectory
    @get:PathSensitive(PathSensitivity.RELATIVE)
    abstract val projectRootDir: DirectoryProperty

    @get:Input
    @get:Optional
    abstract val projectOwner: Property<String>

    init {
        projectRootDir.convention(project.layout.projectDirectory)
        projectOwner.convention(project.providers.gradleProperty("projectOwner"))
    }

    @TaskAction
    fun execute() {
        val rootDir = projectRootDir.get().asFile
        val token = gitHubToken.orNull
        val owner = projectOwner.orNull

        logger.lifecycle("📋 [GHA Project List] Listing GitHub Project boards...")

        val result = GhaProjectManager.listProjects(rootDir, token, owner)

        if (result.isSuccess) {
            if (result.stdout.isNotBlank()) {
                logger.lifecycle(result.stdout.prependIndent("   "))
            } else {
                logger.lifecycle("   No GitHub Project boards found.")
            }
        } else {
            logger.error("❌ ${result.stderr.ifEmpty { result.stdout }}")
        }
    }
}
