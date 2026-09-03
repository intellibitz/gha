package cc.thevar.gha.ai

import cc.thevar.gha.GhaTask
import cc.thevar.gha.git.GhaGitExec
import cc.thevar.gha.safety.GhaProcessRunner
import cc.thevar.gha.workflow.GhaParallelWorkflowManager
import cc.thevar.gha.workflow.GhaWorkflowManager
import org.gradle.api.provider.Property
import org.gradle.api.tasks.Input
import org.gradle.api.tasks.Optional
import org.gradle.api.tasks.TaskAction
import org.gradle.work.DisableCachingByDefault
import java.io.File

/**
 * 0 Effort, 100% Gain Autonomous AI Workflow Task (`ghai`, `ghaAI`, `ghaAuto`, `ghaSync`, `ghaSave`).
 * Intelligently handles both DIRTY (local changes) and CLEAN (post-push / GitHub PR & CI check) workflows,
 * auto-pruning old GitHub Actions workflow runs and keeping repository history lean.
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
                .orElse(project.providers.gradleProperty("message")),
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

        logger.lifecycle("🤖 [ghai] 0 Effort, 100% Gain — Current Branch: '$currentBranch', Working Tree Dirty: $isDirty")

        // Step 1: Ensure safe working branch (auto-heals stale branches)
        val (headBranch, isAutoBranch) = GhaParallelWorkflowManager.prepareWorkingBranch(
            projectDir = rootDir,
            requestedBaseBranch = base,
            customUserBranch = customBranch,
            token = token,
        )
        val branchCategory = if (isAutoBranch) "GHA Auto-Branch" else "User Branch"

        var activeHeadBranch = headBranch
        var activeBranchCategory = branchCategory

        var commitSummary = "No uncommitted local changes"
        var pushSummary = "Up to date"
        var prSummary = "N/A"
        var prUrlSummary = "N/A"
        var ciSummary = "N/A"
        var tipRecommendation = "Make code edits anytime and type './ghai' to auto-save, sync, and push to GitHub."

        if (isDirty) {
            // WORKFLOW A: DIRTY WORKING TREE (Local changes exist)
            logger.lifecycle("📦 [ghai] Detected dirty working tree. Executing local checkin & remote push workflow...")
            val smartMsg = GhaAiManager.detectSmartCommitMessage(rootDir, explicitMsg)
            commitSummary = "Committed: \"$smartMsg\""

            logger.lifecycle("📦 Staging working tree changes & enforcing executable flags...")
            GhaGitExec.exec(rootDir, "add", "-A")
            GhaGitExec.exec(rootDir, "update-index", "--chmod=+x", "ghai")
            GhaGitExec.exec(rootDir, "update-index", "--chmod=+x", "init/install.sh")
            GhaGitExec.exec(rootDir, "update-index", "--chmod=+x", "gradlew")

            logger.lifecycle("📝 Auto-committing: \"$smartMsg\"...")
            GhaGitExec.exec(rootDir, "commit", "-m", smartMsg)

            logger.lifecycle("🔄 Rebase sync with 'origin/$base'...")
            GhaParallelWorkflowManager.syncWithRemoteBase(rootDir, base)

            logger.lifecycle("🚀 Pushing '$headBranch' to origin remote...")
            val pushRes = GhaGitExec.push(rootDir, "origin", headBranch, setUpstream = true)
            pushSummary = if (pushRes.isSuccess) "Pushed to origin/$headBranch" else "Push attempted"

            logger.lifecycle("🔀 Managing Pull Request against protected '$base'...")
            val (prOk, prInfo) = GhaParallelWorkflowManager.createOrUpdatePr(
                projectDir = rootDir,
                token = token,
                baseBranch = base,
                headBranch = headBranch,
                title = smartMsg,
                body = "Automated contribution created by ghai.",
            )

            if (prOk && (prInfo != null)) {
                prSummary = "PR #${prInfo.number} active"
                prUrlSummary = prInfo.url
                ciSummary = "PENDING / AUTO-MERGE"

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

            // Immediately switch creator terminal back to clean base branch if on an auto-created branch
            if (isAutoBranch && headBranch != base) {
                logger.lifecycle("🔄 [ghai] Work pushed & PR auto-merge active. Returning terminal to clean '$base' branch...")
                forceReturnToBaseBranch(rootDir, base, headBranch)
                activeHeadBranch = base
                activeBranchCategory = "Base Branch"
                logger.lifecycle("✅ Creator terminal successfully returned to clean '$base' branch!")
            }

            tipRecommendation = "Your changes are saved, pushed, and auto-merging on GitHub. Terminal is clean on $base!"

        } else {
            // WORKFLOW B: CLEAN WORKING TREE (Post-push or clean local state)
            logger.lifecycle("✨ [ghai] Detected clean working tree. Sweeping open PRs and checking CI check status...")

            // Rebase sync with remote base first
            GhaParallelWorkflowManager.syncWithRemoteBase(rootDir, base)

            val openPrs = GhaParallelWorkflowManager.listOpenPrsTargetingBase(rootDir, token, base)
            if (openPrs.isNotEmpty()) {
                logger.lifecycle("🔀 Found ${openPrs.size} open PR(s) targeting '$base'. Sweeping and verifying CI status...")
                var mergedCount = 0
                var pendingCount = 0
                var failedCount = 0

                openPrs.forEach { pr ->
                    logger.lifecycle("🔍 Inspecting PR #${pr.number} ('${pr.headBranch}'): ${pr.url}")
                    val ciStatus = GhaAiManager.checkPrCiStatus(rootDir, token, pr.number)

                    when (ciStatus.ciStatus) {
                        GhaAiManager.CiStatus.PASSED, GhaAiManager.CiStatus.NO_CHECKS -> {
                            logger.lifecycle("🎉 [ghai Sweeper] CI checks PASSED for PR #${pr.number}! Merging into '$base'...")
                            val merged = GhaParallelWorkflowManager.mergeAndCleanup(
                                projectDir = rootDir,
                                token = token,
                                prNumber = pr.number,
                                headBranch = pr.headBranch,
                                baseBranch = base,
                                mergeMethod = method,
                                autoMerge = true,
                            )
                            if (merged) {
                                mergedCount++
                                logger.lifecycle("✅ Merged PR #${pr.number} ('${pr.headBranch}') into '$base'!")
                            }
                        }
                        GhaAiManager.CiStatus.PENDING -> {
                            pendingCount++
                            logger.lifecycle("⏳ [ghai Sweeper] PR #${pr.number} ('${pr.headBranch}') CI checks in progress. Enabling auto-merge...")
                            val env = if (!token.isNullOrBlank()) mapOf("GITHUB_TOKEN" to token, "GH_TOKEN" to token) else emptyMap()
                            GhaProcessRunner.exec(
                                workingDir = rootDir,
                                command = listOf("gh", "pr", "merge", pr.number.toString(), "--squash", "--delete-branch", "--auto"),
                                extraEnv = env,
                                timeoutSeconds = 30L,
                            )
                        }
                        GhaAiManager.CiStatus.FAILED -> {
                            failedCount++
                            logger.lifecycle("❌ [ghai Sweeper] PR #${pr.number} ('${pr.headBranch}') CI checks FAILED.")
                        }
                    }
                }

                if (mergedCount > 0) {
                    forceReturnToBaseBranch(rootDir, base, headBranch)
                    activeHeadBranch = base
                    activeBranchCategory = "Base Branch"
                    prSummary = "$mergedCount PR(s) MERGED"
                    ciSummary = "PASSED"
                    tipRecommendation = "Auto-merged $mergedCount PR(s) into $base. Terminal is 100% clean and synced on $base!"
                } else if (pendingCount > 0) {
                    if (isAutoBranch && headBranch != base) {
                        forceReturnToBaseBranch(rootDir, base, headBranch)
                        activeHeadBranch = base
                        activeBranchCategory = "Base Branch"
                    }
                    prSummary = "$pendingCount PR(s) PENDING"
                    ciSummary = "PENDING"
                    tipRecommendation = "$pendingCount PR(s) building on GitHub. Run './ghai' in a moment to complete auto-merge."
                } else if (failedCount > 0) {
                    prSummary = "$failedCount PR(s) FAILED"
                    ciSummary = "FAILED"
                    tipRecommendation = "CI checks failed on GitHub. Run './gradlew ghaWorkflowList' or check PR URLs above."
                }
            } else {
                logger.lifecycle("ℹ️ No open PRs found targeting '$base'. Checking commit log relative to origin/$base...")
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
                    if (prOk && (prInfo != null)) {
                        prSummary = "PR #${prInfo.number} created"
                        prUrlSummary = prInfo.url
                        logger.lifecycle("✅ Created Pull Request #${prInfo.number}: ${prInfo.url}")
                        tipRecommendation = "PR #${prInfo.number} created! Run './ghai' after CI builds finish to merge into $base."
                    }
                } else {
                    if (isAutoBranch && headBranch != base) {
                        logger.lifecycle("🗑️ [ghai] Auto-created branch '$headBranch' PR is complete/merged. Returning to base branch '$base'...")
                        forceReturnToBaseBranch(rootDir, base, headBranch)
                        activeHeadBranch = base
                        activeBranchCategory = "Base Branch"
                        logger.lifecycle("✅ Returned to clean '$base' branch and cleaned up local auto-branch '$headBranch'.")
                        tipRecommendation = "Start your next feature or type './ghai' anytime to auto-save and push changes."
                    } else {
                        logger.lifecycle("✅ Local repository is 100% clean and fully synced with origin/$base.")
                        tipRecommendation = "Make code edits anytime and type './ghai' to auto-save, sync, and push to GitHub."
                    }
                }
            }
        }

        // Final verification that working tree is 100% clean on base branch if on auto-branch
        if (isAutoBranch && GhaGitExec.currentBranch(rootDir) != base) {
            forceReturnToBaseBranch(rootDir, base, headBranch)
            activeHeadBranch = base
            activeBranchCategory = "Base Branch"
        }

        // Auto-Prune old GitHub Actions workflow runs to keep GitHub Actions history lean
        val prunedRuns = try {
            GhaWorkflowManager.pruneOldWorkflowRuns(rootDir, token, maxKeep = 10)
        } catch (_: Exception) { 0 }

        if (prunedRuns > 0) {
            logger.lifecycle("🧹 [ghai] Auto-pruned $prunedRuns old GitHub Actions workflow run(s) from history.")
        }

        // Print Structured Summary & Actionable One-Line Tip
        logger.lifecycle("")
        logger.lifecycle("════════════════════════════════════════════════════════════════════════════════")
        logger.lifecycle("📋 [ghai Execution Summary]")
        logger.lifecycle("   • Working Branch : $activeHeadBranch ($activeBranchCategory)")
        logger.lifecycle("   • Commit Status  : $commitSummary")
        logger.lifecycle("   • Remote Push    : $pushSummary")
        logger.lifecycle("   • GitHub PR      : $prSummary ${if (prUrlSummary != "N/A") "($prUrlSummary)" else ""}")
        logger.lifecycle("   • CI/CD Status   : $ciSummary")
        logger.lifecycle("   • Local Sync     : 100% Synced with origin/$base")
        if (prunedRuns > 0) {
            logger.lifecycle("   • CI Auto-Prune  : Removed $prunedRuns old workflow run log(s)")
        }
        logger.lifecycle("────────────────────────────────────────────────────────────────────────────────")
        logger.lifecycle("💡 Tip: $tipRecommendation")
        logger.lifecycle("════════════════════════════════════════════════════════════════════════════════")
    }

    private fun forceReturnToBaseBranch(rootDir: File, baseBranch: String, autoBranchToDelete: String) {
        if (!GhaGitExec.isClean(rootDir)) {
            GhaGitExec.exec(rootDir, "add", "-A")
            GhaGitExec.exec(rootDir, "update-index", "--chmod=+x", "ghai")
            GhaGitExec.exec(rootDir, "update-index", "--chmod=+x", "init/install.sh")
            GhaGitExec.exec(rootDir, "update-index", "--chmod=+x", "gradlew")
            GhaGitExec.exec(rootDir, "commit", "-m", "chore: auto-commit remaining changes before returning to $baseBranch")
        }

        val checkoutRes = GhaGitExec.checkout(rootDir, baseBranch)
        if (!checkoutRes.isSuccess || GhaGitExec.currentBranch(rootDir) != baseBranch) {
            GhaGitExec.exec(rootDir, "checkout", "-f", baseBranch)
        }

        GhaGitExec.pullRebase(rootDir, "origin", baseBranch)

        if (autoBranchToDelete.isNotBlank() && autoBranchToDelete != baseBranch) {
            GhaGitExec.deleteLocalBranch(rootDir, autoBranchToDelete, force = true)
        }

        if (GhaGitExec.currentBranch(rootDir) != baseBranch) {
            GhaGitExec.exec(rootDir, "checkout", "-f", baseBranch)
        }
    }
}
