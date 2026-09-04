package cc.thevar.gha.git

import cc.thevar.gha.GhaInitTask
import cc.thevar.gha.GhaTask
import cc.thevar.gha.projects.GhaProjectManager
import cc.thevar.gha.safety.GhaProcessRunner
import org.gradle.api.provider.Property
import org.gradle.api.tasks.Input
import org.gradle.api.tasks.Optional
import org.gradle.api.tasks.TaskAction
import org.gradle.work.DisableCachingByDefault
import java.io.File

/**
 * Smart Git Clone / Sync Task.
 * Supports "0 effort" setup for two major scenarios:
 * 1. Empty folder -> Install gha -> Clone into '.' -> Project is ready.
 * 2. Existing folder -> Install gha -> Work.
 */
@DisableCachingByDefault(because = "Clones or syncs a remote Git repository")
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
            println("   Usage: ./ghai clone owner/repo [dir]")
            println("   or:    ./gradlew ghaGitClone -PtargetRepo=owner/repo")
            return
        }

        val resolvedUrl = resolveRepoUrl(repoInput, rootDir)
        val targetDirectory = cloneDir.orNull?.trim()

        // Safety Guard: If target directory is "." inside gha engine directory, clone into default subfolder
        val effectiveDir = if (targetDirectory == "." || targetDirectory == "./") {
            val defaultFolderName = resolvedUrl.substringAfterLast('/').removeSuffix(".git")
            if (rootDir.name == "gha" && defaultFolderName != "gha") {
                println("⚠️ [gha Git Clone] Target folder '.' is the gha engine directory. Cloning into './$defaultFolderName' instead...")
                defaultFolderName
            } else {
                "."
            }
        } else {
            targetDirectory
        }

        if (effectiveDir == "." || effectiveDir == "./") {
            println("🚀 [gha Git Clone] Setting up and syncing current directory with '$resolvedUrl'...")
            
            // 1. Ensure Git initialized
            val gitDir = File(rootDir, ".git")
            if (!gitDir.exists()) {
                println("📦 Initializing local Git repository...")
                GhaGitExec.exec(rootDir, "init")
            }

            // 2. Configure remote origin
            val remoteRes = GhaGitExec.exec(rootDir, "remote", "get-url", "origin")
            if (!remoteRes.isSuccess || remoteRes.stdout.isBlank()) {
                GhaGitExec.exec(rootDir, "remote", "add", "origin", resolvedUrl)
            } else {
                GhaGitExec.exec(rootDir, "remote", "set-url", "origin", resolvedUrl)
            }

            // 3. Fetch remote
            println("🔄 Fetching latest changes from '$resolvedUrl'...")
            GhaGitExec.fetch(rootDir, "origin", prune = true)

            // 4. Force sync with remote main (Safe for Scenario 1: Empty folder placeholders)
            println("🔀 Syncing workspace with 'origin/main'...")
            val currentBranch = GhaGitExec.currentBranch(rootDir)
            
            // If we are in Scenario 1 (brand new init), we must force set the HEAD to remote main
            val syncRes = if (currentBranch == "master" || currentBranch == "main" || currentBranch == "unknown") {
                // Attempt hard reset to remote main to handle scaffolded placeholders cleanly
                val res = GhaGitExec.exec(rootDir, "reset", "--hard", "origin/main")
                if (!res.isSuccess) {
                    // Fallback to standard checkout if origin/main doesn't exist yet
                    GhaGitExec.exec(rootDir, "checkout", "-b", "main", "origin/main")
                } else res
            } else {
                // If on a feature branch, just pull --rebase
                GhaGitExec.pullRebase(rootDir, "origin", "main")
            }

            if (syncRes.isSuccess) {
                println("🎉 [gha Git Clone] Successfully configured and synced current directory with '$resolvedUrl'!")
                println("⚡ Finalizing gha integration...")
                
                // Directly call the task logic helper to avoid Configuration Cache violations
                val task = project.tasks.getByName("ghaInit") as GhaInitTask
                task.execute()
                
                println("💡 Tip: Type './ghai' to auto-save and push changes to GitHub.")
            } else {
                println("❌ Sync failed: ${syncRes.stderr.ifEmpty { syncRes.stdout }}")
            }
            return
        }

        println("🚀 [gha Git Clone] Cloning repository from '$resolvedUrl' into '${effectiveDir ?: resolvedUrl.substringAfterLast('/').removeSuffix(".git")}'...")

        val cmd = mutableListOf("git", "clone", resolvedUrl)
        if (!effectiveDir.isNullOrBlank()) {
            cmd.add(effectiveDir)
        }

        val result = GhaProcessRunner.exec(
            workingDir = rootDir,
            command = cmd,
            timeoutSeconds = 120L,
        )

        if (result.isSuccess) {
            val folderName = effectiveDir ?: resolvedUrl.substringAfterLast('/').removeSuffix(".git")
            println("🎉 [gha Git Clone] Cloned '$repoInput' into '$folderName' successfully!")
            
            // Final integration in the new subfolder
            val subDir = File(rootDir, folderName)
            if (subDir.exists()) {
                println("⚡ Initializing gha sandbox in '$folderName'...")
                // We run ghaInit in the target directory using shell to ensure context is correct
                GhaProcessRunner.exec(
                    workingDir = subDir,
                    command = listOf("./gradlew", "--init-script", ".gha/init.gradle.kts", "ghaInit"),
                    timeoutSeconds = 60L
                )
            }
            println("💡 Tip: cd $folderName && ./ghai")
        } else {
            println("❌ Git clone failed: ${result.stderr.ifEmpty { result.stdout }}")
        }
    }

    companion object {
        fun resolveRepoUrl(input: String, rootDir: File? = null): String {
            val trimmed = input.trim()
            return when {
                trimmed.startsWith("http://") || trimmed.startsWith("https://") || trimmed.startsWith("git@") -> trimmed
                trimmed.contains("/") -> "https://github.com/$trimmed"
                else -> {
                    val resolvedOwner = resolveDefaultOwner(rootDir)
                    if (!resolvedOwner.isNullOrBlank()) {
                        "https://github.com/$resolvedOwner/$trimmed"
                    } else {
                        "https://github.com/$trimmed/$trimmed"
                    }
                }
            }
        }

        private fun resolveDefaultOwner(rootDir: File?): String? {
            // 1. Environment variables
            val envOwner = System.getenv("GHA_DEFAULT_OWNER") ?: System.getenv("GHA_OWNER")
            if (!envOwner.isNullOrBlank()) return envOwner.trim()

            // 2. Local git remote origin owner (if working in an existing project)
            if (rootDir != null) {
                val owner = GhaProjectManager.resolveOwner(rootDir)
                if (!owner.isNullOrBlank()) return owner.trim()
            }

            // 3. GitHub CLI authenticated user fallback
            if (rootDir != null) {
                try {
                    val ghUserRes = GhaProcessRunner.exec(
                        workingDir = rootDir,
                        command = listOf("gh", "api", "user", "--jq", ".login"),
                        timeoutSeconds = 5L
                    )
                    if (ghUserRes.isSuccess && ghUserRes.stdout.trim().isNotBlank()) {
                        return ghUserRes.stdout.trim()
                    }
                } catch (_: Exception) {}
            }

            return null
        }
    }
}
