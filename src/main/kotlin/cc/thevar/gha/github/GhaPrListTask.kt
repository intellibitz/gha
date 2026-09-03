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

@DisableCachingByDefault(because = "Lists Pull Requests on GitHub")
abstract class GhaPrListTask : GhaTask() {

    @get:InputDirectory
    @get:PathSensitive(PathSensitivity.RELATIVE)
    abstract val repoDir: DirectoryProperty

    @get:Input
    @get:Optional
    abstract val prState: Property<String>

    @get:Input
    @get:Optional
    abstract val prLimit: Property<String>

    @get:Input
    @get:Optional
    abstract val prBase: Property<String>

    init {
        repoDir.convention(project.layout.projectDirectory)
        prState.convention(project.providers.gradleProperty("prState").orElse("open"))
        prLimit.convention(project.providers.gradleProperty("prLimit").orElse("30"))
        prBase.convention(project.providers.gradleProperty("prBase"))
    }

    @TaskAction
    fun execute() {
        val dir = repoDir.get().asFile
        val token = gitHubToken.orNull ?: ""
        val state = prState.get()
        val limit = prLimit.get()

        logger.lifecycle("📋 [GHA PR List] Listing $state Pull Requests (limit $limit)...")

        val cmd = mutableListOf("gh", "pr", "list", "--state", state, "--limit", limit)

        prBase.orNull?.takeIf { it.isNotBlank() }?.let { base ->
            cmd.add("--base")
            cmd.add(base)
        }

        val env = if (token.isNotBlank()) mapOf("GITHUB_TOKEN" to token, "GH_TOKEN" to token) else emptyMap()
        val result = GhaProcessRunner.exec(
            workingDir = dir,
            command = cmd,
            extraEnv = env,
            timeoutSeconds = 30L
        )

        if (result.isSuccess) {
            if (result.stdout.isNotBlank()) {
                logger.lifecycle(result.stdout.prependIndent("   "))
            } else {
                logger.lifecycle("   No $state Pull Requests found.")
            }
        } else {
            logger.lifecycle("ℹ️ ${result.stdout.ifEmpty { result.stderr }}")
        }
    }
}
