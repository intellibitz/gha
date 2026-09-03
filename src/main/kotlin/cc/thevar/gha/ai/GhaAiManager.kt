package cc.thevar.gha.ai

import cc.thevar.gha.git.GhaGitExec
import cc.thevar.gha.safety.GhaProcessRunner
import java.io.File

/**
 * Autonomous AI Context Engine for zero-effort, 100% gain developer workflows.
 * Analyzes project state, working tree diffs, branch context, CI checks, and remote sync state.
 */
object GhaAiManager {

    enum class CiStatus {
        PASSED, PENDING, FAILED, NO_CHECKS
    }

    data class PrCiStatus(
        val prNumber: Int,
        val state: String,
        val isMergeable: Boolean,
        val ciStatus: CiStatus,
        val rawSummary: String,
    )

    /**
     * Inspects staged and unstaged files in the git repository to generate a smart, semantic commit message.
     */
    fun detectSmartCommitMessage(projectDir: File, userProvidedMessage: String?): String {
        if (!userProvidedMessage.isNullOrBlank()) {
            return userProvidedMessage
        }

        val statusResult = GhaGitExec.exec(projectDir, "status", "--porcelain")
        if (!statusResult.isSuccess || statusResult.stdout.isBlank()) {
            return "chore: automated sync via ghai"
        }

        val changedFiles = statusResult.stdout.lines().mapNotNull { line ->
            if (line.length > 3) line.substring(3).trim() else null
        }

        val hasBuild = changedFiles.any { it.endsWith(".gradle.kts") || it.contains("gradle/") || it.endsWith(".toml") }
        val hasDocs = changedFiles.any { it.endsWith(".md") || it.startsWith("wiki/") || it.startsWith("docs/") }
        val hasGit = changedFiles.any { it.contains("/git/") }
        val hasGitHub = changedFiles.any { it.contains("/github/") || it.contains("/workflow/") || it.contains("/ai/") }
        val hasSecurity = changedFiles.any { it.contains("/security/") }
        val hasTests = changedFiles.any { it.contains("Test") || it.startsWith("src/test/") }

        return when {
            (hasBuild && changedFiles.size == 1) -> "build: update Gradle project configuration"
            (hasDocs && !hasBuild && !hasGit && !hasGitHub) -> "docs: update documentation and project wiki"
            (hasGit && !hasGitHub) -> "feat(git): update Git automation engine"
            hasGitHub -> "feat(github): update GitHub automation workflows and ghai AI engine"
            hasSecurity -> "security: update security policies and dependabot tasks"
            hasTests -> "test: update project test suite"
            else -> {
                val primaryFile = changedFiles.firstOrNull()?.substringAfterLast('/') ?: "project"
                "feat: update $primaryFile and related components"
            }
        }
    }

    /**
     * Queries GitHub for a PR's combined CI/CD status and mergeability.
     */
    fun checkPrCiStatus(projectDir: File, token: String?, prNumber: Int): PrCiStatus {
        val env = if (!token.isNullOrBlank()) mapOf("GITHUB_TOKEN" to token, "GH_TOKEN" to token) else emptyMap()

        val result = GhaProcessRunner.exec(
            workingDir = projectDir,
            command = listOf("gh", "pr", "view", prNumber.toString(), "--json", "state,mergeable,statusCheckRollup"),
            extraEnv = env,
            timeoutSeconds = 30L,
        )

        if (!result.isSuccess || result.stdout.isBlank()) {
            return PrCiStatus(
                prNumber = prNumber,
                state = "OPEN",
                isMergeable = true,
                ciStatus = CiStatus.NO_CHECKS,
                rawSummary = "No check details available",
            )
        }

        val json = result.stdout
        val stateMatch = "\"state\":\"([^\"]+)\"".toRegex().find(json)?.groupValues?.get(1) ?: "OPEN"
        val mergeable = json.contains("\"mergeable\":\"MERGEABLE\"") || !json.contains("\"mergeable\":\"CONFLICTING\"")

        val hasFailure = json.contains("\"state\":\"FAILURE\"") || json.contains("\"conclusion\":\"FAILURE\"")
        val hasPending = json.contains("\"state\":\"PENDING\"") || json.contains("\"status\":\"IN_PROGRESS\"") || json.contains("\"status\":\"QUEUED\"")
        val hasSuccess = json.contains("\"state\":\"SUCCESS\"") || json.contains("\"conclusion\":\"SUCCESS\"")

        val ci = when {
            hasFailure -> CiStatus.FAILED
            hasPending -> CiStatus.PENDING
            hasSuccess -> CiStatus.PASSED
            else -> CiStatus.NO_CHECKS
        }

        return PrCiStatus(prNumber, stateMatch, mergeable, ci, "State: $stateMatch, CI: $ci, Mergeable: $mergeable")
    }

    /**
     * Checks if local branch is ahead of remote base branch.
     */
    fun isBranchAheadOfRemote(projectDir: File, baseBranch: String): Boolean {
        val res = GhaGitExec.exec(projectDir, "log", "origin/$baseBranch..HEAD", "--oneline")
        return res.isSuccess && res.stdout.isNotBlank()
    }
}
