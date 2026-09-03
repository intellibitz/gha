package cc.thevar.gha.git

import cc.thevar.gha.GhaTask
import org.gradle.api.tasks.TaskAction
import org.gradle.work.DisableCachingByDefault

@DisableCachingByDefault(because = "Inspects live Git repository status")
abstract class GhaGitStatusTask : GhaTask() {

    init {
    }

    @TaskAction
    fun execute() {
        val dir = projectRootDir.get().asFile
        val branch = GhaGitExec.currentBranch(dir)
        val clean = GhaGitExec.isClean(dir)
        val statusResult = GhaGitExec.exec(dir, "status", "-s")

        logger.lifecycle("🌿 [GHA Git Status] Branch: $branch")
        logger.lifecycle("   Working Tree Clean: $clean")
        if (statusResult.stdout.isNotBlank()) {
            logger.lifecycle("   Changes:\n${statusResult.stdout.prependIndent("     ")}")
        } else {
            logger.lifecycle("   No staged or unstaged changes.")
        }
    }
}
