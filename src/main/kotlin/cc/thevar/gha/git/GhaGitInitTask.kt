package cc.thevar.gha.git

import cc.thevar.gha.GhaTask
import org.gradle.api.tasks.TaskAction
import org.gradle.work.DisableCachingByDefault

@DisableCachingByDefault(because = "Executes Git repository initialization")
abstract class GhaGitInitTask : GhaTask() {

    @TaskAction
    fun execute() {
        val dir = projectRootDir.get().asFile
        logger.lifecycle("🌿 [GHA Git Init] Initializing Git repository in ${dir.absolutePath}...")
        val result = GhaGitExec.exec(dir, "init")
        if (result.isSuccess) {
            logger.lifecycle("✅ Git repository initialized successfully:\n${result.stdout.prependIndent("   ")}")
        } else {
            logger.error("❌ Git init failed: ${result.stderr}")
        }
    }
}
