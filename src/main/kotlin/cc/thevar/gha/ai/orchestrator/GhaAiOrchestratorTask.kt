package cc.thevar.gha.ai.orchestrator

import cc.thevar.gha.GhaTask
import org.gradle.api.provider.Property
import org.gradle.api.tasks.Input
import org.gradle.api.tasks.Optional
import org.gradle.api.tasks.TaskAction
import org.gradle.work.DisableCachingByDefault

/**
 * GHA AI Orchestrator Task: Entry point for the Master Agent Manager & MCP Host.
 */
@DisableCachingByDefault(because = "Executes real-time AI orchestration actions")
abstract class GhaAiOrchestratorTask : GhaTask() {

    @get:Input
    @get:Optional
    abstract val action: Property<String> // status, models, engines, mcp-hub, download, agent

    @get:Input
    @get:Optional
    abstract val model: Property<String>

    @get:Input
    @get:Optional
    abstract val filter: Property<String>

    @get:Input
    @get:Optional
    abstract val goal: Property<String>

    init {
        val prov = project.providers
        action.convention(prov.gradleProperty("action").orElse("status"))
        model.convention(prov.gradleProperty("model"))
        filter.convention(prov.gradleProperty("filter"))
        goal.convention(prov.gradleProperty("goal").orElse("health check and orchestrate"))
    }

    @TaskAction
    fun execute() {
        verifySandbox()
        val rootDir = taskRootDirFile
        val activeAction = action.getOrElse("status").lowercase()

        logger.lifecycle("🌌 [GHA AI Orchestrator] Action: '$activeAction'")

        when (activeAction) {
            "status", "orchestrate", "agent" -> {
                val orchestrator = GhaAgentOfAgents()
                val result = orchestrator.solve(goal.getOrElse("status report"), rootDir)
                result.log.forEach { logger.lifecycle(it) }
                println("")
                println(result.output)
            }
            "models" -> {
                val models = GhaModelManager.listLocalModels(rootDir)
                logger.lifecycle("📦 Cached AI Models in '.gha/models/' (${models.size}):")
                if (models.isEmpty()) {
                    logger.lifecycle("   └── No models currently cached. Download models via './ghai ai orchestrate -Paction=download -Pmodel=<repoId>'")
                } else {
                    models.forEach { m ->
                        logger.lifecycle("   ├── ${m.repoId} (${String.format("%.1f", m.sizeMb)}MB, ${m.format}) -> ${m.compatibilityNote}")
                    }
                }
            }
            "engines" -> {
                val engines = GhaEngineManager.detectEngines(rootDir)
                logger.lifecycle("⚡ AI Inference Engines Detected (${engines.size}):")
                engines.forEach { e ->
                    val status = if (e.isAvailable) "ACTIVE" else "NOT INSTALLED"
                    logger.lifecycle("   ├── [${e.type}] ${e.name}: $status (${e.version}) - ${e.description}")
                }
            }
            "mcp-hub", "mcp" -> {
                val servers = GhaMcpHubManager.listServers(rootDir)
                logger.lifecycle("🔌 Registered MCP Tool Hub Servers (${servers.size}):")
                servers.forEach { s ->
                    logger.lifecycle("   ├── [${s.type}] ${s.name} (${s.id}): ${s.description}")
                }
            }
            "download" -> {
                val modelRepo = model.orNull
                if (modelRepo.isNullOrBlank()) {
                    logger.error("❌ Action 'download' requires '-Pmodel=<repoId or URL>' argument.")
                    return
                }
                logger.lifecycle("📥 Downloading AI model '$modelRepo'...")
                val res = GhaModelManager.downloadModel(rootDir, modelRepo, filter.orNull)
                logger.lifecycle(res)
            }
            else -> {
                logger.lifecycle("ℹ️ GHA AI Orchestrator: Unknown action '$activeAction'. Supported: status, models, engines, mcp-hub, download, agent.")
            }
        }
    }
}
