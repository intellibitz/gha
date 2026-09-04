package cc.thevar.gha.ai.agent

import cc.thevar.gha.ai.mcp.GhaMcpHost
import cc.thevar.gha.ai.vision.GhaAgentResult
import java.io.File

/**
 * Agent Manager for GHA.
 * Registers specialized Agents and dispatches goals to the appropriate Agent(s).
 * GHA communicates ONLY with Agents managed by GhaAgentManager.
 */
object GhaAgentManager {

    private val scaffoldingAgent = GhaScaffoldingAgent()
    private val buildTestAgent = GhaBuildTestAgent()
    private val vcsAgent = GhaVcsAgent()
    private val securityAgent = GhaSecurityAgent()
    private val wikiAgent = GhaWikiAgent()

    /**
     * Selects and delegates mission execution to the optimal Agent(s) based on the user's natural language goal.
     */
    fun dispatchMission(goal: String, projectDir: File, mcpHost: GhaMcpHost): GhaAgentResult {
        val lowerGoal = goal.lowercase()

        return when {
            lowerGoal.contains("create") || lowerGoal.contains("scaffold") || lowerGoal.contains("app") || lowerGoal.contains("project") -> {
                // 1. Scaffold project
                val scaffoldRes = scaffoldingAgent.solveWithHost(goal, projectDir, mcpHost)
                // 2. Build & Test project
                val buildRes = buildTestAgent.solveWithHost(goal, projectDir, mcpHost)
                GhaAgentResult(
                    success = scaffoldRes.success && buildRes.success,
                    log = scaffoldRes.log + buildRes.log,
                    output = "${scaffoldRes.output}\n\n${buildRes.output}"
                )
            }
            lowerGoal.contains("fix") || lowerGoal.contains("build") || lowerGoal.contains("test") || lowerGoal.contains("repair") -> {
                buildTestAgent.solveWithHost(goal, projectDir, mcpHost)
            }
            lowerGoal.contains("security") || lowerGoal.contains("audit") || lowerGoal.contains("dependabot") -> {
                securityAgent.solveWithHost(goal, projectDir, mcpHost)
            }
            lowerGoal.contains("wiki") || lowerGoal.contains("doc") || lowerGoal.contains("documentation") -> {
                wikiAgent.solveWithHost(goal, projectDir, mcpHost)
            }
            else -> {
                vcsAgent.solveWithHost(goal, projectDir, mcpHost)
            }
        }
    }
}
