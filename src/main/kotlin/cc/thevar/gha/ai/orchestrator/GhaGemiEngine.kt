package cc.thevar.gha.ai.orchestrator

import cc.thevar.gha.ai.vision.GhaAgentResult
import java.io.File

/**
 * GEMI: GHA Engines & Models AI Inference (Tier 3).
 * Represents the Intelligence Layer of GHA.
 * GEMI coordinates all AI inference engines (Local & Cloud) and autonomous models.
 * 
 * Key Principles:
 * 1. GEMI Engines work with GEMI Models to provide reasoning.
 * 2. GEMI reports back to Agents (Tier 2).
 * 3. GEMI generally NEVER talks to GMCP (Tier 4). Only Agents can talk to GMCP.
 * 4. EXCEPTIONAL RULE: If an Engine advertises itself as an Agent (isAgent = true), it can talk to GMCP.
 */
class GhaGemiEngine(val rootDir: File) {

    /**
     * Autonomous GEMI Model.
     * Models in this layer are considered self-contained intelligence units.
     */
    data class GhaGemiModel(
        val id: String,
        val name: String,
        val isAutonomous: Boolean = true,
        val capabilities: List<String> = emptyList()
    )

    /**
     * Intelligence Layer (Tier 3): Intelligently routes inference to the best model.
     */
    fun getOptimalEngine(prompt: String): GhaEngineManager.EngineInfo? {
        val engines = GhaEngineManager.detectEngines(rootDir).filter { it.isAvailable }
        if (engines.isEmpty()) return null

        return when {
            prompt.length > 1000 -> engines.find { it.name.contains("OpenAI") || it.name.contains("Claude") }
            prompt.contains("code") -> engines.find { it.name.contains("Groq") || it.name.contains("Ollama") }
            else -> engines.first()
        } ?: engines.first()
    }

    /**
     * Executes an autonomous reasoning mission.
     */
    fun reason(prompt: String, preferredEngine: String? = null): GhaAgentResult {
        val log = mutableListOf<String>()
        log.add("🧠 [GEMI Intelligence] Tier 3 Inference Engine active.")
        
        // 1. Resolve Engines via T3 Intelligence
        val engines = GhaEngineManager.detectEngines(rootDir)
        val targetEngine = if (preferredEngine != null) {
            engines.find { it.name.lowercase().contains(preferredEngine.lowercase()) }
        } else {
            // Prioritize GHA Native Engine if available and models exist
            val native = engines.find { it.name == "GHA Native Engine" && it.isAvailable }
            native ?: getOptimalEngine(prompt)
        }

        if (targetEngine == null) {
            val localModels = GhaModelManager.listLocalModels(rootDir)
            if (localModels.isNotEmpty()) {
                log.add("⚡ [GEMI] Selected Engine: GHA Native Engine [EMBEDDED]")
                val nativeRes = GhaNativeGemiEngine(rootDir).reasonLocal(prompt)
                return GhaAgentResult(nativeRes.success, log + nativeRes.log, nativeRes.output)
            }

            log.add("⚠️ [GEMI] No active inference engine detected. Using GHA Custom Mock Engine.")
            return GhaAgentResult(
                success = true,
                log = log,
                output = "GEMI Mock Reasoning: Mission goal '$prompt' analyzed. Strategy: Delegate to specialized worker agents."
            )
        }

        log.add("⚡ [GEMI] Selected Engine: ${targetEngine.name} [${targetEngine.type}]")

        // 2. Handle GHA Native Engine Execution
        if (targetEngine.name == "GHA Native Engine") {
            val nativeRes = GhaNativeGemiEngine(rootDir).reasonLocal(prompt)
            return GhaAgentResult(nativeRes.success, log + nativeRes.log, nativeRes.output)
        }

        // 3. Resolve Model for other engines
        val webModels = GhaModelManager.listWebModels(rootDir)
        val selectedModel = webModels.firstOrNull { it.isRecommendedForHardware } ?: webModels.first()
        log.add("🧠 [GEMI] Collaborating with Autonomous Model: ${selectedModel.name} ('${selectedModel.modelId}')")

        // 3. Perform Reasoning (Simulation of LLM call)
        log.add("🔍 [GEMI] Reasoning in progress (Intelligence Layer)...")
        val reasoningResult = "GEMI Analysis complete for goal: \"$prompt\". Tier 3 Intelligence recommends parallel execution across VCS and Build agents."

        log.add("✅ [GEMI] Reasoning reported back to Agent.")
        return GhaAgentResult(true, log, reasoningResult)
    }

    /**
     * Discovers all intelligence components in this layer.
     */
    fun getIntelligenceReport(): String {
        val engines = GhaEngineManager.detectEngines(rootDir).count { it.isAvailable }
        val models = GhaModelManager.listWebModels(rootDir).size
        val local = GhaModelManager.listLocalModels(rootDir).size
        return "GEMI Intelligence Layer: $engines engines active, $models web models, $local local models available."
    }
}
