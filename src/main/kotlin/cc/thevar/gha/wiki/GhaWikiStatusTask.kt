package cc.thevar.gha.wiki

import cc.thevar.gha.GhaTask
import org.gradle.api.file.DirectoryProperty
import org.gradle.api.provider.Property
import org.gradle.api.tasks.Input
import org.gradle.api.tasks.InputDirectory
import org.gradle.api.tasks.PathSensitive
import org.gradle.api.tasks.PathSensitivity
import org.gradle.api.tasks.TaskAction
import org.gradle.work.DisableCachingByDefault
import java.io.File

@DisableCachingByDefault(because = "Displays current GitHub Wiki status")
abstract class GhaWikiStatusTask : GhaTask() {

    @get:Input
    abstract val projectName: Property<String>

    @get:InputDirectory
    @get:PathSensitive(PathSensitivity.RELATIVE)
    abstract val projectRootDir: DirectoryProperty

    init {
        projectName.convention(project.name)
        projectRootDir.convention(project.layout.projectDirectory)
    }

    @TaskAction
    fun execute() {
        val rootDir = projectRootDir.get().asFile
        val wikiDir = File(rootDir, "wiki")
        val wikiUrl = GhaWikiManager.resolveWikiUrl(rootDir, gitHubToken.orNull) ?: "Not configured"

        logger.lifecycle("📚 [GHA Wiki Status] Project: ${projectName.get()}")
        logger.lifecycle("   Remote Wiki URL: $wikiUrl")
        logger.lifecycle("   Local Wiki Dir: ${wikiDir.absolutePath}")

        if (wikiDir.exists() && wikiDir.isDirectory) {
            val pages = wikiDir.listFiles { file -> file.isFile && file.extension == "md" } ?: emptyArray()
            logger.lifecycle("   Local Pages (${pages.size}):")
            pages.forEach { page ->
                logger.lifecycle("     📄 ${page.name} (${page.length()} bytes)")
            }
        } else {
            logger.lifecycle("   Local Wiki Dir: Not initialized (Run './gradlew ghaWikiInit' to create templates)")
        }
    }
}
