package cc.thevar.gha

import cc.thevar.gha.safety.GhaProcessRunner
import org.gradle.api.tasks.TaskAction
import org.gradle.work.DisableCachingByDefault
import java.io.File

@DisableCachingByDefault(because = "Executes sandboxed Gradle build")
abstract class GhaBuildTask : GhaTask() {

    @TaskAction
    fun execute() {
        val rootDir = projectRootDir.get().asFile
        logger.lifecycle("🏗️ [GHA Build] Building project in ${rootDir.absolutePath}...")

        val gradlewFile = File(rootDir, "gradlew")
        val wrapperJar = File(rootDir, "gradle/wrapper/gradle-wrapper.jar")
        val globalGradlew = File(System.getProperty("user.home"), ".gha/gradlew")
        val execGradlew = if (gradlewFile.exists() && wrapperJar.exists()) gradlewFile.absolutePath else globalGradlew.absolutePath

        val result = GhaProcessRunner.exec(rootDir, listOf(execGradlew, "assemble"))
        if (result.isSuccess) {
            logger.lifecycle("✅ Build completed successfully.")
        } else {
            logger.error("❌ Build failed: ${result.stderr.ifEmpty { result.stdout }}")
        }
    }
}
