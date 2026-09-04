package cc.thevar.gha.ai.orchestrator

import cc.thevar.gha.safety.GhaProcessRunner
import java.io.File

/**
 * AI Engine Manager for GHA AI Orchestrator.
 * Detects, bootstraps, and manages AI execution engines (Ollama, llama.cpp, Hugging Face CLI, ONNX Runtime, PyTorch/UV).
 */
object GhaEngineManager {

    data class EngineInfo(
        val name: String,
        val type: String, // REST_API, CLI, EMBEDDED
        val isAvailable: Boolean,
        val version: String,
        val description: String
    )

    fun getEnginesDir(rootDir: File): File {
        val dir = File(rootDir, ".gha/engines")
        if (!dir.exists()) dir.mkdirs()
        return dir
    }

    /**
     * Inspects local environment for available AI inference engines.
     */
    fun detectEngines(rootDir: File): List<EngineInfo> {
        val engines = mutableListOf<EngineInfo>()

        // 1. Ollama Engine
        val ollamaRes = GhaProcessRunner.exec(rootDir, listOf("ollama", "--version"))
        val hasOllama = ollamaRes.isSuccess
        val ollamaVer = if (hasOllama) ollamaRes.stdout.trim() else "Not Installed"
        engines.add(
            EngineInfo(
                name = "Ollama",
                type = "REST_API",
                isAvailable = hasOllama,
                version = ollamaVer,
                description = "High-performance local LLM engine for GGUF models on CPU/GPU"
            )
        )

        // 2. Hugging Face CLI Engine (`hf`)
        val hfRes = GhaProcessRunner.exec(rootDir, listOf("hf", "version"))
        val hasHf = hfRes.isSuccess
        val hfVer = if (hasHf) hfRes.stdout.trim() else "Not Installed"
        engines.add(
            EngineInfo(
                name = "Hugging Face CLI (hf)",
                type = "CLI",
                isAvailable = hasHf,
                version = hfVer,
                description = "Universal Hub CLI for downloading models, datasets, spaces, and inference endpoints"
            )
        )

        // 3. Llama.cpp Engine
        val llamaRes = GhaProcessRunner.exec(rootDir, listOf("llama-cli", "--version"))
        val hasLlama = llamaRes.isSuccess
        val llamaVer = if (hasLlama) llamaRes.stdout.trim().lines().firstOrNull() ?: "Active" else "Not Installed"
        engines.add(
            EngineInfo(
                name = "llama.cpp",
                type = "CLI",
                isAvailable = hasLlama,
                version = llamaVer,
                description = "Ultra-lightweight C++ engine optimized for low-resource home hardware"
            )
        )

        // 4. Python UV / Venv Runtime Engine
        val uvRes = GhaProcessRunner.exec(rootDir, listOf("uv", "--version"))
        val hasUv = uvRes.isSuccess
        val uvVer = if (hasUv) uvRes.stdout.trim() else "Not Installed"
        engines.add(
            EngineInfo(
                name = "Python UV Runtime",
                type = "EMBEDDED",
                isAvailable = hasUv,
                version = uvVer,
                description = "Fast Python package and virtual environment manager for AI libraries"
            )
        )

        return engines
    }
}
