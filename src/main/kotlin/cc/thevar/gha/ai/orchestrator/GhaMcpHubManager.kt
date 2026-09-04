package cc.thevar.gha.ai.orchestrator

import cc.thevar.gha.ai.mcp.GhaSystemMcpServer
import cc.thevar.gha.ai.vision.GhaUniversalMcpServer
import java.io.File

/**
 * MCP Hub Manager for GHA AI Orchestrator.
 * Registers, discovers, and manages available local and remote Model Context Protocol (MCP) servers on the web and system.
 */
object GhaMcpHubManager {

    data class McpServerConfig(
        val id: String,
        val name: String,
        val description: String,
        val type: String, // BUILT_IN, STDIO, HTTP_SSE
        val commandOrUrl: String,
        val isEnabled: Boolean
    )

    fun getMcpDir(rootDir: File): File {
        val dir = File(rootDir, ".gha/mcp")
        if (!dir.exists()) dir.mkdirs()
        return dir
    }

    /**
     * Lists all registered MCP servers hosted and connected by GHA.
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
                description = "Built-in GHA automation engine exposing $toolCount Git/GitHub/Build/Scaffold tools",
                type = "BUILT_IN",
                commandOrUrl = "cc.thevar.gha.ai.vision.GhaUniversalMcpServer",
                isEnabled = true
            )
        )

        // 2. Custom GHA System Tools MCP Server
        val sysServer = GhaSystemMcpServer(rootDir)
        val sysToolCount = sysServer.exposeTools().size
        servers.add(
            McpServerConfig(
                id = "gha-system-tools",
                name = "GHA Custom System Tools MCP Server",
                description = "Custom MCP server created by GHA exposing $sysToolCount user system tools (ADB, Docker, UV, Node, System CLIs)",
                type = "BUILT_IN",
                commandOrUrl = "cc.thevar.gha.ai.mcp.GhaSystemMcpServer",
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

        // 4. Memory Knowledge Graph MCP Server
        servers.add(
            McpServerConfig(
                id = "memory-mcp",
                name = "Memory Knowledge Graph MCP Server",
                description = "Persistent knowledge graph memory MCP server for local context retention",
                type = "STDIO",
                commandOrUrl = "npx -y @modelcontextprotocol/server-memory",
                isEnabled = true
            )
        )

        // 5. Brave Search Web MCP Server
        servers.add(
            McpServerConfig(
                id = "brave-search-mcp",
                name = "Brave Search Web MCP Server",
                description = "Web search and real-time online discovery MCP server",
                type = "STDIO",
                commandOrUrl = "npx -y @modelcontextprotocol/server-brave-search",
                isEnabled = true
            )
        )

        // 6. Puppeteer Browser Automation MCP Server
        servers.add(
            McpServerConfig(
                id = "puppeteer-mcp",
                name = "Puppeteer Browser MCP Server",
                description = "Headless browser automation and web scraping MCP server",
                type = "STDIO",
                commandOrUrl = "npx -y @modelcontextprotocol/server-puppeteer",
                isEnabled = true
            )
        )

        // 7. Filesystem Workspace MCP Server
        servers.add(
            McpServerConfig(
                id = "filesystem-mcp",
                name = "Filesystem Workspace MCP Server",
                description = "Local workspace filesystem operations MCP server",
                type = "STDIO",
                commandOrUrl = "npx -y @modelcontextprotocol/server-filesystem",
                isEnabled = true
            )
        )

        // 8. Fetch Web Scraper MCP Server
        servers.add(
            McpServerConfig(
                id = "fetch-mcp",
                name = "Fetch Web Scraper MCP Server",
                description = "HTTP fetch and markdown conversion web scraper MCP server",
                type = "STDIO",
                commandOrUrl = "npx -y @modelcontextprotocol/server-fetch",
                isEnabled = true
            )
        )

        // 9. Remote HTTP/SSE Web MCP Server Endpoint
        val remoteMcpUrl = System.getenv("GHA_REMOTE_MCP_URL") ?: "https://mcp.github.com/sse"
        servers.add(
            McpServerConfig(
                id = "remote-sse-mcp",
                name = "Remote Web SSE MCP Server",
                description = "Remote web-hosted Server-Sent Events (SSE) MCP server endpoint",
                type = "HTTP_SSE",
                commandOrUrl = remoteMcpUrl,
                isEnabled = true
            )
        )

        return servers
    }
}
