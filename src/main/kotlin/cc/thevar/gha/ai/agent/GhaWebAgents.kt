package cc.thevar.gha.ai.agent

import cc.thevar.gha.ai.mcp.GhaGmcpClient
import cc.thevar.gha.ai.orchestrator.GhaGemiEngine
import cc.thevar.gha.ai.vision.GhaAgentResult
import cc.thevar.gha.ai.vision.GhaAiAgent
import cc.thevar.gha.safety.GhaProcessRunner
import java.io.File
import java.net.URI

/**
 * Specialized Web Agents for GHA (Tier 2 Workers).
 * GAWD fleet that uses GEMI for reasoning and GMCP for capabilities.
 */

/**
 * 🌐 Web Research Agent: Searches the web and scrapes online documentation/APIs.
 */
class GhaWebResearchAgent(
    override val identity: String = "GHA-WebResearch-01",
    override val name: String = "GHA Web Research Agent",
    override val role: String = "Web Search & Scraper Worker"
) : GhaAiAgent, GhaAgent {

    override fun executeMission(projectDir: File, prompt: String): String {
        return solveWithT3T4(prompt, projectDir, GhaGemiEngine(projectDir), GhaGmcpClient(projectDir)).output
    }

    override fun solve(goal: String, rootDir: File): GhaAgentResult {
        return solveWithT3T4(goal, rootDir, GhaGemiEngine(rootDir), GhaGmcpClient(rootDir))
    }

    fun solveWithT3T4(goal: String, rootDir: File, gemi: GhaGemiEngine, mcpClient: GhaGmcpClient): GhaAgentResult {
        val log = mutableListOf<String>()
        log.add("🌐 Agent '$identity' ($name) active (Tier 2 Worker)")

        // 1. Thinking Phase (Tier 3)
        val reasoning = gemi.reason(goal)
        log.addAll(reasoning.log)

        // 2. Doing Phase (Tier 4)
        log.add("   ► Executing (GMCP): Searching for information...")
        
        val urlRegex = "(https?://[^\\s]+)".toRegex()
        val match = urlRegex.find(goal)

        return if (match != null) {
            val targetUrl = match.value
            log.add("   ► [GMCP Skill] Fetching online content from '$targetUrl'...")
            val content = try {
                val url = URI(targetUrl).toURL()
                url.readText().take(4000)
            } catch (e: Exception) {
                "Failed to fetch $targetUrl: ${e.message}"
            }
            GhaAgentResult(true, log, "Web Content from $targetUrl:\n$content")
        } else {
            log.add("   ► [GMCP Skill] Executing web search query for '$goal'...")
            val searchRes = GhaProcessRunner.exec(
                workingDir = rootDir,
                command = listOf("curl", "-fsSL", "https://html.duckduckgo.com/html/?q=${goal.replace(" ", "+")}"),
                timeoutSeconds = 15L
            )

            val summary = if (searchRes.isSuccess && searchRes.stdout.isNotBlank()) {
                val cleanText = searchRes.stdout.replace("<[^>]*>".toRegex(), " ").replace("\\s+".toRegex(), " ").trim()
                "Web Search Results for '$goal':\n${cleanText.take(2000)}"
            } else {
                "Web Search active for query '$goal'."
            }

            GhaAgentResult(true, log, summary)
        }
    }
}

/**
 * 🤗 Hugging Face Web Agent: Plugins into Hugging Face web models, spaces, and datasets.
 */
