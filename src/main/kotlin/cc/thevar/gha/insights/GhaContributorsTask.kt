package cc.thevar.gha.insights

import cc.thevar.gha.GhaTask
import org.gradle.api.tasks.TaskAction
import org.gradle.work.DisableCachingByDefault

@DisableCachingByDefault(because = "Displays repository contributors and commit breakdowns")
abstract class GhaContributorsTask : GhaTask() {

    init {
    }

    @TaskAction
    fun execute() {
        val rootDir = projectRootDir.get().asFile
        logger.lifecycle("👥 [GHA Contributors Insights] Listing repository contributors...")

        val contributors = GhaInsightsManager.fetchContributors(rootDir)

        if (contributors.isNotEmpty()) {
            val totalCommits = contributors.sumOf { it.second }
            logger.lifecycle("┌" + "─".repeat(32) + "┬" + "─".repeat(16) + "┬" + "─".repeat(14) + "┐")
            logger.lifecycle("│ %-30s │ %-14s │ %-12s │".format("Contributor Author", "Commits", "Contribution"))
            logger.lifecycle("├" + "─".repeat(32) + "┼" + "─".repeat(16) + "┼" + "─".repeat(14) + "┤")

            contributors.forEach { (author, commits) ->
                val percentage = if (totalCommits > 0) (commits.toDouble() / totalCommits * 100) else 0.0
                logger.lifecycle("│ %-30s │ %-14d │ %-11.1f%% │".format(author, commits, percentage))
            }

            logger.lifecycle("└" + "─".repeat(32) + "┴" + "─".repeat(16) + "┴" + "─".repeat(14) + "┘")
        } else {
            logger.lifecycle("   No commit history found.")
        }
    }
}
