package cc.thevar.gha.github

import cc.thevar.gha.GhaTask
import cc.thevar.gha.safety.GhaProcessRunner
import org.gradle.api.file.DirectoryProperty
import org.gradle.api.provider.Property
import org.gradle.api.tasks.Input
import org.gradle.api.tasks.InputDirectory
import org.gradle.api.tasks.Optional
import org.gradle.api.tasks.PathSensitive
import org.gradle.api.tasks.PathSensitivity
import org.gradle.api.tasks.TaskAction
import org.gradle.work.DisableCachingByDefault

@DisableCachingByDefault(because = "Checks out a Pull Request branch locally")
abstract class GhaPrCheckoutTask : GhaTask() {

    @get:InputDirectory
    @get:PathSensitive(PathSensitivity.RELATIVE)
    abstract val repoDir: DirectoryProperty

    @get:Input
    @get:Optional
    abstract val prNumber: Property<String>

    init {
        repoDir.convention(project.layout.projectDirectory)
        prNumber.convention(project.providers.gradleProperty("prNumber"))
    }

    @TaskAction
    fun execute() {
        val dir = repoDir.get().asFile
        val num = prNumber.orNull

        if (num.isNullOrBlank()) {
            logger.error("❌ Pull Request number required. Usage: ./gradlew ghaPrCheckout -PprNumber=1")
            return
        }

        val token = gitHubToken.orNull ?: ""
        logger.lifecycle("🔀 [GHA PR Checkout] Checking out branch for Pull Request #$num...")

        val env = if (token.isNotBlank()) mapOf("GITHUB_TOKEN" to token, "GH_TOKEN" to token) else emptyMap()
        val result = GhaProcessRunner.exec(
            workingDir = dir,
            command = listOf("gh", "pr", "checkout", num),
            extraEnv = env,
            timeoutSeconds = 45L
        )

        if (result.isSuccess) {
            logger.lifecycle("✅ ${result.stdout.ifEmpty { "Checked out Pull Request #$num branch successfully." }}")
        } else {
            logger.error("❌ ${result.stderr.ifEmpty { result.stdout }}")
        }
    }
}
