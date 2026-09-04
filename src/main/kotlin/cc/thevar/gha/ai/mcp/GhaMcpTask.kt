package cc.thevar.gha.ai.mcp

import cc.thevar.gha.GhaTask
import cc.thevar.gha.ai.vision.GhaUniversalMcpServer
import org.gradle.api.tasks.TaskAction
import org.gradle.work.DisableCachingByDefault

/**
 * Exposes all gha tasks and universal automation capabilities as Model Context Protocol (MCP) tools.
 * Enables AI Agents and LLMs to discover and invoke gha automation tools via a standardized protocol.
 */
@DisableCachingByDefault(because = "Provides real-time tool discovery for AI Agents")
abstract class GhaMcpTask : GhaTask() {

    @TaskAction
    fun execute() {
        verifySandbox()
        val rootDir = taskRootDirFile
        val server = GhaUniversalMcpServer(rootDir)
        val tools = server.exposeTools()

        println("{\"mcpVersion\":\"1.0.0\",\"protocolVersion\":\"2024-11-05\",\"tools\":[")
        val renderedTools = tools.map { tool ->
            val propertiesJson = if (tool.inputSchema.containsKey("properties")) {
                val props = tool.inputSchema["properties"] as? Map<*, *> ?: emptyMap<Any, Any>()
                props.entries.joinToString(",\n      ") { (k, v) ->
                    val spec = v as? Map<*, *> ?: mapOf("type" to "string", "description" to v.toString())
                    val type = spec["type"] ?: "string"
                    val desc = spec["description"] ?: ""
                    """"$k": { "type": "$type", "description": "$desc" }"""
                }
            } else {
                ""
            }
            val requiredList = (tool.inputSchema["required"] as? List<*>)?.joinToString(",") { """"$it"""" } ?: ""

            """
            {
              "name": "${tool.name}",
              "description": "${tool.description}",
              "inputSchema": {
                "type": "object",
                "properties": {
                  $propertiesJson
                }${if (requiredList.isNotBlank()) ",\n                \"required\": [$requiredList]" else ""}
              }
            }
            """.trimIndent()
        }
        println(renderedTools.joinToString(",\n"))
        println("]}")

        logger.lifecycle("🤖 [gha] MCP Tool Discovery successful. ${tools.size} tools exposed via GhaUniversalMcpServer.")
    }
}
