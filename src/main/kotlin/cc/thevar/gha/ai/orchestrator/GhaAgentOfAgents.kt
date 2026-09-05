package cc.thevar.gha.ai.orchestrator

import cc.thevar.gha.ai.GhaAiManager
import cc.thevar.gha.ai.agent.GhaAgent
import cc.thevar.gha.ai.agent.GhaAgentManager
import cc.thevar.gha.ai.agent.GhaWebAgentManager
import cc.thevar.gha.ai.mcp.GhaGmcpClient
import cc.thevar.gha.ai.vision.GhaAgentResult
import cc.thevar.gha.ai.vision.GhaAiAgent
import cc.thevar.gha.ai.vision.GhaAutonomousAgent
import cc.thevar.gha.safety.GhaSandboxManager
import java.io.File

/**
 * GHA Master Agent / Orchestrator (GMA / AOA): "The Agent of Agents"
 * Tier 1: The Master. The Orchestrator. The One.
 * GMA sits in the front as the singular Sole Interactor for the GHA User.
 * 
 * Compliance:
 * Fully AOA Standard Protocol compliant via GhaAoaComplianceEngine.
 * 
 * Coordinates the 4-tier GHA architecture:
 * 1. Tier 1: GMA Master Agent (Sole Interactor, Orchestrator & One-Point Manager)
 * 2. Tier 2: GAWD (GHA Agents Web & Domain) - Specialized workers.
 * 3. Tier 3: GEMI (GHA Engines & Models AI Inference) - Pure Intelligence Layer.
 * 4. Tier 4: GMCP (GHA Model Context Protocol) - Infrastructure, Hardware & Tools Layer.
 */
/**
 * GMA: Tier 1 - The Master.
 * Stands tall as the singular one-point manager and worker.
 * GMA coordinates and governs the custom intelligence of every tier (GAWD, GEMI, GMCP).
 */
