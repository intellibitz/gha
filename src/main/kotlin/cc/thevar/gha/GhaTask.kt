package cc.thevar.gha

import cc.thevar.gha.safety.GhaSandboxManager
import cc.thevar.gha.security.GhaCredentialsResolver
import org.gradle.api.DefaultTask
import org.gradle.api.GradleException
import org.gradle.api.tasks.Internal
import org.gradle.work.DisableCachingByDefault
import java.io.File

/**
 * Base task for GitHub Automation.
 * Enforces secure credential handling and non-leaking token policies with 100% Configuration Cache support.
 */
@DisableCachingByDefault(because = "Base task for GitHub Automation")
abstract class GhaTask : DefaultTask() {

    @get:Internal
    var taskRootDirFile: File = File(".")

    @get:Internal
    var taskGitHubToken: String = ""

    @get:Internal
    var taskGradleUserHomeDirFile: File = File(".")

    @get:Internal
    var taskProjectNameStr: String = "gha"

    /**
     * Safely resolves the active GitHub token without Configuration Cache violations.
     */
    protected fun resolveToken(): String {
        if (taskGitHubToken.isNotBlank()) return taskGitHubToken
        return GhaCredentialsResolver.resolveDirectToken(taskRootDirFile)
    }

    /**
     * Verifies that the task is running within the GHA Sandbox.
     * Self-heals by auto-creating .gha/gha.json if missing, then verifies sandbox rules.
     */
    fun verifySandbox() {
        val rootDir = taskRootDirFile
        val userHome = taskGradleUserHomeDirFile
        val pName = taskProjectNameStr

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
        return GhaCredentialsResolver.maskToken(resolveToken())
    }
}
