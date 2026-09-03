package cc.thevar.gha.git

import cc.thevar.gha.GhaTask
import org.gradle.api.provider.Property
import org.gradle.api.tasks.Input
import org.gradle.api.tasks.Optional
import org.gradle.api.tasks.TaskAction
import org.gradle.work.DisableCachingByDefault

@DisableCachingByDefault(because = "Displays local and remote Git branches")
abstract class GhaGitBranchTask : GhaTask() {

    @get:Input
    @get:Optional
    abstract val all: Property<String>

    init {
        all.convention(project.providers.gradleProperty("all").orElse("false"))
    }

    @TaskAction
    fun execute() {
        val dir = projectRootDir.get().asFile
        val showAll = all.get().lowercase() == "true"
        
        val args = if (showAll) arrayOf("branch", "-a") else arrayOf("branch")
        val label = if (showAll) "all (local & remote)" else "local"

        logger.lifecycle("🌿 [GHA Git Branches] Listing $label branches...")
        val result = GhaGitExec.exec(dir, *args)
        if (result.isSuccess) {
            val output = result.stdout.trim()
            if (output.isNotEmpty()) {
                logger.lifecycle(output.prependIndent("   "))
            } else {
                logger.lifecycle("   No branches found.")
            }
        } else {
            logger.error("❌ Failed to list branches: ${result.stderr.ifEmpty { result.stdout }}")
        }
    }
}
