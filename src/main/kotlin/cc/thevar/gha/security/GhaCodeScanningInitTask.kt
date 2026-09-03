package cc.thevar.gha.security

import cc.thevar.gha.GhaTask
import org.gradle.api.file.DirectoryProperty
import org.gradle.api.tasks.InputDirectory
import org.gradle.api.tasks.PathSensitive
import org.gradle.api.tasks.PathSensitivity
import org.gradle.api.tasks.TaskAction
import org.gradle.work.DisableCachingByDefault

@DisableCachingByDefault(because = "Initializes CodeQL code scanning workflow")
abstract class GhaCodeScanningInitTask : GhaTask() {

    @get:InputDirectory
    @get:PathSensitive(PathSensitivity.RELATIVE)
    abstract val projectRootDir: DirectoryProperty

    init {
        projectRootDir.convention(project.layout.projectDirectory)
    }

    @TaskAction
    fun execute() {
        val rootDir = projectRootDir.get().asFile
        logger.lifecycle("🔍 [GHA Code Scanning Init] Generating .github/workflows/codeql.yml...")

        if (GhaSecurityManager.initCodeScanningWorkflow(rootDir)) {
            logger.lifecycle("✅ .github/workflows/codeql.yml generated successfully.")
        } else {
            logger.lifecycle("ℹ️ .github/workflows/codeql.yml is already present.")
        }
    }
}
