package cc.thevar.gha.ai.orchestrator

import cc.thevar.gha.ai.agent.GhaAgent
import cc.thevar.gha.ai.agent.GhaAgentManager
import cc.thevar.gha.ai.agent.GhaGawdAgent
import cc.thevar.gha.ai.agent.GhaWebAgentManager
import cc.thevar.gha.ai.mcp.GhaGmcpClient
import cc.thevar.gha.ai.vision.GhaAgentResult
import cc.thevar.gha.ai.vision.GhaAiAgent
import groovy.json.JsonOutput
import groovy.json.JsonSlurper
import java.io.File

/**
 * GMA Supervisor (GMAS) - Tier 1 Agent of Agents (AOA) Supervisor.
 * Sits directly below GMA at Tier 1.
 * 
 * Responsibilities:
 * 1. Follows Industry Standard AOA Protocol (aoa/init, aoa/agents/list, aoa/mission/start, aoa/handoff, aoa/supervise).
 * 2. Supervises internal custom agents (GAWD fleet) and external/downloaded web agents.
 * 3. Reports structured supervisory reports directly back to GMA (GHA Master Agent).
 */
class GhaGmasAgent(
    override val identity: String = "GMAS-Supervisor-01",
    override val name: String = "GMA Supervisor (GMAS)",
    override val role: String = "Tier 1 AOA Supervisor governing internal & downloaded agents for GMA"
) : GhaAiAgent, GhaAgent {

    private val aoaClient = GhaAoaClient(File("."))
    private val slurper = JsonSlurper()

    data class SupervisorReport(
        val taskId: String,
        val goal: String,
        val supervisedAgents: List<String>,
        val executionSuccess: Boolean,
        val summary: String,
        val rawLog: List<String>
    )

    /**
     * Supervises and coordinates execution across custom GAWD agents and downloaded web AOAs.
     * Reports results back to GMA.
     */
    fun superviseMission(goal: String, rootDir: File): SupervisorReport {
        val log = mutableListOf<String>()
        log.add("🏛️ [GMAS Supervisor] Tier 1 AOA Supervisor active under GMA.")
        log.add("   ► Goal: \"$goal\"")

        // 1. AOA Protocol Handshake & Discovery
        log.add("   ► [AOA Protocol] Discovering active internal & downloaded web agents...")
        val customAgents = listOf(
            "GAWD Master Agent (${GhaAgentManager.gawdAgent.name})",
            "Gradle Agent", "Git Agent", "GitHub Agent", "System Agent"
        )
        val webAgents = GhaWebAgentManager.listWebAgents().map { "${it.name} (${it.role})" }
        val allSupervised = customAgents + webAgents
        log.add("   ► Supervised Agent Fleet: ${allSupervised.size} agents connected")

        // 2. Check for External/Downloaded AOA Plugins or Frameworks
        val requestedAoaEnv = System.getenv("GHA_AOA") ?: System.getenv("GHA_AOA_FRAMEWORK")
        val framework = GhaAoaManager.parseFramework(requestedAoaEnv)

        val result = if (framework != GhaAoaManager.Framework.BUILT_IN) {
            log.add("   ► [GMAS Supervisor] Supervising downloaded web AOA Framework: ${framework.displayName}")
            GhaAoaManager.executeMission(framework, goal, rootDir)
        } else {
            // Standard AOA Supervision over GAWD & GEMI
            log.add("   ► [GMAS Supervisor] Directing GAWD agent fleet following Standard AOA Protocol...")
            
            // Send A2A Request to GAWD Agent
            val a2aReq = GhaGawdAgent.A2AMessage(
                sender = identity,
                recipient = GhaAgentManager.gawdAgent.identity,
                performative = GhaGawdAgent.A2APerformative.DELEGATE,
                content = goal
            )
            val a2aResp = GhaAgentManager.dispatchA2AMessage(a2aReq, rootDir)
            
            log.add("   ✅ [GMAS Supervisor] Received A2A ${a2aResp.performative} from GAWD Agent")
            GhaAgentResult(
                success = a2aResp.performative == GhaGawdAgent.A2APerformative.RESPONSE,
                log = log,
                output = a2aResp.content
            )
        }

        log.addAll(result.log)

        return SupervisorReport(
            taskId = "gmas-${System.currentTimeMillis()}",
            goal = goal,
            supervisedAgents = allSupervised,
            executionSuccess = result.success,
            summary = result.output,
            rawLog = log
        )
    }

    /**
     * Standard AOA Request Protocol Handler for GMAS.
     */
    fun handleAoaRequest(jsonRequest: String, rootDir: File): String {
        return try {
            val req = slurper.parseText(jsonRequest) as? Map<String, Any> ?: return createErrorJson(null, -32600, "Invalid Request")
            val id = req["id"]
            val method = req["method"] as? String ?: return createErrorJson(id, -32601, "Method required")
            val params = req["params"] as? Map<String, Any> ?: emptyMap()

            val result = when (method) {
                "aoa/init" -> mapOf(
                    "protocol" to "AOA/1.0",
                    "identity" to identity,
                    "supervisor" to name,
                    "role" to role
                )
                "aoa/supervise" -> {
                    val goal = params["goal"]?.toString() ?: ""
                    val report = superviseMission(goal, rootDir)
                    mapOf(
                        "taskId" to report.taskId,
                        "success" to report.executionSuccess,
                        "summary" to report.summary,
                        "agents" to report.supervisedAgents
                    )
                }
                "aoa/plugin/download" -> {
                    val url = params["url"]?.toString() ?: ""
                    val msg = aoaClient.downloadAoaPlugin(url)
                    mapOf("status" to msg)
                }
                else -> return createErrorJson(id, -32601, "AOA Method '$method' not supported by GMAS")
            }

            JsonOutput.toJson(mapOf("jsonrpc" to "2.0", "id" to id, "result" to result))
        } catch (e: Exception) {
            createErrorJson(null, -32603, "GMAS Internal Error: ${e.message}")
        }
    }

    override fun executeMission(projectDir: File, prompt: String): String {
        val report = superviseMission(prompt, projectDir)
        return report.summary
    }

    override fun solve(goal: String, rootDir: File): GhaAgentResult {
        val report = superviseMission(goal, rootDir)
        return GhaAgentResult(report.executionSuccess, report.rawLog, report.summary)
    }

    private fun createErrorJson(id: Any?, code: Int, message: String): String {
        return JsonOutput.toJson(mapOf(
            "jsonrpc" to "2.0",
            "id" to id,
            "error" to mapOf("code" to code, "message" to message)
        ))
    }
}
