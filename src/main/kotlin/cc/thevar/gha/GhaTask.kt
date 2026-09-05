package cc.thevar.gha

import cc.thevar.gha.provider.GhaBuildProvider
import cc.thevar.gha.provider.GhaProviderRegistry
import cc.thevar.gha.provider.GhaRemoteProvider
import cc.thevar.gha.provider.GhaVcsProvider
import cc.thevar.gha.safety.GhaSandboxManager
import cc.thevar.gha.safety.GhaVersionManager
import cc.thevar.gha.security.GhaCredentialsResolver
import org.gradle.api.DefaultTask
import org.gradle.api.GradleException
import org.gradle.api.file.DirectoryProperty
import org.gradle.api.provider.Property
import org.gradle.api.tasks.Internal
import org.gradle.work.DisableCachingByDefault
import java.io.File

/**
 * Base task for GitHub Automation.
 * Enforces secure credential handling and non-leaking token policies across single and multi-subproject builds.
 */
@DisableCachingByDefault(because = "Base task for GitHub Automation")
abstract class GhaTask : DefaultTask() {

    @get:Internal
    abstract val gitHubToken: Property<String>

    @get:Internal
    abstract val projectRootDir: DirectoryProperty

    @get:Internal
    abstract val gradleUserHomeDir: DirectoryProperty

    @get:Internal
    abstract val ghaProjectName: Property<String>

    @get:Internal
    var taskRootDirFile: File = File(".")

    @get:Internal
    var taskGitHubToken: String = ""

    @get:Internal
    var taskGradleUserHomeDirFile: File = File(".")

    @get:Internal
    var taskProjectNameStr: String = "gha"

    /**
     * Lazy-initialized Version Control System Provider.
     */
    protected fun vcs(): GhaVcsProvider {
        return GhaProviderRegistry.getVcsProvider(taskRootDirFile)
    }

    /**
     * Lazy-initialized Build System Provider.
     */
    protected fun build(): GhaBuildProvider {
        return GhaProviderRegistry.getBuildProvider(taskRootDirFile)
    }

    /**
     * Lazy-initialized Remote Platform Provider.
     */
    protected fun remote(): GhaRemoteProvider {
        return GhaProviderRegistry.getRemoteProvider(taskRootDirFile, resolveToken())
    }

    init {
        // Properties configured via GhaPlugin.configureEach for Configuration Cache safety
    }

    /**
     * Safely resolves the active GitHub token without Configuration Cache violations.
     */
    protected fun resolveToken(): String {
        val t = gitHubToken.orNull ?: ""
        if (t.isNotBlank()) return t
        return GhaCredentialsResolver.resolveDirectToken(projectRootDir.get().asFile)
    }

    /**
     * Verifies that the task is running within the GHA Sandbox.
     * Self-heals by auto-creating .gha/gha.json if missing, then verifies sandbox rules.
     */
    fun verifySandbox(silent: Boolean = false) {
        val rootDir = projectRootDir.get().asFile
        val userHome = gradleUserHomeDir.get().asFile
        val pName = ghaProjectName.getOrElse("gha")

        // Print GHA Version Header for every run (0 Effort, 100% Gain)
        if (!silent) {
            val version = GhaVersionManager.getEngineVersion(rootDir)
            logger.lifecycle("🤖 [gha] Engine Version: $version (Sandboxed)")
        }

        // Self-healing: auto-ensure sandbox integrity
        GhaSandboxManager.selfHeal(rootDir, pName)
        GhaSandboxManager.ensureGlobalSandbox()

        val (isHealthy, message) = GhaSandboxManager.healthCheck(rootDir, userHome)
        if (!isHealthy) {
            throw GradleException(
                "$message\n\n" +
                "🤖 [gha Portability Guard] gha is designed to be 100% portable.\n" +
                "To restore its promised '0 effort, 100% gain' state:\n" +
                "1. Run './ghai' to trigger autonomous self-healing and sandbox restoration.\n" +
                "2. Ensure you are executing via the './ghai' launcher which enforces the sandbox."
            )
        }
    }

    /**
     * Safely returns a masked representation of the GitHub token for logging purposes.
     */
    protected fun maskedToken(): String {
        return GhaCredentialsResolver.maskToken(resolveToken())
    }
}
