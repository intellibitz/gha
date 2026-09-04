package cc.thevar.gha.safety

import cc.thevar.gha.GhaTask
import cc.thevar.gha.git.GhaGitExec
import org.gradle.api.tasks.TaskAction
import org.gradle.work.DisableCachingByDefault

@DisableCachingByDefault(because = "Bumps project version prior to push")
abstract class GhaBumpVersionTask : GhaTask() {

    @TaskAction
    fun execute() {
        val rootDir = projectRootDir.get().asFile
        if (GhaGitExec.isGitRepo(rootDir)) {
            val newVersion = GhaVersionManager.bumpAndCommitVersion(rootDir)
            logger.lifecycle("📈 [GHA Version Bump] Incremented version to $newVersion")
        }
    }
}
