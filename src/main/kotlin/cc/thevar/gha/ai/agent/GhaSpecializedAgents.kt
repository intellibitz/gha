package cc.thevar.gha.ai.agent

import cc.thevar.gha.ai.mcp.GhaMcpHost
import cc.thevar.gha.ai.vision.GhaAgentResult
import cc.thevar.gha.ai.vision.GhaAiAgent
import java.io.File

/**
 * Specialized Agents acting as MCP Clients.
 * Each Agent communicates with GHA MCP Host to execute tools and get work done.
 */

/**
 * 🏗️ Scaffolding Agent: Handles project scaffolding via MCP tools.
 */
class GhaScaffoldingAgent(
    override val identity: String = "GHA-Scaffolder-01",
    override val name: String = "GHA Scaffolding Agent",
    override val role: String = "Project Scaffolding & Application Creator"
) : GhaAiAgent, GhaAgent {

    override fun executeMission(projectDir: File, prompt: String): String {
        val host = GhaMcpHost(projectDir)
        return solveWithHost(prompt, projectDir, host).output
    }

    override fun solve(goal: String, rootDir: File): GhaAgentResult {
        return solveWithHost(goal, rootDir, GhaMcpHost(rootDir))
    }

    fun solveWithHost(goal: String, rootDir: File, mcpHost: GhaMcpHost): GhaAgentResult {
        val log = mutableListOf<String>()
        val lowerGoal = goal.lowercase()
        log.add("🏗️ Agent '$identity' ($name) acting as MCP Client for goal: \"$goal\"")

        val toolName = if (lowerGoal.contains("android")) "scaffold_android" else "scaffold_kotlin"
        log.add("   ► [MCP Client] Calling MCP Tool '$toolName' on GHA MCP Host...")
        val output = mcpHost.callTool(toolName, mapOf("projectName" to rootDir.name, "targetDir" to rootDir.absolutePath))
        log.add("   ✅ [MCP Response] $output")

        return GhaAgentResult(true, log, output)
    }
}

/**
 * 🛠️ Build & Test Agent: Handles build compilation, testing, and self-healing recovery via MCP tools.
 */
class GhaBuildTestAgent(
    override val identity: String = "GHA-Builder-01",
    override val name: String = "GHA Build & Test Agent",
    override val role: String = "Compilation, Testing & Self-Healing Agent"
) : GhaAiAgent, GhaAgent {

    override fun executeMission(projectDir: File, prompt: String): String {
        val host = GhaMcpHost(projectDir)
        return solveWithHost(prompt, projectDir, host).output
    }

    override fun solve(goal: String, rootDir: File): GhaAgentResult {
        return solveWithHost(goal, rootDir, GhaMcpHost(rootDir))
    }

    fun solveWithHost(goal: String, rootDir: File, mcpHost: GhaMcpHost): GhaAgentResult {
        val log = mutableListOf<String>()
        val outputs = mutableListOf<String>()
        log.add("🛠️ Agent '$identity' ($name) acting as MCP Client for goal: \"$goal\"")

        try {
            log.add("   ► [MCP Client] Calling MCP Tool 'clean'...")
            val cleanRes = mcpHost.callTool("clean")
            outputs.add(cleanRes)

            log.add("   ► [MCP Client] Calling MCP Tool 'build'...")
            val buildRes = mcpHost.callTool("build")
            outputs.add(buildRes)

            if (goal.contains("test") || goal.contains("check")) {
                log.add("   ► [MCP Client] Calling MCP Tool 'test'...")
                val testRes = mcpHost.callTool("test")
                outputs.add(testRes)
            }

            return GhaAgentResult(true, log, outputs.joinToString("\n"))
        } catch (e: Exception) {
            log.add("   🩹 [Self-Healing] Exception occurred: ${e.message}. Retrying via MCP 'clean' and 'build'...")
            mcpHost.callTool("clean")
            val retryRes = mcpHost.callTool("build")
            return GhaAgentResult(true, log, "Recovered via self-healing: $retryRes")
        }
    }
}

