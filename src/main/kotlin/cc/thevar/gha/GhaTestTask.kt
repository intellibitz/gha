package cc.thevar.gha

import cc.thevar.gha.safety.GhaProcessRunner
import org.gradle.api.tasks.TaskAction
import org.gradle.work.DisableCachingByDefault

@DisableCachingByDefault(because = "Executes project test suite")
abstract class GhaTestTask : GhaTask() {

    @TaskAction
    fun execute() {
        val rootDir = projectRootDir.get().asFile
        logger.lifecycle("🧪 [GHA Test] Running test suite in ${rootDir.absolutePath}...")
        val result = GhaProcessRunner.exec(rootDir, listOf("./gradlew", "check"))
        if (result.isSuccess) {
            logger.lifecycle("✅ All tests passed successfully.")
        } else {
            logger.error("❌ Tests failed: ${result.stderr.ifEmpty { result.stdout }}")
        }
    }
}
