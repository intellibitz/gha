package cc.thevar.gha.safety

import java.io.File
import java.net.URI

/**
 * Manages project versioning and autonomous version bumping.
 */
object GhaVersionManager {

    private const val VERSION_FILE = "version.txt"

    /**
     * Reads the current version from version.txt in the project root.
     */
    fun readVersion(rootDir: File): String {
        val file = File(rootDir, VERSION_FILE)
        return if (file.exists()) {
            file.readText().trim()
        } else {
            "0.1.0-SNAPSHOT"
        }
    }

    /**
     * Fetches the latest version from the GitHub source.
     */
    fun fetchRemoteVersion(): String {
        return try {
            val url = URI("https://raw.githubusercontent.com/intellibitz/gha/main/version.txt").toURL()
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
        
        // If versions are same but one is SNAPSHOT, treat SNAPSHOT as older for upgrade purposes
        if (v1.endsWith("-SNAPSHOT") && !v2.endsWith("-SNAPSHOT")) return -1
        if (!v1.endsWith("-SNAPSHOT") && v2.endsWith("-SNAPSHOT")) return 1
        
        return 0
    }

    /**
     * Bumps the version (patch level) and writes it back to version.txt.
     */
    fun bumpVersion(rootDir: File): String {
        val current = readVersion(rootDir)
        val newVersion = incrementVersion(current)
        File(rootDir, VERSION_FILE).writeText(newVersion + "\n")
        return newVersion
    }

    /**
     * Simple semantic version incrementer (supports 0.1.0 and 0.1.0-SNAPSHOT).
     */
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
