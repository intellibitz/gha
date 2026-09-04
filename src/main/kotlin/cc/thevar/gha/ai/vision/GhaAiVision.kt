package cc.thevar.gha.ai.vision

import java.io.File

/**
 * GHA AI Vision: Bridging Project Automation with the AI Ecosystem (MCP, Agents, Models).
 * Decoupled, sandboxed, and 100% Kotlin.
 */

/**
 * Interface for AI Model Providers (e.g., Gemini, OpenAI, Claude, Local Llama).
 */
interface GhaAiModelProvider {
    val name: String
    val capabilities: List<String>
    
    fun generateText(prompt: String, systemInstruction: String? = null): String
    fun analyzeProject(rootDir: File): GhaProjectInsight
}

/**
 * Structured project insight for AI consumption.
 */
data class GhaProjectInsight(
    val techStack: String,
    val healthScore: Int,
    val suggestions: List<String>,
    val rawMetadata: Map<String, Any>
)

/**
 * Model Context Protocol (MCP) Server Interface for GHA.
 * Allows AI models to "discover" and "use" GHA as a set of tools.
 */
interface GhaMcpServer {
    fun exposeTools(): List<GhaAiTool>
    fun executeTool(toolName: String, arguments: Map<String, Any>): String
}

/**
 * Definition of a tool that can be used by an AI Agent or MCP Client.
 */
data class GhaAiTool(
    val name: String,
    val description: String,
    val inputSchema: Map<String, Any> = mapOf("type" to "object", "properties" to emptyMap<String, Any>())
)

/**
 * Autonomous AI Agent that uses GHA tools to achieve a goal.
 */
interface GhaAiAgent {
    val identity: String
    fun solve(goal: String, rootDir: File): GhaAgentResult
}

data class GhaAgentResult(
    val success: Boolean,
    val log: List<String>,
    val output: String
)
