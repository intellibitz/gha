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
        if (!userProvidedMessage.isNullOrBlank() && !userProvidedMessage.contains("autonomous", ignoreCase = true)) {
            return userProvidedMessage
        }

        val statusResult = GhaGitExec.exec(projectDir, "status", "--porcelain")
        if (!statusResult.isSuccess || statusResult.stdout.isBlank()) {
            val localFiles = projectDir.listFiles()?.filter { !it.name.startsWith(".") && it.name != "ghai" } ?: emptyList()
            return if (localFiles.isEmpty()) "chore: initialize project with gha automation" else "chore: automated sync via ghai"
        }
        
        // ...

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
            command = listOf(
                "gh", "pr", "view", prNumber.toString(),
                "--json", "state,mergeable,statusCheckRollup",
                "--template", "{{.state}}|{{.mergeable}}|{{range .statusCheckRollup}}{{.conclusion}}|{{.status}},{{end}}",
            ),
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

        val parts = result.stdout.trim().split("|")
        val state = parts.getOrNull(0) ?: "OPEN"
        val mergeableStr = parts.getOrNull(1) ?: "MERGEABLE"
        val checksStr = parts.getOrNull(2) ?: ""

        val isMergeable = mergeableStr == "MERGEABLE"
        val checkEntries = checksStr.split(",").filter { it.isNotBlank() }

        val ciStatus = when {
            checkEntries.isEmpty() -> CiStatus.NO_CHECKS
            checkEntries.any { it.startsWith("FAILURE") || it.startsWith("CANCELLED") || it.startsWith("STARTUP_FAILURE") } -> CiStatus.FAILED
            checkEntries.any { it.endsWith("IN_PROGRESS") || it.endsWith("QUEUED") || it.endsWith("PENDING") } -> CiStatus.PENDING
            checkEntries.all { it.startsWith("SUCCESS") || it.startsWith("NEUTRAL") || it.startsWith("SKIPPED") } -> CiStatus.PASSED
            else -> CiStatus.PENDING
        }

        return PrCiStatus(prNumber, state, isMergeable, ciStatus, "State: $state, CI: $ciStatus, Mergeable: $isMergeable")
    }

    /**
     * Checks if local branch is ahead of remote base branch.
     */
    fun isBranchAheadOfRemote(projectDir: File, baseBranch: String): Boolean {
        val res = GhaGitExec.exec(projectDir, "log", "origin/$baseBranch..HEAD", "--oneline")
        return res.isSuccess && res.stdout.isNotBlank()
    }

    /**
     * Detects the type and state of the project for better automation decisions.
     */
    fun detectProjectContext(projectDir: File): String {
        val buildKts = File(projectDir, "build.gradle.kts")
        val buildGroovy = File(projectDir, "build.gradle")
        val settingsKts = File(projectDir, "settings.gradle.kts")
        val hasGradle = buildKts.exists() || buildGroovy.exists() || settingsKts.exists()
        
        val srcDir = File(projectDir, "src")
        val hasSrc = srcDir.exists() && srcDir.isDirectory
        
        val hasKotlin = hasSrc && projectDir.walk().any { it.extension == "kt" }
        val hasAndroid = projectDir.walk().any { it.name == "AndroidManifest.xml" }
        
        val localFiles = projectDir.listFiles()?.filter { 
            !it.name.startsWith(".") && it.name != "ghai" && it.name != "init" && it.name != "gradlew"
        } ?: emptyList()

        return when {
            hasAndroid -> "Android Mobile Project"
            hasKotlin -> "Kotlin Multiplatform/JVM Project"
            hasGradle -> "Gradle Automation Project"
            localFiles.isEmpty() -> "Empty Folder (New Project Seed)"
            else -> "Generic File Project"
        }
    }
}
