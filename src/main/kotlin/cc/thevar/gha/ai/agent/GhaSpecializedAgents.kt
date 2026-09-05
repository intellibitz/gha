package cc.thevar.gha.ai.agent

import cc.thevar.gha.ai.mcp.GhaGmcpClient
import cc.thevar.gha.ai.vision.GhaAgentResult
import cc.thevar.gha.ai.vision.GhaAiAgent
import java.io.File

/**
 * Specialized Agents acting as MCP Clients.
 * Each Agent communicates with GHA MCP Host to execute tools and get work done.
 */

/**
 * 🛠️ Gradle Agent: Handles Gradle build, testing, and project scaffolding via MCP tools.
 */
class GhaGradleAgent(
    override val identity: String = "GHA-Gradle-01",
    override val name: String = "GHA Gradle Agent",
    override val role: String = "Gradle Build, Test & Scaffolding Agent"
) : GhaAiAgent, GhaAgent {

    override fun executeMission(projectDir: File, prompt: String): String {
        val client = GhaGmcpClient(projectDir)
        return solveWithHost(prompt, projectDir, client).output
    }

    override fun solve(goal: String, rootDir: File): GhaAgentResult {
        return solveWithHost(goal, rootDir, GhaGmcpClient(rootDir))
    }

    fun solveWithHost(goal: String, rootDir: File, mcpClient: GhaGmcpClient): GhaAgentResult {
        val log = mutableListOf<String>()
        val lowerGoal = goal.lowercase()
        log.add("🛠️ Agent '$identity' ($name) acting as MCP Client for Gradle goal: \"$goal\"")

        val toolName = when {
            lowerGoal.contains("android") -> "scaffold_android"
            lowerGoal.contains("scaffold") || lowerGoal.contains("kotlin") -> "scaffold_kotlin"
            lowerGoal.contains("clean") -> "clean"
            lowerGoal.contains("test") || lowerGoal.contains("check") -> "test"
            lowerGoal.contains("uninstall") -> "uninstall"
            else -> "build"
        }

        log.add("   ► [MCP Client] Calling Gradle MCP Tool '$toolName' on GHA MCP Host...")
        val args = when (toolName) {
            "scaffold_android", "scaffold_kotlin" -> mapOf("projectName" to rootDir.name, "targetDir" to rootDir.absolutePath)
            else -> emptyMap()
        }

        val output = mcpClient.callTool(toolName, args)
        log.add("   ✅ [MCP Response] ${output.take(100).replace("\n", " ")}...")

        return GhaAgentResult(true, log, output)
    }
}

/**
 * 🔄 Git Agent: Handles Git version control, status, and repository cloning via MCP tools.
 */
class GhaGitAgent(
    override val identity: String = "GHA-Git-01",
    override val name: String = "GHA Git Agent",
    override val role: String = "Git Version Control & Repository Agent"
) : GhaAiAgent, GhaAgent {

    override fun executeMission(projectDir: File, prompt: String): String {
        val client = GhaGmcpClient(projectDir)
        return solveWithHost(prompt, projectDir, client).output
    }

    override fun solve(goal: String, rootDir: File): GhaAgentResult {
        return solveWithHost(goal, rootDir, GhaGmcpClient(rootDir))
    }

    fun solveWithHost(goal: String, rootDir: File, mcpClient: GhaGmcpClient): GhaAgentResult {
        val log = mutableListOf<String>()
        val lowerGoal = goal.lowercase()
        log.add("🔄 Agent '$identity' ($name) acting as MCP Client for Git goal: \"$goal\"")

        val toolName = when {
            lowerGoal.contains("clone") -> "clone"
            lowerGoal.contains("context") -> "context"
            else -> "status"
        }

        log.add("   ► [MCP Client] Calling Git MCP Tool '$toolName' on GHA MCP Host...")
        val args = when (toolName) {
            "clone" -> {
                val repo = goal.split(" ").find { it.contains("/") } ?: ""
                mapOf("repo" to repo)
            }
            else -> emptyMap()
        }

        val output = mcpClient.callTool(toolName, args)
        log.add("   ✅ [MCP Response] ${output.take(100).replace("\n", " ")}...")

        return GhaAgentResult(true, log, output)
    }
}

/**
 * 🐙 GitHub Agent: Handles GitHub platform tasks including PRs, Issues, Workflows, and Sync via MCP tools.
 */
