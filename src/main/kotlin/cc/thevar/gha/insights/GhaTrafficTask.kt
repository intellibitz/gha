package cc.thevar.gha.insights

import cc.thevar.gha.GhaTask
import org.gradle.api.tasks.TaskAction
import org.gradle.work.DisableCachingByDefault

@DisableCachingByDefault(because = "Displays repository traffic, clones, and view statistics")
abstract class GhaTrafficTask : GhaTask() {

    init {
    }

    @TaskAction
    fun execute() {
        val rootDir = projectRootDir.get().asFile
        val token = gitHubToken.orNull
        val ownerRepo = GhaInsightsManager.resolveOwnerAndRepo(rootDir) ?: project.name

        logger.lifecycle("📈 [GHA Traffic Insights] Repository: $ownerRepo")

        val clonesTraffic = GhaInsightsManager.fetchClonesTraffic(rootDir, token)
        val viewsTraffic = GhaInsightsManager.fetchViewsTraffic(rootDir, token)

        logger.lifecycle("   📥 $clonesTraffic")
        logger.lifecycle("   👀 $viewsTraffic")
    }
}
