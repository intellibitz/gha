package cc.thevar.gha.security

import cc.thevar.gha.GhaTask
import org.gradle.api.file.DirectoryProperty
import org.gradle.api.tasks.InputDirectory
import org.gradle.api.tasks.PathSensitive
import org.gradle.api.tasks.PathSensitivity
import org.gradle.api.tasks.TaskAction
import org.gradle.work.DisableCachingByDefault

@DisableCachingByDefault(because = "Initializes Dependabot configuration file")
abstract class GhaDependabotInitTask : GhaTask() {

    @get:InputDirectory
    @get:PathSensitive(PathSensitivity.RELATIVE)
    abstract val projectRootDir: DirectoryProperty

    init {
        projectRootDir.convention(project.layout.projectDirectory)
    }

    @TaskAction
    fun execute() {
        val rootDir = projectRootDir.get().asFile
        logger.lifecycle("🤖 [GHA Dependabot Init] Generating .github/dependabot.yml...")

        if (GhaSecurityManager.initDependabotConfig(rootDir)) {
            logger.lifecycle("✅ .github/dependabot.yml generated successfully.")
        } else {
            logger.lifecycle("ℹ️ .github/dependabot.yml is already present.")
        }
    }
}
