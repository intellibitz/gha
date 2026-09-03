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

@DisableCachingByDefault(because = "Requests Dependabot to rebase or recreate pull requests to resolve conflicts")
abstract class GhaDependabotRebaseTask : GhaTask() {

    @get:InputDirectory
    @get:PathSensitive(PathSensitivity.RELATIVE)
    abstract val projectRootDir: DirectoryProperty

    @get:Input
    @get:Optional
    abstract val prNumber: Property<String>

    @get:Input
    @get:Optional
    abstract val rebaseAll: Property<String>

    @get:Input
    @get:Optional
    abstract val recreate: Property<String>

    init {
        projectRootDir.convention(project.layout.projectDirectory)
        prNumber.convention(project.providers.gradleProperty("prNumber"))
        rebaseAll.convention(project.providers.gradleProperty("rebaseAll").orElse("false"))
        recreate.convention(project.providers.gradleProperty("recreate").orElse("false"))
    }

    @TaskAction
    fun execute() {
        val rootDir = projectRootDir.get().asFile
        val token = gitHubToken.orNull
        val isRecreate = recreate.get().lowercase() == "true"
        val specificNum = prNumber.orNull?.toIntOrNull()
        val isRebaseAll = rebaseAll.get().lowercase() == "true"
        val actionName = if (isRecreate) "recreate" else "rebase"

        if (specificNum != null) {
            logger.lifecycle("🤖 [GHA Dependabot Rebase] Requesting Dependabot $actionName for PR #$specificNum...")
            val result = GhaDependabotManager.rebaseDependabotPr(rootDir, token, specificNum, isRecreate)
            if (result.isSuccess) {
                logger.lifecycle("✅ Commented '@dependabot $actionName' on PR #$specificNum successfully.")
            } else {
                logger.error("❌ Failed to request Dependabot $actionName for PR #$specificNum: ${result.stderr.ifEmpty { result.stdout }}")
            }
        } else if (isRebaseAll) {
            logger.lifecycle("🤖 [GHA Dependabot Rebase] Fetching all open Dependabot PRs...")
            val prs = GhaDependabotManager.listDependabotPrs(rootDir, token)
            if (prs.isEmpty()) {
                logger.lifecycle("   No open Dependabot PRs to $actionName.")
                return
            }

            logger.lifecycle("   Requesting Dependabot $actionName for ${prs.size} PR(s)...")
            prs.forEach { pr ->
                val result = GhaDependabotManager.rebaseDependabotPr(rootDir, token, pr.number, isRecreate)
                if (result.isSuccess) {
                    logger.lifecycle("   ✅ Requested $actionName for #%-5d ${pr.title}".format(pr.number))
                } else {
                    logger.error("   ❌ Failed #%-5d ${pr.title}: ${result.stderr.ifEmpty { result.stdout }}".format(pr.number))
                }
            }
        } else {
            logger.error("❌ Specify Dependabot PR number (-PprNumber=123) or rebase all (-PrebaseAll=true).")
        }
    }
}
