package cc.thevar.gha.workflow

import cc.thevar.gha.GhaTask
import cc.thevar.gha.git.GhaGitExec
import cc.thevar.gha.safety.GhaVersionManager
import org.gradle.api.provider.Property
import org.gradle.api.tasks.Input
import org.gradle.api.tasks.Optional
import org.gradle.api.tasks.TaskAction
import org.gradle.work.DisableCachingByDefault

@DisableCachingByDefault(because = "Executes end-to-end enterprise parallel collaboration workflows on protected branches")
abstract class GhaParallelWorkflowTask : GhaTask() {

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
    abstract val prTitle: Property<String>

    @get:Input
    @get:Optional
    abstract val prBody: Property<String>

    @get:Input
    @get:Optional
    abstract val autoMerge: Property<String>

    @get:Input
    @get:Optional
    abstract val mergeMethod: Property<String>

    @get:Input
    @get:Optional
    abstract val prReviewers: Property<String>

    @get:Input
    @get:Optional
    abstract val prLabels: Property<String>

    init {
        baseBranch.convention(project.providers.gradleProperty("baseBranch").orElse("main"))
        userBranch.convention(project.providers.gradleProperty("userBranch").orElse(project.providers.gradleProperty("branch")))
        commitMessage.convention(
            project.providers.gradleProperty("commitMessage")
                .orElse(project.providers.gradleProperty("message"))
                .orElse("Automated update via GHA Parallel Workflow")
        )
        prTitle.convention(
            project.providers.gradleProperty("prTitle")
                .orElse("Automated Contribution via GHA Workflow")
        )
        prBody.convention(
            project.providers.gradleProperty("prBody")
                .orElse("Automated parallel contribution created by GHA.")
        )
        autoMerge.convention(project.providers.gradleProperty("autoMerge").orElse("false"))
        mergeMethod.convention(project.providers.gradleProperty("mergeMethod").orElse("squash"))
        prReviewers.convention(project.providers.gradleProperty("prReviewers"))
        prLabels.convention(project.providers.gradleProperty("prLabels"))
    }

    @TaskAction
    fun execute() {
        val rootDir = projectRootDir.get().asFile
        val token = gitHubToken.orNull
        val base = baseBranch.get()
        val customBranch = userBranch.orNull
        val msg = commitMessage.get()
        val title = prTitle.get()
        val body = prBody.get()
        val isAutoMerge = autoMerge.get().lowercase() == "true"
        val method = mergeMethod.get()
        val reviewers = prReviewers.orNull
        val labels = prLabels.orNull

        logger.lifecycle("🌐 [GHA Enterprise Parallel Workflow] Starting workflow for target base branch '$base'...")

        // Step 1: Ensure safe working branch (auto-branch if directly on protected base branch)
        val (headBranch, isAutoBranch) = GhaParallelWorkflowManager.prepareWorkingBranch(
            projectDir = rootDir,
            requestedBaseBranch = base,
            customUserBranch = customBranch
        )

        val branchType = if (isAutoBranch) "GHA Auto-Created" else "User-Created"
        logger.lifecycle("🌿 [Branch Strategy] Working branch: '$headBranch' ($branchType)")

        // Step 2: Sync working branch with upstream remote base
        logger.lifecycle("🔄 [GHA Sync] Pulling & rebasing from 'origin/$base'...")
        val syncRes = GhaParallelWorkflowManager.syncWithRemoteBase(rootDir, base)
        if (syncRes.isSuccess) {
            logger.lifecycle("✅ Rebase pull successful.")
        } else {
            logger.lifecycle("ℹ️ Rebase pull status: ${syncRes.stderr.ifEmpty { syncRes.stdout }}")
        }

        // Step 3: Checkin local changes
        logger.lifecycle("📦 [GHA Checkin] Staging working tree changes...")
        GhaGitExec.exec(rootDir, "add", "-A")

        if (!GhaGitExec.isClean(rootDir)) {
            logger.lifecycle("📝 Committing changes: \"$msg\"...")
            val commitRes = GhaGitExec.exec(rootDir, "commit", "-m", msg)
            if (commitRes.isSuccess) {
                logger.lifecycle("✅ Checkin committed.")
            }
        } else {
            logger.lifecycle("ℹ️ Working tree clean, skipping commit.")
        }

        // Step 4: Push head branch to remote
        val newVersion = GhaVersionManager.bumpAndCommitVersion(rootDir)
        logger.lifecycle("📈 [GHA Version Bump] Incremented version to $newVersion for push.")

        logger.lifecycle("🚀 [GHA Push] Pushing branch '$headBranch' to origin...")
        val pushRes = GhaGitExec.push(rootDir, "origin", headBranch, setUpstream = true)
        if (!pushRes.isSuccess) {
            logger.lifecycle("ℹ️ Push info: ${pushRes.stderr.ifEmpty { pushRes.stdout }}")
        } else {
            logger.lifecycle("✅ Branch '$headBranch' pushed successfully.")
        }

        // Step 5: Create or retrieve existing PR (Loop & Duplication Guard)
        logger.lifecycle("🔀 [GHA PR] Managing Pull Request against protected base branch '$base'...")
        val (prSuccess, prInfo) = GhaParallelWorkflowManager.createOrUpdatePr(
            projectDir = rootDir,
            token = token,
            baseBranch = base,
            headBranch = headBranch,
            title = title,
            body = body,
            reviewers = reviewers,
            labels = labels
        )

        if (!prSuccess || prInfo == null) {
            logger.lifecycle("ℹ️ PR status: Active or waiting on upstream branch state.")
            return
        }

        logger.lifecycle("✅ Pull Request #${prInfo.number} active: ${prInfo.url}")

        // Step 6: Automated Merge & Smart Branch Cleanup
        if (isAutoMerge) {
            logger.lifecycle("🔀 [GHA PR Merge] Auto-merging PR #${prInfo.number} (method: $method)...")
            val mergeOk = GhaParallelWorkflowManager.mergeAndCleanup(
                projectDir = rootDir,
                token = token,
                prNumber = prInfo.number,
                headBranch = headBranch,
                baseBranch = base,
                mergeMethod = method,
                autoMerge = true
            )

            if (mergeOk) {
                if (isAutoBranch) {
                    logger.lifecycle("🗑️ [GHA Auto Cleanup] Merged PR #${prInfo.number} and cleaned up auto-created branch '$headBranch' (local & remote).")
                } else {
                    logger.lifecycle("🛡️ [GHA Branch Preservation] Merged PR #${prInfo.number} and preserved user-created branch '$headBranch'.")
                }
            } else {
                logger.lifecycle("ℹ️ Merge requested / pending CI checks or review approvals.")
            }
        } else {
            if (isAutoBranch) {
                logger.lifecycle("ℹ️ GHA auto-created branch '$headBranch' will be automatically deleted upon PR merge.")
            } else {
                logger.lifecycle("🛡️ User-created branch '$headBranch' is preserved.")
            }
        }

        logger.lifecycle("✅ [GHA Enterprise Parallel Workflow] Completed successfully.")
    }
}
