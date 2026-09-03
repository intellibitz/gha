package cc.thevar.gha.projects

import cc.thevar.gha.GhaTask
import org.gradle.api.file.DirectoryProperty
import org.gradle.api.provider.Property
import org.gradle.api.tasks.Input
import org.gradle.api.tasks.InputDirectory
import org.gradle.api.tasks.PathSensitive
import org.gradle.api.tasks.PathSensitivity
import org.gradle.api.tasks.TaskAction
import org.gradle.work.DisableCachingByDefault

@DisableCachingByDefault(because = "Creates default GitHub Project boards for the repository")
abstract class GhaProjectInitTask : GhaTask() {

    @get:Input
    abstract val projectName: Property<String>

    @get:InputDirectory
    @get:PathSensitive(PathSensitivity.RELATIVE)
    abstract val projectRootDir: DirectoryProperty

    init {
        projectName.convention(project.name)
        projectRootDir.convention(project.layout.projectDirectory)
    }

    @TaskAction
    fun execute() {
        val rootDir = projectRootDir.get().asFile
        val token = gitHubToken.orNull
        val name = projectName.get()

        logger.lifecycle("📋 [GHA Project Init] Creating default GitHub Project boards for $name...")

        val created = GhaProjectManager.initDefaultProjects(rootDir, token, name)

        if (created.isNotEmpty()) {
            logger.lifecycle("✅ GitHub Project boards created successfully:")
            created.forEach { board ->
                logger.lifecycle("   📌 $board")
            }
        } else {
            logger.lifecycle("ℹ️ Project boards creation completed or boards already exist.")
        }
    }
}
