package cc.thevar.gha.config

/**
 * Single source of truth for GHA tools, SDKs, frameworks, and dependency versions.
 * Strictly enforces official stable versions from verified, trusted vendors.
 */
object GhaConfig {
    const val JAVA_VERSION = "21"
    const val JAVA_VENDOR = "Eclipse Adoptium (Temurin) / Oracle"

    const val KOTLIN_VERSION = "2.1.0"
    const val KOTLIN_VENDOR = "JetBrains"

    const val GRADLE_VERSION = "9.7.1"
    const val GRADLE_VENDOR = "Gradle Inc."

    const val PLUGIN_PUBLISH_VERSION = "1.3.1"
    const val PLUGIN_PUBLISH_VENDOR = "Gradle Inc."

    const val FOOJAY_RESOLVER_VERSION = "0.9.0"
    const val FOOJAY_RESOLVER_VENDOR = "Foojay / Gradle Inc."

    const val GIT_VENDOR = "Software Freedom Conservancy"
    const val GH_CLI_VENDOR = "GitHub Inc."

    data class DependencyInfo(
        val name: String,
        val category: String,
        val vendor: String,
        val configuredVersion: String,
        val status: String,
        val isOfficialStable: Boolean = true
    )

    /**
     * Verifies that a version string represents an official stable release (no preview, alpha, beta, dev, rc, or snapshot).
     */
    fun isStable(version: String): Boolean {
        val lower = version.lowercase()
        return !lower.contains("alpha") &&
               !lower.contains("beta") &&
               !lower.contains("dev") &&
               !lower.contains("snapshot") &&
               !lower.contains("rc") &&
               !lower.contains("preview")
    }
}
