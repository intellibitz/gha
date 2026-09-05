package cc.thevar.gha.ai.agent

import cc.thevar.gha.ai.mcp.GhaMcpHost
import cc.thevar.gha.ai.vision.GhaAgentResult
import java.io.File

/**
 * Agent Manager for GHA.
 * Registers specialized Agents and dispatches goals to the appropriate Agent(s).
 * GHA Master Agent (GMA) communicates ONLY with Agents managed by GhaAgentManager.
 */
object GhaAgentManager {

    private val gradleAgent = GhaGradleAgent()
    private val gitAgent = GhaGitAgent()
    private val githubAgent = GhaGitHubAgent()
    private val systemAgent = GhaSystemAgent()

    /**
     * Selects and delegates mission execution to the optimal Agent(s) based on the user's natural language goal.
     */
    fun dispatchMission(goal: String, projectDir: File, mcpHost: GhaMcpHost): GhaAgentResult {
        val lowerGoal = goal.lowercase()

        return when {
            lowerGoal.contains("create") || lowerGoal.contains("scaffold") || lowerGoal.contains("app") || lowerGoal.contains("project") -> {
                // 1. Scaffold & Build via Gradle Agent
                gradleAgent.solveWithHost(goal, projectDir, mcpHost)
            }
            lowerGoal.contains("build") || lowerGoal.contains("test") || lowerGoal.contains("clean") || lowerGoal.contains("gradle") -> {
                gradleAgent.solveWithHost(goal, projectDir, mcpHost)
            }
            lowerGoal.contains("pr") || lowerGoal.contains("issue") || lowerGoal.contains("workflow") ||
                    lowerGoal.contains("github") || lowerGoal.contains("sync") || lowerGoal.contains("wiki") ||
                    lowerGoal.contains("security") || lowerGoal.contains("audit") || lowerGoal.contains("dependabot") -> {
                githubAgent.solveWithHost(goal, projectDir, mcpHost)
            }
            lowerGoal.contains("git") || lowerGoal.contains("clone") || lowerGoal.contains("status") || lowerGoal.contains("context") -> {
                gitAgent.solveWithHost(goal, projectDir, mcpHost)
            }
            lowerGoal.contains("web") || lowerGoal.contains("search") || lowerGoal.contains("fetch") || lowerGoal.contains("huggingface") || lowerGoal.contains("url") -> {
                GhaWebAgentManager.routeWebMission(goal, projectDir)
            }
            lowerGoal.contains("system") || lowerGoal.contains("adb") || lowerGoal.contains("docker") ||
                    lowerGoal.contains("python") || lowerGoal.contains("shell") || lowerGoal.contains("exec") ||
                    lowerGoal.contains("profile") || lowerGoal.contains("hardware") -> {
                systemAgent.solveWithHost(goal, projectDir, mcpHost)
            }
            else -> {
                // Default to Git Agent for repository-wide goals
                gitAgent.solveWithHost(goal, projectDir, mcpHost)
            }
        }
    }
}
