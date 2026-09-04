package cc.thevar.gha.ai.mcp

import cc.thevar.gha.ai.orchestrator.GhaMcpHubManager
import cc.thevar.gha.ai.vision.GhaAiTool
import cc.thevar.gha.ai.vision.GhaUniversalMcpServer
import java.io.File

/**
 * GHA MCP Host: Central MCP Tool Host that manages MCP Servers and serves Agents (MCP Clients).
 * All tool execution in GHA flows through the GHA MCP Host.
 */
class GhaMcpHost(val rootDir: File) {

    private val universalServer = GhaUniversalMcpServer(rootDir)

    /**
     * Lists all tools exposed by hosted MCP servers for Agents (MCP Clients).
     */
    fun listTools(): List<GhaAiTool> {
        val tools = mutableListOf<GhaAiTool>()
        tools.addAll(universalServer.exposeTools())
        return tools
    }

    /**
     * Executes a tool request on behalf of an Agent (MCP Client).
     */
    fun callTool(toolName: String, arguments: Map<String, Any> = emptyMap()): String {
        return universalServer.executeTool(toolName, arguments)
    }

    /**
     * Returns a status summary of all active MCP servers hosted by GHA.
     */
    fun getStatusReport(): String {
        val hubServers = GhaMcpHubManager.listServers(rootDir)
        val tools = listTools()
        return "GHA MCP Host active: ${hubServers.size} MCP servers hosted, ${tools.size} tools available for Agents."
    }
}
