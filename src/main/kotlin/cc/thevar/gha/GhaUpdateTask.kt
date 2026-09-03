package cc.thevar.gha

import cc.thevar.gha.config.GhaConfig
import cc.thevar.gha.safety.GhaProcessRunner
import org.gradle.api.tasks.TaskAction
import org.gradle.work.DisableCachingByDefault
import java.io.File

@DisableCachingByDefault(because = "Updates gha init scripts, version configs, and runner wrappers")
abstract class GhaUpdateTask : GhaTask() {

    @TaskAction
    fun execute() {
        val rootDir = projectRootDir.get().asFile
        logger.lifecycle("🚀 [GHA Update] Executing 100% Kotlin gha update engine...")

        // Fetch latest version information via GhaProcessRunner / curl
        val initScriptRes = GhaProcessRunner.exec(
            workingDir = rootDir,
            command = listOf("curl", "-sSL", "https://raw.githubusercontent.com/intellibitz/gha/main/init/install.sh"),
            timeoutSeconds = 30L
        )

        if (initScriptRes.isSuccess && initScriptRes.stdout.isNotBlank()) {
            val installSh = File(rootDir, "init/install.sh")
            installSh.parentFile?.mkdirs()
            installSh.writeText(initScriptRes.stdout)
            installSh.setExecutable(true, false)
            logger.lifecycle("   ✅ Refreshed init/install.sh with latest release")
        }

        logger.lifecycle("🎉 [GHA Update] Updated gha & ghai to configured release (${GhaConfig.KOTLIN_VERSION} / Gradle ${GhaConfig.GRADLE_VERSION})!")
    }
}
