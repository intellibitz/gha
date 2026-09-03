package cc.thevar.gha.ai

import cc.thevar.gha.git.GhaGitExec
import java.io.File

/**
 * Autonomous AI Context Engine for zero-effort, 100% gain developer workflows.
 * Analyzes project state, working tree diffs, branch context, and remote sync state.
 */
object GhaAiManager {

    /**
     * Inspects staged and unstaged files in the git repository to generate a smart, semantic commit message.
     */
    fun detectSmartCommitMessage(projectDir: File, userProvidedMessage: String?): String {
        if (!userProvidedMessage.isNullOrBlank()) {
            return userProvidedMessage
        }

        val statusResult = GhaGitExec.exec(projectDir, "status", "--porcelain")
        if (!statusResult.isSuccess || statusResult.stdout.isBlank()) {
            return "chore: automated sync via ghaAI"
        }

        val changedFiles = statusResult.stdout.lines().mapNotNull { line ->
            if (line.length > 3) line.substring(3).trim() else null
        }

        val hasBuild = changedFiles.any { it.endsWith(".gradle.kts") || it.contains("gradle/") || it.endsWith(".toml") }
        val hasDocs = changedFiles.any { it.endsWith(".md") || it.startsWith("wiki/") || it.startsWith("docs/") }
        val hasGit = changedFiles.any { it.contains("/git/") }
        val hasGitHub = changedFiles.any { it.contains("/github/") || it.contains("/workflow/") }
        val hasSecurity = changedFiles.any { it.contains("/security/") }
        val hasTests = changedFiles.any { it.contains("Test") || it.startsWith("src/test/") }

        return when {
            hasBuild && changedFiles.size == 1 -> "build: update Gradle project configuration"
            hasDocs && !hasBuild && !hasGit && !hasGitHub -> "docs: update documentation and project wiki"
            hasGit && !hasGitHub -> "feat(git): update Git automation engine"
            hasGitHub -> "feat(github): update GitHub automation workflows"
            hasSecurity -> "security: update security policies and dependabot tasks"
            hasTests -> "test: update project test suite"
            else -> {
                val primaryFile = changedFiles.firstOrNull()?.substringAfterLast('/') ?: "project"
                "feat: update $primaryFile and related components"
            }
        }
    }

    /**
     * Summary of working tree changes for AI reporting.
     */
    fun summarizeChanges(projectDir: File): String {
        val res = GhaGitExec.exec(projectDir, "status", "--short")
        return if (res.isSuccess && res.stdout.isNotBlank()) {
            res.stdout.lines().take(10).joinToString("\n") { "   $it" }
        } else {
            "   (no uncommitted changes)"
        }
    }
}
