package cc.thevar.gha.git

import cc.thevar.gha.GhaTask
import org.gradle.api.provider.Property
import org.gradle.api.tasks.Input
import org.gradle.api.tasks.Optional
import org.gradle.api.tasks.TaskAction
import org.gradle.work.DisableCachingByDefault

@DisableCachingByDefault(because = "Executes Git push operations")
abstract class GhaGitPushTask : GhaTask() {

    @get:Input
    @get:Optional
    abstract val remoteName: Property<String>

    init {
        remoteName.convention("origin")
    }

    @TaskAction
    fun execute() {
        val dir = projectRootDir.get().asFile
        val remote = remoteName.get()
        val branch = GhaGitExec.currentBranch(dir)

        logger.lifecycle("🚀 [GHA Git Push] Pushing $branch to $remote...")
        val pushResult = GhaGitExec.exec(dir, "push", remote, branch)
        if (pushResult.isSuccess) {
            logger.lifecycle("✅ Git push successful.")
        } else {
            logger.error("❌ Git push failed: ${pushResult.stderr}")
        }
    }
}
