package cc.thevar.gha.security

import cc.thevar.gha.GhaTask
import org.gradle.api.file.DirectoryProperty
import org.gradle.api.provider.Property
import org.gradle.api.tasks.Input
import org.gradle.api.tasks.InputDirectory
import org.gradle.api.tasks.Optional
import org.gradle.api.tasks.PathSensitive
import org.gradle.api.tasks.PathSensitivity
import org.gradle.api.tasks.TaskAction
import org.gradle.work.DisableCachingByDefault

@DisableCachingByDefault(because = "Merges open Dependabot pull requests and deletes remote branches")
abstract class GhaDependabotMergeTask : GhaTask() {

    @get:InputDirectory
    @get:PathSensitive(PathSensitivity.RELATIVE)
    abstract val projectRootDir: DirectoryProperty

    @get:Input
    @get:Optional
    abstract val prNumber: Property<String>

    @get:Input
    @get:Optional
    abstract val mergeAll: Property<String>

    @get:Input
    @get:Optional
    abstract val mergeMethod: Property<String>

    init {
        projectRootDir.convention(project.layout.projectDirectory)
        prNumber.convention(project.providers.gradleProperty("prNumber"))
        mergeAll.convention(project.providers.gradleProperty("mergeAll").orElse("false"))
        mergeMethod.convention(project.providers.gradleProperty("mergeMethod").orElse("squash"))
    }

    @TaskAction
    fun execute() {
        val rootDir = projectRootDir.get().asFile
        val token = gitHubToken.orNull
        val method = mergeMethod.get()
        val specificNum = prNumber.orNull?.toIntOrNull()
        val isMergeAll = mergeAll.get().lowercase() == "true"

        if (specificNum != null) {
            logger.lifecycle("🤖 [GHA Dependabot Merge] Merging Dependabot PR #$specificNum (method: $method)...")
            val result = GhaDependabotManager.mergeDependabotPr(rootDir, token, specificNum, method)
            if (result.isSuccess) {
                logger.lifecycle("✅ Dependabot PR #$specificNum merged and branch deleted successfully.")
            } else {
                logger.error("❌ Failed to merge Dependabot PR #$specificNum: ${result.stderr.ifEmpty { result.stdout }}")
            }
        } else if (isMergeAll) {
            logger.lifecycle("🤖 [GHA Dependabot Merge] Fetching all open Dependabot PRs...")
            val prs = GhaDependabotManager.listDependabotPrs(rootDir, token)
            if (prs.isEmpty()) {
                logger.lifecycle("   No open Dependabot PRs to merge.")
                return
            }

            logger.lifecycle("   Merging ${prs.size} Dependabot PR(s)...")
            prs.forEach { pr ->
                val result = GhaDependabotManager.mergeDependabotPr(rootDir, token, pr.number, method)
                if (result.isSuccess) {
                    logger.lifecycle("   ✅ Merged #%-5d ${pr.title}".format(pr.number))
                } else {
                    logger.error("   ❌ Failed #%-5d ${pr.title}: ${result.stderr.ifEmpty { result.stdout }}".format(pr.number))
                }
            }
        } else {
            logger.error("❌ Specify Dependabot PR number (-PprNumber=123) or merge all (-PmergeAll=true).")
        }
    }
}
