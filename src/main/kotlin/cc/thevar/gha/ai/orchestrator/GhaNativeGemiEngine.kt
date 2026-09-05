package cc.thevar.gha.ai.orchestrator

import cc.thevar.gha.ai.vision.GhaAgentResult
import cc.thevar.gha.safety.GhaProcessRunner
import java.io.File

/**
 * GHA Native GEMI Engine: High-performance local inference for GGUF models.
 * Operates as a managed local server providing an OpenAI-compatible API.
 */
class GhaNativeGemiEngine(val rootDir: File) {

    data class ChatMessage(val role: String, val content: String)
    data class ChatCompletionRequest(
        val model: String? = null,
        val messages: List<ChatMessage>,
        val temperature: Double = 0.7,
        val maxTokens: Int = 512
    )
    data class ChatChoice(val index: Int, val message: ChatMessage, val finishReason: String = "stop")
    data class ChatCompletionResponse(
        val id: String,
        val objectType: String = "chat.completion",
        val created: Long = System.currentTimeMillis() / 1000,
        val model: String,
        val choices: List<ChatChoice>
    )

    /**
     * Executes OpenAI-style Chat Completion.
     */
    fun chatCompletion(request: ChatCompletionRequest): ChatCompletionResponse {
        val prompt = request.messages.joinToString("\n") { "${it.role}: ${it.content}" }
        val result = reasonLocal(prompt, request.model)
        return ChatCompletionResponse(
            id = "chatcmpl-gha-native-${System.currentTimeMillis()}",
            model = request.model ?: "gha-native-gguf",
            choices = listOf(
                ChatChoice(
                    index = 0,
                    message = ChatMessage(role = "assistant", content = result.output)
                )
            )
        )
    }

    /**
     * Executes local reasoning using a downloaded GGUF model with hardware acceleration.
     */
    fun reasonLocal(prompt: String, modelId: String? = null): GhaAgentResult {
        val log = mutableListOf<String>()
        log.add("🧠 [GHA Native Engine] Local Inference requested.")

        // 1. Resolve Local Model
        val localModels = GhaModelManager.listLocalModels(rootDir)
        if (localModels.isEmpty()) {
            return GhaAgentResult(false, log + "❌ [GHA Native Engine] No local models found in global sandbox.", "")
        }

        val selectedModel = if (modelId != null) {
            localModels.find { it.repoId.contains(modelId) || it.localPath.contains(modelId) } ?: localModels.first()
        } else {
            localModels.first()
        }
        
        log.add("📦 [GHA Native Engine] Using Model: ${selectedModel.repoId}")

        // 2. Hardware Profiling & Optimization
        val hwProfile = GhaHardwareProfiler.profile(rootDir)
        val threads = (hwProfile.cpuCores - 1).coerceAtLeast(1)
        log.add("⚙️ [GHA Native Engine] Hardware Acceleration: Cores=$threads, GPU=${hwProfile.gpuInfo}")

        // 3. Managed Inference (Lazy Loading via llama-cli or similar)
        val cmd = mutableListOf(
            "llama-cli", 
            "-m", selectedModel.localPath, 
            "-p", prompt, 
            "-n", "512", 
            "--temp", "0.7",
            "-t", threads.toString(),
            "--no-display-prompt"
        )
        if (hwProfile.hasGpu) {
            cmd.addAll(listOf("-ngl", "99"))
        }

        log.add("⚡ [GHA Native Engine] Executing local inference trace...")
        val res = GhaProcessRunner.exec(rootDir, cmd, timeoutSeconds = 120L)

        return if (res.isSuccess) {
            log.add("✅ [GHA Native Engine] Local reasoning complete.")
            GhaAgentResult(true, log, res.stdout.trim())
        } else {
            log.add("⚠️ [GHA Native Engine] Local inference failed: ${res.stderr}")
            GhaAgentResult(false, log, "Error: Local inference engine failed to respond.")
        }
    }

    /**
     * Checks if the native engine has all requirements met.
     */
    fun isAvailable(): Boolean {
        val llamaCheck = GhaProcessRunner.exec(rootDir, listOf("llama-cli", "--version"))
        return llamaCheck.isSuccess && GhaModelManager.listLocalModels(rootDir).isNotEmpty()
    }
}
