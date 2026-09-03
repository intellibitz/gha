package cc.thevar.gha

import cc.thevar.gha.config.GhaConfig
import cc.thevar.gha.git.GhaGitExec
import cc.thevar.gha.safety.GhaProcessRunner
import org.gradle.api.provider.Property
import org.gradle.api.tasks.Input
import org.gradle.api.tasks.TaskAction
import org.gradle.work.DisableCachingByDefault

@DisableCachingByDefault(because = "Prints real-time dependency and toolchain versions from trusted vendors")
abstract class GhaDependenciesTask : GhaTask() {

    @get:Input
    abstract val gradleVersion: Property<String>

    init {
        gradleVersion.convention(project.gradle.gradleVersion)
    }

    @TaskAction
    fun execute() {
        val dir = projectRootDir.get().asFile

        // Detect Git version
        val gitResult = GhaGitExec.exec(dir, "version")
        val gitVersion = if (gitResult.isSuccess) gitResult.stdout else "Not found"

        // Detect GitHub CLI version
        val ghResult = GhaProcessRunner.exec(dir, listOf("gh", "version"))
        val ghVersion = if (ghResult.isSuccess) {
            ghResult.stdout.lines().firstOrNull() ?: "Detected"
        } else {
            "Not found"
        }

        val dependencies = listOf(
            GhaConfig.DependencyInfo("Java / JDK Toolchain", "SDK", GhaConfig.JAVA_VENDOR, GhaConfig.JAVA_VERSION, System.getProperty("java.version"), GhaConfig.isStable(System.getProperty("java.version"))),
            GhaConfig.DependencyInfo("Kotlin Language & DSL", "Framework", GhaConfig.KOTLIN_VENDOR, GhaConfig.KOTLIN_VERSION, KotlinVersion.CURRENT.toString(), GhaConfig.isStable(KotlinVersion.CURRENT.toString())),
            GhaConfig.DependencyInfo("Gradle Build Engine", "Build Tool", GhaConfig.GRADLE_VENDOR, GhaConfig.GRADLE_VERSION, gradleVersion.get(), GhaConfig.isStable(gradleVersion.get())),
            GhaConfig.DependencyInfo("Gradle Plugin Publish", "Plugin", GhaConfig.PLUGIN_PUBLISH_VENDOR, GhaConfig.PLUGIN_PUBLISH_VERSION, "Active (${GhaConfig.PLUGIN_PUBLISH_VERSION})", GhaConfig.isStable(GhaConfig.PLUGIN_PUBLISH_VERSION)),
            GhaConfig.DependencyInfo("Foojay JDK Resolver", "Plugin", GhaConfig.FOOJAY_RESOLVER_VENDOR, GhaConfig.FOOJAY_RESOLVER_VERSION, "Active (${GhaConfig.FOOJAY_RESOLVER_VERSION})", GhaConfig.isStable(GhaConfig.FOOJAY_RESOLVER_VERSION)),
            GhaConfig.DependencyInfo("Git VCS Engine", "CLI Tool", GhaConfig.GIT_VENDOR, "Latest Compatible", gitVersion, GhaConfig.isStable(gitVersion)),
            GhaConfig.DependencyInfo("GitHub CLI (gh)", "CLI Tool", GhaConfig.GH_CLI_VENDOR, "Latest Compatible", ghVersion, GhaConfig.isStable(ghVersion)),
        )

        println("🔒 [GHA Trusted Vendors & Official Stable Dependencies]")
        println("┌" + "─".repeat(24) + "┬" + "─".repeat(12) + "┬" + "─".repeat(30) + "┬" + "─".repeat(14) + "┬" + "─".repeat(28) + "┬" + "─".repeat(22) + "┐")
        println("│ %-22s │ %-10s │ %-28s │ %-12s │ %-26s │ %-20s │".format("Tool / Dependency", "Category", "Trusted Vendor", "Configured", "Runtime / Active", "Status"))
        println("├" + "─".repeat(24) + "┼" + "─".repeat(12) + "┼" + "─".repeat(30) + "┼" + "─".repeat(14) + "┼" + "─".repeat(28) + "┼" + "─".repeat(22) + "┤")

        dependencies.forEach { dep ->
            val statusLabel = if (dep.isOfficialStable) "✅ Official Stable" else "⚠️ Unstable / Preview"
            println("│ %-22s │ %-10s │ %-28s │ %-12s │ %-26s │ %-20s │".format(dep.name, dep.category, dep.vendor, dep.configuredVersion, dep.status, statusLabel))
        }

        println("└" + "─".repeat(24) + "┴" + "─".repeat(12) + "┴" + "─".repeat(30) + "┴" + "─".repeat(14) + "┴" + "─".repeat(28) + "┴" + "─".repeat(22) + "┘")
    }
}
