package cc.thevar.gha.ai.agent

import cc.thevar.gha.ai.vision.GhaAgentResult
import java.io.File

/**
 * Web Agent Manager for GHA.
 * Discovers, registers, and routes missions to web-based AI Agents (Web Research, Hugging Face Web, GitHub Web API, Remote MCP Web Agents).
 */
object GhaWebAgentManager {

    val webResearchAgent = GhaWebResearchAgent()
    val hfWebAgent = GhaHfWebAgent()
    val githubWebAgent = GhaGitHubWebAgent()
    val remoteMcpWebAgent = GhaRemoteMcpWebAgent()

    data class WebAgentInfo(
        val id: String,
        val name: String,
        val role: String,
        val protocol: String
    )

    /**
     * Lists all registered Web Agents available to GHA AOA.
     */
    fun listWebAgents(): List<WebAgentInfo> {
        return listOf(
            WebAgentInfo("web-research", webResearchAgent.name, webResearchAgent.role, "HTTPS / REST"),
            WebAgentInfo("hf-web", hfWebAgent.name, hfWebAgent.role, "Hugging Face Hub / REST"),
            WebAgentInfo("github-web", githubWebAgent.name, githubWebAgent.role, "GitHub REST / GraphQL"),
            WebAgentInfo("remote-mcp", remoteMcpWebAgent.name, remoteMcpWebAgent.role, "Model Context Protocol (HTTP/SSE)")
        )
    }

    /**
     * Routes a web mission goal to the optimal Web Agent.
     */
    fun routeWebMission(goal: String, projectDir: File): GhaAgentResult {
        val lowerGoal = goal.lowercase()

        return when {
            lowerGoal.contains("huggingface") || lowerGoal.contains("hf") || lowerGoal.contains("space") -> {
                hfWebAgent.solve(goal, projectDir)
            }
            lowerGoal.contains("github") || lowerGoal.contains("gh") || lowerGoal.contains("api") -> {
                githubWebAgent.solve(goal, projectDir)
            }
            lowerGoal.contains("mcp") || lowerGoal.contains("remote") || lowerGoal.contains("sse") -> {
                remoteMcpWebAgent.solve(goal, projectDir)
            }
            else -> {
                webResearchAgent.solve(goal, projectDir)
            }
        }
    }
}
