package cc.thevar.gha

import org.gradle.api.tasks.TaskAction
import org.gradle.work.DisableCachingByDefault
import java.io.File

@DisableCachingByDefault(because = "Removes gha sandbox, runner scripts, and workflows cleanly")
abstract class GhaUninstallTask : GhaTask() {

    @TaskAction
    fun execute() {
        val rootDir = projectRootDir.get().asFile
        logger.lifecycle("🧹 [GHA Uninstall] Executing 100% Kotlin cleanup across root & subprojects...")

        // 1. Remove .gha sandbox directory
        val ghaDir = File(rootDir, ".gha")
        if (ghaDir.exists()) {
            ghaDir.deleteRecursively()
            logger.lifecycle("   🗑️ Removed .gha/ sandbox directory")
        }

        // 2. Remove init/gha.init.gradle.kts
        val initScript = File(rootDir, "init/gha.init.gradle.kts")
        if (initScript.exists()) {
            initScript.delete()
            logger.lifecycle("   🗑️ Removed init/gha.init.gradle.kts")
        }

        // 3. Remove .github/workflows/gha.yml
        val workflowFile = File(rootDir, ".github/workflows/gha.yml")
        if (workflowFile.exists()) {
            workflowFile.delete()
            logger.lifecycle("   🗑️ Removed .github/workflows/gha.yml")
        }

        // 4. Remove local launcher scripts
        val ghaiScript = File(rootDir, "ghai")
        if (ghaiScript.exists()) {
            ghaiScript.delete()
            logger.lifecycle("   🗑️ Removed ghai runner script")
        }

        val ghaiBat = File(rootDir, "ghai.bat")
        if (ghaiBat.exists()) {
            ghaiBat.delete()
            logger.lifecycle("   🗑️ Removed ghai.bat runner script")
        }

        // 5. Remove user symlink if present
        val userSymlink = File(System.getProperty("user.home"), ".local/bin/ghai")
        if (userSymlink.exists()) {
            userSymlink.delete()
            logger.lifecycle("   🗑️ Removed ~/.local/bin/ghai symlink")
        }

        logger.lifecycle("✨ [GHA Uninstall] gha removed completely with 0 lingering system modifications!")
    }
}
