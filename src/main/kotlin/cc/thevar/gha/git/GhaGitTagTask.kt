package cc.thevar.gha.git

import cc.thevar.gha.GhaTask
import org.gradle.api.provider.Property
import org.gradle.api.tasks.Input
import org.gradle.api.tasks.Optional
import org.gradle.api.tasks.TaskAction
import org.gradle.work.DisableCachingByDefault

@DisableCachingByDefault(because = "Creates and pushes Git tags")
abstract class GhaGitTagTask : GhaTask() {

    @get:Input
    abstract val tagName: Property<String>

    @get:Input
    @get:Optional
    abstract val tagMessage: Property<String>

    init {
    }

    @TaskAction
    fun execute() {
        val dir = projectRootDir.get().asFile
        val tag = tagName.get()
        val msg = tagMessage.orNull ?: "Release $tag"

        logger.lifecycle("🏷️ [GHA Git Tag] Creating tag '$tag'...")
        val tagResult = GhaGitExec.exec(dir, "tag", "-a", tag, "-m", msg)
        if (tagResult.isSuccess) {
            logger.lifecycle("✅ Tag '$tag' created successfully.")
            logger.lifecycle("🚀 Pushing tag to origin...")
            GhaGitExec.exec(dir, "push", "origin", tag)
        } else {
            logger.error("❌ Tag creation failed: ${tagResult.stderr}")
        }
    }
}
