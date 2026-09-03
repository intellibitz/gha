package cc.thevar.gha.ai

import cc.thevar.gha.GhaTask
import cc.thevar.gha.git.GhaGitExec
import cc.thevar.gha.safety.GhaProcessRunner
import cc.thevar.gha.workflow.GhaParallelWorkflowManager
import org.gradle.api.provider.Property
import org.gradle.api.tasks.Input
import org.gradle.api.tasks.Optional
import org.gradle.api.tasks.TaskAction
import org.gradle.work.DisableCachingByDefault

/**
 * 0 Effort, 100% Gain Autonomous AI Workflow Task (`ghai`, `ghaAI`, `ghaAuto`, `ghaSync`, `ghaSave`).
 * Intelligently handles both DIRTY (local changes) and CLEAN (post-push / GitHub PR & CI check) workflows.
 */
@DisableCachingByDefault(because = "Executes autonomous AI context workflow actions")
abstract class GhaAiTask : GhaTask() {

    @get:Input
    @get:Optional
    abstract val baseBranch: Property<String>

    @get:Input
    @get:Optional
    abstract val userBranch: Property<String>

    @get:Input
    @get:Optional
    abstract val commitMessage: Property<String>

    @get:Input
    @get:Optional
    abstract val autoMerge: Property<String>

    @get:Input
    @get:Optional
    abstract val mergeMethod: Property<String>

    init {
        baseBranch.convention(project.providers.gradleProperty("baseBranch").orElse("main"))
        userBranch.convention(project.providers.gradleProperty("userBranch").orElse(project.providers.gradleProperty("branch")))
        commitMessage.convention(
            project.providers.gradleProperty("commitMessage")
                .orElse(project.providers.gradleProperty("message"))
        )
        autoMerge.convention(project.providers.gradleProperty("autoMerge").orElse("true"))
        mergeMethod.convention(project.providers.gradleProperty("mergeMethod").orElse("squash"))
    }

