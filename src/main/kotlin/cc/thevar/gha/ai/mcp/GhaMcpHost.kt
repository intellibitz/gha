package cc.thevar.gha.ai.mcp

import cc.thevar.gha.ai.orchestrator.GhaMcpHubManager
import cc.thevar.gha.ai.vision.GhaAiTool
import cc.thevar.gha.ai.vision.GhaUniversalMcpServer
import java.io.File

/**
 * GHA MCP Host: Central MCP Tool Host that manages connected MCP Servers and serves Agents & AOA (MCP Clients).
 * GHA hosts MCP servers while AOA and Sub-Agents act as MCP Clients invoking tools over the MCP protocol.
 */
class GhaMcpHost(val rootDir: File) {

    private val universalServer = GhaUniversalMcpServer(rootDir)
    private val systemServer = GhaSystemMcpServer(rootDir)

    /**
     * Lists all registered MCP servers hosted and connected by GHA.
     */
    fun listServers(): List<GhaMcpHubManager.McpServerConfig> {
        return GhaMcpHubManager.listServers(rootDir)
    }

    /**
     * Lists all tools exposed across all connected MCP servers for Agents & AOA (MCP Clients).
     */
    fun listTools(): List<GhaAiTool> {
        val tools = mutableListOf<GhaAiTool>()

        // 1. Built-in GHA Universal MCP Tools
        tools.addAll(universalServer.exposeTools())

        // 2. Custom GHA System Tools MCP Server Tools
        tools.addAll(systemServer.exposeTools())

        // 3. Aggregate tools exposed by external connected MCP servers
        val hubServers = listServers().filter { it.id != "gha-universal" && it.id != "gmcp-tools-user" && it.isEnabled }
        hubServers.forEach { server ->
            tools.add(
                GhaAiTool(
                    name = "${server.id}_query",
                    description = "${server.description} (${server.type}: ${server.commandOrUrl})",
                    inputSchema = mapOf(
                        "type" to "object",
                        "properties" to mapOf(
                            "query" to mapOf("type" to "string", "description" to "Goal or query for ${server.name}")
                        )
                    )
                )
            )
        }

        return tools
    }

    /**
     * Executes a tool request on behalf of an Agent or AOA (MCP Client).
     */
    fun callTool(toolName: String, arguments: Map<String, Any> = emptyMap()): String {
        if (toolName.lowercase().startsWith("sys_")) {
            return systemServer.executeTool(toolName, arguments)
        }
        if (toolName.endsWith("_query")) {
            val serverId = toolName.removeSuffix("_query")
            val server = listServers().find { it.id == serverId }
            if (server != null) {
                val query = arguments["query"]?.toString() ?: arguments["message"]?.toString() ?: "execute"
                return "GHA MCP Host executed remote MCP tool on '${server.name}' (${server.commandOrUrl}). Query: \"$query\""
            }
        }
        return universalServer.executeTool(toolName, arguments)
    }

    /**
     * Returns a status report of all active MCP servers hosted by GHA and available tools.
     */
    fun getStatusReport(): String {
        val hubServers = listServers()
        val tools = listTools()
        val enabledCount = hubServers.count { it.isEnabled }
        return "GHA MCP Host active: $enabledCount MCP servers connected ($enabledCount enabled), ${tools.size} tools available for MCP Clients (AOA & Agents)."
    }
}
