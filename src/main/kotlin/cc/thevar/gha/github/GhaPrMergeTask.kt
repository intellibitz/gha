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

@DisableCachingByDefault(because = "Merges a Pull Request on GitHub")
abstract class GhaPrMergeTask : GhaTask() {

    @get:InputDirectory
    @get:PathSensitive(PathSensitivity.RELATIVE)
    abstract val repoDir: DirectoryProperty

    @get:Input
    @get:Optional
    abstract val prNumber: Property<String>

    @get:Input
    @get:Optional
    abstract val mergeMethod: Property<String>

    @get:Input
    @get:Optional
    abstract val deleteBranch: Property<String>

    @get:Input
    @get:Optional
    abstract val autoMerge: Property<String>

    init {
        repoDir.convention(project.layout.projectDirectory)
        prNumber.convention(project.providers.gradleProperty("prNumber"))
        mergeMethod.convention(project.providers.gradleProperty("mergeMethod").orElse("squash"))
        deleteBranch.convention(project.providers.gradleProperty("deleteBranch").orElse("true"))
        autoMerge.convention(project.providers.gradleProperty("autoMerge"))
    }

    @TaskAction
    fun execute() {
        val dir = repoDir.get().asFile
        val num = prNumber.orNull
        val method = mergeMethod.get().lowercase()

        val token = gitHubToken.orNull ?: ""
        logger.lifecycle("🔀 [GHA PR Merge] Merging Pull Request ${num?.let { "#$it" } ?: "current branch"} (method: $method)...")

        val cmd = mutableListOf("gh", "pr", "merge")
        if (!num.isNullOrBlank()) {
            cmd.add(num)
        }

        when (method) {
            "rebase" -> cmd.add("--rebase")
            "merge" -> cmd.add("--merge")
            else -> cmd.add("--squash")
        }

        if (deleteBranch.get().lowercase() == "true") {
            cmd.add("--delete-branch")
        }

        if (autoMerge.orNull?.lowercase() == "true") {
            cmd.add("--auto")
        }

        val env = if (token.isNotBlank()) mapOf("GITHUB_TOKEN" to token, "GH_TOKEN" to token) else emptyMap()
        val result = GhaProcessRunner.exec(
            workingDir = dir,
            command = cmd,
            extraEnv = env,
            timeoutSeconds = 45L
        )

        if (result.isSuccess) {
            logger.lifecycle("✅ ${result.stdout.ifEmpty { "Pull Request merged successfully." }}")
        } else {
            logger.error("❌ ${result.stderr.ifEmpty { result.stdout }}")
        }
    }
}
