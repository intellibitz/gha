package cc.thevar.gha.ai.mcp

import cc.thevar.gha.GhaTask
import org.gradle.api.tasks.TaskAction
import org.gradle.work.DisableCachingByDefault

/**
 * Exposes all gha tasks as Model Context Protocol (MCP) tools.
 * This allows AI Agents to discover and invoke gha automation tasks via a standardized protocol.
 */
@DisableCachingByDefault(because = "Provides real-time tool discovery for AI Agents")
abstract class GhaMcpTask : GhaTask() {

    @TaskAction
    fun execute() {
        verifySandbox()
        
        println("{\"mcpVersion\":\"1.0.0\",\"tools\":[")
        val tools = project.tasks
            .filter { it.name.startsWith("gha") && it.name != "ghaMcp" }
            .map { task ->
                """
                {
                  "name": "${task.name}",
                  "description": "${task.description ?: "No description"}",
                  "parameters": {
                    "type": "object",
                    "properties": {}
                  }
                }
                """.trimIndent()
            }
        println(tools.joinToString(",\n"))
        println("]}")
        
        logger.lifecycle("🤖 [gha] MCP Tool Discovery successful. ${tools.size} tools exposed.")
    }
}