/**
 * 🔄 VCS & PR Agent: Handles Git commits, rebase sync, push, and GitHub PRs via MCP tools.
 */
class GhaVcsAgent(
    override val identity: String = "GHA-VCS-01",
    override val name: String = "GHA Version Control & PR Agent",
    override val role: String = "Git VCS, Sync & GitHub PR Management Agent"
) : GhaAiAgent, GhaAgent {

    override fun executeMission(projectDir: File, prompt: String): String {
        val host = GhaMcpHost(projectDir)
        return solveWithHost(prompt, projectDir, host).output
    }

    override fun solve(goal: String, rootDir: File): GhaAgentResult {
        return solveWithHost(goal, rootDir, GhaMcpHost(rootDir))
    }

    fun solveWithHost(goal: String, rootDir: File, mcpHost: GhaMcpHost): GhaAgentResult {
        val log = mutableListOf<String>()
        log.add("🔄 Agent '$identity' ($name) acting as MCP Client for goal: \"$goal\"")

        log.add("   ► [MCP Client] Calling MCP Tool 'status'...")
        val statusRes = mcpHost.callTool("status")
        log.add("   ├── [MCP Response] $statusRes")

        log.add("   ► [MCP Client] Calling MCP Tool 'sync'...")
        val syncRes = mcpHost.callTool("sync", mapOf("message" to goal))
        log.add("   └── [MCP Response] $syncRes")

        return GhaAgentResult(true, log, "$statusRes\n$syncRes")
    }
}

/**
 * 🛡️ Security Agent: Handles Dependabot and security audits via MCP tools.
 */
class GhaSecurityAgent(
    override val identity: String = "GHA-Security-01",
    override val name: String = "GHA Security Agent",
    override val role: String = "Dependabot & Security Audit Agent"
) : GhaAiAgent, GhaAgent {

    override fun executeMission(projectDir: File, prompt: String): String {
        val host = GhaMcpHost(projectDir)
        return solveWithHost(prompt, projectDir, host).output
    }

    override fun solve(goal: String, rootDir: File): GhaAgentResult {
        return solveWithHost(goal, rootDir, GhaMcpHost(rootDir))
    }

    fun solveWithHost(goal: String, projectDir: File, mcpHost: GhaMcpHost): GhaAgentResult {
        val log = mutableListOf<String>()
        log.add("🛡️ Agent '$identity' ($name) acting as MCP Client for security audit ($goal) on ${projectDir.name}")

        log.add("   ► [MCP Client] Calling MCP Tool 'security_status'...")
        val output = mcpHost.callTool("security_status")
        log.add("   └── [MCP Response] $output")

        return GhaAgentResult(true, log, output)
    }
}

/**
 * 📚 Wiki Agent: Handles documentation and wiki sync via MCP tools.
 */
class GhaWikiAgent(
    override val identity: String = "GHA-Wiki-01",
    override val name: String = "GHA Documentation & Wiki Agent",
    override val role: String = "Documentation & GitHub Wiki Agent"
) : GhaAiAgent, GhaAgent {

    override fun executeMission(projectDir: File, prompt: String): String {
        val host = GhaMcpHost(projectDir)
        return solveWithHost(prompt, projectDir, host).output
    }

    override fun solve(goal: String, rootDir: File): GhaAgentResult {
        return solveWithHost(goal, rootDir, GhaMcpHost(rootDir))
    }

    fun solveWithHost(goal: String, projectDir: File, mcpHost: GhaMcpHost): GhaAgentResult {
        val log = mutableListOf<String>()
        log.add("📚 Agent '$identity' ($name) acting as MCP Client for wiki sync ($goal) on ${projectDir.name}")

        log.add("   ► [MCP Client] Calling MCP Tool 'wiki_sync'...")
        val output = mcpHost.callTool("wiki_sync")
        log.add("   └── [MCP Response] $output")

        return GhaAgentResult(true, log, output)
    }
}
