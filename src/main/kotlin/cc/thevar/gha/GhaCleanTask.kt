package cc.thevar.gha

import org.gradle.api.tasks.TaskAction
import org.gradle.work.DisableCachingByDefault
import java.io.File

@DisableCachingByDefault(because = "Cleans build directory and temporary caches across root and subprojects")
abstract class GhaCleanTask : GhaTask() {

    @TaskAction
    fun execute() {
        val rootDir = projectRootDir.get().asFile
        logger.lifecycle("🧹 [GHA Clean] Delegating clean to Gradle engine across root & subprojects...")
        val allProjects = project.rootProject.allprojects
        var removedCount = 0

        allProjects.forEach { proj ->
            val bDir = File(proj.projectDir, "build")
            if (bDir.exists()) {
                val relPath = try { bDir.relativeTo(rootDir).path } catch (_: Exception) { bDir.name }
                bDir.deleteRecursively()
                removedCount++
                logger.lifecycle("   🗑️ Removed build folder: $relPath")
            }
        }
        logger.lifecycle("✅ Clean completed successfully ($removedCount build folder(s) cleaned).")
    }
}
