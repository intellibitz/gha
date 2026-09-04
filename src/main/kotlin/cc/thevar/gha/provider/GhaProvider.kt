package cc.thevar.gha.provider

import java.io.File

/**
 * Base interface for GHA Providers.
 * Supports the "Any Project, Anywhere" vision by decoupling logic from specific tools.
 */
interface GhaProvider {
    val name: String
    val version: String
    val vendor: String
    
    fun isAvailable(rootDir: File): Boolean
}

/**
 * Version Control System Provider (e.g., Git, SVN, Mercurial).
 */
interface GhaVcsProvider : GhaProvider {
    fun currentBranch(rootDir: File): String
    fun isDirty(rootDir: File): Boolean
    fun init(rootDir: File)
    fun commit(rootDir: File, message: String)
    fun push(rootDir: File, remote: String, branch: String)
    fun pull(rootDir: File, remote: String, branch: String, rebase: Boolean = true)
}

/**
 * Build System Provider (e.g., Gradle, Maven, Bazel, NPM).
 */
interface GhaBuildProvider : GhaProvider {
    fun build(rootDir: File)
    fun clean(rootDir: File)
    fun test(rootDir: File)
}

/**
 * Remote Platform Provider (e.g., GitHub, GitLab, Bitbucket).
 */
interface GhaRemoteProvider : GhaProvider {
    fun createRepository(rootDir: File, name: String, public: Boolean = true)
    fun createPullRequest(rootDir: File, base: String, head: String, title: String, body: String): String?
    fun mergePullRequest(rootDir: File, prNumber: Int, method: String = "squash")
    fun getPrStatus(rootDir: File, prNumber: Int): String
}
