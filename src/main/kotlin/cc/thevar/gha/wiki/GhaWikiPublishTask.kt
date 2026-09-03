package cc.thevar.gha.wiki

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
import java.io.File

@DisableCachingByDefault(because = "Publishes local wiki/ directory pages to remote GitHub Wiki")
abstract class GhaWikiPublishTask : GhaTask() {

    @get:InputDirectory
    @get:PathSensitive(PathSensitivity.RELATIVE)
    abstract val projectRootDir: DirectoryProperty

    @get:Input
    @get:Optional
    abstract val commitMessage: Property<String>

    init {
        projectRootDir.convention(project.layout.projectDirectory)
        commitMessage.convention("Update GitHub Wiki pages via GHA")
    }

    @TaskAction
    fun execute() {
        val rootDir = projectRootDir.get().asFile
        val wikiDir = File(rootDir, "wiki")

        if (!wikiDir.exists() || (wikiDir.listFiles { file -> file.isFile }?.isEmpty() != false)) {
            logger.lifecycle("📚 [GHA Wiki Publish] Local wiki/ directory is empty. Initializing template pages...")
            GhaWikiManager.initLocalWiki(wikiDir, project.name)
        }

        val msg = commitMessage.get()
        val token = gitHubToken.orNull

        logger.lifecycle("🚀 [GHA Wiki Publish] Publishing local wiki/ pages to remote GitHub Wiki...")
        val result = GhaWikiManager.publishWiki(rootDir, wikiDir, token, msg)

        if (result.isSuccess) {
            logger.lifecycle("✅ ${result.stdout.ifEmpty { "GitHub Wiki published successfully!" }}")
        } else {
            logger.error("❌ GitHub Wiki publish failed: ${result.stderr.ifEmpty { result.stdout }}")
        }
    }
}
