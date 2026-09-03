package cc.thevar.gha.security

import cc.thevar.gha.GhaTask
import org.gradle.api.tasks.TaskAction
import org.gradle.work.DisableCachingByDefault

@DisableCachingByDefault(because = "Lists active Dependabot pull requests and branches")
abstract class GhaDependabotListTask : GhaTask() {

    init {
    }

    @TaskAction
    fun execute() {
        val rootDir = projectRootDir.get().asFile
        val token = gitHubToken.orNull
        logger.lifecycle("🤖 [GHA Dependabot List] Listing open Dependabot PRs and branches...")

        val prs = GhaDependabotManager.listDependabotPrs(rootDir, token)

        if (prs.isNotEmpty()) {
            logger.lifecycle("Found ${prs.size} open Dependabot PR(s):")
            prs.forEach { pr ->
                logger.lifecycle("   #%-5d %-35s [%s]".format(pr.number, pr.title, pr.headBranch))
                logger.lifecycle("          URL: ${pr.url}")
            }
        } else {
            logger.lifecycle("   No open Dependabot PRs found.")
        }
    }
}
