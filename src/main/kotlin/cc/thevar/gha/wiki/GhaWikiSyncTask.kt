package cc.thevar.gha.wiki

import cc.thevar.gha.GhaTask
import org.gradle.api.tasks.TaskAction
import org.gradle.work.DisableCachingByDefault
import java.io.File

@DisableCachingByDefault(because = "Pulls latest GitHub Wiki changes from remote")
abstract class GhaWikiSyncTask : GhaTask() {

    init {
    }

    @TaskAction
    fun execute() {
        val rootDir = projectRootDir.get().asFile
        val wikiDir = File(rootDir, "wiki")
        if (!wikiDir.exists()) {
            wikiDir.mkdirs()
        }

        logger.lifecycle("🔄 [GHA Wiki Sync] Pulling remote Wiki pages...")
        val (workspace, prepareResult) = GhaWikiManager.prepareWikiWorkspace(rootDir, gitHubToken.orNull)

        if (workspace == null) {
            logger.error("❌ Failed to sync GitHub Wiki: ${prepareResult.stderr}")
            return
        }

        // Copy remote workspace contents to local wiki/
        var syncedCount = 0
        workspace.listFiles()?.forEach { file ->
            if (file.isFile && !file.name.startsWith(".")) {
                file.copyTo(File(wikiDir, file.name), overwrite = true)
                syncedCount++
            }
        }

        logger.lifecycle("✅ Successfully synced $syncedCount Wiki page(s) into ${wikiDir.name}/.")
    }
}
