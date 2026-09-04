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
        val createdAt: String,
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
            timeoutSeconds = 30L,
        )
    }

    /**
     * Deletes a specific completed or cancelled workflow run by databaseId.
     */
    fun deleteWorkflowRun(projectDir: File, token: String?, runId: String): GhaProcessRunner.ProcessResult {
        val env = if (!token.isNullOrBlank()) mapOf("GITHUB_TOKEN" to token, "GH_TOKEN" to token) else emptyMap()

        return GhaProcessRunner.exec(
            workingDir = projectDir,
            command = listOf("gh", "run", "delete", runId),
            extraEnv = env,
            timeoutSeconds = 30L,
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
            timeoutSeconds = 30L,
        )
    }

    /**
     * Fetches workflow runs list using gh template for robust parsing.
     * Enforces numeric formatting for databaseId to avoid scientific notation.
     */
    fun fetchWorkflowRunsList(projectDir: File, token: String?, limit: Int = 1000): List<WorkflowRun> {
        val env = if (!token.isNullOrBlank()) mapOf("GITHUB_TOKEN" to token, "GH_TOKEN" to token) else emptyMap()

        val result = GhaProcessRunner.exec(
            workingDir = projectDir,
            command = listOf(
                "gh", "run", "list",
                "--limit", limit.toString(),
                "--json", "databaseId,name,status,conclusion,createdAt",
                "--template",
                "{{range .}}{{printf \"%.0f\" .databaseId}}|{{.name}}|{{.status}}|{{.conclusion}}|{{.createdAt}}{{\"\\n\"}}{{end}}",
            ),
            extraEnv = env,
            timeoutSeconds = 30L,
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
     * Automatically prunes older completed/failed/cancelled workflow runs across the entire repository history.
     * Keeps the most recent `maxKeep` runs intact.
     */
    fun pruneOldWorkflowRuns(projectDir: File, token: String?, maxKeep: Int = 5): Int {
        val runs = fetchWorkflowRunsList(projectDir, token, limit = 1000)
        if (runs.isEmpty()) return 0

        val completedRuns = runs.filter { run ->
            val status = run.status.lowercase()
            val conclusion = run.conclusion.lowercase()
            status == "completed" || conclusion in listOf("success", "failure", "cancelled", "startup_failure", "skipped", "action_required")
        }

        if (completedRuns.size <= maxKeep) return 0

        val runsToDelete = completedRuns.drop(maxKeep)
        var deletedCount = 0

        for (run in runsToDelete) {
            val res = deleteWorkflowRun(projectDir, token, run.databaseId)
            if (res.isSuccess) {
                deletedCount++
            }
        }
        return deletedCount
    }

    /**
     * Automatically cleans up and deletes completed/failed/cancelled/in-progress workflow runs.
     */
    fun cleanupWorkflowRuns(projectDir: File, token: String?, statusFilter: String = "all"): Pair<Int, Int> {
        var totalDeleted = 0
        var totalFailed = 0

        while (true) {
            val runs = fetchWorkflowRunsList(projectDir, token, limit = 1000)
            if (runs.isEmpty()) break

            var deletedInBatch = 0
            var failedInBatch = 0

            for (run in runs) {
                val status = run.status.lowercase()
                val conclusion = run.conclusion.lowercase()

                if (status == "in_progress" || status == "queued" || status == "requested") {
                    cancelWorkflowRun(projectDir, token, run.databaseId)
                }

                val matches = when (statusFilter.lowercase()) {
                    "failed" -> conclusion == "failure"
                    "cancelled" -> conclusion == "cancelled"
                    "success" -> conclusion == "success"
                    else -> true
                }

                if (matches) {
                    val res = deleteWorkflowRun(projectDir, token, run.databaseId)
                    if (res.isSuccess) {
                        deletedInBatch++
                    } else {
                        failedInBatch++
                    }
                }
            }

            totalDeleted += deletedInBatch
            totalFailed += failedInBatch

            if (deletedInBatch == 0) break
        }

        return Pair(totalDeleted, totalFailed)
    }
}