class GhaAgentOfAgents(
    override val identity: String = "GMA-Master-Orchestrator",
    override val name: String = "GHA Master Agent (GMA)",
    override val role: String = "Sole Interactor for GHA User & Singular Master Orchestrator"
) : GhaAiAgent, GhaAgent {

    private val aoaCompliance = GhaAoaComplianceEngine(File(".")) 

    /**
     * Master Coordination Intelligence (Tier 1): Governs the entire 4-tier ecosystem.
     */
    fun completeUserWorkWithExecution(goal: String, rootDir: File): GhaAgentResult {
        System.err.println("🌌 [GMA Master Intelligence] Tier 1 governing the 4-tier cascade...")
        return solve(goal, rootDir)
    }

    data class CoordinationReport(
        val targetDir: File,
        val hardwareProfile: GhaHardwareProfiler.HardwareProfile,
        val managers: List<String>,
        val localAgents: List<String>,
        val webAgents: List<GhaWebAgentManager.WebAgentInfo>,
        val engines: List<GhaEngineManager.EngineInfo>,
        val localModelsCount: Int,
        val webModelsCount: Int,
        val gmcpStatus: String,
        val mcpServers: List<GhaMcpHubManager.McpServerConfig>,
        val mcpToolsCount: Int,
        val projectContext: String
    )

    override fun executeMission(projectDir: File, prompt: String): String {
        val res = completeUserWorkWithExecution(prompt, projectDir)
        return """
            |🌌 [GMA Master Interactor] Mission Status: ${if (res.success) "SUCCESS" else "FAILED"}
            |
            |Summary of Best Possible Execution:
            |${res.output}
            |
            |Execution Trace Log:
            |${res.log.joinToString("\n")}
            |
            |✨ GHA Master Agent (GMA) is ready and awaiting your next command.
        """.trimMargin()
    }

    /**
     * Inspects and gathers the singular master coordination report across all components.
     */
    fun getCoordinationReport(targetDir: File): CoordinationReport {
        GhaSandboxManager.ensureSandbox(targetDir, targetDir.name)
        val hardware = GhaHardwareProfiler.profile(targetDir)
        val mcpServers = GhaMcpHubManager.listServers(targetDir)
        val engines = GhaEngineManager.detectEngines(targetDir)
        val localModels = GhaModelManager.listLocalModels(targetDir)
        val webModels = GhaModelManager.listWebModels(targetDir)
        val projectContext = GhaAiManager.detectProjectContext(targetDir)
        val gmcpClient = GhaGmcpClient(targetDir)
        val mcpTools = gmcpClient.listTools()

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
            gmcpStatus = "GMCP Host/Client/Server active with ${mcpTools.size} tools",
            mcpServers = mcpServers,
            mcpToolsCount = mcpTools.size,
            projectContext = projectContext
        )
    }

    /**
     * Master Orchestration Entry Point: Coordinates every tier from T1 to T4.
     */
    override fun solve(goal: String, rootDir: File): GhaAgentResult {
        // 0. AOA Protocol Interceptor (Handles external standard protocol requests)
        if (goal.startsWith("{") && goal.contains("jsonrpc")) {
            val response = aoaCompliance.handleAoaRequest(goal, this)
            if (response != null) {
                return GhaAgentResult(true, listOf("AOA Protocol Request Handled"), response)
            }
        }

        // Tier 1 Custom Intelligence: Decide if we should delegate to another AOA
        val aoaIntelligenceResult = manageOtherAoas(goal, rootDir)
        if (aoaIntelligenceResult != null) return aoaIntelligenceResult

        val requestedAoaEnv = System.getenv("GHA_AOA") ?: System.getenv("GHA_AOA_FRAMEWORK")
        if (!requestedAoaEnv.isNullOrBlank()) {
            val requestedFramework = GhaAoaManager.parseFramework(requestedAoaEnv)
            if (requestedFramework != GhaAoaManager.Framework.BUILT_IN) {
                val topLog = mutableListOf<String>()
                topLog.add("🌌 [GMA Master Interactor] Delegating mission to Web AOA Framework: ${requestedFramework.displayName}")

                val delegateResult = GhaAoaManager.executeMission(requestedFramework, goal, rootDir)
                return GhaAgentResult(
                    success = delegateResult.success,
                    log = topLog + delegateResult.log,
                    output = delegateResult.output
                )
            }
        }

        val log = mutableListOf<String>()
        log.add("🌌 [GMA Master Interactor] GHA Master Agent (GMA) initialized as Sole Interactor.")
        log.add("🎯 Target Workspace : ${rootDir.absolutePath}")
        log.add("🎯 Mission Objective : \"$goal\"")

        // 0. Auto-Installer & Bootstrapper Phase
        val bootstrapLogs = GhaBootstrapManager.autoBootstrapEnvironment(rootDir)
        bootstrapLogs.forEach { log.add(it) }

        // 1. Hardware Profiling (Hardware Awareness)
        val hardware = GhaHardwareProfiler.profile(rootDir)
        log.add("💻 [Phase 1: Hardware Constraints Profiled] ${hardware.cpuCores} Cores, ${String.format("%.1f", hardware.availableRamGb)}GB RAM avail.")

        // 2. GEMI Intelligence Layer Coordination (Tier 3)
        val gemi = GhaGemiEngine(rootDir)
        log.add("🧠 [Phase 2: GEMI Intelligence Coordinated] ${gemi.getIntelligenceReport()}")
        
        val reasoningRes = gemi.reason(goal)
        reasoningRes.log.forEach { log.add("   $it") }
        log.add("   └── GEMI Analysis: ${reasoningRes.output.take(100)}...")

        // 3. GMCP Infrastructure Coordination (Tier 4)
        val gmcpClient = GhaGmcpClient(rootDir)
        val mcpServers = GhaMcpHubManager.listServers(rootDir)
        val exposedTools = gmcpClient.listTools()
        log.add("🔌 [Phase 3: GMCP Infrastructure Coordinated] (${mcpServers.size} Servers, ${exposedTools.size} Tools active)")
        log.add("   └── GMCP Server/Client/Host active. Infrastructure decoupled from Intelligence (except for Agent-engines).")

        // 4. Agent Manager Dispatch (Tier 2 Workers)
        log.add("🤖 [Phase 4: Agent Manager & Worker Dispatch]")
        log.add("   ► GMA delegating goal to Workers using GEMI reasoning and GMCP tools...")

        val lowerGoal = goal.lowercase()
        val agentResult = when {
            lowerGoal.contains("web") || lowerGoal.contains("search") || lowerGoal.contains("huggingface") || lowerGoal.contains("fetch") -> {
                GhaWebAgentManager.routeWebMission(goal, rootDir, gemi, gmcpClient)
            }
            lowerGoal.contains("autonomous") || lowerGoal.contains("auto") -> {
                GhaAutonomousAgent().solveWithT3T4(goal, rootDir, gemi, gmcpClient)
            }
            else -> {
                GhaAgentManager.dispatchMission(goal, rootDir, gemi, gmcpClient)
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
        summary.append("- **GEMI Intelligence**: ${report.engines.count { it.isAvailable }} Active Engines, ${report.webModelsCount} web models\n")
        summary.append("- **GMCP Interactor**: ${gmcpClient.listTools().size} tools served across ${mcpServers.size} MCP Servers\n\n")

        summary.append("## Mission Execution Output\n")
        summary.append("${agentResult.output}\n")

        return GhaAgentResult(
            success = agentResult.success,
            log = log,
            output = summary.toString()
        )
    }

    /**
     * GMA Custom Built-in Intelligence: Manages and decides when to use other AOAs.
     */
    private fun manageOtherAoas(goal: String, rootDir: File): GhaAgentResult? {
        val lowerGoal = goal.lowercase()
        val aoaClient = GhaAoaClient(rootDir)

        // Rule-based custom intelligence for inter-AOA interaction
        return when {
            lowerGoal.contains("use aoa") || lowerGoal.contains("delegate to") -> {
                val targetAoa = goal.split(" ").find { it.contains("-aoa") }?.removeSuffix("-aoa") ?: "Public-AOA-01"
                val cleanedGoal = goal.replace("use aoa", "").replace(targetAoa, "").trim()
                
                val log = mutableListOf<String>()
                log.add("🧠 [GMA Intelligence] Decision: Delegating to external AOA '$targetAoa'")
                
                val res = aoaClient.delegateToOtherAoa(targetAoa, cleanedGoal)
                GhaAgentResult(res.success, log + res.log, res.output)
            }
            lowerGoal.contains("download aoa") || lowerGoal.contains("install aoa") -> {
                val url = goal.split(" ").find { it.startsWith("http") } ?: "https://registry.gha.ai/plugins/standard-aoa.zip"
                val output = aoaClient.downloadAoaPlugin(url)
                GhaAgentResult(true, listOf("GMA Intelligence: Plugin installation triggered"), output)
            }
            else -> null // GMA handles it locally
        }
    }
}