    @TaskAction
    fun execute() {
        val rootDir = projectRootDir.get().asFile
        val token = gitHubToken.orNull
        val base = baseBranch.get()
        val customBranch = userBranch.orNull
        val explicitMsg = commitMessage.orNull
        val method = mergeMethod.get()

        val isDirty = !GhaGitExec.isClean(rootDir)
        val currentBranch = GhaGitExec.currentBranch(rootDir)

        logger.lifecycle("🤖 [ghai] 0 Effort, 100% Gain — Branch: '$currentBranch', Working Tree Dirty: $isDirty")

        // Step 1: Ensure safe working branch
        val (headBranch, isAutoBranch) = GhaParallelWorkflowManager.prepareWorkingBranch(
            projectDir = rootDir,
            requestedBaseBranch = base,
            customUserBranch = customBranch,
        )
        val branchCategory = if (isAutoBranch) "GHA Auto-Branch" else "User Branch"

        if (isDirty) {
            // WORKFLOW A: DIRTY WORKING TREE (Local changes exist)
            logger.lifecycle("📦 [ghai] Detected dirty working tree. Executing local checkin & remote push workflow...")
            val smartMsg = GhaAiManager.detectSmartCommitMessage(rootDir, explicitMsg)

            logger.lifecycle("📦 Staging working tree changes...")
            GhaGitExec.exec(rootDir, "add", "-A")

            logger.lifecycle("📝 Auto-committing: \"$smartMsg\"...")
            GhaGitExec.exec(rootDir, "commit", "-m", smartMsg)

            logger.lifecycle("🔄 Rebase sync with 'origin/$base'...")
            GhaParallelWorkflowManager.syncWithRemoteBase(rootDir, base)

            logger.lifecycle("🚀 Pushing '$headBranch' to origin remote...")
            GhaGitExec.push(rootDir, "origin", headBranch, setUpstream = true)

            logger.lifecycle("🔀 Managing Pull Request against protected '$base'...")
            val (prOk, prInfo) = GhaParallelWorkflowManager.createOrUpdatePr(
                projectDir = rootDir,
                token = token,
                baseBranch = base,
                headBranch = headBranch,
                title = smartMsg,
                body = "Automated contribution created by ghai.",
            )

            if (prOk && prInfo != null) {
                logger.lifecycle("✅ Pull Request #${prInfo.number} active on GitHub: ${prInfo.url}")
                val env = if (!token.isNullOrBlank()) mapOf("GITHUB_TOKEN" to token, "GH_TOKEN" to token) else emptyMap()
                GhaProcessRunner.exec(
                    workingDir = rootDir,
                    command = listOf("gh", "pr", "merge", prInfo.number.toString(), "--squash", "--delete-branch", "--auto"),
                    extraEnv = env,
                    timeoutSeconds = 30L,
                )
                logger.lifecycle("🤖 GitHub Auto-Merge enabled for PR #${prInfo.number}.")
            }

            logger.lifecycle("🎉 [ghai] Complete! Work saved, committed, pushed, and PR auto-merge requested.")
            logger.lifecycle("   Branch: $headBranch ($branchCategory)")
            logger.lifecycle("   Commit Message: \"$smartMsg\"")

        } else {
            // WORKFLOW B: CLEAN WORKING TREE (Post-push or clean local state)
            logger.lifecycle("✨ [ghai] Detected clean working tree. Checking GitHub PR and CI check status...")

            // Rebase sync with remote base first
            GhaParallelWorkflowManager.syncWithRemoteBase(rootDir, base)

            val openPr = GhaParallelWorkflowManager.findOpenPr(rootDir, token, headBranch, base)
            if (openPr != null) {
                logger.lifecycle("🔀 Active PR #${openPr.number} found: ${openPr.url}")
                val ciStatus = GhaAiManager.checkPrCiStatus(rootDir, token, openPr.number)
                logger.lifecycle("📊 CI Check Status: ${ciStatus.ciStatus} (Mergeable: ${ciStatus.isMergeable})")

                when (ciStatus.ciStatus) {
                    GhaAiManager.CiStatus.PASSED, GhaAiManager.CiStatus.NO_CHECKS -> {
                        logger.lifecycle("🎉 [ghai] CI checks PASSED for PR #${openPr.number}! Auto-merging into '$base'...")
                        val merged = GhaParallelWorkflowManager.mergeAndCleanup(
                            projectDir = rootDir,
                            token = token,
                            prNumber = openPr.number,
                            headBranch = headBranch,
                            baseBranch = base,
                            mergeMethod = method,
                            autoMerge = true,
                        )

                        if (merged) {
                            if (isAutoBranch) {
                                logger.lifecycle("🗑️ Merged PR #${openPr.number} and auto-cleaned branch '$headBranch'.")
                            } else {
                                logger.lifecycle("🛡️ Merged PR #${openPr.number} and preserved user branch '$headBranch'.")
                            }
                            GhaGitExec.checkout(rootDir, base)
                            GhaGitExec.pullRebase(rootDir, "origin", base)
                            logger.lifecycle("✅ Local repository is 100% synced with merged origin/$base.")
                        } else {
                            logger.lifecycle("ℹ️ Merge requested on GitHub for PR #${openPr.number}.")
                        }
                    }
                    GhaAiManager.CiStatus.PENDING -> {
                        logger.lifecycle("⏳ [ghai] CI checks in progress for PR #${openPr.number}. Enabling GitHub auto-merge...")
                        val env = if (!token.isNullOrBlank()) mapOf("GITHUB_TOKEN" to token, "GH_TOKEN" to token) else emptyMap()
                        GhaProcessRunner.exec(
                            workingDir = rootDir,
                            command = listOf("gh", "pr", "merge", openPr.number.toString(), "--squash", "--delete-branch", "--auto"),
                            extraEnv = env,
                            timeoutSeconds = 30L,
                        )
                        logger.lifecycle("🤖 Auto-merge enabled on GitHub. PR #${openPr.number} will merge automatically once CI passes.")
                    }
                    GhaAiManager.CiStatus.FAILED -> {
                        logger.lifecycle("❌ [ghai] CI checks FAILED for PR #${openPr.number}. Please check build logs at ${openPr.url}")
                    }
                }
            } else {
                logger.lifecycle("ℹ️ No open PR found for branch '$headBranch'. Checking commit log relative to origin/$base...")
                if (GhaAiManager.isBranchAheadOfRemote(rootDir, base) && headBranch != base) {
                    logger.lifecycle("🚀 Local branch '$headBranch' is ahead of '$base'. Creating Pull Request...")
                    val lastCommitMsg = GhaGitExec.exec(rootDir, "log", "-1", "--pretty=%s").stdout.ifBlank { "Update $headBranch" }
                    val (prOk, prInfo) = GhaParallelWorkflowManager.createOrUpdatePr(
                        projectDir = rootDir,
                        token = token,
                        baseBranch = base,
                        headBranch = headBranch,
                        title = lastCommitMsg,
                        body = "Automated contribution created by ghai.",
                    )
                    if (prOk && prInfo != null) {
                        logger.lifecycle("✅ Created Pull Request #${prInfo.number}: ${prInfo.url}")
                    }
                } else {
                    logger.lifecycle("✅ Local repository is 100% clean and fully synced with origin/$base.")
                }
            }
        }
    }
}
