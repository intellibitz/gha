package cc.thevar.gha.ai

import cc.thevar.gha.GhaTask
import cc.thevar.gha.git.GhaGitExec
import cc.thevar.gha.workflow.GhaParallelWorkflowManager
import org.gradle.api.provider.Property
import org.gradle.api.tasks.Input
import org.gradle.api.tasks.Optional
import org.gradle.api.tasks.TaskAction
import org.gradle.work.DisableCachingByDefault

/**
 * 0 Effort, 100% Gain Autonomous AI Workflow Task (`ghaAI`, `ghaAuto`, `ghaSync`, `ghaSave`).
 * Automatically analyzes project state, stages changes, generates smart commit messages,
 * syncs with remote rebase, pushes to GitHub, and manages Pull Requests seamlessly.
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
        autoMerge.convention(project.providers.gradleProperty("autoMerge").orElse("false"))
        mergeMethod.convention(project.providers.gradleProperty("mergeMethod").orElse("squash"))
    }

    @TaskAction
    fun execute() {
        val rootDir = projectRootDir.get().asFile
        val token = gitHubToken.orNull
        val base = baseBranch.get()
        val customBranch = userBranch.orNull
        val explicitMsg = commitMessage.orNull
        val isAutoMerge = autoMerge.get().lowercase() == "true"
        val method = mergeMethod.get()

        logger.lifecycle("🤖 [ghaAI] 0 Effort, 100% Gain — Analyzing project context...")

        // Step 1: Detect AI Smart Commit Message
        val smartMsg = GhaAiManager.detectSmartCommitMessage(rootDir, explicitMsg)

        // Step 2: Ensure safe working branch
        val (headBranch, isAutoBranch) = GhaParallelWorkflowManager.prepareWorkingBranch(
            projectDir = rootDir,
            requestedBaseBranch = base,
            customUserBranch = customBranch,
        )

        val branchCategory = if (isAutoBranch) "GHA Auto-Branch" else "User Branch"
        logger.lifecycle("🌿 [ghaAI] Working Branch: '$headBranch' ($branchCategory)")

        // Step 3: Rebase Sync with upstream
        logger.lifecycle("🔄 [ghaAI] Syncing with 'origin/$base' via rebase...")
        val syncRes = GhaParallelWorkflowManager.syncWithRemoteBase(rootDir, base)
        if (syncRes.isSuccess) {
            logger.lifecycle("✅ Local branch rebased and 100% synced with origin/$base.")
        } else {
            logger.lifecycle("ℹ️ Sync status: ${syncRes.stderr.ifEmpty { syncRes.stdout }}")
        }

        // Step 4: Stage & Auto-Commit
        logger.lifecycle("📦 [ghaAI] Staging working tree changes...")
        GhaGitExec.exec(rootDir, "add", "-A")

        if (!GhaGitExec.isClean(rootDir)) {
            logger.lifecycle("📝 [ghaAI] Auto-committing: \"$smartMsg\"...")
            val commitRes = GhaGitExec.exec(rootDir, "commit", "-m", smartMsg)
            if (commitRes.isSuccess) {
                logger.lifecycle("✅ Work saved and committed successfully.")
            }
        } else {
            logger.lifecycle("ℹ️ Working tree clean, no new changes to commit.")
        }

        // Step 5: Push to Remote
        logger.lifecycle("🚀 [ghaAI] Pushing '$headBranch' to origin remote...")
        val pushRes = GhaGitExec.push(rootDir, "origin", headBranch, setUpstream = true)
        if (pushRes.isSuccess) {
            logger.lifecycle("✅ Branch '$headBranch' pushed to GitHub remote.")
        } else {
            logger.lifecycle("ℹ️ Push info: ${pushRes.stderr.ifEmpty { pushRes.stdout }}")
        }

        // Step 6: Create or Update Pull Request
        logger.lifecycle("🔀 [ghaAI] Managing Pull Request against protected '$base'...")
        val (prOk, prInfo) = GhaParallelWorkflowManager.createOrUpdatePr(
            projectDir = rootDir,
            token = token,
            baseBranch = base,
            headBranch = headBranch,
            title = smartMsg,
            body = "Automated zero-effort contribution created by ghaAI.",
        )

        var finalPrUrl = "N/A"
        if (prOk && prInfo != null) {
            finalPrUrl = prInfo.url
            logger.lifecycle("✅ Pull Request #${prInfo.number} active on GitHub: ${prInfo.url}")

            if (isAutoMerge) {
                logger.lifecycle("🔀 [ghaAI] Auto-merging PR #${prInfo.number} (method: $method)...")
                val mergeOk = GhaParallelWorkflowManager.mergeAndCleanup(
                    projectDir = rootDir,
                    token = token,
                    prNumber = prInfo.number,
                    headBranch = headBranch,
                    baseBranch = base,
                    mergeMethod = method,
                    autoMerge = true,
                )

                if (mergeOk) {
                    if (isAutoBranch) {
                        logger.lifecycle("🗑️ [ghaAI] Merged PR #${prInfo.number} and auto-cleaned branch '$headBranch'.")
                    } else {
                        logger.lifecycle("🛡️ [ghaAI] Merged PR #${prInfo.number} and preserved user branch '$headBranch'.")
                    }
                }
            }
        }

        logger.lifecycle("🎉 [ghaAI] Complete! 0 Effort, 100% Gain.")
        logger.lifecycle("   Branch: $headBranch ($branchCategory)")
        logger.lifecycle("   Commit Message: \"$smartMsg\"")
        logger.lifecycle("   GitHub PR: $finalPrUrl")
    }
}
