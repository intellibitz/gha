package cc.thevar.gha.security

import org.gradle.api.provider.Provider
import org.gradle.api.provider.ProviderFactory
import org.gradle.api.provider.ValueSource
import org.gradle.api.provider.ValueSourceParameters
import org.gradle.process.ExecOperations
import java.io.ByteArrayOutputStream
import javax.inject.Inject

/**
 * ValueSource implementation to safely execute `gh auth token` with Configuration Cache support.
 */
abstract class GhAuthTokenValueSource : ValueSource<String, ValueSourceParameters.None> {

    @get:Inject
    abstract val execOperations: ExecOperations

    override fun obtain(): String? {
        return try {
            val outputStream = ByteArrayOutputStream()
            val result = execOperations.exec {
                commandLine("gh", "auth", "token")
                standardOutput = outputStream
                isIgnoreExitValue = true
            }
            if (result.exitValue == 0) {
                val token = outputStream.toString(Charsets.UTF_8.name()).trim()
                if (token.isNotBlank()) token else null
            } else {
                null
            }
        } catch (_: Exception) {
            null
        }
    }
}

/**
 * Secure credential provider for GitHub Automation.
 * Resolves Git/GitHub credentials dynamically without persisting or logging raw secrets.
 */
object GhaCredentialsResolver {

    /**
     * Safely resolves the GitHub token in order of precedence:
     * 1. Environment variable: GITHUB_TOKEN
     * 2. Environment variable: GH_TOKEN
     * 3. Gradle property: gha.github.token
     * 4. System GitHub CLI (`gh auth token`) via Gradle ValueSource
     */
    fun resolveGitHubToken(providers: ProviderFactory): Provider<String> {
        return providers.environmentVariable("GITHUB_TOKEN")
            .orElse(providers.environmentVariable("GH_TOKEN"))
            .orElse(providers.gradleProperty("gha.github.token"))
            .orElse(providers.of(GhAuthTokenValueSource::class.java) {})
    }

    /**
     * Masks sensitive tokens so they never leak in console logs, task inputs, or build reports.
     * Example: "ghp_1234567890abcdef" -> "ghp_...cdef"
     */
    fun maskToken(token: String?): String {
        if (token.isNullOrBlank()) return "<not configured>"
        return if (token.length > 8) {
            "${token.take(4)}...${token.takeLast(4)}"
        } else {
            "****"
        }
    }
}
