package cc.thevar.gha.ai.orchestrator

import cc.thevar.gha.ai.GhaAiManager
import cc.thevar.gha.ai.agent.GhaAgent
import cc.thevar.gha.ai.agent.GhaAgentManager
import cc.thevar.gha.ai.agent.GhaWebAgentManager
import cc.thevar.gha.ai.mcp.GhaMcpHost
import cc.thevar.gha.ai.vision.GhaAgentResult
import cc.thevar.gha.ai.vision.GhaAiAgent
import cc.thevar.gha.ai.vision.GhaAutonomousAgent
import cc.thevar.gha.safety.GhaSandboxManager
import java.io.File

/**
 * GHA Master Agent / Orchestrator (GMA / AOA): "The Agent of Agents"
 * Serves as the SOLE INTERACTOR for the GHA User and singular master orchestrator & manager for GHA.
 *
 * Coordinates every:
 * 1. Manager (AiManager, AgentManager, WebAgentManager, EngineManager, ModelManager, McpHubManager, BootstrapManager,
 *             InsightsManager, ProjectManager, SandboxManager, SecurityManager, DependabotManager, WikiManager, WorkflowManager)
 * 2. Agent (Local Specialized Agents, Web Agents, Autonomous Agent)
 * 3. Engine (Local & Web AI Inference Engines)
 * 4. Model (Local Hardware-Optimized Models & Web Models)
 * 5. MCP Host (Central Tool Host for workspace)
 * 6. MCP Client (Agents & AOA acting as clients)
 * 7. MCP Server (Built-in Universal, System Tools, GitHub, HF, Memory, Brave Search, Puppeteer, Filesystem, Fetch, Remote SSE)
 */
