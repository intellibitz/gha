package cc.thevar.gha.security

import cc.thevar.gha.safety.GhaProcessRunner
import java.io.File

/**
 * Helper manager for querying, merging, closing, rebasing, and maintaining Dependabot PRs & remote branches.
 */
object GhaDependabotManager {

    data class DependabotPr(
        val number: Int,
        val title: String,
        val headBranch: String,
        val url: String
    )

    /**
     * Lists open Dependabot pull requests.
     */
    fun listDependabotPrs(projectDir: File, token: String?): List<DependabotPr> {
        val env = if (!token.isNullOrBlank()) mapOf("GITHUB_TOKEN" to token, "GH_TOKEN" to token) else emptyMap()

        val result = GhaProcessRunner.exec(
            workingDir = projectDir,
            command = listOf("gh", "pr", "list", "--search", "head:dependabot/", "--json", "number,title,headRefName,url"),
            extraEnv = env,
            timeoutSeconds = 30L
        )

        if (!result.isSuccess || result.stdout.isBlank()) return emptyList()

        return parsePrJsonList(result.stdout)
    }

    /**
     * Merges a Dependabot PR and deletes the remote branch.
     */
    fun mergeDependabotPr(projectDir: File, token: String?, prNumber: Int, method: String = "squash"): GhaProcessRunner.ProcessResult {
        val env = if (!token.isNullOrBlank()) mapOf("GITHUB_TOKEN" to token, "GH_TOKEN" to token) else emptyMap()
        val flag = when (method.lowercase()) {
            "rebase" -> "--rebase"
            "merge" -> "--merge"
            else -> "--squash"
        }

        return GhaProcessRunner.exec(
            workingDir = projectDir,
            command = listOf("gh", "pr", "merge", prNumber.toString(), flag, "--delete-branch", "--auto"),
            extraEnv = env,
            timeoutSeconds = 45L
        )
    }

    /**
     * Closes a Dependabot PR and deletes the remote branch.
     */
    fun closeDependabotPr(projectDir: File, token: String?, prNumber: Int, comment: String = "Closed by Maintainer via GHA"): GhaProcessRunner.ProcessResult {
        val env = if (!token.isNullOrBlank()) mapOf("GITHUB_TOKEN" to token, "GH_TOKEN" to token) else emptyMap()

        return GhaProcessRunner.exec(
            workingDir = projectDir,
            command = listOf("gh", "pr", "close", prNumber.toString(), "--comment", comment, "--delete-branch"),
            extraEnv = env,
            timeoutSeconds = 30L
        )
    }

    /**
     * Comments `@dependabot rebase` or `@dependabot recreate` on a Dependabot PR to fix conflicts or re-trigger builds.
     */
    fun rebaseDependabotPr(projectDir: File, token: String?, prNumber: Int, recreate: Boolean = false): GhaProcessRunner.ProcessResult {
        val env = if (!token.isNullOrBlank()) mapOf("GITHUB_TOKEN" to token, "GH_TOKEN" to token) else emptyMap()
        val body = if (recreate) "@dependabot recreate" else "@dependabot rebase"

        return GhaProcessRunner.exec(
            workingDir = projectDir,
            command = listOf("gh", "pr", "comment", prNumber.toString(), "--body", body),
            extraEnv = env,
            timeoutSeconds = 30L
        )
    }

    /**
     * Lists all remote dependabot/ branches.
     */
    fun listDependabotBranches(projectDir: File, token: String?): List<String> {
        val env = if (!token.isNullOrBlank()) mapOf("GITHUB_TOKEN" to token, "GH_TOKEN" to token) else emptyMap()
        
        val result = GhaProcessRunner.exec(
            workingDir = projectDir,
            command = listOf("gh", "api", "repos/:owner/:repo/git/matching-refs/heads/dependabot/"),
            extraEnv = env,
            timeoutSeconds = 30L
        )

        if (!result.isSuccess || result.stdout.isBlank()) return emptyList()

        // Extract "ref": "refs/heads/dependabot/..."
        val regex = "\"ref\":\"refs/heads/(dependabot/[^\"]+)\"".toRegex()
        return regex.findAll(result.stdout).map { it.groupValues[1] }.toList()
    }

    /**
     * Deletes a remote branch.
     */
    fun deleteRemoteBranch(projectDir: File, token: String?, branch: String): GhaProcessRunner.ProcessResult {
        val env = if (!token.isNullOrBlank()) mapOf("GITHUB_TOKEN" to token, "GH_TOKEN" to token) else emptyMap()
        
        return GhaProcessRunner.exec(
            workingDir = projectDir,
            command = listOf("gh", "api", "-X", "DELETE", "repos/:owner/:repo/git/refs/heads/$branch"),
            extraEnv = env,
            timeoutSeconds = 30L
        )
    }

    private fun parsePrJsonList(json: String): List<DependabotPr> {
        val list = mutableListOf<DependabotPr>()
        val regex = "\\{\"headRefName\":\"([^\"]+)\",\"number\":(\\d+),\"title\":\"([^\"]+)\",\"url\":\"([^\"]+)\"\\}".toRegex()

        regex.findAll(json).forEach { match ->
            val branch = match.groupValues[1]
            val num = match.groupValues[2].toIntOrNull() ?: 0
            val title = match.groupValues[3]
            val url = match.groupValues[4]
            if (num > 0) {
                list.add(DependabotPr(num, title, branch, url))
            }
        }
        return list
    }
}
