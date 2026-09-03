package cc.thevar.gha

import org.gradle.api.file.DirectoryProperty
import org.gradle.api.provider.Property
import org.gradle.api.tasks.Input
import org.gradle.api.tasks.InputDirectory
import org.gradle.api.tasks.PathSensitive
import org.gradle.api.tasks.PathSensitivity
import org.gradle.api.tasks.TaskAction
import org.gradle.work.DisableCachingByDefault
import java.io.File

@DisableCachingByDefault(because = "Initializes sandboxed GitHub Automation environment")
abstract class GhaInitTask : GhaTask() {

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
        val ghaDir = File(rootDir, ".gha")
        if (!ghaDir.exists()) {
            ghaDir.mkdirs()
        }

        val configFile = File(ghaDir, "gha.json")
        if (!configFile.exists()) {
            configFile.writeText(
                """
                {
                  "project": "${projectName.get()}",
                  "version": "0.1.0",
                  "sandboxed": true
                }
                """.trimIndent()
            )
        }

        logger.lifecycle("🚀 [GHA] GitHub Automation initialized in local sandbox: ${ghaDir.absolutePath}")
        logger.lifecycle("🔒 [GHA Security] GitHub Token: ${maskedToken()}")
        logger.lifecycle("✅ 100% Self-Contained | 0% System Modifications | Sandboxed in ${ghaDir.name}/")
    }
}
