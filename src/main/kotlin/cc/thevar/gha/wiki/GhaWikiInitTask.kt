package cc.thevar.gha.wiki

import cc.thevar.gha.GhaTask
import org.gradle.api.provider.Property
import org.gradle.api.tasks.Input
import org.gradle.api.tasks.TaskAction
import org.gradle.work.DisableCachingByDefault
import java.io.File

@DisableCachingByDefault(because = "Initializes local GitHub Wiki structure and templates")
abstract class GhaWikiInitTask : GhaTask() {

    @get:Input
    abstract val projectName: Property<String>

    init {
        projectName.convention(project.name)
    }

    @TaskAction
    fun execute() {
        val rootDir = projectRootDir.get().asFile
        val wikiDir = File(rootDir, "wiki")

        logger.lifecycle("📚 [GHA Wiki Init] Initializing local Wiki structure in ${wikiDir.name}/...")
        GhaWikiManager.initLocalWiki(wikiDir, projectName.get())

        logger.lifecycle("✅ Local GitHub Wiki pages initialized successfully:")
        wikiDir.listFiles()?.forEach { file ->
            logger.lifecycle("   📄 ${file.name}")
        }
        logger.lifecycle("💡 Edit pages in wiki/ and run './gradlew ghaWikiPublish' to publish to GitHub Wiki.")
    }
}
