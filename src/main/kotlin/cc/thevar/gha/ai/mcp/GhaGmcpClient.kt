package cc.thevar.gha.ai.mcp

import groovy.json.JsonOutput
import groovy.json.JsonSlurper
import java.io.File

/**
 * GMA Master MCP Client (GMCP): Communicates with the GMCP Server via the MCP Protocol (JSON-RPC).
 * Ensures 100% use of the MCP protocol for all internal tool calls.
 */
class GhaGmcpClient(val rootDir: File) {

    private val engine = GhaGmcpEngine(rootDir)
    private val slurper = JsonSlurper()

    /**
     * Lists all tools available on the GMCP Server.
     */
    fun listTools(): List<Map<String, Any>> {
        val request = createRequest("tools/list")
        val responseJson = engine.handleRequest(request) ?: return emptyList()
        val response = slurper.parseText(responseJson) as? Map<String, Any>
        val result = response?.get("result") as? Map<String, Any>
        return (result?.get("tools") as? List<Map<String, Any>>) ?: emptyList()
    }

    /**
     * Calls a tool on the GMCP Server.
     */
    fun callTool(name: String, arguments: Map<String, Any> = emptyMap()): String {
        val request = createRequest("tools/call", mapOf(
            "name" to name,
            "arguments" to arguments
        ))
        val responseJson = engine.handleRequest(request) ?: return "Error: No response from GMCP Server"
        val response = slurper.parseText(responseJson) as? Map<String, Any>
        
        if (response?.containsKey("error") == true) {
            val error = response["error"] as? Map<String, Any>
            return "GMCP Error [${error?.get("code")}]: ${error?.get("message")}"
        }

        val result = response?.get("result") as? Map<String, Any>
        val content = result?.get("content") as? List<Map<String, Any>>
        return content?.firstOrNull()?.get("text")?.toString() ?: "Success (Empty response)"
    }

    private fun createRequest(method: String, params: Map<String, Any> = emptyMap()): String {
        return JsonOutput.toJson(mapOf(
            "jsonrpc" to "2.0",
            "id" to System.currentTimeMillis(),
            "method" to method,
            "params" to params
        ))
    }
}
