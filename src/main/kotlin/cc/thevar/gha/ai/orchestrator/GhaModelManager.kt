package cc.thevar.gha.ai.orchestrator

import cc.thevar.gha.safety.GhaProcessRunner
import cc.thevar.gha.safety.GhaSandboxManager
import java.io.File

/**
 * AI Model Manager for GHA AI Orchestrator.
 * Handles searching, resolving, downloading (via Hugging Face Hub `hf download` / cURL),
 * caching, and listing local and WEB-based AI models across Hugging Face Hub, OpenRouter,
 * Ollama Library, and Groq.
 */
object GhaModelManager {

    data class AiModelInfo(
        val repoId: String,
        val localPath: String,
        val sizeMb: Double,
        val format: String, // GGUF, ONNX, SafeTensors, PyTorch
        val isHardwareCompatible: Boolean,
        val compatibilityNote: String
    )

    data class WebModelInfo(
        val modelId: String,
        val registry: String, // HUGGING_FACE, OPENROUTER, OLLAMA_LIBRARY, GROQ
        val name: String,
        val description: String,
        val contextWindow: String,
        val isRecommendedForHardware: Boolean
    )

    fun getModelsDir(rootDir: File): File {
        return File(GhaSandboxManager.getGlobalGhaDir(), "models").apply { if (!exists()) mkdirs() }
    }

    /**
     * Lists all models currently cached in `.gha/models/`.
     */
    fun listLocalModels(rootDir: File): List<AiModelInfo> {
        val modelsDir = getModelsDir(rootDir)
        val profile = GhaHardwareProfiler.profile(rootDir)

        val files = modelsDir.walkTopDown()
            .filter { it.isFile && (it.extension in listOf("gguf", "onnx", "bin", "safetensors", "pt")) }
            .toList()

        return files.map { file ->
            val sizeMb = file.length() / (1024.0 * 1024.0)
            val sizeGb = sizeMb / 1024.0
            val format = file.extension.uppercase()

            val fitsRam = sizeGb <= profile.availableRamGb
            val note = if (fitsRam) {
                "Compatible: Model size (${String.format("%.1f", sizeGb)}GB) fits available RAM (${String.format("%.1f", profile.availableRamGb)}GB)"
            } else {
                "Warning: Model size (${String.format("%.1f", sizeGb)}GB) exceeds available RAM (${String.format("%.1f", profile.availableRamGb)}GB). May run slow or require swap."
            }

            AiModelInfo(
                repoId = file.name,
                localPath = file.absolutePath,
                sizeMb = sizeMb,
                format = format,
                isHardwareCompatible = fitsRam,
                compatibilityNote = note
            )
        }
    }

    /**
     * Lists popular and discovered AI models available on the Web for engines to consume.
     */
    fun listWebModels(rootDir: File): List<WebModelInfo> {
        val webModels = mutableListOf<WebModelInfo>()

        // 1. DeepSeek R1 Series (Hugging Face / OpenRouter / Ollama)
        webModels.add(
            WebModelInfo(
                modelId = "deepseek-ai/DeepSeek-R1-Distill-Qwen-1.5B-GGUF",
                registry = "HUGGING_FACE",
                name = "DeepSeek R1 Distill Qwen 1.5B GGUF",
                description = "Ultra-fast reasoning model optimized for low-resource laptop/mobile hardware",
                contextWindow = "128k context",
                isRecommendedForHardware = true
            )
        )
        webModels.add(
            WebModelInfo(
                modelId = "deepseek/deepseek-r1",
                registry = "OPENROUTER",
                name = "DeepSeek R1 (Web API)",
                description = "Full-capability open-weights reasoning model hosted on OpenRouter",
                contextWindow = "128k context",
                isRecommendedForHardware = true
            )
        )

        // 2. Meta Llama 3.3 / 3.2 Series
        webModels.add(
            WebModelInfo(
                modelId = "bartowski/Llama-3.3-70B-Instruct-GGUF",
                registry = "HUGGING_FACE",
                name = "Llama 3.3 70B Instruct GGUF",
                description = "State-of-the-art open-weights model for high-end workstations and GPU servers",
                contextWindow = "128k context",
                isRecommendedForHardware = true
            )
        )
        webModels.add(
            WebModelInfo(
                modelId = "llama3.3",
                registry = "OLLAMA_LIBRARY",
                name = "Llama 3.3 (Ollama Web Library)",
                description = "Official Ollama library model for local and remote Ollama engines",
                contextWindow = "128k context",
                isRecommendedForHardware = true
            )
        )

        // 3. Qwen 2.5 Coder Series (Code Intelligence)
        webModels.add(
            WebModelInfo(
                modelId = "Qwen/Qwen2.5-Coder-7B-Instruct-GGUF",
                registry = "HUGGING_FACE",
                name = "Qwen 2.5 Coder 7B Instruct GGUF",
                description = "Leading open coding model for Kotlin, Android, Java, Python, and SQL",
                contextWindow = "32k context",
                isRecommendedForHardware = true
            )
        )
        webModels.add(
            WebModelInfo(
                modelId = "qwen2.5-coder",
                registry = "OLLAMA_LIBRARY",
                name = "Qwen 2.5 Coder (Ollama Web Library)",
                description = "Ollama Library coding model optimized for IDE automation",
                contextWindow = "32k context",
                isRecommendedForHardware = true
            )
        )

        // 4. Groq LPU Web Models
        webModels.add(
            WebModelInfo(
                modelId = "llama-3.3-70b-versatile",
                registry = "GROQ",
                name = "Llama 3.3 70B Versatile (Groq LPU)",
                description = "Sub-second inference model running on Groq LPU hardware",
                contextWindow = "128k context",
                isRecommendedForHardware = true
            )
        )

        // 5. OpenRouter Web Models (Claude 3.5 Sonnet / GPT-4o)
        webModels.add(
            WebModelInfo(
                modelId = "anthropic/claude-3.5-sonnet",
                registry = "OPENROUTER",
                name = "Claude 3.5 Sonnet (OpenRouter Web)",
                description = "Top-tier AI coding model accessible via OpenRouter web engine",
                contextWindow = "200k context",
                isRecommendedForHardware = true
            )
        )
        webModels.add(
            WebModelInfo(
                modelId = "openai/gpt-4o",
                registry = "OPENROUTER",
                name = "GPT-4o (OpenRouter Web)",
                description = "Multimodal GPT-4o model accessible via OpenRouter web engine",
                contextWindow = "128k context",
                isRecommendedForHardware = true
            )
        )

        return webModels
    }

