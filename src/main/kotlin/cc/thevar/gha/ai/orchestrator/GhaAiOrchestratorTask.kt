package cc.thevar.gha.ai.orchestrator

import cc.thevar.gha.GhaTask
import org.gradle.api.provider.Property
import org.gradle.api.tasks.Input
import org.gradle.api.tasks.Optional
import org.gradle.api.tasks.TaskAction
import org.gradle.work.DisableCachingByDefault
import java.io.File

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

    @get:Input
    @get:Optional
    abstract val aoaFramework: Property<String>

    @get:Input
    @get:Optional
    abstract val targetDirProperty: Property<String>

    init {
        val prov = project.providers
        val cmdTargetDir = (project.findProperty("targetDir") ?: project.findProperty("dir"))?.toString()
        action.convention(prov.gradleProperty("action").orElse("status"))
        model.convention(prov.gradleProperty("model"))
        filter.convention(prov.gradleProperty("filter"))
        goal.convention(prov.gradleProperty("goal").orElse("health check and orchestrate"))
        aoaFramework.convention(prov.gradleProperty("aoa").orElse(prov.gradleProperty("framework")).orElse("builtin"))
        targetDirProperty.convention(
            prov.gradleProperty("targetDir")
                .orElse(prov.gradleProperty("dir"))
                .orElse(prov.provider { cmdTargetDir })
        )
    }

    @TaskAction
    fun execute() {
        verifySandbox()
        val targetDirStr = targetDirProperty.orNull
        val rootDir = if (!targetDirStr.isNullOrBlank()) File(targetDirStr) else taskRootDirFile
        val activeAction = action.getOrElse("status").lowercase()

        logger.lifecycle("🌌 [GMA Master Interactor] Action: '$activeAction', Target Directory: ${rootDir.absolutePath}")

        when (activeAction) {
            "status", "orchestrate", "agent" -> {
                val framework = GhaAoaManager.parseFramework(aoaFramework.orNull ?: System.getenv("GHA_AOA"))
                val result = GhaAoaManager.executeMission(framework, goal.getOrElse("status report"), rootDir)
                result.log.forEach { logger.lifecycle(it) }
                println("")
                println(result.output)
            }
            "models" -> {
                val models = GhaModelManager.listLocalModels(rootDir)
                logger.lifecycle("📦 Cached Local AI Models in '.gha/models/' (${models.size}):")
                if (models.isEmpty()) {
                    logger.lifecycle("   └── No local models cached. Download models via './ghai ai orchestrate -Paction=download -Pmodel=<repoId>'")
                } else {
                    models.forEach { m ->
                        logger.lifecycle("   ├── ${m.repoId} (${String.format("%.1f", m.sizeMb)}MB, ${m.format}) -> ${m.compatibilityNote}")
                    }
                }

                val webModels = GhaModelManager.listWebModels(rootDir)
                logger.lifecycle("\n🌐 Available AI Models on the Web for Engines (${webModels.size}):")
                webModels.forEach { wm ->
                    logger.lifecycle("   ├── [${wm.registry}] ${wm.name} ('${wm.modelId}') - ${wm.description} (${wm.contextWindow})")
                }
            }
            "engines" -> {
                val engines = GhaEngineManager.detectEngines(rootDir)
                logger.lifecycle("⚡ AI Inference Engines Coordinated (${engines.size}):")
                engines.forEach { e ->
                    val status = if (e.isAvailable) "ACTIVE" else "NOT INSTALLED"
                    logger.lifecycle("   ├── [${e.type}] ${e.name}: $status (${e.version}) - ${e.description}")
                }
            }
            "mcp-hub", "mcp" -> {
                val servers = GhaMcpHubManager.listServers(rootDir)
                logger.lifecycle("🔌 Coordinated MCP Tool Hub Servers (${servers.size}):")
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
                logger.lifecycle("📥 GMA downloading AI model '$modelRepo'...")
                val res = GhaModelManager.downloadModel(rootDir, modelRepo, filter.orNull)
                logger.lifecycle(res)
            }
            else -> {
                logger.lifecycle("ℹ️ GMA Master Interactor: Unknown action '$activeAction'. Supported: status, orchestrate, agent, models, engines, mcp-hub, download.")
            }
        }
    }
}
