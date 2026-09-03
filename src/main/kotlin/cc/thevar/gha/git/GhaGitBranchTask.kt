package cc.thevar.gha.git

import cc.thevar.gha.GhaTask
import org.gradle.api.provider.Property
import org.gradle.api.tasks.Input
import org.gradle.api.tasks.Optional
import org.gradle.api.tasks.TaskAction
import org.gradle.work.DisableCachingByDefault

@DisableCachingByDefault(because = "Lists, creates, and manages Git branches")
abstract class GhaGitBranchTask : GhaTask() {

    @get:Input
    @get:Optional
    abstract val all: Property<String>

    @get:Input
    @get:Optional
    abstract val createBranch: Property<String>

    @get:Input
    @get:Optional
    abstract val deleteBranch: Property<String>

    @get:Input
    @get:Optional
    abstract val forceDelete: Property<String>

    @get:Input
    @get:Optional
    abstract val remoteDelete: Property<String>

    init {
        all.convention(project.providers.gradleProperty("all").orElse("false"))
        createBranch.convention(project.providers.gradleProperty("createBranch"))
        deleteBranch.convention(project.providers.gradleProperty("deleteBranch"))
        forceDelete.convention(project.providers.gradleProperty("forceDelete").orElse("false"))
        remoteDelete.convention(project.providers.gradleProperty("remoteDelete").orElse("false"))
    }

    @TaskAction
    fun execute() {
        val dir = projectRootDir.get().asFile
        val toCreate = createBranch.orNull
        val toDelete = deleteBranch.orNull
        val isForce = forceDelete.get().lowercase() == "true"
        val isRemote = remoteDelete.get().lowercase() == "true"

        if (!toCreate.isNullOrBlank()) {
            logger.lifecycle("🌱 [GHA Git Branch] Creating branch '$toCreate'...")
            val result = GhaGitExec.checkout(dir, toCreate, createIfMissing = true)
            if (result.isSuccess) {
                logger.lifecycle("✅ Branch '$toCreate' created and checked out.")
            } else {
                logger.error("❌ Failed to create branch '$toCreate': ${result.stderr.ifEmpty { result.stdout }}")
            }
            return
        }

        if (!toDelete.isNullOrBlank()) {
            logger.lifecycle("🗑️ [GHA Git Branch] Deleting branch '$toDelete' (remote: $isRemote)...")
            if (isRemote) {
                val remRes = GhaGitExec.deleteRemoteBranch(dir, "origin", toDelete)
                if (remRes.isSuccess) {
                    logger.lifecycle("✅ Remote branch 'origin/$toDelete' deleted successfully.")
                } else {
                    logger.error("❌ Failed to delete remote branch 'origin/$toDelete': ${remRes.stderr}")
                }
            }
            val locRes = GhaGitExec.deleteLocalBranch(dir, toDelete, force = isForce)
            if (locRes.isSuccess) {
                logger.lifecycle("✅ Local branch '$toDelete' deleted successfully.")
            } else {
                logger.lifecycle("ℹ️ Local branch deletion: ${locRes.stderr.ifEmpty { locRes.stdout }}")
            }
            return
        }

        val showAll = all.get().lowercase() == "true"
        val args = if (showAll) arrayOf("branch", "-a") else arrayOf("branch")
        val label = if (showAll) "all (local & remote)" else "local"

        logger.lifecycle("🌿 [GHA Git Branches] Listing $label branches...")
        val result = GhaGitExec.exec(dir, *args)
        if (result.isSuccess) {
            val output = result.stdout.trim()
            if (output.isNotEmpty()) {
                logger.lifecycle(output.prependIndent("   "))
            } else {
                logger.lifecycle("   No branches found.")
            }
        } else {
            logger.error("❌ Failed to list branches: ${result.stderr.ifEmpty { result.stdout }}")
        }
    }
}