    /**
     * Searches web registries (Hugging Face Hub API, OpenRouter API) for matching online models.
     */
    fun searchWebModels(query: String, rootDir: File): List<WebModelInfo> {
        val localMatch = listWebModels(rootDir).filter {
            it.name.lowercase().contains(query.lowercase()) || it.modelId.lowercase().contains(query.lowercase())
        }
        if (localMatch.isNotEmpty()) return localMatch

        // Dynamic Hugging Face Hub Web API Query
        val hfRes = GhaProcessRunner.exec(
            workingDir = rootDir,
            command = listOf("curl", "-fsSL", "https://huggingface.co/api/models?search=${query.replace(" ", "+")}&limit=5"),
            timeoutSeconds = 15L
        )

        if (hfRes.isSuccess && hfRes.stdout.contains("id")) {
            val ids = "\"id\":\"([^\"]+)\"".toRegex().findAll(hfRes.stdout).map { it.groupValues[1] }.toList()
            return ids.map { id ->
                WebModelInfo(
                    modelId = id,
                    registry = "HUGGING_FACE",
                    name = id.substringAfterLast("/"),
                    description = "Discovered model on Hugging Face Hub Web Registry",
                    contextWindow = "Variable context",
                    isRecommendedForHardware = true
                )
            }
        }

        return listWebModels(rootDir)
    }

    /**
     * Resolves a web model ID or repository for an engine to consume.
     */
    fun resolveWebModel(modelId: String, rootDir: File): WebModelInfo? {
        val models = searchWebModels(modelId, rootDir)
        return models.find { it.modelId.equals(modelId, ignoreCase = true) } ?: models.firstOrNull()
    }

    /**
     * Downloads an AI model from Hugging Face Hub or URL into `.gha/models/`.
     */
    fun downloadModel(rootDir: File, repoId: String, filenameFilter: String? = null): String {
        val modelsDir = getModelsDir(rootDir)
        val targetSubDir = File(modelsDir, repoId.replace("/", "_"))
        if (!targetSubDir.exists()) targetSubDir.mkdirs()

        // 1. Try Hugging Face Hub CLI (`hf download`) first if available
        val hfArgs = mutableListOf("hf", "download", repoId, "--local-dir", targetSubDir.absolutePath)
        if (!filenameFilter.isNullOrBlank()) {
            hfArgs.addAll(listOf("--include", filenameFilter))
        }

        val hfRes = GhaProcessRunner.exec(rootDir, hfArgs, timeoutSeconds = 300L)
        if (hfRes.isSuccess) {
            return "✅ Model '$repoId' downloaded successfully to ${targetSubDir.absolutePath} via Hugging Face Hub CLI."
        }

        // 2. Direct cURL download fallback for GGUF / model files
        if (repoId.startsWith("http://") || repoId.startsWith("https://")) {
            val url = repoId
            val fileName = url.substringAfterLast("/").ifBlank { "model.gguf" }
            val destFile = File(targetSubDir, fileName)

            val curlRes = GhaProcessRunner.exec(rootDir, listOf("curl", "-L", url, "-o", destFile.absolutePath), timeoutSeconds = 600L)
            if (curlRes.isSuccess && destFile.exists()) {
                return "✅ Model downloaded from URL to ${destFile.absolutePath}"
            }
        } else {
            // HF direct resolve URL fallback: https://huggingface.co/<repoId>/resolve/main/<filter>
            val filterName = filenameFilter ?: "model.gguf"
            val directUrl = "https://huggingface.co/$repoId/resolve/main/$filterName"
            val destFile = File(targetSubDir, filterName)

            val curlRes = GhaProcessRunner.exec(rootDir, listOf("curl", "-L", directUrl, "-o", destFile.absolutePath), timeoutSeconds = 600L)
            if (curlRes.isSuccess && destFile.exists() && destFile.length() > 1024) {
                return "✅ Model '$repoId/$filterName' downloaded successfully via Hugging Face Hub direct resolver to ${destFile.absolutePath}"
            }
        }

        return "⚠️ Download attempted for '$repoId'. Ensure Hugging Face CLI ('hf') or cURL is installed."
    }
}
