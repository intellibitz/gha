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
 * 2. Server Mode: Exposes GHA/GMA internal tools via JSON-RPC 2.0.
 * 3. Client Mode: Used by GMA and Agents to invoke tools through the GMCP Server.
 */
class GhaGmcpEngine(val rootDir: File) {

    private val mcpHost = GhaMcpHost(rootDir)
    private val slurper = JsonSlurper()

    /**
     * Starts a long-running MCP Server over stdio.
     */
    fun startServer() {
        val scanner = Scanner(System.`in`)
        while (scanner.hasNextLine()) {
            val line = scanner.nextLine()
            if (line.isBlank()) continue
            val response = handleRequest(line)
            if (response != null) {
                println(response)
            }
        }
    }

    /**
     * Processes a single JSON-RPC request and returns a JSON-RPC response.
     */
    fun handleRequest(jsonRequest: String): String? {
        return try {
            val request = slurper.parseText(jsonRequest) as? Map<String, Any> ?: return null
            val id = request["id"]
            val method = request["method"] as? String ?: return null
            val params = request["params"] as? Map<String, Any> ?: emptyMap()

            val result = when (method) {
                "initialize" -> mapOf(
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
                )
                "notifications/initialized" -> return null
                "tools/list" -> {
                    val tools = mcpHost.listTools().map { renderTool(it) }
                    mapOf("tools" to tools)
                }
                "tools/call" -> {
                    val toolName = params["name"] as? String ?: ""
                    val toolArgs = params["arguments"] as? Map<String, Any> ?: emptyMap()
                    val output = mcpHost.callTool(toolName, toolArgs)
                    mapOf(
                        "content" to listOf(mapOf("type" to "text", "text" to output)),
                        "isError" to false
                    )
                }
                "resources/list" -> mapOf("resources" to emptyList<Any>())
                "prompts/list" -> mapOf("prompts" to emptyList<Any>())
                "ping" -> mapOf<String, Any>()
                else -> return createError(id, -32601, "Method not found: $method")
            }

            createResponse(id, result)
        } catch (e: Exception) {
            createError(null, -32603, "Internal error: ${e.message}")
        }
    }

    private fun renderTool(tool: GhaAiTool): Map<String, Any> {
        return mapOf(
            "name" to tool.name,
            "description" to tool.description,
            "inputSchema" to tool.inputSchema
        )
    }

    private fun createResponse(id: Any?, result: Any): String {
        val response = mutableMapOf<String, Any>(
            "jsonrpc" to "2.0",
            "result" to result
        )
        if (id != null) response["id"] = id
        return JsonOutput.toJson(response)
    }

    private fun createError(id: Any?, code: Int, message: String): String {
        val response = mutableMapOf<String, Any>(
            "jsonrpc" to "2.0",
            "error" to mapOf("code" to code, "message" to message)
        )
        if (id != null) response["id"] = id
        return JsonOutput.toJson(response)
    }
}