class GhaGitHubAgent(
    override val identity: String = "GHA-GitHub-01",
    override val name: String = "GHA GitHub Agent",
    override val role: String = "GitHub Platform, PR & Issue Agent"
) : GhaAiAgent, GhaAgent {

    override fun executeMission(projectDir: File, prompt: String): String {
        val client = GhaGmcpClient(projectDir)
        return solveWithHost(prompt, projectDir, client).output
    }

    override fun solve(goal: String, rootDir: File): GhaAgentResult {
        return solveWithHost(goal, rootDir, GhaGmcpClient(rootDir))
    }

    fun solveWithHost(goal: String, rootDir: File, mcpClient: GhaGmcpClient): GhaAgentResult {
        val log = mutableListOf<String>()
        val lowerGoal = goal.lowercase()
        log.add("🐙 Agent '$identity' ($name) acting as MCP Client for GitHub goal: \"$goal\"")

        val toolName = when {
            lowerGoal.contains("pr") && lowerGoal.contains("list") -> "pr_list"
            lowerGoal.contains("pr") && lowerGoal.contains("create") -> "pr_create"
            lowerGoal.contains("pr") && lowerGoal.contains("merge") -> "pr_merge"
            lowerGoal.contains("issue") && lowerGoal.contains("list") -> "issue_list"
            lowerGoal.contains("issue") && lowerGoal.contains("create") -> "issue_create"
            lowerGoal.contains("workflow") -> "workflow_list"
            lowerGoal.contains("security") || lowerGoal.contains("audit") || lowerGoal.contains("dependabot") -> "security_status"
            lowerGoal.contains("wiki") || lowerGoal.contains("documentation") -> "wiki_sync"
            else -> "sync"
        }

        log.add("   ► [MCP Client] Calling GitHub MCP Tool '$toolName' on GHA MCP Host...")
        val args = when (toolName) {
            "pr_create", "issue_create" -> mapOf("title" to "Automated Goal: $goal")
            "pr_merge" -> {
                val num = goal.split(" ").find { it.all { c -> c.isDigit() } } ?: "1"
                mapOf("prNumber" to num)
            }
            "sync" -> mapOf("message" to goal)
            else -> emptyMap()
        }

        val output = mcpClient.callTool(toolName, args)
        log.add("   ✅ [MCP Response] ${output.take(100).replace("\n", " ")}...")

        return GhaAgentResult(true, log, output)
    }
}

/**
 * 💻 System Agent: Handles interaction with user system tools (ADB, Docker, Python, Shell) via MCP tools.
 */
class GhaSystemAgent(
    override val identity: String = "GHA-System-01",
    override val name: String = "GHA System Agent",
    override val role: String = "User System Tools & Environment Agent"
) : GhaAiAgent, GhaAgent {

    override fun executeMission(projectDir: File, prompt: String): String {
        val client = GhaGmcpClient(projectDir)
        return solveWithHost(prompt, projectDir, client).output
    }

    override fun solve(goal: String, rootDir: File): GhaAgentResult {
        return solveWithHost(goal, rootDir, GhaGmcpClient(rootDir))
    }

    fun solveWithHost(goal: String, rootDir: File, mcpClient: GhaGmcpClient): GhaAgentResult {
        val log = mutableListOf<String>()
        val lowerGoal = goal.lowercase()
        log.add("💻 Agent '$identity' ($name) acting as MCP Client for system goal: \"$goal\"")

        val toolName = when {
            lowerGoal.contains("adb") || lowerGoal.contains("android device") -> "sys_adb_device"
            lowerGoal.contains("docker") || lowerGoal.contains("container") -> "sys_docker_container"
            lowerGoal.contains("python") || lowerGoal.contains("uv") -> "sys_python_env"
            lowerGoal.contains("exec") || lowerGoal.contains("shell") || lowerGoal.contains("command") -> "sys_exec_command"
            lowerGoal.contains("profile") || lowerGoal.contains("hardware") -> "sys_system_profile"
            else -> "sys_detect_tools"
        }

        log.add("   ► [MCP Client] Calling System MCP Tool '$toolName' on GHA MCP Host...")
        val args = when (toolName) {
            "sys_exec_command" -> mapOf("command" to goal.replace("exec", "").replace("shell", "").replace("command", "").trim())
            "sys_python_env" -> mapOf("codeOrScript" to goal.replace("python", "").trim())
            else -> emptyMap()
        }

        val output = mcpClient.callTool(toolName, args)
        log.add("   ✅ [MCP Response] ${output.take(100).replace("\n", " ")}...")

        return GhaAgentResult(true, log, output)
    }
}
