package cc.thevar.gha

import cc.thevar.gha.security.GhaCredentialsResolver
import org.gradle.api.DefaultTask
import org.gradle.api.provider.Property
import org.gradle.api.tasks.Internal
import org.gradle.work.DisableCachingByDefault

/**
 * Base task for GitHub Automation.
 * Enforces secure credential handling and non-leaking token policies.
 */
@DisableCachingByDefault(because = "Base task for GitHub Automation")
abstract class GhaTask : DefaultTask() {

    /**
     * Marked @get:Internal so credentials are NEVER recorded in
     * task inputs, build scans, or configuration cache reports.
     */
    @get:Internal
    abstract val gitHubToken: Property<String>

    init {
        gitHubToken.convention(
            GhaCredentialsResolver.resolveGitHubToken(project.providers)
        )
    }

    /**
     * Safely returns a masked representation of the GitHub token for logging purposes.
     */
    protected fun maskedToken(): String {
        return GhaCredentialsResolver.maskToken(gitHubToken.orNull)
    }
}
