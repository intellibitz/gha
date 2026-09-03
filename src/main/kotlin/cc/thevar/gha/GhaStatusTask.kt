package cc.thevar.gha

import cc.thevar.gha.safety.GhaSandboxManager
import org.gradle.api.provider.Property
import org.gradle.api.tasks.Input
import org.gradle.api.tasks.TaskAction
import org.gradle.work.DisableCachingByDefault

@DisableCachingByDefault(because = "Prints real-time workflow status to console")
abstract class GhaStatusTask : GhaTask() {

    @get:Input
    abstract val projectName: Property<String>

    @get:Input
    abstract val gradleVersion: Property<String>

    init {
        projectName.convention(project.name)
        gradleVersion.convention(project.gradle.gradleVersion)
    }

    @TaskAction
    fun execute() {
        val rootDir = projectRootDir.get().asFile
        val userHome = gradleUserHomeDir.get().asFile
        val (isHealthy, _) = GhaSandboxManager.healthCheck(rootDir, userHome)

        logger.lifecycle("📊 [GHA Status] Project: ${projectName.get()}")
        logger.lifecycle("   RootDir: ${rootDir.absolutePath}")
        logger.lifecycle("   Sandbox: ${if (isHealthy) "✅ HEALTHY" else "❌ UNHEALTHY"}")
        logger.lifecycle("   Platform: ${System.getProperty("os.name")} (${System.getProperty("os.arch")})")
        logger.lifecycle("   Gradle Version: ${gradleVersion.get()}")
        logger.lifecycle("   GitHub Token: ${maskedToken()}")
    }
}
