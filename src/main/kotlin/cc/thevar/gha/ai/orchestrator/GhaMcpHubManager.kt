package cc.thevar.gha.ai.orchestrator

import cc.thevar.gha.ai.vision.GhaUniversalMcpServer
import java.io.File

/**
 * MCP Hub Manager for GHA AI Orchestrator.
 * Registers, discovers, and manages community MCP (Model Context Protocol) servers.
 */
object GhaMcpHubManager {

    data class McpServerConfig(
        val id: String,
        val name: String,
        val description: String,
        val type: String, // BUILT_IN, STDIO, HTTP
        val commandOrUrl: String,
        val isEnabled: Boolean
    )

    fun getMcpDir(rootDir: File): File {
        val dir = File(rootDir, ".gha/mcp")
        if (!dir.exists()) dir.mkdirs()
        return dir
    }

    /**
     * Lists all registered MCP servers in the Hub.
     */
    fun listServers(rootDir: File): List<McpServerConfig> {
        val servers = mutableListOf<McpServerConfig>()

        // 1. Built-in GHA Universal MCP Server
        val ghaServer = GhaUniversalMcpServer(rootDir)
        val toolCount = ghaServer.exposeTools().size
        servers.add(
            McpServerConfig(
                id = "gha-universal",
                name = "GHA Universal MCP Server",
                description = "Built-in GHA automation engine exposing $toolCount Git/GitHub/Build tools",
                type = "BUILT_IN",
                commandOrUrl = "cc.thevar.gha.ai.vision.GhaUniversalMcpServer",
                isEnabled = true
            )
        )

        // 2. Official GitHub MCP Server
        servers.add(
            McpServerConfig(
                id = "github-mcp",
                name = "GitHub MCP Server",
                description = "Official GitHub REST/GraphQL API integration server for issues, PRs, and repos",
                type = "STDIO",
                commandOrUrl = "npx -y @modelcontextprotocol/server-github",
                isEnabled = true
            )
        )

        // 3. Hugging Face Hub MCP Server
        servers.add(
            McpServerConfig(
                id = "hf-hub-mcp",
                name = "Hugging Face Hub MCP Server",
                description = "Hugging Face Hub MCP server for model, dataset, space, and paper discovery",
                type = "STDIO",
                commandOrUrl = "hf mcp server",
                isEnabled = true
            )
        )

        // 4. Memory / Context MCP Server
        servers.add(
            McpServerConfig(
                id = "memory-mcp",
                name = "Memory MCP Server",
                description = "Persistent knowledge graph memory MCP server for local context retention",
                type = "STDIO",
                commandOrUrl = "npx -y @modelcontextprotocol/server-memory",
                isEnabled = true
            )
        )

        return servers
    }
}
