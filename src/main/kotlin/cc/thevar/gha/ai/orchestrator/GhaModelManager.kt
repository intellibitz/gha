package cc.thevar.gha.ai.orchestrator

import cc.thevar.gha.safety.GhaProcessRunner
import java.io.File

/**
 * AI Model Manager for GHA AI Orchestrator.
 * Handles searching, downloading (via Hugging Face Hub `hf download` / cURL),
 * caching, and listing AI models in the sandboxed repository storage `.gha/models/`.
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

    fun getModelsDir(rootDir: File): File {
        val dir = File(rootDir, ".gha/models")
        if (!dir.exists()) dir.mkdirs()
        return dir
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
