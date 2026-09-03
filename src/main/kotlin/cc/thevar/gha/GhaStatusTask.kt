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
        projectName.convention(project.rootProject.name)
        gradleVersion.convention(project.gradle.gradleVersion)
    }

    @TaskAction
    fun execute() {
        val rootDir = projectRootDir.get().asFile
        val userHome = gradleUserHomeDir.get().asFile
        val (isHealthy, _) = GhaSandboxManager.healthCheck(rootDir, userHome)
        val subprojects = project.rootProject.subprojects.map { ":${it.name}" }

        logger.lifecycle("📊 [GHA Status] Project: ${projectName.get()} ${if (subprojects.isNotEmpty()) "(Subprojects: ${subprojects.joinToString(", ")})" else "(Single Project)"}")
        logger.lifecycle("   RootDir: ${rootDir.absolutePath}")
        logger.lifecycle("   Sandbox: ${if (isHealthy) "✅ HEALTHY" else "❌ UNHEALTHY"}")
        logger.lifecycle("   Platform: ${System.getProperty("os.name")} (${System.getProperty("os.arch")})")
        logger.lifecycle("   Gradle Engine: ${gradleVersion.get()} (Delegating to official Gradle Engine)")
        logger.lifecycle("   Git Engine: Official Git VCS CLI (Delegating to GhaGitExec)")
        logger.lifecycle("   GitHub Engine: Official GitHub CLI & REST API (Delegating to gh CLI)")
        logger.lifecycle("   GitHub Token: ${maskedToken()}")
    }
}
