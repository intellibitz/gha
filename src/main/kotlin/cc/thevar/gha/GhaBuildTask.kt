package cc.thevar.gha

import cc.thevar.gha.safety.GhaProcessRunner
import org.gradle.api.tasks.TaskAction
import org.gradle.work.DisableCachingByDefault

@DisableCachingByDefault(because = "Executes sandboxed Gradle build")
abstract class GhaBuildTask : GhaTask() {

    @TaskAction
    fun execute() {
        val rootDir = projectRootDir.get().asFile
        logger.lifecycle("🏗️ [GHA Build] Building project in ${rootDir.absolutePath}...")
        val result = GhaProcessRunner.exec(rootDir, listOf("./gradlew", "assemble"))
        if (result.isSuccess) {
            logger.lifecycle("✅ Build completed successfully.")
        } else {
            logger.error("❌ Build failed: ${result.stderr.ifEmpty { result.stdout }}")
        }
    }
}
