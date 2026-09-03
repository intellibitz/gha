package cc.thevar.gha.github

import cc.thevar.gha.GhaTask
import cc.thevar.gha.safety.GhaProcessRunner
import org.gradle.api.provider.Property
import org.gradle.api.tasks.Input
import org.gradle.api.tasks.Optional
import org.gradle.api.tasks.TaskAction
import org.gradle.work.DisableCachingByDefault

@DisableCachingByDefault(because = "Configures repository secrets safely")
abstract class GhaSecretSetTask : GhaTask() {

    @get:Input
    @get:Optional
    abstract val secretName: Property<String>

    @get:Input
    @get:Optional
    abstract val secretValue: Property<String>

    init {
        secretName.convention(project.providers.gradleProperty("secretName").orElse(""))
        secretValue.convention(project.providers.gradleProperty("secretValue").orElse(""))
    }

    @TaskAction
    fun execute() {
        val dir = projectRootDir.get().asFile
        val key = secretName.get()
        val value = secretValue.get()
        val token = gitHubToken.orNull

        if (key.isEmpty() || value.isEmpty()) {
            logger.lifecycle("ℹ️ Usage: ./gradlew ghaSecretSet -PsecretName=\"KEY\" -PsecretValue=\"VALUE\"")
            return
        }

        logger.lifecycle("🔒 [GHA Secret Set] Setting secret $key...")
        val env = mutableMapOf<String, String>()
        if (!token.isNullOrEmpty()) {
            env["GH_TOKEN"] = token
        }

        val result = GhaProcessRunner.exec(dir, listOf("gh", "secret", "set", key, "-b", value), env)
        if (result.isSuccess) {
            logger.lifecycle("✅ Secret $key configured successfully.")
        } else {
            logger.error("❌ Secret set failed: ${result.stderr}")
        }
    }
}
