package cc.thevar.gha.projects

import cc.thevar.gha.GhaTask
import org.gradle.api.provider.Property
import org.gradle.api.tasks.Input
import org.gradle.api.tasks.TaskAction
import org.gradle.work.DisableCachingByDefault

@DisableCachingByDefault(because = "Creates default GitHub Project boards for the repository")
abstract class GhaProjectInitTask : GhaTask() {

    @get:Input
    abstract val projectName: Property<String>

    init {
        projectName.convention(project.name)
    }

    @TaskAction
    fun execute() {
        val rootDir = projectRootDir.get().asFile
        val token = gitHubToken.orNull
        val name = projectName.get()

        logger.lifecycle("📋 [GHA Project Init] Creating default GitHub Project boards for $name...")

        val created = GhaProjectManager.initDefaultProjects(rootDir, token, name)

        if (created.isNotEmpty()) {
            logger.lifecycle("✅ GitHub Project boards created successfully:")
            created.forEach { board ->
                logger.lifecycle("   📌 $board")
            }
        } else {
            logger.lifecycle("ℹ️ Project boards creation completed or boards already exist.")
        }
    }
}
