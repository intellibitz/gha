package cc.thevar.gha.ai.agent

import cc.thevar.gha.ai.mcp.GhaGmcpClient
import cc.thevar.gha.ai.orchestrator.GhaGemiEngine
import cc.thevar.gha.ai.vision.GhaAgentResult
import cc.thevar.gha.ai.vision.GhaAiAgent
import java.io.File

/**
 * GAWD: GHA Agents Web & Domain (Tier 2).
 * Workers that make use of GEMI (Tier 3) for reasoning and GMCP (Tier 4) for skills.
 */

/**
 * 🛠️ Gradle Agent: Handles Gradle build, testing, and project scaffolding.
 */
class GhaGradleAgent(
    override val identity: String = "GHA-Gradle-01",
    override val name: String = "GHA Gradle Agent",
    override val role: String = "Gradle Build, Test & Scaffolding Worker"
) : GhaAiAgent, GhaAgent {

    override fun executeMission(projectDir: File, prompt: String): String {
        val client = GhaGmcpClient(projectDir)
        val gemi = GhaGemiEngine(projectDir)
        return solveWithT3T4(prompt, projectDir, gemi, client).output
    }

    override fun solve(goal: String, rootDir: File): GhaAgentResult {
        return solveWithT3T4(goal, rootDir, GhaGemiEngine(rootDir), GhaGmcpClient(rootDir))
    }

    fun solveWithT3T4(goal: String, rootDir: File, gemi: GhaGemiEngine, mcpClient: GhaGmcpClient): GhaAgentResult {
        val log = mutableListOf<String>()
        log.add("🛠️ Agent '$identity' ($name) active (Tier 2 Worker)")

        // 1. Thinking Phase (Tier 3)
        val reasoning = gemi.reason(goal)
        log.addAll(reasoning.log)
        log.add("   💭 Thinking (GEMI): ${reasoning.output}")

        // 2. Doing Phase (Tier 4)
        val lowerGoal = goal.lowercase()
        val toolName = when {
            lowerGoal.contains("android") -> "scaffold_android"
            lowerGoal.contains("scaffold") || lowerGoal.contains("kotlin") -> "scaffold_kotlin"
            lowerGoal.contains("clean") -> "clean"
            lowerGoal.contains("test") || lowerGoal.contains("check") -> "test"
            lowerGoal.contains("uninstall") -> "uninstall"
            else -> "build"
        }

        log.add("   ► Executing (GMCP): Calling Gradle Tool '$toolName'...")
        val args = when (toolName) {
            "scaffold_android", "scaffold_kotlin" -> mapOf("projectName" to rootDir.name, "targetDir" to rootDir.absolutePath)
            else -> emptyMap()
        }

        val output = mcpClient.callTool(toolName, args)
        log.add("   ✅ Completed: ${output.take(100).replace("\n", " ")}...")

        return GhaAgentResult(true, log, output)
    }
}

/**
 * 🔄 Git Agent: Handles Git version control and repository management.
 */
class GhaGitAgent(
    override val identity: String = "GHA-Git-01",
    override val name: String = "GHA Git Agent",
    override val role: String = "Git Version Control & Repository Worker"
) : GhaAiAgent, GhaAgent {

    override fun executeMission(projectDir: File, prompt: String): String {
        val client = GhaGmcpClient(projectDir)
        val gemi = GhaGemiEngine(projectDir)
        return solveWithT3T4(prompt, projectDir, gemi, client).output
    }

    override fun solve(goal: String, rootDir: File): GhaAgentResult {
        return solveWithT3T4(goal, rootDir, GhaGemiEngine(rootDir), GhaGmcpClient(rootDir))
    }

    fun solveWithT3T4(goal: String, rootDir: File, gemi: GhaGemiEngine, mcpClient: GhaGmcpClient): GhaAgentResult {
        val log = mutableListOf<String>()
        log.add("🔄 Agent '$identity' ($name) active (Tier 2 Worker)")

        // 1. Thinking Phase (Tier 3)
        val reasoning = gemi.reason(goal)
        log.addAll(reasoning.log)
        log.add("   💭 Thinking (GEMI): ${reasoning.output}")

        // 2. Doing Phase (Tier 4)
        val lowerGoal = goal.lowercase()
        val toolName = when {
            lowerGoal.contains("clone") -> "clone"
            lowerGoal.contains("context") -> "context"
            else -> "status"
        }

        log.add("   ► Executing (GMCP): Calling Git Tool '$toolName'...")
        val args = when (toolName) {
            "clone" -> {
                val repo = goal.split(" ").find { it.contains("/") } ?: ""
                mapOf("repo" to repo)
            }
            else -> emptyMap()
        }

        val output = mcpClient.callTool(toolName, args)
        log.add("   ✅ Completed: ${output.take(100).replace("\n", " ")}...")

        return GhaAgentResult(true, log, output)
    }
}

