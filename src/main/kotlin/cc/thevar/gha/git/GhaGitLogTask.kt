package cc.thevar.gha.git

import cc.thevar.gha.GhaTask
import org.gradle.api.provider.Property
import org.gradle.api.tasks.Input
import org.gradle.api.tasks.Optional
import org.gradle.api.tasks.TaskAction
import org.gradle.work.DisableCachingByDefault

@DisableCachingByDefault(because = "Displays recent Git commits")
abstract class GhaGitLogTask : GhaTask() {

    @get:Input
    @get:Optional
    abstract val maxCount: Property<Int>

    init {
        maxCount.convention(5)
    }

    @TaskAction
    fun execute() {
        val dir = projectRootDir.get().asFile
        val count = maxCount.get()

        logger.lifecycle("📜 [GHA Git Log] Showing last $count commits...")
        val logResult = GhaGitExec.exec(dir, "log", "-n", count.toString(), "--oneline")
        if (logResult.isSuccess) {
            logger.lifecycle(logResult.stdout.prependIndent("   "))
        } else {
            logger.error("❌ Git log failed: ${logResult.stderr}")
        }
    }
}
