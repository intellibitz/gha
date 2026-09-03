package cc.thevar.gha.insights

import cc.thevar.gha.GhaTask
import cc.thevar.gha.git.GhaGitExec
import org.gradle.api.file.DirectoryProperty
import org.gradle.api.provider.Property
import org.gradle.api.tasks.Input
import org.gradle.api.tasks.InputDirectory
import org.gradle.api.tasks.PathSensitive
import org.gradle.api.tasks.PathSensitivity
import org.gradle.api.tasks.TaskAction
import org.gradle.work.DisableCachingByDefault

@DisableCachingByDefault(because = "Displays real-time GitHub Insights and repository metrics")
abstract class GhaInsightsTask : GhaTask() {

    @get:Input
    abstract val projectName: Property<String>

    @get:InputDirectory
    @get:PathSensitive(PathSensitivity.RELATIVE)
    abstract val projectRootDir: DirectoryProperty

    init {
        projectName.convention(project.name)
        projectRootDir.convention(project.layout.projectDirectory)
    }

    @TaskAction
    fun execute() {
        val rootDir = projectRootDir.get().asFile
        val token = gitHubToken.orNull
        val ownerRepo = GhaInsightsManager.resolveOwnerAndRepo(rootDir) ?: projectName.get()

        logger.lifecycle("📊 [GHA GitHub Insights] Repository: $ownerRepo")

        val overview = GhaInsightsManager.fetchRepoOverview(rootDir, token)
        if (overview != null) {
            logger.lifecycle("   ⭐ Stars: ${overview.stars} | 🍴 Forks: ${overview.forks} | 👁️ Watchers: ${overview.watchers}")
            logger.lifecycle("   📌 Open Issues / PRs: ${overview.openIssues}")
            logger.lifecycle("   🌿 Default Branch: ${overview.defaultBranch}")
        } else {
            logger.lifecycle("   ℹ️ Remote metrics require active GitHub connection and authentication.")
        }

        val branch = GhaGitExec.currentBranch(rootDir)
        val commitResult = GhaGitExec.exec(rootDir, "rev-list", "--count", "HEAD")
        val totalCommits = if (commitResult.isSuccess) commitResult.stdout.trim() else "N/A"

        logger.lifecycle("   🌱 Active Branch: $branch")
        logger.lifecycle("   📜 Total Commit Count: $totalCommits")

        val topContributors = GhaInsightsManager.fetchContributors(rootDir).take(5)
        if (topContributors.isNotEmpty()) {
            logger.lifecycle("   🏆 Top Contributors:")
            topContributors.forEach { (author, commits) ->
                logger.lifecycle("      👤 %-25s : %d commits".format(author, commits))
            }
        }
    }
}
