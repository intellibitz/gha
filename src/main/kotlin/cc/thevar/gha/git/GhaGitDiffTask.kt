package cc.thevar.gha.git

import cc.thevar.gha.GhaTask
import org.gradle.api.tasks.TaskAction
import org.gradle.work.DisableCachingByDefault

@DisableCachingByDefault(because = "Executes Git diff operations")
abstract class GhaGitDiffTask : GhaTask() {

    @TaskAction
    fun execute() {
        val dir = projectRootDir.get().asFile
        logger.lifecycle("🔍 [GHA Git Diff] Inspecting working tree changes...")
        val result = GhaGitExec.exec(dir, "diff")
        if (result.isSuccess) {
            val output = result.stdout.ifEmpty { "Working tree clean. No unstaged changes." }
            logger.lifecycle("✅ Git Diff:\n${output.prependIndent("   ")}")
        } else {
            logger.error("❌ Git diff failed: ${result.stderr}")
        }
    }
}
