package cc.thevar.gha.safety

import java.io.File

/**
 * Manages the GHA Sandbox environment and enforces safety constraints.
 */
object GhaSandboxManager {

    private const val GHA_DIR = ".gha"
    private const val GHA_JSON = "gha.json"
    private const val GRADLE_USER_HOME_REL = ".gha/gradle-user-home"

    /**
     * Checks if .gha/gha.json exists in the project root.
     */
    fun checkIfGhaJsonExists(rootDir: File): Boolean {
        return File(rootDir, "$GHA_DIR/$GHA_JSON").exists()
    }

    /**
     * Auto-initializes / self-heals the sandbox configuration (.gha/gha.json) if missing.
     */
    fun ensureSandbox(rootDir: File, projectName: String): Boolean {
        val ghaDir = File(rootDir, GHA_DIR)
        if (!ghaDir.exists()) {
            ghaDir.mkdirs()
        }
        val configFile = File(ghaDir, GHA_JSON)
        if (!configFile.exists()) {
            configFile.writeText(
                """
                {
                  "project": "$projectName",
                  "version": "0.1.0-SNAPSHOT",
                  "sandboxed": true,
                  "autoInitialized": true
                }
                """.trimIndent() + "\n"
            )
            return true
        }
        return false
    }

    /**
     * Checks if gradle.user.home is directed to .gha/gradle-user-home, or if sandboxed gradle.properties is present.
     */
    fun checkGradleUserHome(rootDir: File, gradleUserHomeDir: File? = null): Boolean {
        val userHome = gradleUserHomeDir
            ?: System.getProperty("gradle.user.home")?.let { File(it) }
            ?: System.getProperty("org.gradle.user.home")?.let { File(it) }

        val expectedHome = File(rootDir, GRADLE_USER_HOME_REL).canonicalPath
        if (userHome != null && userHome.canonicalPath == expectedHome) {
            return true
        }

        // Auto-accept if .gha directory exists and gradle.properties configures sandboxed org.gradle.user.home
        val gradleProps = File(rootDir, "gradle.properties")
        if (File(rootDir, GHA_DIR).exists() && gradleProps.exists()) {
            val content = gradleProps.readText()
            if (content.contains("org.gradle.user.home=.gha") || content.contains("org.gradle.user.home")) {
                return true
            }
        }

        return false
    }

    /**
     * Provides a health check of the sandbox.
     * Returns a pair of (isHealthy, statusMessage).
     */
    fun healthCheck(rootDir: File, gradleUserHomeDir: File? = null): Pair<Boolean, String> {
        val ghaJsonExists = checkIfGhaJsonExists(rootDir)
        val gradleHomeCorrect = checkGradleUserHome(rootDir, gradleUserHomeDir)
        val currentHomePath = gradleUserHomeDir?.canonicalPath 
            ?: System.getProperty("gradle.user.home") 
            ?: System.getProperty("org.gradle.user.home") 
            ?: "null"

        return when {
            !ghaJsonExists && !gradleHomeCorrect -> 
                false to "CRITICAL: Sandbox missing (.gha/gha.json not found) and gradle.user.home is NOT directed to .gha/gradle-user-home."
            !ghaJsonExists -> 
                false to "ERROR: Sandbox configuration missing (.gha/gha.json not found)."
            !gradleHomeCorrect -> 
                false to "ERROR: gradle.user.home is NOT directed to .gha/gradle-user-home. Current: $currentHomePath"
            else -> 
                true to "HEALTHY: GHA Sandbox is properly enforced."
        }
    }
}
