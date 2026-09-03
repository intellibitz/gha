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
     * Checks if gradle.user.home is directed to .gha/gradle-user-home.
     */
    fun checkGradleUserHome(rootDir: File, gradleUserHomeDir: File? = null): Boolean {
        val userHome = gradleUserHomeDir
            ?: System.getProperty("gradle.user.home")?.let { File(it) }
            ?: System.getProperty("org.gradle.user.home")?.let { File(it) }
            ?: return false
        val expectedHome = File(rootDir, GRADLE_USER_HOME_REL).canonicalPath
        return userHome.canonicalPath == expectedHome
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
