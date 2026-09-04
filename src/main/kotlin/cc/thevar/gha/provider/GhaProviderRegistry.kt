package cc.thevar.gha.provider

import java.io.File

/**
 * Registry for GHA Providers.
 * Centralizes provider discovery and allows for future pluggable architecture.
 */
object GhaProviderRegistry {

    fun getVcsProvider(rootDir: File): GhaVcsProvider {
        // Future: Logic to detect SVN, Mercurial, etc.
        return GitVcsProvider()
    }

    fun getBuildProvider(rootDir: File): GhaBuildProvider {
        // Future: Logic to detect Maven, Bazel, etc.
        return GradleBuildProvider()
    }

    fun getRemoteProvider(rootDir: File, token: String): GhaRemoteProvider {
        // Future: Logic to detect GitLab, Bitbucket, etc.
        return GitHubRemoteProvider(token)
    }
}
