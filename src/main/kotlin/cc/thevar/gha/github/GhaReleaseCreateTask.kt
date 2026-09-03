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

@DisableCachingByDefault(because = "Creates Releases on GitHub")
abstract class GhaReleaseCreateTask : GhaTask() {

    @get:InputDirectory
    @get:PathSensitive(PathSensitivity.RELATIVE)
    abstract val repoDir: DirectoryProperty

    @get:Input
    abstract val tag: Property<String>

    @get:Input
    @get:Optional
    abstract val title: Property<String>

    @get:Input
    @get:Optional
    abstract val notes: Property<String>

    init {
        repoDir.convention(project.layout.projectDirectory)
    }

    @TaskAction
    fun execute() {
        val dir = repoDir.get().asFile
        val tagName = tag.get()
        val relTitle = title.orNull ?: "Release $tagName"
        val relNotes = notes.orNull ?: "Automated Release $tagName created by GHA"
        val token = gitHubToken.orNull ?: ""

        logger.lifecycle("🚀 [GHA Release Create] Creating Release: \"$relTitle\" ($tagName)...")

        val env = if (token.isNotBlank()) mapOf("GITHUB_TOKEN" to token) else emptyMap()
        val result = GhaProcessRunner.exec(
            workingDir = dir,
            command = listOf("gh", "release", "create", tagName, "--title", relTitle, "--notes", relNotes),
            extraEnv = env,
            timeoutSeconds = 45L
        )

        if (result.isSuccess) {
            logger.lifecycle("✅ Release created successfully:\n${result.stdout.prependIndent("   ")}")
        } else {
            logger.lifecycle("ℹ️ ${result.stdout.ifEmpty { result.stderr }}")
        }
    }
}
