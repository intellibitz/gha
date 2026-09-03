package cc.thevar.gha

import org.gradle.api.tasks.TaskAction
import org.gradle.work.DisableCachingByDefault
import java.io.File

@DisableCachingByDefault(because = "Cleans build directory and temporary caches")
abstract class GhaCleanTask : GhaTask() {

    @TaskAction
    fun execute() {
        val rootDir = projectRootDir.get().asFile
        val buildDir = File(rootDir, "build")
        
        logger.lifecycle("🧹 [GHA Clean] Cleaning build directories in ${rootDir.absolutePath}...")
        if (buildDir.exists()) {
            buildDir.deleteRecursively()
            logger.lifecycle("   🗑️ Removed ${buildDir.relativeTo(rootDir).path}")
        }
        logger.lifecycle("✅ Clean completed successfully.")
    }
}
