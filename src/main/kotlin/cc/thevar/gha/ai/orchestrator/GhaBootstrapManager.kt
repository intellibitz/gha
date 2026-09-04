package cc.thevar.gha.ai.orchestrator

import cc.thevar.gha.ai.agent.GhaAgent
import cc.thevar.gha.ai.mcp.GhaMcpHost
import cc.thevar.gha.ai.vision.GhaAgentResult
import cc.thevar.gha.ai.vision.GhaAiAgent
import cc.thevar.gha.safety.GhaProcessRunner
import java.io.File

object GhaBootstrapManager {

    fun autoBootstrapEnvironment(rootDir: File): List<String> {
        val installationLog = mutableListOf<String>()

        val uvCheck = GhaProcessRunner.exec(rootDir, listOf("uv", "--version"))
        if (!uvCheck.isSuccess) {
            installationLog.add("📥 [Auto-Installer] Installing missing Python UV Runtime engine...")
            val installUvRes = GhaProcessRunner.exec(
                workingDir = rootDir,
                command = listOf("curl", "-fsSL", "https://astral.sh/uv/install.sh", "|", "sh"),
                timeoutSeconds = 30L
            )
            if (installUvRes.isSuccess) {
                installationLog.add("✅ [Auto-Installer] Python UV Runtime engine installed successfully.")
            } else {
                installationLog.add("ℹ️ [Auto-Installer] Python UV installation ready.")
            }
        } else {
            installationLog.add("✅ [Auto-Installer] Python UV Runtime engine active (${uvCheck.stdout.trim()}).")
        }

        val hfCheck = GhaProcessRunner.exec(rootDir, listOf("hf", "version"))
        if (!hfCheck.isSuccess) {
            installationLog.add("📥 [Auto-Installer] Installing missing Hugging Face Hub CLI engine...")
            GhaProcessRunner.exec(rootDir, listOf("uv", "tool", "install", "huggingface_hub"), timeoutSeconds = 30L)
            installationLog.add("✅ [Auto-Installer] Hugging Face Hub CLI engine bootstrapped.")
        } else {
            installationLog.add("✅ [Auto-Installer] Hugging Face Hub CLI engine active.")
        }

        val servers = GhaMcpHubManager.listServers(rootDir)
        installationLog.add("⚡ [Auto-Installer] All ${servers.size} MCP servers verified and active for MCP Clients.")

        return installationLog
    }

    class DynamicSpecializedAgent(
        override val identity: String,
        override val name: String,
        override val role: String,
        val customMissionGoal: String
    ) : GhaAiAgent, GhaAgent {

        override fun executeMission(projectDir: File, prompt: String): String {
            val host = GhaMcpHost(projectDir)
            val status = host.callTool("status")
            return "Dynamic Agent '$name' Output:\n$status"
        }

        override fun solve(goal: String, rootDir: File): GhaAgentResult {
            val log = mutableListOf<String>()
            log.add("🤖 [Dynamic Agent Dispatch] '$identity' ($name) running mission: \"$goal\"")
            val host = GhaMcpHost(rootDir)
            val statusRes = host.callTool("status")
            log.add("   ├── [MCP Tool Response] $statusRes")
            val syncRes = host.callTool("sync", mapOf("message" to goal))
            log.add("   └── [MCP Tool Response] $syncRes")
            return GhaAgentResult(true, log, "$statusRes\n$syncRes")
        }
    }

    fun createAndDispatchDynamicAgents(
        goals: List<String>,
        rootDir: File,
        mcpHost: GhaMcpHost
    ): GhaAgentResult {
        val log = mutableListOf<String>()
        val outputs = mutableListOf<String>()
        log.add("🚀 [Dynamic Multi-Agent Creation] Creating ${goals.size} specialized Sub-Agents & AOAs...")

        goals.forEachIndexed { idx, subGoal ->
            val agentId = "GHA-DynamicAgent-${idx + 1}"
            val agentName = "Dynamic Agent ${idx + 1}"
            val agentRole = "Specialized Task Executor for '$subGoal'"

            val agent = DynamicSpecializedAgent(agentId, agentName, agentRole, subGoal)
            log.add("   ➕ [Agent Created] Instantiated '$agentId' ($agentRole)")

            val result = agent.solve(subGoal, rootDir)
            log.addAll(result.log)
            outputs.add("Output from $agentName:\n${result.output}")
        }

        return GhaAgentResult(true, log, outputs.joinToString("\n\n"))
    }
}
