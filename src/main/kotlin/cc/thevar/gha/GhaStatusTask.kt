package cc.thevar.gha

import cc.thevar.gha.ai.orchestrator.GhaDaemonManager
import cc.thevar.gha.safety.GhaSandboxManager
import org.gradle.api.tasks.Input
import org.gradle.api.tasks.TaskAction
import org.gradle.work.DisableCachingByDefault

@DisableCachingByDefault(because = "Prints real-time workflow status to console")
abstract class GhaStatusTask : GhaTask() {

    @get:Input
    var taskGradleVersionStr: String = "9.7.1"

    @get:Input
    var taskSubprojectsSummary: String = "(Single Project)"

    @TaskAction
    fun execute() {
        verifySandbox()
        val rootDir = taskRootDirFile
        val userHome = taskGradleUserHomeDirFile
        val (isHealthy, _) = GhaSandboxManager.healthCheck(rootDir, userHome)

        println("📊 [GHA Status] Project: $taskProjectNameStr $taskSubprojectsSummary")
        println("   RootDir: ${rootDir.absolutePath}")
        println("   Sandbox: ${if (isHealthy) "✅ HEALTHY" else "❌ UNHEALTHY"}")
        println("   Master Interactor: ${GhaDaemonManager.getStatus()}")
        println("   Portability: ✅ 100% SELF-CONTAINED (All tools & caches in .gha/)")
        println("   Platform: ${System.getProperty("os.name")} (${System.getProperty("os.arch")})")
        println("   Gradle Engine: $taskGradleVersionStr (Delegating to official Gradle Engine)")
        println("   Git Engine: Official Git VCS CLI (Delegating to GhaGitExec)")
        println("   GitHub Engine: Official GitHub CLI & REST API (Delegating to gh CLI)")
        println("   GitHub Token: ${maskedToken()}")
    }
}
