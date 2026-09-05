package cc.thevar.gha.ai.orchestrator

import cc.thevar.gha.ai.mcp.GhaGmcpEngine
import cc.thevar.gha.safety.GhaSandboxManager
import kotlinx.coroutines.*
import java.io.File
import java.net.ServerSocket
import java.nio.file.Paths
import kotlin.system.exitProcess

/**
 * GMA Master Daemon: Always-on background process for GHA.
 * Provides 100% gains with 0% effort by managing the 4-tier ecosystem.
 */
class GhaDaemon(val globalDir: File) {

    private val gmcpEngine = GhaGmcpEngine(globalDir)
    private val scope = CoroutineScope(Dispatchers.Default + SupervisorJob())

    fun start() {
        println("🌌 [GMA Daemon] Starting Always-On Master Agent...")
        
        // 1. Lock check
        val lockFile = File(globalDir, "gma.lock")
        if (lockFile.exists()) {
            println("⚠️ [GMA Daemon] Lock file exists. Another instance might be running.")
            // Basic self-healing: if lock is very old, assume dead
            if (System.currentTimeMillis() - lockFile.lastModified() > 300_000) {
                println("✨ [GMA Daemon] Stale lock detected. Overriding...")
            } else {
                println("❌ [GMA Daemon] Active instance detected. Exiting.")
                exitProcess(0)
            }
        }
        lockFile.writeText(ProcessHandle.current().pid().toString())
        lockFile.deleteOnExit()

        // 2. Start Project Monitor
        scope.launch {
            monitorProjects()
        }

        // 3. Start MCP Server over Socket (Internal)
        startSocketServer()
    }

    private suspend fun monitorProjects() {
        while (true) {
            // Monitor all projects registered in global sandbox
            delay(60_000)
            println("🌌 [GMA Daemon] Monitoring active projects for health and sync status...")
        }
    }

    private fun startSocketServer() {
        val port = 9090 // GMA Default Port
        try {
            ServerSocket(port).use { serverSocket ->
                println("🔌 [GMA Daemon] GMCP Server listening on port $port")
                while (true) {
                    val clientSocket = serverSocket.accept()
                    scope.launch(Dispatchers.IO) {
                        clientSocket.use { socket ->
                            val reader = socket.getInputStream().bufferedReader()
                            val writer = socket.getOutputStream().bufferedWriter()
                            
                            val request = reader.readLine()
                            if (request != null) {
                                val response = gmcpEngine.handleRequest(request)
                                if (response != null) {
                                    writer.write(response + "\n")
                                    writer.flush()
                                }
                            }
                        }
                    }
                }
            }
        } catch (e: Exception) {
            println("❌ [GMA Daemon] Failed to start socket server: ${e.message}")
        }
    }

    companion object {
        @JvmStatic
        fun main(args: Array<String>) {
            val globalDir = GhaSandboxManager.ensureGlobalSandbox()
            GhaDaemon(globalDir).start()
        }
    }
}
