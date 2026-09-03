package cc.thevar.gha.github

import cc.thevar.gha.GhaTask
import cc.thevar.gha.safety.GhaProcessRunner
import org.gradle.api.provider.Property
import org.gradle.api.tasks.Input
import org.gradle.api.tasks.Optional
import org.gradle.api.tasks.TaskAction
import org.gradle.work.DisableCachingByDefault

@DisableCachingByDefault(because = "Creates GitHub Gists")
abstract class GhaGistCreateTask : GhaTask() {

    @get:Input
    @get:Optional
    abstract val filePath: Property<String>

    @get:Input
    @get:Optional
    abstract val gistDescription: Property<String>

    init {
        filePath.convention(project.providers.gradleProperty("filePath").orElse("README.md"))
        gistDescription.convention(project.providers.gradleProperty("gistDescription").orElse("Created via GHA"))
    }

    @TaskAction
    fun execute() {
        val dir = projectRootDir.get().asFile
        val path = filePath.get()
        val desc = gistDescription.get()
        val token = gitHubToken.orNull

        logger.lifecycle("📜 [GHA Gist Create] Creating Gist for $path...")
        val env = mutableMapOf<String, String>()
        if (!token.isNullOrEmpty()) {
            env["GH_TOKEN"] = token
        }

        val result = GhaProcessRunner.exec(dir, listOf("gh", "gist", "create", path, "-d", desc), env)
        if (result.isSuccess) {
            logger.lifecycle("✅ Gist created successfully: ${result.stdout.trim()}")
        } else {
            logger.error("❌ Gist creation failed: ${result.stderr}")
        }
    }
}
