package cc.thevar.gha.security

import cc.thevar.gha.GhaTask
import org.gradle.api.provider.Property
import org.gradle.api.tasks.Input
import org.gradle.api.tasks.Optional
import org.gradle.api.tasks.TaskAction
import org.gradle.work.DisableCachingByDefault

@DisableCachingByDefault(because = "Closes Dependabot pull requests and deletes remote dependabot/ branches")
abstract class GhaDependabotCloseTask : GhaTask() {

    @get:Input
    @get:Optional
    abstract val prNumber: Property<String>

    @get:Input
    @get:Optional
    abstract val closeAll: Property<String>

    @get:Input
    @get:Optional
    abstract val closeComment: Property<String>

    init {
        prNumber.convention(project.providers.gradleProperty("prNumber"))
        closeAll.convention(project.providers.gradleProperty("closeAll").orElse("false"))
        closeComment.convention(project.providers.gradleProperty("closeComment").orElse("Closed by Maintainer via GHA"))
    }

    @TaskAction
    fun execute() {
        val rootDir = projectRootDir.get().asFile
        val token = gitHubToken.orNull
        val comment = closeComment.get()
        val specificNum = prNumber.orNull?.toIntOrNull()
        val isCloseAll = closeAll.get().lowercase() == "true"

        if (specificNum != null) {
            logger.lifecycle("🤖 [GHA Dependabot Close] Closing Dependabot PR #$specificNum...")
            val result = GhaDependabotManager.closeDependabotPr(rootDir, token, specificNum, comment)
            if (result.isSuccess) {
                logger.lifecycle("✅ Dependabot PR #$specificNum closed and branch deleted successfully.")
            } else {
                logger.error("❌ Failed to close Dependabot PR #$specificNum: ${result.stderr.ifEmpty { result.stdout }}")
            }
        } else if (isCloseAll) {
            logger.lifecycle("🤖 [GHA Dependabot Close] Fetching all open Dependabot PRs...")
            val prs = GhaDependabotManager.listDependabotPrs(rootDir, token)
            if (prs.isEmpty()) {
                logger.lifecycle("   No open Dependabot PRs to close.")
                return
            }

            logger.lifecycle("   Closing ${prs.size} Dependabot PR(s)...")
            prs.forEach { pr ->
                val result = GhaDependabotManager.closeDependabotPr(rootDir, token, pr.number, comment)
                if (result.isSuccess) {
                    logger.lifecycle("   ✅ Closed #%-5d ${pr.title}".format(pr.number))
                } else {
                    logger.error("   ❌ Failed #%-5d ${pr.title}: ${result.stderr.ifEmpty { result.stdout }}".format(pr.number))
                }
            }
        } else {
            logger.error("❌ Specify Dependabot PR number (-PprNumber=123) or close all (-PcloseAll=true).")
        }
    }
}
