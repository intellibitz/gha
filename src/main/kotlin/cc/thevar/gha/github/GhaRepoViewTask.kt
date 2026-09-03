package cc.thevar.gha.github

import cc.thevar.gha.GhaTask
import cc.thevar.gha.safety.GhaProcessRunner
import org.gradle.api.tasks.TaskAction
import org.gradle.work.DisableCachingByDefault

@DisableCachingByDefault(because = "Displays GitHub repository details")
abstract class GhaRepoViewTask : GhaTask() {

    @TaskAction
    fun execute() {
        val dir = projectRootDir.get().asFile
        val token = gitHubToken.orNull

        logger.lifecycle("🌐 [GHA Repo View] Fetching GitHub repository information...")
        val env = mutableMapOf<String, String>()
        if (!token.isNullOrEmpty()) {
            env["GH_TOKEN"] = token
        }

        val result = GhaProcessRunner.exec(dir, listOf("gh", "repo", "view"), env)
        if (result.isSuccess) {
            logger.lifecycle("✅ Repository Details:\n${result.stdout.prependIndent("   ")}")
        } else {
            logger.error("❌ Repo view failed: ${result.stderr}")
        }
    }
}
