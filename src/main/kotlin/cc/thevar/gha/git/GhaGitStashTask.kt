package cc.thevar.gha.git

import cc.thevar.gha.GhaTask
import org.gradle.api.provider.Property
import org.gradle.api.tasks.Input
import org.gradle.api.tasks.Optional
import org.gradle.api.tasks.TaskAction
import org.gradle.work.DisableCachingByDefault

@DisableCachingByDefault(because = "Executes Git stash operations")
abstract class GhaGitStashTask : GhaTask() {

    @get:Input
    @get:Optional
    abstract val actionName: Property<String>

    init {
        actionName.convention(project.providers.gradleProperty("stashAction").orElse("push"))
    }

    @TaskAction
    fun execute() {
        val dir = projectRootDir.get().asFile
        val action = actionName.get()
        logger.lifecycle("📦 [GHA Git Stash] Executing git stash $action...")
        val result = GhaGitExec.exec(dir, "stash", action)
        if (result.isSuccess) {
            logger.lifecycle("✅ Git stash $action successful:\n${result.stdout.prependIndent("   ")}")
        } else {
            logger.error("❌ Git stash $action failed: ${result.stderr}")
        }
    }
}