class GhaAgentOfAgents(
    override val identity: String = "GMA-Master-Orchestrator",
    override val name: String = "GHA Master Agent (GMA)",
    override val role: String = "Sole Interactor for GHA User & Singular Master Orchestrator"
) : GhaAiAgent, GhaAgent {

    data class CoordinationReport(
        val targetDir: File,
        val hardwareProfile: GhaHardwareProfiler.HardwareProfile,
        val managers: List<String>,
        val localAgents: List<String>,
        val webAgents: List<GhaWebAgentManager.WebAgentInfo>,
        val engines: List<GhaEngineManager.EngineInfo>,
        val localModelsCount: Int,
        val webModelsCount: Int,
        val mcpHostStatus: String,
        val mcpServers: List<GhaMcpHubManager.McpServerConfig>,
        val mcpToolsCount: Int,
        val projectContext: String
    )

    override fun executeMission(projectDir: File, prompt: String): String {
        val res = solve(prompt, projectDir)
        return "Master Orchestrator Mission: ${if (res.success) "SUCCESS" else "FAILED"}\nSummary:\n${res.output}\n\nExecution Trace Log:\n${res.log.joinToString("\n")}"
    }

    /**
     * Inspects and gathers the singular master coordination report across all components.
     */
    fun getCoordinationReport(targetDir: File): CoordinationReport {
        GhaSandboxManager.ensureSandbox(targetDir, targetDir.name)
        val hardware = GhaHardwareProfiler.profile(targetDir)
        val mcpHost = GhaMcpHost(targetDir)
        val mcpServers = GhaMcpHubManager.listServers(targetDir)
        val mcpTools = mcpHost.listTools()
        val engines = GhaEngineManager.detectEngines(targetDir)
        val localModels = GhaModelManager.listLocalModels(targetDir)
        val webModels = GhaModelManager.listWebModels(targetDir)
        val projectContext = GhaAiManager.detectProjectContext(targetDir)

        val managersList = listOf(
            "GhaAiManager (Context & Smart Commit Engine)",
            "GhaAgentManager (Local Agents Dispatcher)",
            "GhaWebAgentManager (Web Agents Router)",
            "GhaEngineManager (Local & Web Inference Engines)",
            "GhaModelManager (Hardware-Optimized Model Resolver)",
            "GhaMcpHubManager (MCP Tool Servers Registry)",
            "GhaBootstrapManager (Environment Auto-Installer)",
            "GhaSandboxManager (Security & Sandbox Verification)",
            "GhaWorkflowManager & GhaParallelWorkflowManager (Actions & PR Sweeper)",
            "GhaSecurityManager & GhaDependabotManager (Security Audits & Dependabot)",
            "GhaWikiManager (Documentation & Wiki Sync)",
            "GhaProjectManager (GitHub Project Boards)",
            "GhaInsightsManager (Repository Analytics)"
        )

        val localAgentsList = listOf(
            "Gradle Agent (GhaGradleAgent)",
            "Git Agent (GhaGitAgent)",
            "GitHub Agent (GhaGitHubAgent)",
            "System Agent (GhaSystemAgent)",
            "Autonomous Agent (GhaAutonomousAgent)"
        )

        return CoordinationReport(
            targetDir = targetDir,
            hardwareProfile = hardware,
            managers = managersList,
            localAgents = localAgentsList,
            webAgents = GhaWebAgentManager.listWebAgents(),
            engines = engines,
            localModelsCount = localModels.size,
            webModelsCount = webModels.size,
            mcpHostStatus = mcpHost.getStatusReport(),
            mcpServers = mcpServers,
            mcpToolsCount = mcpTools.size,
            projectContext = projectContext
        )
    }

    /**
     * Master Orchestration Entry Point: Coordinates every manager, agent, engine, model, mcp host, mcp client, and mcp server.
     */
    override fun solve(goal: String, rootDir: File): GhaAgentResult {
        val requestedAoaEnv = System.getenv("GHA_AOA") ?: System.getenv("GHA_AOA_FRAMEWORK")
        if (!requestedAoaEnv.isNullOrBlank()) {
            val requestedFramework = GhaAoaManager.parseFramework(requestedAoaEnv)
            if (requestedFramework != GhaAoaManager.Framework.BUILT_IN) {
                val topLog = mutableListOf<String>()
                topLog.add("🌌 [GHA Master Orchestrator] Agent of Agents initialized for target: ${rootDir.absolutePath}")
                topLog.add("🎯 Mission Objective: \"$goal\"")
                topLog.add("🌐 [Top-Level Delegation] Master Orchestrator delegating mission from the TOP to Web AOA Framework: ${requestedFramework.displayName}")

                val delegateResult = GhaAoaManager.executeMission(requestedFramework, goal, rootDir)
                return GhaAgentResult(
                    success = delegateResult.success,
                    log = topLog + delegateResult.log,
                    output = delegateResult.output
                )
            }
        }

        val log = mutableListOf<String>()
        log.add("🌌 [GMA Master Interactor] GHA Master Agent (GMA) initialized as Sole Interactor for GHA User.")
        log.add("🎯 Target Workspace : ${rootDir.absolutePath}")
        log.add("🎯 Mission Objective : \"$goal\"")

        // 0. Auto-Installer & Bootstrapper Phase
        val bootstrapLogs = GhaBootstrapManager.autoBootstrapEnvironment(rootDir)
        bootstrapLogs.forEach { log.add(it) }

        // 1. Initialize Central GHA MCP Host
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

        // 3. Engine Discovery & Manager Coordination
        val engines = GhaEngineManager.detectEngines(rootDir)
        val activeEngines = engines.filter { it.isAvailable }
        log.add("⚡ [Phase 3: AI Inference Engines Coordinated] (${activeEngines.size}/${engines.size} Active)")
        engines.forEach { engine ->
            val statusSymbol = if (engine.isAvailable) "✅" else "❌"
            log.add("   ├── $statusSymbol ${engine.name} [${engine.type}]: ${engine.version}")
        }

        // 3.5 Web & Local AI Models Resolution
        val localModels = GhaModelManager.listLocalModels(rootDir)
        val webModels = GhaModelManager.listWebModels(rootDir)
        log.add("🧠 [Phase 3.5: AI Models Resolved] (${localModels.size} Local Cached, ${webModels.size} Web Models available)")

        // 4. MCP Servers Coordination
        val mcpServers = GhaMcpHubManager.listServers(rootDir)
        val exposedTools = mcpHost.listTools()
        log.add("🔌 [Phase 4: MCP Tool Hub & Servers Coordinated] (${mcpServers.size} Servers, ${exposedTools.size} Tools available)")
        mcpServers.forEach { server ->
            val statusSymbol = if (server.isEnabled) "✅" else "❌"
            log.add("   ├── $statusSymbol [${server.type}] ${server.name} (${server.id}): ${server.description}")
        }

        // 5. Agent Manager Dispatch (Full Top-to-Bottom Delegation)
        log.add("🤖 [Phase 5: Agent Manager & MCP Client Dispatch]")
        log.add("   ► Delegating goal down through Agents, Engines, Models, and MCP Servers...")

        val lowerGoal = goal.lowercase()
        val agentResult = when {
            lowerGoal.contains("web") || lowerGoal.contains("search") || lowerGoal.contains("huggingface") || lowerGoal.contains("fetch") -> {
                GhaWebAgentManager.routeWebMission(goal, rootDir)
            }
            lowerGoal.contains("autonomous") || lowerGoal.contains("auto") -> {
                GhaAutonomousAgent().solve(goal, rootDir)
            }
            else -> {
                GhaAgentManager.dispatchMission(goal, rootDir, mcpHost)
            }
        }

        agentResult.log.forEach { log.add("     $it") }

        // Synthesize Master Report
        val report = getCoordinationReport(rootDir)
        val summary = StringBuilder()
        summary.append("# 🌌 GHA Master Agent (GMA) Sole Interactor Report\n\n")
        summary.append("## Target Workspace & Context\n")
        summary.append("- **Directory**: `${report.targetDir.absolutePath}`\n")
        summary.append("- **Project Stack**: ${report.projectContext}\n\n")

        summary.append("## System Hardware Profile\n")
        summary.append("- **RAM**: ${String.format("%.1f", hardware.totalRamGb)} GB Total / ${String.format("%.1f", hardware.availableRamGb)} GB Available\n")
        summary.append("- **CPU/GPU**: ${hardware.cpuCores} Cores | ${hardware.gpuInfo}\n")
        summary.append("- **Optimal Tier**: ${hardware.maxRecommendedModelParams} (${hardware.recommendedQuantization})\n\n")

        summary.append("## Coordinated System Components\n")
        summary.append("- **Active Managers**: ${report.managers.size} System Managers\n")
        summary.append("- **Registered Agents**: ${report.localAgents.size} Local Agents, ${report.webAgents.size} Web Agents\n")
        summary.append("- **Inference Engines**: ${activeEngines.size}/${engines.size} Active (${activeEngines.joinToString(", ") { it.name }})\n")
        summary.append("- **AI Models**: ${report.localModelsCount} Local Cached, ${report.webModelsCount} Web Models\n")
        summary.append("- **MCP Tool Host**: ${mcpHost.listTools().size} tools served across ${mcpServers.size} MCP Servers\n\n")

        summary.append("## Mission Execution Output\n")
        summary.append("${agentResult.output}\n")

        return GhaAgentResult(
            success = agentResult.success,
            log = log,
            output = summary.toString()
        )
    }
}
