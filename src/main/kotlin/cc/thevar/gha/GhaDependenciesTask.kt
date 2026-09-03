package cc.thevar.gha

import cc.thevar.gha.config.GhaConfig
import cc.thevar.gha.git.GhaGitExec
import cc.thevar.gha.safety.GhaProcessRunner
import org.gradle.api.file.DirectoryProperty
import org.gradle.api.provider.Property
import org.gradle.api.tasks.Input
import org.gradle.api.tasks.InputDirectory
import org.gradle.api.tasks.PathSensitive
import org.gradle.api.tasks.PathSensitivity
import org.gradle.api.tasks.TaskAction
import org.gradle.work.DisableCachingByDefault

@DisableCachingByDefault(because = "Prints real-time dependency and toolchain versions")
abstract class GhaDependenciesTask : GhaTask() {

    @get:InputDirectory
    @get:PathSensitive(PathSensitivity.RELATIVE)
    abstract val repoDir: DirectoryProperty

    @get:Input
    abstract val gradleVersion: Property<String>

    init {
        repoDir.convention(project.layout.projectDirectory)
        gradleVersion.convention(project.gradle.gradleVersion)
    }

    @TaskAction
    fun execute() {
        val dir = repoDir.get().asFile

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
            GhaConfig.DependencyInfo("Java / JDK Toolchain", "SDK", GhaConfig.JAVA_VERSION, System.getProperty("java.version")),
            GhaConfig.DependencyInfo("Kotlin Language & DSL", "Framework", GhaConfig.KOTLIN_VERSION, KotlinVersion.CURRENT.toString()),
            GhaConfig.DependencyInfo("Gradle Build Engine", "Build Tool", GhaConfig.GRADLE_VERSION, gradleVersion.get()),
            GhaConfig.DependencyInfo("Gradle Plugin Publish", "Plugin", GhaConfig.PLUGIN_PUBLISH_VERSION, "Active (${GhaConfig.PLUGIN_PUBLISH_VERSION})"),
            GhaConfig.DependencyInfo("Foojay JDK Resolver", "Plugin", GhaConfig.FOOJAY_RESOLVER_VERSION, "Active (${GhaConfig.FOOJAY_RESOLVER_VERSION})"),
            GhaConfig.DependencyInfo("Git VCS Engine", "CLI Tool", "Latest Compatible", gitVersion),
            GhaConfig.DependencyInfo("GitHub CLI (gh)", "CLI Tool", "Latest Compatible", ghVersion)
        )

        logger.lifecycle("📦 [GHA Dependencies & Toolchains]")
        logger.lifecycle("┌" + "─".repeat(26) + "┬" + "─".repeat(14) + "┬" + "─".repeat(18) + "┬" + "─".repeat(30) + "┐")
        logger.lifecycle("│ %-24s │ %-12s │ %-16s │ %-28s │".format("Tool / Dependency", "Category", "Configured", "Runtime / Active"))
        logger.lifecycle("├" + "─".repeat(26) + "┼" + "─".repeat(14) + "┼" + "─".repeat(18) + "┼" + "─".repeat(30) + "┤")

        dependencies.forEach { dep ->
            logger.lifecycle("│ %-24s │ %-12s │ %-16s │ %-28s │".format(dep.name, dep.category, dep.configuredVersion, dep.status))
        }

        logger.lifecycle("└" + "─".repeat(26) + "┴" + "─".repeat(14) + "┴" + "─".repeat(18) + "┴" + "─".repeat(30) + "┘")
    }
}
