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
     * Executes an autonomous reasoning mission.
     * This is the pure intelligence phase - no tool execution happens here.
     */
    fun reason(prompt: String, preferredEngine: String? = null): GhaAgentResult {
        val log = mutableListOf<String>()
        log.add("🧠 [GEMI Intelligence] Tier 3 Inference Engine active.")
        
        // 1. Resolve Engines (Available & Custom GHA Engine)
        val engines = GhaEngineManager.detectEngines(rootDir)
        val targetEngine = if (preferredEngine != null) {
            engines.find { it.name.lowercase().contains(preferredEngine.lowercase()) }
        } else {
            engines.firstOrNull { it.isAvailable }
        }

        if (targetEngine == null) {
            log.add("⚠️ [GEMI] No active inference engine detected. Using GHA Custom Mock Engine.")
            return GhaAgentResult(
                success = true,
                log = log,
                output = "GEMI Mock Reasoning: Mission goal '$prompt' analyzed. Strategy: Delegate to specialized worker agents."
            )
        }

        log.add("⚡ [GEMI] Selected Engine: ${targetEngine.name} [${targetEngine.type}]")

        // 2. Resolve Model
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
