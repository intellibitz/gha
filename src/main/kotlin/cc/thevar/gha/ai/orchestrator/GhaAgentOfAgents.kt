package cc.thevar.gha.ai.orchestrator

import cc.thevar.gha.ai.agent.GhaAgent
import cc.thevar.gha.ai.vision.GhaAgentResult
import cc.thevar.gha.ai.vision.GhaAiAgent
import cc.thevar.gha.ai.vision.GhaAutonomousAgent
import java.io.File

/**
 * GHA Master AI Orchestrator: "The Agent of Agents"
 * Brings the power of AI to every home user and hardware configuration.
 * Coordinates hardware profiling, model caching, engine discovery, MCP server hubs, and autonomous agent missions.
 */
class GhaAgentOfAgents(
    override val identity: String = "GHA-Master-Orchestrator",
    override val name: String = "GHA Agent of Agents",
    override val role: String = "Universal AI Orchestrator & Multi-Agent Master"
) : GhaAiAgent, GhaAgent {

    override fun executeMission(projectDir: File, prompt: String): String {
        val res = solve(prompt, projectDir)
        return "Master Orchestrator Mission: ${if (res.success) "SUCCESS" else "FAILED"}\nSummary:\n${res.output}\n\nExecution Trace Log:\n${res.log.joinToString("\n")}"
    }

    override fun solve(goal: String, rootDir: File): GhaAgentResult {
        val log = mutableListOf<String>()
        log.add("🌌 [GHA Agent of Agents] Master AI Orchestrator initialized.")
        log.add("🎯 Mission Objective: \"$goal\"")

        // 1. Hardware Profiling for Limited/Home Hardware Optimization
        val hardware = GhaHardwareProfiler.profile(rootDir)
        log.add("💻 [Phase 1: Hardware Profiler]")
        log.add("   ├── Host OS      : ${hardware.osName}")
        log.add("   ├── CPU Cores    : ${hardware.cpuCores}")
        log.add("   ├── System RAM   : ${String.format("%.1f", hardware.totalRamGb)} GB (Available: ${String.format("%.1f", hardware.availableRamGb)} GB)")
        log.add("   ├── Acceleration : ${hardware.gpuInfo}")
        log.add("   └── Recommendation: ${hardware.maxRecommendedModelParams} (${hardware.recommendedQuantization})")

        // 2. Engine Discovery
        val engines = GhaEngineManager.detectEngines(rootDir)
        val activeEngines = engines.filter { it.isAvailable }
        log.add("⚡ [Phase 2: AI Engine Discovery] (${activeEngines.size}/${engines.size} Available)")
        engines.forEach { engine ->
            val statusSymbol = if (engine.isAvailable) "✅" else "❌"
            log.add("   ├── $statusSymbol ${engine.name} [${engine.type}]: ${engine.version}")
        }

        // 3. Cached Model Inspection
        val localModels = GhaModelManager.listLocalModels(rootDir)
        log.add("📦 [Phase 3: Model Cache Inspection] (${localModels.size} Local Models Found)")
        if (localModels.isEmpty()) {
            log.add("   └── No local models cached in '.gha/models/'. Ready to download on demand.")
        } else {
            localModels.forEach { model ->
                log.add("   ├── Model: ${model.repoId} (${String.format("%.1f", model.sizeMb / 1024.0)}GB, ${model.format}) -> ${model.compatibilityNote}")
            }
        }

        // 4. MCP Hub Discovery
        val mcpServers = GhaMcpHubManager.listServers(rootDir)
        log.add("🔌 [Phase 4: MCP Tool Hub Discovery] (${mcpServers.size} Servers Registered)")
        mcpServers.forEach { server ->
            log.add("   ├── MCP Server '${server.id}': ${server.name} [${server.type}] -> ${server.description}")
        }

        // 5. Multi-Agent Delegation & Mission Execution
        log.add("🤖 [Phase 5: Multi-Agent Dispatch & Execution]")
        val agent = GhaAutonomousAgent(identity = "GHA-SubAgent-01")
        log.add("   ► Delegating mission to GHA Autonomous Agent...")
        val subResult = agent.solve(goal, rootDir)
        subResult.log.forEach { log.add("     $it") }

        val summary = StringBuilder()
        summary.append("# 🌌 GHA AI Orchestrator Report\n\n")
        summary.append("## System Hardware Profile\n")
        summary.append("- **RAM**: ${String.format("%.1f", hardware.totalRamGb)} GB Total / ${String.format("%.1f", hardware.availableRamGb)} GB Available\n")
        summary.append("- **CPU/GPU**: ${hardware.cpuCores} Cores | ${hardware.gpuInfo}\n")
        summary.append("- **Optimal Tier**: ${hardware.maxRecommendedModelParams} (${hardware.recommendedQuantization})\n\n")

        summary.append("## Orchestration Matrix\n")
        summary.append("- **Active AI Engines**: ${activeEngines.joinToString(", ") { it.name }}\n")
        summary.append("- **Cached Models**: ${localModels.size} local models in `.gha/models/`\n")
        summary.append("- **Registered MCP Hub Servers**: ${mcpServers.size} servers\n\n")

        summary.append("## Mission Result\n")
        summary.append("${subResult.output}\n")

        return GhaAgentResult(
            success = subResult.success,
            log = log,
            output = summary.toString()
        )
    }
}
