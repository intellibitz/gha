package cc.thevar.gha

import org.gradle.api.DefaultTask
import org.gradle.api.file.DirectoryProperty
import org.gradle.api.provider.Property
import org.gradle.api.tasks.Input
import org.gradle.api.tasks.InputDirectory
import org.gradle.api.tasks.PathSensitive
import org.gradle.api.tasks.PathSensitivity
import org.gradle.api.tasks.TaskAction
import org.gradle.work.DisableCachingByDefault

@DisableCachingByDefault(because = "Prints real-time workflow status to console")
abstract class GhaStatusTask : DefaultTask() {

    @get:Input
    abstract val projectName: Property<String>

    @get:InputDirectory
    @get:PathSensitive(PathSensitivity.RELATIVE)
    abstract val projectRootDir: DirectoryProperty

    @get:Input
    abstract val gradleVersion: Property<String>

    init {
        projectName.convention(project.name)
        projectRootDir.convention(project.layout.projectDirectory)
        gradleVersion.convention(project.gradle.gradleVersion)
    }

    @TaskAction
    fun execute() {
        logger.lifecycle("📊 [GHA Status] Project: ${projectName.get()}")
        logger.lifecycle("   RootDir: ${projectRootDir.get().asFile.absolutePath}")
        logger.lifecycle("   Platform: ${System.getProperty("os.name")} (${System.getProperty("os.arch")})")
        logger.lifecycle("   Gradle Version: ${gradleVersion.get()}")
    }
}
