package cc.thevar.gha.config

/**
 * Single source of truth for GHA tools, SDKs, frameworks, and dependency versions.
 */
object GhaConfig {
    const val JAVA_VERSION = "17"
    const val KOTLIN_VERSION = "2.1.0"
    const val GRADLE_VERSION = "9.7.1"
    const val PLUGIN_PUBLISH_VERSION = "1.3.1"
    const val FOOJAY_RESOLVER_VERSION = "0.9.0"

    data class DependencyInfo(
        val name: String,
        val category: String,
        val configuredVersion: String,
        val status: String
    )
}
