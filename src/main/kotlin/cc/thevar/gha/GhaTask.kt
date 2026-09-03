package cc.thevar.gha

import cc.thevar.gha.safety.GhaSandboxManager
import cc.thevar.gha.security.GhaCredentialsResolver
import org.gradle.api.DefaultTask
import org.gradle.api.GradleException
import org.gradle.api.file.DirectoryProperty
import org.gradle.api.provider.Property
import org.gradle.api.tasks.InputDirectory
import org.gradle.api.tasks.Internal
import org.gradle.api.tasks.PathSensitive
import org.gradle.api.tasks.PathSensitivity
import org.gradle.work.DisableCachingByDefault

/**
 * Base task for GitHub Automation.
 * Enforces secure credential handling and non-leaking token policies across single and multi-subproject builds.
 */
@DisableCachingByDefault(because = "Base task for GitHub Automation")
abstract class GhaTask : DefaultTask() {

    /**
     * Marked @get:Internal so credentials are NEVER recorded in
     * task inputs, build scans, or configuration cache reports.
     */
    @get:Internal
    abstract val gitHubToken: Property<String>

    @get:InputDirectory
    @get:PathSensitive(PathSensitivity.RELATIVE)
    abstract val projectRootDir: DirectoryProperty

    @get:Internal
    abstract val gradleUserHomeDir: DirectoryProperty

    @get:Internal
    abstract val ghaProjectName: Property<String>

    init {
        gitHubToken.convention(
            GhaCredentialsResolver.resolveGitHubToken(project.providers)
        )
        // Resolves the root project directory for Git & GitHub operations across multi-module subprojects
        projectRootDir.convention(project.rootProject.layout.projectDirectory)
        ghaProjectName.convention(project.rootProject.name)
        val homeDir = project.gradle.gradleUserHomeDir
        gradleUserHomeDir.convention(project.layout.dir(project.providers.provider { homeDir }))
    }

    /**
     * Verifies that the task is running within the GHA Sandbox.
     * Self-heals by auto-creating .gha/gha.json if missing, then verifies sandbox rules.
     */
    fun verifySandbox() {
        val rootDir = projectRootDir.get().asFile
        val userHome = gradleUserHomeDir.get().asFile
        val pName = ghaProjectName.getOrElse("gha")

        // Self-healing: auto-ensure .gha/gha.json exists
        GhaSandboxManager.ensureSandbox(rootDir, pName)

        val (isHealthy, message) = GhaSandboxManager.healthCheck(rootDir, userHome)
        if (!isHealthy) {
            throw GradleException(
                "$message\n\n" +
                "To fix this:\n" +
                "1. Run './gradlew ghaInit' to initialize the sandbox.\n" +
                "2. Ensure you are running Gradle with '-Dgradle.user.home=.gha/gradle-user-home'."
            )
        }
    }

    /**
     * Safely returns a masked representation of the GitHub token for logging purposes.
     */
    protected fun maskedToken(): String {
        return GhaCredentialsResolver.maskToken(gitHubToken.orNull)
    }
}
