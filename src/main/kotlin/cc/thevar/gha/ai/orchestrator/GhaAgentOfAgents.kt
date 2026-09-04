package cc.thevar.gha.ai.orchestrator

import cc.thevar.gha.ai.agent.GhaAgent
import cc.thevar.gha.ai.agent.GhaAgentManager
import cc.thevar.gha.ai.mcp.GhaMcpHost
import cc.thevar.gha.ai.vision.GhaAgentResult
import cc.thevar.gha.ai.vision.GhaAiAgent
import java.io.File

/**
 * GHA Master AI Orchestrator: "The Agent of Agents"
 * Serves as the MCP Host, Agent Manager, and single-point orchestrator for the GHA User.
 *
 * Architecture Principles:
 * 1. GHA Master Orchestrator communicates ONLY with Agents.
 * 2. Agents act as MCP Clients and call tools provided by the GHA MCP Host.
 * 3. All tools are served by MCP Servers hosted on the GHA MCP Host.
 * 4. Agents communicate with inference models/engines to execute goals autonomously.
 */
class GhaAgentOfAgents(
    override val identity: String = "GHA-Master-Orchestrator",
    override val name: String = "GHA Agent of Agents",
    override val role: String = "Universal Agent Manager & MCP Host Orchestrator"
) : GhaAiAgent, GhaAgent {

    override fun executeMission(projectDir: File, prompt: String): String {
        val res = solve(prompt, projectDir)
        return "Master Orchestrator Mission: ${if (res.success) "SUCCESS" else "FAILED"}\nSummary:\n${res.output}\n\nExecution Trace Log:\n${res.log.joinToString("\n")}"
    }

    override fun solve(goal: String, rootDir: File): GhaAgentResult {
        val requestedAoaEnv = System.getenv("GHA_AOA") ?: System.getenv("GHA_AOA_FRAMEWORK")
        if (!requestedAoaEnv.isNullOrBlank()) {
            val requestedFramework = GhaAoaManager.parseFramework(requestedAoaEnv)
            if (requestedFramework != GhaAoaManager.Framework.BUILT_IN) {
                val topLog = mutableListOf<String>()
                topLog.add("🌌 [GHA Master Orchestrator] Agent of Agents initialized for GHA User.")
                topLog.add("🎯 Mission Objective: \"$goal\"")
                topLog.add("🌐 [Top-Level Delegation] Master Orchestrator delegating mission from the TOP to reliable Web AOA Framework: ${requestedFramework.displayName}")
                
                val delegateResult = GhaAoaManager.executeMission(requestedFramework, goal, rootDir)
                return GhaAgentResult(
                    success = delegateResult.success,
                    log = topLog + delegateResult.log,
                    output = delegateResult.output
                )
            }
        }

        val log = mutableListOf<String>()
        log.add("🌌 [GHA Master Orchestrator] Agent of Agents initialized for GHA User.")
        log.add("🎯 Mission Objective: \"$goal\"")

        // 0. Auto-Installer & Bootstrapper Phase
        val bootstrapLogs = GhaBootstrapManager.autoBootstrapEnvironment(rootDir)
        bootstrapLogs.forEach { log.add(it) }

        // 1. Initialize GHA MCP Host
        val mcpHost = GhaMcpHost(rootDir)
        log.add("🔌 [Phase 1: GHA MCP Host Initialized] ${mcpHost.getStatusReport()}")

        // 2. Hardware Profiling for Limited/Home Hardware Optimization
        val hardware = GhaHardwareProfiler.profile(rootDir)
        log.add("💻 [Phase 2: Hardware Constraints Profiled]")
        log.add("   ├── Host OS      : ${hardware.osName}")
        log.add("   ├── CPU Cores    : ${hardware.cpuCores}")
        log.add("   ├── System RAM   : ${String.format("%.1f", hardware.totalRamGb)} GB (Available: ${String.format("%.1f", hardware.availableRamGb)} GB)")
        log.add("   ├── Acceleration : ${hardware.gpuInfo}")
        log.add("   └── Recommendation: ${hardware.maxRecommendedModelParams} (${hardware.recommendedQuantization})")

        // 3. Engine Discovery
        val engines = GhaEngineManager.detectEngines(rootDir)
        val activeEngines = engines.filter { it.isAvailable }
        log.add("⚡ [Phase 3: AI Inference Engines] (${activeEngines.size}/${engines.size} Available)")
        engines.forEach { engine ->
            val statusSymbol = if (engine.isAvailable) "✅" else "❌"
            log.add("   ├── $statusSymbol ${engine.name} [${engine.type}]: ${engine.version}")
        }

        // 4. Agent Manager Dispatch (GHA communicates ONLY with Agents)
        log.add("🤖 [Phase 4: Agent Manager Dispatch]")
        log.add("   ► Dispatching goal to specialized Agent(s) (acting as MCP Clients)...")
        val agentResult = GhaAgentManager.dispatchMission(goal, rootDir, mcpHost)
        agentResult.log.forEach { log.add("     $it") }

        val summary = StringBuilder()
        summary.append("# 🌌 GHA Master Agent Manager & MCP Host Report\n\n")
        summary.append("## System Hardware Profile\n")
        summary.append("- **RAM**: ${String.format("%.1f", hardware.totalRamGb)} GB Total / ${String.format("%.1f", hardware.availableRamGb)} GB Available\n")
        summary.append("- **CPU/GPU**: ${hardware.cpuCores} Cores | ${hardware.gpuInfo}\n")
        summary.append("- **Optimal Tier**: ${hardware.maxRecommendedModelParams} (${hardware.recommendedQuantization})\n\n")

        summary.append("## MCP Host & Agent Execution\n")
        summary.append("- **MCP Tool Host**: ${mcpHost.listTools().size} tools served to Agents\n")
        summary.append("- **Active AI Engines**: ${activeEngines.joinToString(", ") { it.name }}\n\n")

        summary.append("## Agent Execution Results\n")
        summary.append("${agentResult.output}\n")

        return GhaAgentResult(
            success = agentResult.success,
            log = log,
            output = summary.toString()
        )
    }
}
