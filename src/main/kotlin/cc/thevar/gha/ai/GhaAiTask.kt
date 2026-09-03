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
 * Intelligently handles both DIRTY (local changes) and CLEAN (post-push / GitHub PR & CI check) workflows,
 * printing a clear execution summary and an actionable one-line next step tip.
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

        logger.lifecycle("🤖 [ghai] 0 Effort, 100% Gain — Branch: '$currentBranch', Working Tree Dirty: $isDirty")

        // Step 1: Ensure safe working branch
        val (headBranch, isAutoBranch) = GhaParallelWorkflowManager.prepareWorkingBranch(
            projectDir = rootDir,
            requestedBaseBranch = base,
            customUserBranch = customBranch,
        )
        val branchCategory = if (isAutoBranch) "GHA Auto-Branch" else "User Branch"

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

            tipRecommendation = "Run './ghai' after GitHub CI builds finish to verify and complete auto-merge into $base."

        } else {
            // WORKFLOW B: CLEAN WORKING TREE (Post-push or clean local state)
            logger.lifecycle("✨ [ghai] Detected clean working tree. Checking GitHub PR and CI check status...")

            // Rebase sync with remote base first
            GhaParallelWorkflowManager.syncWithRemoteBase(rootDir, base)

            val openPr = GhaParallelWorkflowManager.findOpenPr(rootDir, token, headBranch, base)
            if (openPr != null) {
                prSummary = "PR #${openPr.number} open"
                prUrlSummary = openPr.url
                logger.lifecycle("🔀 Active PR #${openPr.number} found: ${openPr.url}")

                val ciStatus = GhaAiManager.checkPrCiStatus(rootDir, token, openPr.number)
                ciSummary = ciStatus.ciStatus.name
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
                            prSummary = "PR #${openPr.number} MERGED"
                            if (isAutoBranch) {
                                logger.lifecycle("🗑️ Merged PR #${openPr.number} and auto-cleaned branch '$headBranch'.")
                            } else {
                                logger.lifecycle("🛡️ Merged PR #${openPr.number} and preserved user branch '$headBranch'.")
                            }
                            GhaGitExec.checkout(rootDir, base)
                            GhaGitExec.pullRebase(rootDir, "origin", base)
                            logger.lifecycle("✅ Local repository is 100% synced with merged origin/$base.")
                            tipRecommendation = "Start your next feature or type './gradlew ghaStatus' to inspect repo health."
                        } else {
                            tipRecommendation = "Merge requested on GitHub for PR #${openPr.number}. Re-run './ghai' in a moment."
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
                        tipRecommendation = "CI checks in progress. Run './ghai' in a moment to auto-verify and complete merge."
                    }
                    GhaAiManager.CiStatus.FAILED -> {
                        logger.lifecycle("❌ [ghai] CI checks FAILED for PR #${openPr.number}. Please check build logs at ${openPr.url}")
                        tipRecommendation = "Run './gradlew ghaWorkflowList' or visit $prUrlSummary to view build failure logs."
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
                    if (prOk && (prInfo != null)) {
                        prSummary = "PR #${prInfo.number} created"
                        prUrlSummary = prInfo.url
                        logger.lifecycle("✅ Created Pull Request #${prInfo.number}: ${prInfo.url}")
                        tipRecommendation = "PR #${prInfo.number} created! Run './ghai' after CI builds finish to merge into $base."
                    }
                } else {
                    logger.lifecycle("✅ Local repository is 100% clean and fully synced with origin/$base.")
                    tipRecommendation = "Make code edits anytime and type './ghai' to auto-save, sync, and push to GitHub."
                }
            }
        }

        // Print Structured Summary & Actionable One-Line Tip
        logger.lifecycle("")
        logger.lifecycle("════════════════════════════════════════════════════════════════════════════════")
        logger.lifecycle("📋 [ghai Execution Summary]")
        logger.lifecycle("   • Working Branch : $headBranch ($branchCategory)")
        logger.lifecycle("   • Commit Status  : $commitSummary")
        logger.lifecycle("   • Remote Push    : $pushSummary")
        logger.lifecycle("   • GitHub PR      : $prSummary ${if (prUrlSummary != "N/A") "($prUrlSummary)" else ""}")
        logger.lifecycle("   • CI/CD Status   : $ciSummary")
        logger.lifecycle("   • Local Sync     : 100% Synced with origin/$base")
        logger.lifecycle("────────────────────────────────────────────────────────────────────────────────")
        logger.lifecycle("💡 Tip: $tipRecommendation")
        logger.lifecycle("════════════════════════════════════════════════════════════════════════════════")
    }
}
