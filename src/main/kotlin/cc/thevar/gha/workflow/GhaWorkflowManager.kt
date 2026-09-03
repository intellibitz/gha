package cc.thevar.gha.workflow

import cc.thevar.gha.insights.GhaInsightsManager
import cc.thevar.gha.safety.GhaProcessRunner
import java.io.File

/**
 * Manager for listing, inspecting, cancelling, and cleaning up GitHub Actions workflow runs.
 */
object GhaWorkflowManager {

    data class WorkflowRun(
        val databaseId: String,
        val name: String,
        val status: String,
        val conclusion: String,
        val createdAt: String
    )

    /**
     * Lists recent workflow runs.
     */
    fun listWorkflowRuns(projectDir: File, token: String?, limit: Int = 30): GhaProcessRunner.ProcessResult {
        val env = if (!token.isNullOrBlank()) mapOf("GITHUB_TOKEN" to token, "GH_TOKEN" to token) else emptyMap()

        return GhaProcessRunner.exec(
            workingDir = projectDir,
            command = listOf("gh", "run", "list", "--limit", limit.toString()),
            extraEnv = env,
            timeoutSeconds = 30L
        )
    }

    /**
     * Deletes a specific completed or cancelled workflow run by databaseId via REST API.
     */
    fun deleteWorkflowRun(projectDir: File, token: String?, runId: String): GhaProcessRunner.ProcessResult {
        val ownerRepo = GhaInsightsManager.resolveOwnerAndRepo(projectDir)
            ?: return GhaProcessRunner.ProcessResult(-1, "", "Could not resolve owner/repo for workflow cleanup.")

        val env = if (!token.isNullOrBlank()) mapOf("GITHUB_TOKEN" to token, "GH_TOKEN" to token) else emptyMap()

        return GhaProcessRunner.exec(
            workingDir = projectDir,
            command = listOf("gh", "api", "-X", "DELETE", "repos/$ownerRepo/actions/runs/$runId"),
            extraEnv = env,
            timeoutSeconds = 30L
        )
    }

    /**
     * Cancels an in-progress workflow run.
     */
    fun cancelWorkflowRun(projectDir: File, token: String?, runId: String): GhaProcessRunner.ProcessResult {
        val env = if (!token.isNullOrBlank()) mapOf("GITHUB_TOKEN" to token, "GH_TOKEN" to token) else emptyMap()

        return GhaProcessRunner.exec(
            workingDir = projectDir,
            command = listOf("gh", "run", "cancel", runId),
            extraEnv = env,
            timeoutSeconds = 30L
        )
    }

    /**
     * Fetches workflow runs list using gh template for robust parsing.
     */
    fun fetchWorkflowRunsList(projectDir: File, token: String?, limit: Int = 100): List<WorkflowRun> {
        val env = if (!token.isNullOrBlank()) mapOf("GITHUB_TOKEN" to token, "GH_TOKEN" to token) else emptyMap()

        val result = GhaProcessRunner.exec(
            workingDir = projectDir,
            command = listOf(
                "gh", "run", "list",
                "--limit", limit.toString(),
                "--json", "databaseId,name,status,conclusion,createdAt",
                "--template",
                "{{range .}}{{printf \"%.0f\" .databaseId}}|{{.name}}|{{.status}}|{{.conclusion}}|{{.createdAt}}{{\"\\n\"}}{{end}}"
            ),
            extraEnv = env,
            timeoutSeconds = 30L
        )

        if (!result.isSuccess || result.stdout.isBlank()) return emptyList()

        return result.stdout.lines().filter { it.isNotBlank() }.mapNotNull { line ->
            val parts = line.split("|")
            if (parts.size >= 5) {
                WorkflowRun(parts[0], parts[1], parts[2], parts[3], parts[4])
            } else null
        }
    }

    /**
     * Automatically cleans up and deletes completed/failed/cancelled workflow runs.
     */
    fun cleanupWorkflowRuns(projectDir: File, token: String?, statusFilter: String = "all"): Pair<Int, Int> {
        val runs = fetchWorkflowRunsList(projectDir, token, limit = 100)
        if (runs.isEmpty()) return Pair(0, 0)

        var deleted = 0
        var failed = 0

        runs.forEach { run ->
            // Only completed/cancelled runs can be deleted via API. 
            // 'completed' status usually has a conclusion (success, failure, cancelled, etc.)
            if (run.status.equals("completed", ignoreCase = true)) {
                val matches = when (statusFilter.lowercase()) {
                    "failed" -> run.conclusion.equals("failure", ignoreCase = true)
                    "cancelled" -> run.conclusion.equals("cancelled", ignoreCase = true)
                    "success" -> run.conclusion.equals("success", ignoreCase = true)
                    else -> true
                }

                if (matches) {
                    val res = deleteWorkflowRun(projectDir, token, run.databaseId)
                    if (res.isSuccess) {
                        deleted++
                    } else {
                        failed++
                    }
                }
            }
        }

        return Pair(deleted, failed)
    }

}
