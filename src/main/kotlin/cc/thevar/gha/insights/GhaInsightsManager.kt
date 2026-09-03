package cc.thevar.gha.insights

import cc.thevar.gha.git.GhaGitExec
import cc.thevar.gha.safety.GhaProcessRunner
import java.io.File

/**
 * Manager for retrieving and formatting GitHub Insights, repository traffic,
 * contributor metrics, and commit activity statistics.
 */
object GhaInsightsManager {

    data class RepoOverview(
        val name: String,
        val stars: Int,
        val forks: Int,
        val watchers: Int,
        val openIssues: Int,
        val defaultBranch: String
    )

    /**
     * Resolves owner and repository name from local `origin` git remote URL.
     * Example: "https://github.com/intellibitz/gha.git" -> "intellibitz/gha"
     */
    fun resolveOwnerAndRepo(projectDir: File): String? {
        val result = GhaGitExec.exec(projectDir, "config", "--get", "remote.origin.url")
        if (!result.isSuccess || result.stdout.isBlank()) {
            return null
        }

        val rawUrl = result.stdout.trim()
            .removeSuffix(".git")
            .removeSuffix("/")

        return when {
            rawUrl.startsWith("https://github.com/") -> rawUrl.removePrefix("https://github.com/")
            rawUrl.startsWith("git@github.com:") -> rawUrl.removePrefix("git@github.com:")
            else -> null
        }
    }

    /**
     * Fetches repository overview metrics from GitHub API using `gh api repos/{owner}/{repo}`.
     */
    fun fetchRepoOverview(projectDir: File, token: String?): RepoOverview? {
        val ownerRepo = resolveOwnerAndRepo(projectDir) ?: return null
        val env = if (!token.isNullOrBlank()) mapOf("GITHUB_TOKEN" to token, "GH_TOKEN" to token) else emptyMap()

        val result = GhaProcessRunner.exec(
            workingDir = projectDir,
            command = listOf("gh", "api", "repos/$ownerRepo"),
            extraEnv = env,
            timeoutSeconds = 20L
        )

        if (!result.isSuccess) return null

        val json = result.stdout
        return RepoOverview(
            name = ownerRepo,
            stars = extractJsonInt(json, "stargazers_count"),
            forks = extractJsonInt(json, "forks_count"),
            watchers = extractJsonInt(json, "subscribers_count"),
            openIssues = extractJsonInt(json, "open_issues_count"),
            defaultBranch = extractJsonString(json, "default_branch") ?: "main"
        )
    }

    /**
     * Fetches top contributors using local `git shortlog -sn --no-merges`.
     */
    fun fetchContributors(projectDir: File): List<Pair<String, Int>> {
        val result = GhaGitExec.exec(projectDir, "shortlog", "-sn", "--no-merges", "HEAD")
        if (!result.isSuccess || result.stdout.isBlank()) return emptyList()

        return result.stdout.lines().mapNotNull { line ->
            val trimmed = line.trim()
            val parts = trimmed.split("\\s+".toRegex(), limit = 2)
            if (parts.size == 2) {
                val count = parts[0].toIntOrNull() ?: 0
                val author = parts[1]
                Pair(author, count)
            } else null
        }
    }

    /**
     * Fetches repository traffic clone statistics using `gh api repos/{owner}/{repo}/traffic/clones`.
     */
    fun fetchClonesTraffic(projectDir: File, token: String?): String {
        val ownerRepo = resolveOwnerAndRepo(projectDir) ?: return "Remote repository not configured."
        val env = if (!token.isNullOrBlank()) mapOf("GITHUB_TOKEN" to token, "GH_TOKEN" to token) else emptyMap()

        val result = GhaProcessRunner.exec(
            workingDir = projectDir,
            command = listOf("gh", "api", "repos/$ownerRepo/traffic/clones"),
            extraEnv = env,
            timeoutSeconds = 20L
        )

        return if (result.isSuccess) {
            val count = extractJsonInt(result.stdout, "count")
            val uniques = extractJsonInt(result.stdout, "uniques")
            "Clones (last 14 days): $count total ($uniques unique visitors)"
        } else {
            "Clones statistics require push access to repository."
        }
    }

    /**
     * Fetches repository traffic view statistics using `gh api repos/{owner}/{repo}/traffic/views`.
     */
    fun fetchViewsTraffic(projectDir: File, token: String?): String {
        val ownerRepo = resolveOwnerAndRepo(projectDir) ?: return "Remote repository not configured."
        val env = if (!token.isNullOrBlank()) mapOf("GITHUB_TOKEN" to token, "GH_TOKEN" to token) else emptyMap()

        val result = GhaProcessRunner.exec(
            workingDir = projectDir,
            command = listOf("gh", "api", "repos/$ownerRepo/traffic/views"),
            extraEnv = env,
            timeoutSeconds = 20L
        )

        return if (result.isSuccess) {
            val count = extractJsonInt(result.stdout, "count")
            val uniques = extractJsonInt(result.stdout, "uniques")
            "Page Views (last 14 days): $count total ($uniques unique visitors)"
        } else {
            "Views statistics require push access to repository."
        }
    }

    private fun extractJsonInt(json: String, key: String): Int {
        val regex = "\"$key\":\\s*(\\d+)".toRegex()
        val match = regex.find(json)
        return match?.groupValues?.get(1)?.toIntOrNull() ?: 0
    }

    private fun extractJsonString(json: String, key: String): String? {
        val regex = "\"$key\":\\s*\"([^\"]+)\"".toRegex()
        val match = regex.find(json)
        return match?.groupValues?.get(1)
    }
}