/**
 * 🐙 GitHub Agent: Handles GitHub platform tasks (PRs, Issues, Workflows).
 */
class GhaGitHubAgent(
    override val identity: String = "GHA-GitHub-01",
    override val name: String = "GHA GitHub Agent",
    override val role: String = "GitHub Platform, PR & Issue Worker"
) : GhaAiAgent, GhaAgent {

    override fun executeMission(projectDir: File, prompt: String): String {
        val client = GhaGmcpClient(projectDir)
        val gemi = GhaGemiEngine(projectDir)
        return solveWithT3T4(prompt, projectDir, gemi, client).output
    }

    override fun solve(goal: String, rootDir: File): GhaAgentResult {
        return solveWithT3T4(goal, rootDir, GhaGemiEngine(rootDir), GhaGmcpClient(rootDir))
    }

    fun solveWithT3T4(goal: String, rootDir: File, gemi: GhaGemiEngine, mcpClient: GhaGmcpClient): GhaAgentResult {
        val log = mutableListOf<String>()
        log.add("🐙 Agent '$identity' ($name) active (Tier 2 Worker)")

        // 1. Thinking Phase (Tier 3)
        val reasoning = gemi.reason(goal)
        log.addAll(reasoning.log)
        log.add("   💭 Thinking (GEMI): ${reasoning.output}")

        // 2. Doing Phase (Tier 4)
        val lowerGoal = goal.lowercase()
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

        log.add("   ► Executing (GMCP): Calling GitHub Tool '$toolName'...")
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
        log.add("   ✅ Completed: ${output.take(100).replace("\n", " ")}...")

        return GhaAgentResult(true, log, output)
    }
}

/**
 * 💻 System Agent: Handles interaction with user system tools (ADB, Docker, Python).
 */
class GhaSystemAgent(
    override val identity: String = "GHA-System-01",
    override val name: String = "GHA System Agent",
    override val role: String = "User System Tools & Environment Worker"
) : GhaAiAgent, GhaAgent {

    override fun executeMission(projectDir: File, prompt: String): String {
        val client = GhaGmcpClient(projectDir)
        val gemi = GhaGemiEngine(projectDir)
        return solveWithT3T4(prompt, projectDir, gemi, client).output
    }

    override fun solve(goal: String, rootDir: File): GhaAgentResult {
        return solveWithT3T4(goal, rootDir, GhaGemiEngine(rootDir), GhaGmcpClient(rootDir))
    }

    fun solveWithT3T4(goal: String, rootDir: File, gemi: GhaGemiEngine, mcpClient: GhaGmcpClient): GhaAgentResult {
        val log = mutableListOf<String>()
        log.add("💻 Agent '$identity' ($name) active (Tier 2 Worker)")

        // 1. Thinking Phase (Tier 3)
        val reasoning = gemi.reason(goal)
        log.addAll(reasoning.log)
        log.add("   💭 Thinking (GEMI): ${reasoning.output}")

        // 2. Doing Phase (Tier 4)
        val lowerGoal = goal.lowercase()
        val toolName = when {
            lowerGoal.contains("adb") || lowerGoal.contains("android device") -> "sys_adb_device"
            lowerGoal.contains("docker") || lowerGoal.contains("container") -> "sys_docker_container"
            lowerGoal.contains("python") || lowerGoal.contains("uv") -> "sys_python_env"
            lowerGoal.contains("exec") || lowerGoal.contains("shell") || lowerGoal.contains("command") -> "sys_exec_command"
            lowerGoal.contains("profile") || lowerGoal.contains("hardware") -> "sys_system_profile"
            else -> "sys_detect_tools"
        }

        log.add("   ► Executing (GMCP): Calling System Tool '$toolName'...")
        val args = when (toolName) {
            "sys_exec_command" -> mapOf("command" to goal.replace("exec", "").replace("shell", "").replace("command", "").trim())
            "sys_python_env" -> mapOf("codeOrScript" to goal.replace("python", "").trim())
            else -> emptyMap()
        }

        val output = mcpClient.callTool(toolName, args)
        log.add("   ✅ Completed: ${output.take(100).replace("\n", " ")}...")

        return GhaAgentResult(true, log, output)
    }
}
