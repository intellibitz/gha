package cc.thevar.gha.git

import cc.thevar.gha.GhaTask
import org.gradle.api.provider.Property
import org.gradle.api.tasks.Input
import org.gradle.api.tasks.Optional
import org.gradle.api.tasks.TaskAction
import org.gradle.work.DisableCachingByDefault

@DisableCachingByDefault(because = "Executes Git reset operations")
abstract class GhaGitResetTask : GhaTask() {

    @get:Input
    @get:Optional
    abstract val resetMode: Property<String>

    init {
        resetMode.convention(project.providers.gradleProperty("resetMode").orElse("hard"))
    }

    @TaskAction
    fun execute() {
        val dir = projectRootDir.get().asFile
        val mode = resetMode.get()
        logger.lifecycle("🔄 [GHA Git Reset] Resetting working tree (--$mode)...")
        val result = GhaGitExec.exec(dir, "reset", "--$mode")
        if (result.isSuccess) {
            logger.lifecycle("✅ Git reset successful:\n${result.stdout.prependIndent("   ")}")
        } else {
            logger.error("❌ Git reset failed: ${result.stderr}")
        }
    }
}
