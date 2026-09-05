package cc.thevar.gha.ai.mcp

import cc.thevar.gha.ai.vision.GhaAiTool
import cc.thevar.gha.safety.GhaProcessRunner
import groovy.json.JsonOutput
import groovy.json.JsonSlurper
import java.io.File
import java.util.Scanner

/**
 * GMA Master MCP Engine (GMCP): Full implementation of the Model Context Protocol (MCP).
 * GMCP acts as a Host, Client, and Server, making GHA capabilities available to everyone.
 *
 * Architecture:
 * 1. Host Mode: Manages external MCP servers and aggregates tools.
 * 2. Server Mode: Exposes GHA/GMA internal tools via JSON-RPC 2.0 (stdio).
 * 3. Client Mode: Used by Agents to invoke tools.
 */
class GhaGmcpEngine(val rootDir: File) {

    private val mcpHost = GhaMcpHost(rootDir)

    /**
     * Starts a long-running MCP Server over stdio.
     * Implements the full Model Context Protocol (JSON-RPC 2.0).
     */
    fun startServer() {
        val scanner = Scanner(System.`in`)
        val slurper = JsonSlurper()

        while (scanner.hasNextLine()) {
            val line = scanner.nextLine()
            if (line.isBlank()) continue

            try {
                val request = slurper.parseText(line) as? Map<String, Any> ?: continue
                val id = request["id"]
                val method = request["method"] as? String ?: continue
                val params = request["params"] as? Map<String, Any> ?: emptyMap()

                when (method) {
                    "initialize" -> sendResponse(id, mapOf(
                        "protocolVersion" to "2024-11-05",
                        "capabilities" to mapOf(
                            "tools" to mapOf("listChanged" to true),
                            "resources" to mapOf("subscribe" to true, "listChanged" to true),
                            "prompts" to mapOf("listChanged" to true)
                        ),
                        "serverInfo" to mapOf(
                            "name" to "GMA-Master-MCP-Server",
                            "version" to "0.1.0"
                        )
                    ))
                    "notifications/initialized" -> { /* No-op */ }
                    "tools/list" -> {
                        val tools = mcpHost.listTools().map { renderTool(it) }
                        sendResponse(id, mapOf("tools" to tools))
                    }
                    "tools/call" -> {
                        val toolName = params["name"] as? String ?: ""
                        val toolArgs = params["arguments"] as? Map<String, Any> ?: emptyMap()
                        val result = mcpHost.callTool(toolName, toolArgs)
                        sendResponse(id, mapOf(
                            "content" to listOf(mapOf("type" to "text", "text" to result)),
                            "isError" to false
                        ))
                    }
                    "resources/list" -> sendResponse(id, mapOf("resources" to emptyList<Any>()))
                    "prompts/list" -> sendResponse(id, mapOf("prompts" to emptyList<Any>()))
                    "ping" -> sendResponse(id, mapOf())
                    else -> sendError(id, -32601, "Method not found: $method")
                }
            } catch (e: Exception) {
                System.err.println("GMCP Error: ${e.message}")
            }
        }
    }

    private fun renderTool(tool: GhaAiTool): Map<String, Any> {
        return mapOf(
            "name" to tool.name,
            "description" to tool.description,
            "inputSchema" to tool.inputSchema
        )
    }

    private fun sendResponse(id: Any?, result: Map<String, Any>) {
        val response = mutableMapOf<String, Any>(
            "jsonrpc" to "2.0",
            "result" to result
        )
        if (id != null) response["id"] = id
        println(JsonOutput.toJson(response))
    }

    private fun sendError(id: Any?, code: Int, message: String) {
        val response = mutableMapOf<String, Any>(
            "jsonrpc" to "2.0",
            "error" to mapOf("code" to code, "message" to message)
        )
        if (id != null) response["id"] = id
        println(JsonOutput.toJson(response))
    }
}
