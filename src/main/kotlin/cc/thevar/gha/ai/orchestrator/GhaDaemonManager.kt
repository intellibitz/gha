package cc.thevar.gha.ai.orchestrator

import cc.thevar.gha.safety.GhaProcessRunner
import cc.thevar.gha.safety.GhaSandboxManager
import java.io.File

/**
 * Manages the GMA Daemon lifecycle (start, stop, status).
 */
object GhaDaemonManager {

    private const val PORT = 9090

    fun isRunning(): Boolean {
        val globalDir = GhaSandboxManager.getGlobalGhaDir()
        val lockFile = File(globalDir, "gma.lock")
        if (!lockFile.exists()) return false
        
        val pid = lockFile.readText().trim().toLongOrNull() ?: return false
        return ProcessHandle.of(pid).isPresent
    }

    fun startDaemon(projectDir: File): String {
        if (isRunning()) return "✅ GMA Daemon is already running."

        println("🚀 [GhaDaemonManager] Spawning GMA Master Daemon in background...")
        
        // Use gradlew to run the daemon task (to be added)
        val process = ProcessBuilder(
            "./gradlew", ":ghaAiOrchestrate", "-Paction=daemon", "-Dgradle.user.home=.gha/gradle-user-home"
        ).directory(projectDir)
         .start()
        
        return "🌌 GMA Daemon start triggered. Use './ghai :status' to verify."
    }

    fun stopDaemon(): String {
        val globalDir = GhaSandboxManager.getGlobalGhaDir()
        val lockFile = File(globalDir, "gma.lock")
        if (!lockFile.exists()) return "ℹ️ GMA Daemon is not running."
        
        val pid = lockFile.readText().trim().toLongOrNull() ?: return "Error: Invalid PID in lock file."
        ProcessHandle.of(pid).ifPresent { it.destroy() }
        lockFile.delete()
        
        return "🛑 GMA Daemon stopped successfully."
    }

    fun getStatus(): String {
        return if (isRunning()) {
            "✅ GMA Daemon is ACTIVE (Always-On Master Agent)"
        } else {
            "🛑 GMA Daemon is INACTIVE"
        }
    }
}
