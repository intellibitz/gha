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

    init {
        goal.convention(project.providers.gradleProperty("goal").orElse("health check"))
        mode.convention(project.providers.gradleProperty("mode").orElse("agent"))
    }

    @TaskAction
    fun execute() {
        verifySandbox()
        val rootDir = taskRootDirFile
        val activeMode = mode.getOrElse("agent")
        val activeGoal = goal.getOrElse("status report")

        logger.lifecycle("🌌 [GHA AI Vision] Mode: $activeMode, Goal: $activeGoal")

        when (activeMode) {
            "mcp" -> {
                val server = GhaUniversalMcpServer(rootDir)
                logger.lifecycle("🔌 MCP Server active. Exposed tools:")
                server.exposeTools().forEach { tool ->
                    logger.lifecycle("   ├── ${tool.name}: ${tool.description}")
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
}