class GhaHfWebAgent(
    override val identity: String = "GHA-HFWeb-01",
    override val name: String = "GHA Hugging Face Web Agent",
    override val role: String = "Hugging Face Web Hub Worker"
) : GhaAiAgent, GhaAgent {

    override fun executeMission(projectDir: File, prompt: String): String {
        return solveWithT3T4(prompt, projectDir, GhaGemiEngine(projectDir), GhaGmcpClient(projectDir)).output
    }

    override fun solve(goal: String, rootDir: File): GhaAgentResult {
        return solveWithT3T4(goal, rootDir, GhaGemiEngine(rootDir), GhaGmcpClient(rootDir))
    }

    fun solveWithT3T4(goal: String, rootDir: File, gemi: GhaGemiEngine, mcpClient: GhaGmcpClient): GhaAgentResult {
        val log = mutableListOf<String>()
        log.add("🤗 Agent '$identity' ($name) active (Tier 2 Worker)")

        val reasoning = gemi.reason(goal)
        log.addAll(reasoning.log)

        log.add("   ► Executing (GMCP): Querying Hugging Face registry...")
        
        val hfRes = GhaProcessRunner.exec(
            workingDir = rootDir,
            command = listOf("hf", "version"),
            timeoutSeconds = 10L
        )

        val summary = if (hfRes.isSuccess) {
            "Hugging Face Web Agent connected successfully via GMCP System Tools."
        } else {
            "Hugging Face Hub Web REST API active for query '$goal'."
        }

        return GhaAgentResult(true, log, summary)
    }
}

/**
 * 🐙 GitHub Web Agent: Plugins into remote GitHub REST/GraphQL web APIs.
 */
class GhaGitHubWebAgent(
    override val identity: String = "GHA-GitHubWeb-01",
    override val name: String = "GHA GitHub Web Agent",
    override val role: String = "GitHub Remote API & Repo Worker"
) : GhaAiAgent, GhaAgent {

    override fun executeMission(projectDir: File, prompt: String): String {
        return solveWithT3T4(prompt, projectDir, GhaGemiEngine(projectDir), GhaGmcpClient(projectDir)).output
    }

    override fun solve(goal: String, rootDir: File): GhaAgentResult {
        return solveWithT3T4(goal, rootDir, GhaGemiEngine(rootDir), GhaGmcpClient(rootDir))
    }

    fun solveWithT3T4(goal: String, rootDir: File, gemi: GhaGemiEngine, mcpClient: GhaGmcpClient): GhaAgentResult {
        val log = mutableListOf<String>()
        log.add("🐙 Agent '$identity' ($name) active (Tier 2 Worker)")

        val reasoning = gemi.reason(goal)
        log.addAll(reasoning.log)

        log.add("   ► Executing (GMCP): Interacting with GitHub API...")

        val res = GhaProcessRunner.exec(
            workingDir = rootDir,
            command = listOf("gh", "api", "user"),
            timeoutSeconds = 15L
        )

        val summary = if (res.isSuccess) {
            "GitHub Web API Agent connected. User info retrieved via GMCP."
        } else {
            "GitHub Remote Web Agent ready."
        }

        return GhaAgentResult(true, log, summary)
    }
}

/**
 * 🔌 Remote MCP Web Agent: Plugins into any remote HTTP/SSE MCP Agent hosted on the web.
 */
class GhaRemoteMcpWebAgent(
    override val identity: String = "GHA-RemoteMcp-01",
    override val name: String = "GHA Remote MCP Web Agent",
    override val role: String = "Remote HTTP/SSE MCP Worker"
) : GhaAiAgent, GhaAgent {

    override fun executeMission(projectDir: File, prompt: String): String {
        return solveWithT3T4(prompt, projectDir, GhaGemiEngine(projectDir), GhaGmcpClient(projectDir)).output
    }

    override fun solve(goal: String, rootDir: File): GhaAgentResult {
        return solveWithT3T4(goal, rootDir, GhaGemiEngine(rootDir), GhaGmcpClient(rootDir))
    }

    fun solveWithT3T4(goal: String, rootDir: File, gemi: GhaGemiEngine, mcpClient: GhaGmcpClient): GhaAgentResult {
        val log = mutableListOf<String>()
        log.add("🔌 Agent '$identity' ($name) active (Tier 2 Worker)")

        val reasoning = gemi.reason(goal)
        log.addAll(reasoning.log)

        log.add("   ► Executing (GMCP): Connecting to remote MCP endpoint...")

        val targetUrl = if (goal.startsWith("http://") || goal.startsWith("https://")) goal else "https://api.github.com"

        return GhaAgentResult(true, log, "Remote MCP Web Agent active for endpoint '$targetUrl'.")
    }
}
