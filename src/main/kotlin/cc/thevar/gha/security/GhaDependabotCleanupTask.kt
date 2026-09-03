package cc.thevar.gha.security

import cc.thevar.gha.GhaTask
import org.gradle.api.provider.Property
import org.gradle.api.tasks.Input
import org.gradle.api.tasks.Optional
import org.gradle.api.tasks.TaskAction
import org.gradle.work.DisableCachingByDefault

@DisableCachingByDefault(because = "Removes stale remote dependabot branches")
abstract class GhaDependabotCleanupTask : GhaTask() {

    @get:Input
    @get:Optional
    abstract val dryRun: Property<String>

    init {
        dryRun.convention(project.providers.gradleProperty("dryRun").orElse("false"))
    }

    @TaskAction
    fun execute() {
        val rootDir = projectRootDir.get().asFile
        val token = gitHubToken.orNull
        val isDryRun = dryRun.get().lowercase() == "true"

        logger.lifecycle("🤖 [GHA Dependabot Cleanup] Fetching remote dependabot/ branches...")
        val branches = GhaDependabotManager.listDependabotBranches(rootDir, token)
        
        if (branches.isEmpty()) {
            logger.lifecycle("   No remote dependabot/ branches found.")
            return
        }

        logger.lifecycle("   Found ${branches.size} branch(es):")
        branches.forEach { logger.lifecycle("   - $it") }

        if (isDryRun) {
            logger.lifecycle("ℹ️ Dry run enabled. No branches were deleted.")
            return
        }

        var deleted = 0
        var failed = 0
        branches.forEach { branch ->
            logger.lifecycle("🗑️ Deleting branch: $branch...")
            val result = GhaDependabotManager.deleteRemoteBranch(rootDir, token, branch)
            if (result.isSuccess) {
                deleted++
            } else {
                failed++
                logger.error("   ❌ Failed to delete $branch: ${result.stderr.ifEmpty { result.stdout }}")
            }
        }

        logger.lifecycle("✅ Cleanup completed: $deleted deleted, $failed failed.")
    }
}
