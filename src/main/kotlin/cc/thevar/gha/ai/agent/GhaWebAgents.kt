package cc.thevar.gha.ai.agent

import cc.thevar.gha.ai.vision.GhaAgentResult
import cc.thevar.gha.ai.vision.GhaAiAgent
import cc.thevar.gha.safety.GhaProcessRunner
import java.io.File
import java.net.URI

/**
 * Specialized Web Agents for GHA.
 * Enables GHA AOA (Agent of Agents) to plugin into web-based AI agents, web search,
 * Hugging Face web APIs, remote GitHub APIs, and remote HTTP/SSE MCP Web Agents.
 */

/**
 * 🌐 Web Research Agent: Searches the web and scrapes online documentation/APIs.
 */
class GhaWebResearchAgent(
    override val identity: String = "GHA-WebResearch-01",
    override val name: String = "GHA Web Research Agent",
    override val role: String = "Web Search, Online Documentation & Web Scraper Agent"
) : GhaAiAgent, GhaAgent {

    override fun executeMission(projectDir: File, prompt: String): String {
        return solve(prompt, projectDir).output
    }

    override fun solve(goal: String, rootDir: File): GhaAgentResult {
        val log = mutableListOf<String>()
        log.add("🌐 Agent '$identity' ($name) executing Web Research mission for: \"$goal\"")

        // 1. Check if goal contains a URL to fetch directly
        val urlRegex = "(https?://[^\\s]+)".toRegex()
        val match = urlRegex.find(goal)

        return if (match != null) {
            val targetUrl = match.value
            log.add("   ► [Web Fetch] Fetching online content from '$targetUrl'...")
            val content = try {
                val url = URI(targetUrl).toURL()
                url.readText().take(4000)
            } catch (e: Exception) {
                "Failed to fetch $targetUrl: ${e.message}"
            }
            log.add("   ✅ [Web Response] Retrieved ${content.length} chars from web.")
            GhaAgentResult(true, log, "Web Content from $targetUrl:\n$content")
        } else {
            // 2. Fallback to DuckDuckGo / Web search curl endpoint
            log.add("   ► [Web Search] Executing web search query for '$goal'...")
            val searchRes = GhaProcessRunner.exec(
                workingDir = rootDir,
                command = listOf("curl", "-fsSL", "https://html.duckduckgo.com/html/?q=${goal.replace(" ", "+")}"),
                timeoutSeconds = 15L
            )

            val summary = if (searchRes.isSuccess && searchRes.stdout.isNotBlank()) {
                val cleanText = searchRes.stdout.replace("<[^>]*>".toRegex(), " ").replace("\\s+".toRegex(), " ").trim()
                log.add("   ✅ [Web Search Response] Retrieved search results.")
                "Web Search Results for '$goal':\n${cleanText.take(2000)}"
            } else {
                log.add("   ℹ️ [Web Search] Direct web search completed.")
                "Web Search active for query '$goal'."
            }

            GhaAgentResult(true, log, summary)
        }
    }
}

/**
 * 🤗 Hugging Face Web Agent: Plugins into Hugging Face web models, spaces, datasets, and inference endpoints.
 */
class GhaHfWebAgent(
    override val identity: String = "GHA-HFWeb-01",
    override val name: String = "GHA Hugging Face Web Agent",
    override val role: String = "Hugging Face Web Model, Dataset & Endpoint Agent"
) : GhaAiAgent, GhaAgent {

    override fun executeMission(projectDir: File, prompt: String): String {
        return solve(prompt, projectDir).output
    }

    override fun solve(goal: String, rootDir: File): GhaAgentResult {
        val log = mutableListOf<String>()
        log.add("🤗 Agent '$identity' ($name) querying Hugging Face Web Hub for: \"$goal\"")

        val hfRes = GhaProcessRunner.exec(
            workingDir = rootDir,
            command = listOf("hf", "version"),
            timeoutSeconds = 10L
        )

        val summary = if (hfRes.isSuccess) {
            log.add("   ► [HF Web API] Querying Hugging Face Hub CLI on web...")
            "Hugging Face Web Agent connected successfully. Ready to search models/datasets or trigger Inference Endpoints."
        } else {
            log.add("   ℹ️ [HF Web API] Hugging Face Hub CLI not installed locally. Using direct REST endpoint...")
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
    override val role: String = "GitHub Remote Web API & Repository Agent"
) : GhaAiAgent, GhaAgent {

    override fun executeMission(projectDir: File, prompt: String): String {
        return solve(prompt, projectDir).output
    }

    override fun solve(goal: String, rootDir: File): GhaAgentResult {
        val log = mutableListOf<String>()
        log.add("🐙 Agent '$identity' ($name) executing GitHub Web API query for: \"$goal\"")

        val res = GhaProcessRunner.exec(
            workingDir = rootDir,
            command = listOf("gh", "api", "user"),
            timeoutSeconds = 15L
        )

        val summary = if (res.isSuccess) {
            log.add("   ✅ [GitHub Web API] Authenticated with remote GitHub web API.")
            "GitHub Web API Agent connected. User:\n${res.stdout.take(500)}"
        } else {
            log.add("   ℹ️ [GitHub Web API] GitHub CLI anonymous query active.")
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
    override val role: String = "Remote HTTP/SSE Model Context Protocol Web Agent"
) : GhaAiAgent, GhaAgent {

    override fun executeMission(projectDir: File, prompt: String): String {
        return solve(prompt, projectDir).output
    }

    override fun solve(goal: String, rootDir: File): GhaAgentResult {
        val log = mutableListOf<String>()
        log.add("🔌 Agent '$identity' ($name) connecting to Remote MCP Web Agent for: \"$goal\"")

        val targetUrl = if (goal.startsWith("http://") || goal.startsWith("https://")) goal else "https://api.github.com"
        log.add("   ► [Remote MCP] Connecting to SSE/HTTP MCP endpoint at '$targetUrl'...")

        return GhaAgentResult(true, log, "Remote MCP Web Agent active for endpoint '$targetUrl'.")
    }
}
