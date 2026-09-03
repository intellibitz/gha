package cc.thevar.gha

import cc.thevar.gha.safety.GhaSandboxManager
import org.gradle.api.provider.Property
import org.gradle.api.tasks.Input
import org.gradle.api.tasks.TaskAction
import org.gradle.work.DisableCachingByDefault

@DisableCachingByDefault(because = "Prints real-time workflow status to console")
abstract class GhaStatusTask : GhaTask() {

    @get:Input
    abstract val gradleVersion: Property<String>

    init {
        gradleVersion.convention(project.gradle.gradleVersion)
    }

    @TaskAction
    fun execute() {
        val rootDir = projectRootDir.get().asFile
        val userHome = gradleUserHomeDir.get().asFile
        val pName = ghaProjectName.getOrElse("gha")
        val (isHealthy, _) = GhaSandboxManager.healthCheck(rootDir, userHome)
        val subprojects = project.rootProject.subprojects.map { ":${it.name}" }

        println("📊 [GHA Status] Project: $pName ${if (subprojects.isNotEmpty()) "(Subprojects: ${subprojects.joinToString(", ")})" else "(Single Project)"}")
        println("   RootDir: ${rootDir.absolutePath}")
        println("   Sandbox: ${if (isHealthy) "✅ HEALTHY" else "❌ UNHEALTHY"}")
        println("   Platform: ${System.getProperty("os.name")} (${System.getProperty("os.arch")})")
        println("   Gradle Engine: ${gradleVersion.get()} (Delegating to official Gradle Engine)")
        println("   Git Engine: Official Git VCS CLI (Delegating to GhaGitExec)")
        println("   GitHub Engine: Official GitHub CLI & REST API (Delegating to gh CLI)")
        println("   GitHub Token: ${maskedToken()}")
    }
}
