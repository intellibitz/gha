package cc.thevar.gha.security

import cc.thevar.gha.GhaTask
import org.gradle.api.file.DirectoryProperty
import org.gradle.api.provider.Property
import org.gradle.api.tasks.Input
import org.gradle.api.tasks.InputDirectory
import org.gradle.api.tasks.PathSensitive
import org.gradle.api.tasks.PathSensitivity
import org.gradle.api.tasks.TaskAction
import org.gradle.work.DisableCachingByDefault

@DisableCachingByDefault(because = "Initializes default GitHub security workflows, Dependabot, and security policies")
abstract class GhaSecurityInitTask : GhaTask() {

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
        logger.lifecycle("🛡️ [GHA Security Init] Generating default GitHub security workflows & Dependabot configs...")

        val createdFiles = GhaSecurityManager.initAllSecurityWorkflows(rootDir, projectName.get())

        if (createdFiles.isNotEmpty()) {
            logger.lifecycle("✅ Security workflows & policies generated successfully:")
            createdFiles.forEach { file ->
                logger.lifecycle("   📄 $file")
            }
        } else {
            logger.lifecycle("ℹ️ All security workflows & policies are already present in .github/")
        }
    }
}
