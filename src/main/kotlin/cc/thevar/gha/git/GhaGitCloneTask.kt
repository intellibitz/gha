package cc.thevar.gha.git

import cc.thevar.gha.GhaTask
import cc.thevar.gha.safety.GhaProcessRunner
import org.gradle.api.provider.Property
import org.gradle.api.tasks.Input
import org.gradle.api.tasks.Optional
import org.gradle.api.tasks.TaskAction
import org.gradle.work.DisableCachingByDefault

@DisableCachingByDefault(because = "Clones a remote Git repository")
abstract class GhaGitCloneTask : GhaTask() {

    @get:Input
    @get:Optional
    abstract val targetRepo: Property<String>

    @get:Input
    @get:Optional
    abstract val cloneDir: Property<String>

    init {
        val prov = project.providers
        targetRepo.convention(
            prov.gradleProperty("targetRepo")
                .orElse(prov.gradleProperty("repo")),
        )
        cloneDir.convention(prov.gradleProperty("dir"))
    }

    @TaskAction
    fun execute() {
        val rootDir = taskRootDirFile
        val repoInput = targetRepo.orNull

        if (repoInput.isNullOrBlank()) {
            println("❌ [gha Git Clone] Repository parameter required.")
            println("   Usage: ./ghai clone intellibitz")
            println("   or:    ./gradlew ghaGitClone -PtargetRepo=intellibitz/gha")
            return
        }

        val resolvedUrl = resolveRepoUrl(repoInput)
        val targetDirectory = cloneDir.orNull

        println("🚀 [gha Git Clone] Cloning repository from '$resolvedUrl'...")

        val cmd = mutableListOf("git", "clone", resolvedUrl)
        if (!targetDirectory.isNullOrBlank()) {
            cmd.add(targetDirectory)
        }

        val result = GhaProcessRunner.exec(
            workingDir = rootDir,
            command = cmd,
            timeoutSeconds = 120L,
        )

        if (result.isSuccess) {
            val folderName = targetDirectory ?: resolvedUrl.substringAfterLast('/').removeSuffix(".git")
            println("🎉 [gha Git Clone] Cloned '$repoInput' into '$folderName' successfully!")
            println("💡 Tip: cd $folderName && ./ghai")
        } else {
            println("❌ Git clone failed: ${result.stderr.ifEmpty { result.stdout }}")
        }
    }

    companion object {
        fun resolveRepoUrl(input: String): String {
            val trimmed = input.trim()
            return when {
                trimmed.startsWith("http://") || trimmed.startsWith("https://") || trimmed.startsWith("git@") -> trimmed
                trimmed.contains("/") -> "https://github.com/$trimmed"
                else -> "https://github.com/$trimmed/$trimmed"
            }
        }
    }
}
