package cc.thevar.gha.safety

import java.io.File

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
