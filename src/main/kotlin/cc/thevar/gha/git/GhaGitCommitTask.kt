package cc.thevar.gha.git

import cc.thevar.gha.GhaTask
import org.gradle.api.file.DirectoryProperty
import org.gradle.api.provider.Property
import org.gradle.api.tasks.Input
import org.gradle.api.tasks.InputDirectory
import org.gradle.api.tasks.Optional
import org.gradle.api.tasks.PathSensitive
import org.gradle.api.tasks.PathSensitivity
import org.gradle.api.tasks.TaskAction
import org.gradle.work.DisableCachingByDefault

@DisableCachingByDefault(because = "Executes Git stage and commit operations")
abstract class GhaGitCommitTask : GhaTask() {

    @get:InputDirectory
    @get:PathSensitive(PathSensitivity.RELATIVE)
    abstract val repoDir: DirectoryProperty

    @get:Input
    @get:Optional
    abstract val commitMessage: Property<String>

    init {
        repoDir.convention(project.layout.projectDirectory)
        commitMessage.convention(project.providers.gradleProperty("commitMessage").orElse("Automated commit via GHA"))
    }

    @TaskAction
    fun execute() {
        val dir = repoDir.get().asFile
        val msg = commitMessage.get()

        logger.lifecycle("📦 [GHA Git Commit] Staging all files...")
        val addResult = GhaGitExec.exec(dir, "add", "-A")
        if (!addResult.isSuccess) {
            logger.error("❌ Git add failed: ${addResult.stderr}")
            return
        }

        logger.lifecycle("📝 [GHA Git Commit] Committing: \"$msg\"...")
        val commitResult = GhaGitExec.exec(dir, "commit", "-m", msg)
        if (commitResult.isSuccess) {
            logger.lifecycle("✅ Git commit successful:\n${commitResult.stdout.prependIndent("   ")}")
        } else {
            logger.lifecycle("ℹ️ ${commitResult.stdout.ifEmpty { commitResult.stderr }}")
        }
    }
}
