package cc.thevar.gha.provider

import cc.thevar.gha.safety.GhaProcessRunner
import java.io.File

class GitHubRemoteProvider(private val token: String) : GhaRemoteProvider {
    override val name = "GitHub"
    override val version = "Official Remote"
    override val vendor = "GitHub Inc."

    override fun isAvailable(rootDir: File): Boolean = 
        GhaProcessRunner.exec(rootDir, listOf("gh", "--version")).isSuccess

    override fun createRepository(rootDir: File, name: String, public: Boolean) {
        val env = mapOf("GITHUB_TOKEN" to token, "GH_TOKEN" to token)
        val visibility = if (public) "--public" else "--private"
        GhaProcessRunner.exec(rootDir, listOf("gh", "repo", "create", name, "--source=.", visibility, "--push"), extraEnv = env)
    }

    override fun createPullRequest(rootDir: File, base: String, head: String, title: String, body: String): String? {
        val env = mapOf("GITHUB_TOKEN" to token, "GH_TOKEN" to token)
        val res = GhaProcessRunner.exec(rootDir, listOf("gh", "pr", "create", "--base", base, "--head", head, "--title", title, "--body", body), extraEnv = env)
        return if (res.isSuccess) res.stdout.trim() else null
    }

    override fun mergePullRequest(rootDir: File, prNumber: Int, method: String) {
        val env = mapOf("GITHUB_TOKEN" to token, "GH_TOKEN" to token)
        val flag = "--$method"
        GhaProcessRunner.exec(rootDir, listOf("gh", "pr", "merge", prNumber.toString(), flag, "--delete-branch", "--auto"), extraEnv = env)
    }

    override fun getPrStatus(rootDir: File, prNumber: Int): String {
        val env = mapOf("GITHUB_TOKEN" to token, "GH_TOKEN" to token)
        val res = GhaProcessRunner.exec(rootDir, listOf("gh", "pr", "view", prNumber.toString(), "--json", "state", "--template", "{{.state}}"), extraEnv = env)
        return if (res.isSuccess) res.stdout.trim() else "UNKNOWN"
    }
}
