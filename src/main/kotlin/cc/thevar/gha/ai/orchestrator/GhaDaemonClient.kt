package cc.thevar.gha.ai.orchestrator

import cc.thevar.gha.safety.GhaSandboxManager
import groovy.json.JsonOutput
import groovy.json.JsonSlurper
import java.io.File
import java.net.Socket

/**
 * Client to communicate with the GhaDaemon.
 */
class GhaDaemonClient {

    companion object {
        private const val HOST = "localhost"
        private const val PORT = 9090
    }

    fun sendMission(goal: String, rootDir: File): String {
        return try {
            Socket(HOST, PORT).use { socket ->
                val writer = socket.getOutputStream().bufferedWriter()
                val reader = socket.getInputStream().bufferedReader()

                val request = JsonOutput.toJson(mapOf(
                    "jsonrpc" to "2.0",
                    "id" to System.currentTimeMillis(),
                    "method" to "tools/call",
                    "params" to mapOf(
                        "name" to "agent",
                        "arguments" to mapOf(
                            "goal" to goal,
                            "rootDir" to rootDir.absolutePath
                        )
                    )
                ))

                writer.write(request + "\n")
                writer.flush()

                val responseJson = reader.readLine() ?: return "Error: Empty response from GMA Daemon"
                val slurper = JsonSlurper()
                val response = slurper.parseText(responseJson) as? Map<String, Any>
                val result = response?.get("result") as? Map<String, Any>
                val content = result?.get("content") as? List<Map<String, Any>>
                content?.firstOrNull()?.get("text")?.toString() ?: responseJson
            }
        } catch (e: Exception) {
            "Error communicating with GMA Daemon: ${e.message}. Fallback to local execution may be required."
        }
    }
}
