package cc.thevar.gha

import cc.thevar.gha.safety.GhaSandboxManager
import org.gradle.api.tasks.TaskAction
import org.gradle.work.DisableCachingByDefault

@DisableCachingByDefault(because = "Prints real-time sandbox status to console")
abstract class GhaSandboxTask : GhaTask() {

    @TaskAction
    fun execute() {
        verifySandbox()
        val rootDir = projectRootDir.get().asFile
        val userHome = gradleUserHomeDir.get().asFile
        val (isHealthy, message) = GhaSandboxManager.healthCheck(rootDir, userHome)

        logger.lifecycle("🛡️ [GHA Sandbox Report]")
        logger.lifecycle("   Status: ${if (isHealthy) "✅ HEALTHY" else "❌ UNHEALTHY"}")
        logger.lifecycle("   Message: $message")
        
        logger.lifecycle("\n   Checks:")
        logger.lifecycle("   - .gha/gha.json: ${if (GhaSandboxManager.checkIfGhaJsonExists(rootDir)) "✅ Found" else "❌ Missing"}")
        logger.lifecycle("   - gradle.user.home: ${if (GhaSandboxManager.checkGradleUserHome(rootDir, userHome)) "✅ Enforced" else "❌ Incorrect"}")
        logger.lifecycle("     Current Home: ${userHome.canonicalPath}")
    }
}
