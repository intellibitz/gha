package cc.thevar.gha.git

import cc.thevar.gha.GhaTask
import org.gradle.api.provider.Property
import org.gradle.api.tasks.Input
import org.gradle.api.tasks.Optional
import org.gradle.api.tasks.TaskAction
import org.gradle.work.DisableCachingByDefault

@DisableCachingByDefault(because = "Executes Git stage and checkin/commit operations")
abstract class GhaGitCheckinTask : GhaTask() {

    @get:Input
    @get:Optional
    abstract val commitMessage: Property<String>

    @get:Input
    @get:Optional
    abstract val allowEmpty: Property<String>

    init {
        commitMessage.convention(
            project.providers.gradleProperty("commitMessage")
                .orElse(project.providers.gradleProperty("message"))
                .orElse("Automated checkin via GHA")
        )
        allowEmpty.convention(project.providers.gradleProperty("allowEmpty").orElse("false"))
    }

    @TaskAction
    fun execute() {
        val dir = projectRootDir.get().asFile
        val msg = commitMessage.get()
        val isAllowEmpty = allowEmpty.get().lowercase() == "true"

        logger.lifecycle("📦 [GHA Git Checkin] Staging all files...")
        val addResult = GhaGitExec.exec(dir, "add", "-A")
        if (!addResult.isSuccess) {
            logger.error("❌ Git add failed: ${addResult.stderr}")
            return
        }

        if (!isAllowEmpty && GhaGitExec.isClean(dir)) {
            logger.lifecycle("ℹ️ Working tree clean, nothing to commit.")
            return
        }

        logger.lifecycle("📝 [GHA Git Checkin] Committing: \"$msg\"...")
        val args = if (isAllowEmpty) arrayOf("commit", "--allow-empty", "-m", msg) else arrayOf("commit", "-m", msg)
        val commitResult = GhaGitExec.exec(dir, *args)

        if (commitResult.isSuccess) {
            logger.lifecycle("✅ Git checkin committed successfully:\n${commitResult.stdout.prependIndent("   ")}")
        } else {
            logger.lifecycle("ℹ️ ${commitResult.stdout.ifEmpty { commitResult.stderr }}")
        }
    }
}
