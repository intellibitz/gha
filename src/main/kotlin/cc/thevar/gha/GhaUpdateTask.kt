package cc.thevar.gha

import cc.thevar.gha.safety.GhaProcessRunner
import cc.thevar.gha.safety.GhaVersionManager
import org.gradle.api.tasks.TaskAction
import org.gradle.work.DisableCachingByDefault
import java.io.File

@DisableCachingByDefault(because = "Updates gha init scripts, version configs, and runner wrappers")
abstract class GhaUpdateTask : GhaTask() {

    @TaskAction
    fun execute() {
        verifySandbox()
        val rootDir = projectRootDir.get().asFile
        val localVersion = GhaVersionManager.readVersion(rootDir)
        val remoteVersion = GhaVersionManager.fetchRemoteVersion()

        logger.lifecycle("🚀 [GHA Update] Local Version: $localVersion, Remote Version: $remoteVersion")

        val comparison = GhaVersionManager.compareVersions(remoteVersion, localVersion)

        if (comparison > 0) {
            logger.lifecycle("📈 Newer version $remoteVersion found! Performing autonomous upgrade...")
            
            // 1. Refresh version.txt
            File(rootDir, "version.txt").writeText(remoteVersion + "\n")
            
            // 2. Fetch latest install.sh
            val installShRes = GhaProcessRunner.exec(
                workingDir = rootDir,
                command = listOf("curl", "-sSL", "https://raw.githubusercontent.com/intellibitz/gha/main/init/install.sh"),
                timeoutSeconds = 30L
            )
            if (installShRes.isSuccess && installShRes.stdout.isNotBlank()) {
                val installSh = File(rootDir, "init/install.sh")
                installSh.parentFile?.mkdirs()
                installSh.writeText(installShRes.stdout)
                installSh.setExecutable(true, false)
                logger.lifecycle("   ✅ Refreshed init/install.sh")
            }

            // 3. Fetch latest ghai launcher
            val ghaiRes = GhaProcessRunner.exec(
                workingDir = rootDir,
                command = listOf("curl", "-sSL", "https://raw.githubusercontent.com/intellibitz/gha/main/ghai"),
                timeoutSeconds = 30L
            )
            if (ghaiRes.isSuccess && ghaiRes.stdout.isNotBlank()) {
                val ghai = File(rootDir, "ghai")
                ghai.writeText(ghaiRes.stdout)
                ghai.setExecutable(true, false)
                logger.lifecycle("   ✅ Refreshed ghai launcher")
            }

            // 4. Trigger ghaInit to refresh the rest of the sandbox
            logger.lifecycle("⚡ Finalizing upgrade via ghaInit...")
            val initRes = GhaProcessRunner.exec(
                workingDir = rootDir,
                command = listOf("./gradlew", "-Dgradle.user.home=.gha/gradle-user-home", "--init-script", "init/gha.init.gradle.kts", "ghaInit"),
                timeoutSeconds = 60L
            )
            
            if (initRes.isSuccess) {
                logger.lifecycle("🎉 [GHA Update] Successfully upgraded to $remoteVersion!")
            } else {
                logger.lifecycle("⚠️ [GHA Update] Upgrade partially completed. Please run './ghai' to self-heal.")
            }
        } else if (comparison == 0) {
            logger.lifecycle("✅ [GHA Update] gha is already at the latest version ($localVersion).")
        } else {
            logger.lifecycle("ℹ️ [GHA Update] Local version ($localVersion) is ahead of remote ($remoteVersion). No update needed.")
        }
    }
}
