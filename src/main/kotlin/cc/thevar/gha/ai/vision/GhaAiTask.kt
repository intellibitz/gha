package cc.thevar.gha.ai.vision

import cc.thevar.gha.GhaTask
import org.gradle.api.provider.Property
import org.gradle.api.tasks.Input
import org.gradle.api.tasks.Optional
import org.gradle.api.tasks.TaskAction
import org.gradle.work.DisableCachingByDefault

/**
 * GHA AI Task: Entry point for the AI Vision (MCP, Agents, Models).
 */
@DisableCachingByDefault(because = "Executes autonomous AI agent actions")
abstract class GhaAiTask : GhaTask() {

    @get:Input
    @get:Optional
    abstract val goal: Property<String>

    @get:Input
    @get:Optional
    abstract val mode: Property<String> // "agent", "mcp", "insight"

    @get:Input
    @get:Optional
    abstract val toolName: Property<String>

    @get:Input
    @get:Optional
    abstract val toolArgs: Property<String>

    init {
        val prov = project.providers
        goal.convention(prov.gradleProperty("goal").orElse("health check"))
        mode.convention(prov.gradleProperty("mode").orElse("agent"))
        toolName.convention(prov.gradleProperty("toolName"))
        toolArgs.convention(prov.gradleProperty("toolArgs"))
    }

    @TaskAction
    fun execute() {
        verifySandbox()
        val rootDir = taskRootDirFile
        val activeMode = mode.getOrElse("agent")
        val activeGoal = goal.getOrElse("status report")

        logger.lifecycle("🌌 [GHA AI Vision] Mode: $activeMode, Goal: $activeGoal")

        val server = GhaUniversalMcpServer(rootDir)

        when (activeMode.lowercase()) {
            "mcp" -> {
                val tName = toolName.orNull
                if (!tName.isNullOrBlank()) {
                    val argsMap = parseToolArgs(toolArgs.orNull)
                    logger.lifecycle("🛠️ [MCP Execute] Tool: '$tName', Args: $argsMap")
                    val result = server.executeTool(tName, argsMap)
                    println("✨ MCP Result: $result")
                } else {
                    logger.lifecycle("🔌 MCP Server active. Exposed tools (${server.exposeTools().size}):")
                    server.exposeTools().forEach { tool ->
                        logger.lifecycle("   ├── ${tool.name}: ${tool.description}")
                    }
                }
            }
            "agent" -> {
                val agent = GhaAutonomousAgent()
                val result = agent.solve(activeGoal, rootDir)
                result.log.forEach { logger.lifecycle("   $it") }
                logger.lifecycle("✨ Agent Output: ${result.output}")
            }
            else -> {
                logger.lifecycle("ℹ️ Insight: GHA is 100% Kotlin, Sandboxed, and AI-Ready.")
            }
        }
    }

    private fun parseToolArgs(rawArgs: String?): Map<String, Any> {
        if (rawArgs.isNullOrBlank()) return emptyMap()
        return rawArgs.split(",")
            .mapNotNull { pair ->
                val parts = pair.split("=", limit = 2)
                if (parts.size == 2) parts[0].trim() to parts[1].trim() else null
            }.toMap()
    }
}
