package cc.thevar.gha.ai.orchestrator

import cc.thevar.gha.safety.GhaProcessRunner
import com.sun.management.OperatingSystemMXBean
import java.io.File
import java.lang.management.ManagementFactory

/**
 * Hardware Profiler for GHA AI Orchestrator.
 * Analyzes local hardware constraints (RAM, CPU, GPU) to optimize AI model execution on limited home hardware.
 */
object GhaHardwareProfiler {

    data class HardwareProfile(
        val totalRamGb: Double,
        val availableRamGb: Double,
        val cpuCores: Int,
        val osName: String,
        val hasGpu: Boolean,
        val gpuInfo: String,
        val maxRecommendedModelParams: String,
        val recommendedQuantization: String
    )

    /**
     * Inspects current system hardware and returns a hardware profile report.
     */
    fun profile(rootDir: File): HardwareProfile {
        val runtime = Runtime.getRuntime()
        val cpuCores = runtime.availableProcessors()
        val osName = System.getProperty("os.name") ?: "Unknown OS"

        val totalRamGb = getTotalRamGb()
        val availableRamGb = getAvailableRamGb()
        val (hasGpu, gpuInfo) = detectGpu(rootDir)

        val (recommendedParams, recommendedQuant) = when {
            totalRamGb < 8.0 -> "1B - 3B Parameters" to "GGUF Q4_K_M (Low Footprint)"
            totalRamGb < 16.0 -> "7B - 8B Parameters" to "GGUF Q4_K_M / Q8_0"
            totalRamGb < 32.0 -> "13B - 14B Parameters" to "GGUF Q5_K_M / FP16"
            else -> "32B+ Parameters" to "Full Precision / FP16"
        }

        return HardwareProfile(
            totalRamGb = totalRamGb,
            availableRamGb = availableRamGb,
            cpuCores = cpuCores,
            osName = osName,
            hasGpu = hasGpu,
            gpuInfo = gpuInfo,
            maxRecommendedModelParams = recommendedParams,
            recommendedQuantization = recommendedQuant
        )
    }

    private fun getTotalRamGb(): Double {
        return try {
            val memBean = ManagementFactory.getOperatingSystemMXBean()
            if (memBean is OperatingSystemMXBean) {
                memBean.totalMemorySize / (1024.0 * 1024.0 * 1024.0)
            } else {
                8.0
            }
        } catch (_: Exception) {
            8.0
        }
    }

    private fun getAvailableRamGb(): Double {
        return try {
            val memBean = ManagementFactory.getOperatingSystemMXBean()
            if (memBean is OperatingSystemMXBean) {
                memBean.freeMemorySize / (1024.0 * 1024.0 * 1024.0)
            } else {
                4.0
            }
        } catch (_: Exception) {
            4.0
        }
    }

    private fun detectGpu(rootDir: File): Pair<Boolean, String> {
        val os = System.getProperty("os.name")?.lowercase() ?: ""
        if (os.contains("mac")) {
            val res = GhaProcessRunner.exec(rootDir, listOf("sysctl", "-n", "machdep.cpu.brand_string"))
            if (res.isSuccess && res.stdout.contains("Apple")) {
                return true to "Apple Silicon Metal Acceleration (${res.stdout.trim()})"
            }
        }

        val nvidiaRes = GhaProcessRunner.exec(rootDir, listOf("nvidia-smi", "--query-gpu=name", "--format=csv,noheader"))
        if (nvidiaRes.isSuccess && nvidiaRes.stdout.isNotBlank()) {
            return true to "NVIDIA GPU: ${nvidiaRes.stdout.trim().lines().first()}"
        }

        return false to "CPU Only Execution (Optimized GGUF Threading)"
    }
}
