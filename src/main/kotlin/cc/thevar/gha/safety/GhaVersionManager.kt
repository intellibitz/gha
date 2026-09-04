package cc.thevar.gha.safety

import cc.thevar.gha.insights.GhaInsightsManager
import java.io.File
import java.net.URI

/**
 * Manages GHA Engine versioning and User Project versioning cleanly separated.
 */
object GhaVersionManager {

    /**
     * Returns the installed GHA Engine version (e.g. 0.1.26-SNAPSHOT).
     */
    fun getEngineVersion(rootDir: File): String {
        val engineFile = File(rootDir, ".gha/gha-engine-version.txt")
        if (engineFile.exists()) return engineFile.readText().trim()

        val rootVersionFile = File(rootDir, "version.txt")
        if (rootVersionFile.exists() && rootDir.name == "gha") return rootVersionFile.readText().trim()

        return readProjectVersion(rootDir)
    }

    /**
     * Reads the User Project's version (defaults to "0.1.0" for new user projects).
     */
    fun readProjectVersion(rootDir: File): String {
        val rootFile = File(rootDir, "version.txt")
        if (rootFile.exists() && rootFile.readText().trim().isNotBlank()) {
            return rootFile.readText().trim()
        }

        val sandboxFile = File(rootDir, ".gha/version.txt")
        if (sandboxFile.exists() && sandboxFile.readText().trim().isNotBlank()) {
            return sandboxFile.readText().trim()
        }

        return if (rootDir.name == "gha") "0.1.0-SNAPSHOT" else "0.1.0"
    }

    fun readVersion(rootDir: File): String = readProjectVersion(rootDir)

    /**
     * Resolves the upstream GHA Engine repository slug dynamically (e.g. "intellibitz/gha" or custom fork/mirror).
     */
    fun resolveEngineRepo(rootDir: File? = null): String {
        val envRepo = System.getenv("GHA_REPO") ?: System.getenv("GHA_ENGINE_REPO")
        if (!envRepo.isNullOrBlank()) return envRepo.trim()

        if (rootDir != null) {
            val gitRepo = GhaInsightsManager.resolveOwnerAndRepo(rootDir)
            if (!gitRepo.isNullOrBlank() && (rootDir.name == "gha" || File(rootDir, "src/main/kotlin/cc/thevar/gha").exists())) {
                return gitRepo.trim()
            }
        }

        return "intellibitz/gha"
    }

    /**
     * Fetches the latest remote GHA Engine version from GitHub source.
     */
    fun fetchRemoteVersion(rootDir: File? = null): String {
        return try {
            val engineRepo = resolveEngineRepo(rootDir)
            val url = URI("https://raw.githubusercontent.com/$engineRepo/main/version.txt").toURL()
            url.readText().trim()
        } catch (_: Exception) {
            "unknown"
        }
    }

    /**
     * Compares two semantic versions. Returns 1 if v1 > v2, -1 if v1 < v2, 0 if equal.
     */
    fun compareVersions(v1: String, v2: String): Int {
        if (v1 == v2) return 0
        if (v1 == "unknown" || v2 == "unknown") return 0
        
        val b1 = v1.removeSuffix("-SNAPSHOT").split(".").map { it.toIntOrNull() ?: 0 }
        val b2 = v2.removeSuffix("-SNAPSHOT").split(".").map { it.toIntOrNull() ?: 0 }
        
        for (i in 0 until maxOf(b1.size, b2.size)) {
            val p1 = b1.getOrElse(i) { 0 }
            val p2 = b2.getOrElse(i) { 0 }
            if (p1 > p2) return 1
            if (p1 < p2) return -1
        }
        
        if (v1.endsWith("-SNAPSHOT") && !v2.endsWith("-SNAPSHOT")) return -1
        if (!v1.endsWith("-SNAPSHOT") && v2.endsWith("-SNAPSHOT")) return 1
        
        return 0
    }

    /**
     * Bumps the User Project version (patch level) and writes to .gha/version.txt (and root version.txt if in gha project).
     */
    fun bumpVersion(rootDir: File): String {
        val current = readProjectVersion(rootDir)
        val newVersion = incrementVersion(current)

        val sandboxDir = File(rootDir, ".gha")
        if (!sandboxDir.exists()) sandboxDir.mkdirs()

        // Clean up old version-*.txt files in .gha/ and root
        sandboxDir.listFiles()?.filter { it.name.startsWith("version-") && it.name.endsWith(".txt") }?.forEach {
            try { it.delete() } catch (_: Exception) {}
        }
        rootDir.listFiles()?.filter { it.name.startsWith("version-") && it.name.endsWith(".txt") }?.forEach {
            try { it.delete() } catch (_: Exception) {}
        }

        File(sandboxDir, "version-$newVersion.txt").writeText(newVersion + "\n")
        File(sandboxDir, "version.txt").writeText(newVersion + "\n")

        val isGhaRepo = rootDir.name == "gha" || File(rootDir, "src/main/kotlin/cc/thevar/gha").exists()
        if (isGhaRepo) {
            File(rootDir, "version-$newVersion.txt").writeText(newVersion + "\n")
            File(rootDir, "version.txt").writeText(newVersion + "\n")
            File(sandboxDir, "gha-engine-version.txt").writeText(newVersion + "\n")
        }

        return newVersion
    }

    private fun incrementVersion(version: String): String {
        val isSnapshot = version.endsWith("-SNAPSHOT")
        val base = if (isSnapshot) version.removeSuffix("-SNAPSHOT") else version
        val parts = base.split(".").toMutableList()
        
        if (parts.size >= 3) {
            val patch = parts[2].toIntOrNull() ?: 0
            parts[2] = (patch + 1).toString()
        } else if (parts.size == 2) {
            parts.add("1")
        } else {
            return "$version.1"
        }

        val bumpedBase = parts.joinToString(".")
        return if (isSnapshot) "$bumpedBase-SNAPSHOT" else bumpedBase
    }
}
