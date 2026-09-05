package cc.thevar.gha.workflow

import cc.thevar.gha.ai.GhaAiManager
import cc.thevar.gha.git.GhaGitExec
import cc.thevar.gha.safety.GhaProcessRunner
import cc.thevar.gha.safety.GhaVersionManager
import java.io.File
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

/**
 * Enterprise engine for worldwide parallel developer collaboration on protected branches.
 */
object GhaParallelWorkflowManager {

    private val PROTECTED_BRANCH_PATTERNS = listOf(
        "main", "master", "production", "release", "stable", "dev", "development",
    )

    private val AUTO_BRANCH_PREFIXES = listOf(
        "gha/", "gha-auto/", "gha-fix/", "gha-tmp/", "dependabot/", "gha-",
    )

    data class ExistingPrInfo(
        val number: Int,
        val title: String,
        val state: String,
        val url: String,
    )

    data class OpenPrDetails(
        val number: Int,
        val headBranch: String,
        val baseBranch: String,
        val title: String,
        val url: String,
    )

    /**
     * Checks if a branch name matches known protected base branch conventions.
     */
    fun isProtectedBranch(branchName: String): Boolean {
        val lower = branchName.lowercase().trim()
        return PROTECTED_BRANCH_PATTERNS.any { pattern ->
            (lower == pattern) || lower.startsWith("$pattern/") || lower.startsWith("$pattern-")
        }
    }

    /**
     * Determines whether a branch was created automatically by GHA or manually by a user.
     */
    fun isAutoCreatedBranch(branchName: String): Boolean {
        val lower = branchName.lowercase().trim()
        return AUTO_BRANCH_PREFIXES.any { prefix -> lower.startsWith(prefix) } || lower.contains("[gha-auto]")
    }

    /**
     * Generates a unique, conflict-free GHA auto-branch name.
     */
    fun generateAutoBranchName(taskPrefix: String = "task"): String {
        val timestamp = SimpleDateFormat("yyyyMMdd-HHmmss", Locale.US).format(Date())
        val sanitizedPrefix = taskPrefix.lowercase().replace(Regex("[^a-z0-9_-]"), "-").take(20).trim('-')
        val prefix = sanitizedPrefix.ifBlank { "work" }
        return "gha-auto/$prefix-$timestamp"
    }

    /**
     * Ensures the repository is on a safe working branch.
     * Self-heals if the repository is stuck on a clean, stale auto-branch from a previous run.
     * Auto-initializes Git if run in a directory that is not yet a Git repository.
     */
    fun prepareWorkingBranch(
        projectDir: File,
        requestedBaseBranch: String = "main",
        customUserBranch: String? = null,
        token: String? = null,
    ): Pair<String, Boolean> {
        var current = GhaGitExec.currentBranch(projectDir)
        val targetBase = if (requestedBaseBranch.isNotBlank()) requestedBaseBranch else "main"

        // Auto-initialize Git repository if running in a brand new non-git folder
        if (current == "unknown" || !File(projectDir, ".git").exists()) {
            GhaGitExec.exec(projectDir, "init")
            GhaGitExec.exec(projectDir, "checkout", "-b", targetBase)
            current = GhaGitExec.currentBranch(projectDir).ifBlank { targetBase }
        }

        if (!isProtectedBranch(current)) {
            val isAuto = isAutoCreatedBranch(current)
            if (isAuto && GhaGitExec.isClean(projectDir)) {
                val openPr = findOpenPr(projectDir, token, current, targetBase)
                if (openPr == null) {
                    // Stale auto-branch recovery: switch back to base branch and delete stale auto-branch
                    GhaGitExec.checkout(projectDir, targetBase)
                    GhaGitExec.pullRebase(projectDir, "origin", targetBase)
                    GhaGitExec.deleteLocalBranch(projectDir, current, force = true)
                    current = targetBase
                } else {
                    return Pair(current, true)
                }
            } else {
                return Pair(current, isAuto)
            }
        }

        // We are on a protected branch (e.g., main).
        // If user explicit branch or default main mode is active, stay on targetBase (direct main sync)
        val targetBranch = if (!customUserBranch.isNullOrBlank()) {
            customUserBranch
        } else {
            targetBase
        }

        if (targetBranch == targetBase) {
            return Pair(targetBase, false)
        }

        val isAuto = isAutoCreatedBranch(targetBranch)
        val checkoutRes = GhaGitExec.checkout(
            workingDir = projectDir,
            branchName = targetBranch,
            createIfMissing = true,
            startPoint = targetBase,
        )
        if (!checkoutRes.isSuccess) {
            val fallback = generateAutoBranchName("fallback")
            GhaGitExec.checkout(projectDir, fallback, createIfMissing = true)
            return Pair(fallback, true)
        }

        return Pair(targetBranch, isAuto)
    }

    /**
     * Syncs current branch with remote base branch via rebase pull.
     * Aborts rebase safely if a merge conflict occurs to prevent leaving the repo in a broken state.
     */
    fun syncWithRemoteBase(projectDir: File, baseBranch: String, remoteName: String = "origin"): GhaProcessRunner.ProcessResult {
        GhaGitExec.fetch(projectDir, remoteName)
        val res = GhaGitExec.pullRebase(projectDir, remoteName, baseBranch)
        if (!res.isSuccess && (res.stderr.contains("conflict", ignoreCase = true) || res.stdout.contains("conflict", ignoreCase = true))) {
            GhaGitExec.exec(projectDir, "rebase", "--abort")
        }
        return res
    }

    /**
     * Finds an open PR targeting baseBranch with headBranch.
     */
    fun findOpenPr(projectDir: File, token: String?, headBranch: String, baseBranch: String): ExistingPrInfo? {
        val env = if (!token.isNullOrBlank()) mapOf("GITHUB_TOKEN" to token, "GH_TOKEN" to token) else emptyMap()

        val result = GhaProcessRunner.exec(
            workingDir = projectDir,
            command = listOf(
                "gh", "pr", "list",
                "--head", headBranch,
                "--base", baseBranch,
                "--state", "open",
                "--json", "number,title,state,url",
                "--template", "{{range .}}{{.number}}|{{.state}}|{{.title}}|{{.url}}{{\"\\n\"}}{{end}}",
            ),
            extraEnv = env,
            timeoutSeconds = 30L,
        )

        if (!result.isSuccess || result.stdout.isBlank()) return null

        val line = result.stdout.lines().firstOrNull { it.isNotBlank() } ?: return null
        val parts = line.split("|")
        if (parts.size < 4) return null

        val num = parts[0].toIntOrNull() ?: return null
        val state = parts[1]
        val title = parts[2]
        val url = parts[3]

        return ExistingPrInfo(num, title, state, url)
    }

    /**
     * Lists all open PRs targeting baseBranch (regardless of head branch).
     */
    fun listOpenPrsTargetingBase(projectDir: File, token: String?, baseBranch: String): List<OpenPrDetails> {
        val env = if (!token.isNullOrBlank()) mapOf("GITHUB_TOKEN" to token, "GH_TOKEN" to token) else emptyMap()

        val result = GhaProcessRunner.exec(
            workingDir = projectDir,
            command = listOf(
                "gh", "pr", "list",
                "--base", baseBranch,
                "--state", "open",
                "--json", "number,headRefName,baseRefName,title,url",
                "--template", "{{range .}}{{.number}}|{{.headRefName}}|{{.baseRefName}}|{{.title}}|{{.url}}{{\"\\n\"}}{{end}}",
            ),
            extraEnv = env,
            timeoutSeconds = 30L,
        )

        if (!result.isSuccess || result.stdout.isBlank()) return emptyList()

        return result.stdout.lines().filter { it.isNotBlank() }.mapNotNull { line ->
            val parts = line.split("|")
            if (parts.size >= 5) {
                val num = parts[0].toIntOrNull() ?: 0
                if (num > 0) {
                    OpenPrDetails(num, parts[1], parts[2], parts[3], parts[4])
                } else null
            } else null
        }
    }

    /**
     * Creates a new Pull Request or returns existing PR info if already created.
     */
    fun createOrUpdatePr(
        projectDir: File,
        token: String?,
        baseBranch: String,
        headBranch: String,
        title: String,
        body: String,
        draft: Boolean = false,
        reviewers: String? = null,
        labels: String? = null,
    ): Pair<Boolean, ExistingPrInfo?> {

        // Check if PR already exists
        val existing = findOpenPr(projectDir, token, headBranch, baseBranch)
        if (existing != null) return Pair(true, existing)

        val env = if (!token.isNullOrBlank()) mapOf("GITHUB_TOKEN" to token, "GH_TOKEN" to token) else emptyMap()
        val cmd = mutableListOf("gh", "pr", "create", "--base", baseBranch, "--head", headBranch, "--title", title, "--body", body)

        if (draft) cmd.add("--draft")
        reviewers?.takeIf { it.isNotBlank() }?.let { cmd.addAll(listOf("--reviewer", it)) }
        labels?.takeIf { it.isNotBlank() }?.let { cmd.addAll(listOf("--label", it)) }

        val createRes = GhaProcessRunner.exec(
            workingDir = projectDir,
            command = cmd,
            extraEnv = env,
            timeoutSeconds = 45L,
        )

        if (createRes.isSuccess) {
            val newlyCreated = findOpenPr(projectDir, token, headBranch, baseBranch)
            return Pair(true, newlyCreated)
        }

        return Pair(false, null)
    }

    /**
     * Merges PR and performs branch cleanup (deletes GHA auto-branches, preserves User-created branches).
     */
    fun mergeAndCleanup(
        projectDir: File,
        token: String?,
        prNumber: Int,
        headBranch: String,
        baseBranch: String,
        mergeMethod: String = "squash",
        autoMerge: Boolean = true,
    ): Boolean {

        val env = if (!token.isNullOrBlank()) mapOf("GITHUB_TOKEN" to token, "GH_TOKEN" to token) else emptyMap()
        val flag = when (mergeMethod.lowercase()) {
            "rebase" -> "--rebase"
            "merge" -> "--merge"
            else -> "--squash"
        }

        val cmd = mutableListOf("gh", "pr", "merge", prNumber.toString(), flag, "--delete-branch")
        if (autoMerge) {
            cmd.add("--auto")
        }

        val mergeRes = GhaProcessRunner.exec(
            workingDir = projectDir,
            command = cmd,
            extraEnv = env,
            timeoutSeconds = 60L,
        )

        val isAuto = isAutoCreatedBranch(headBranch)

        // Post-merge branch management
        if (isAuto) {
            // Auto-branch cleanup
            GhaGitExec.checkout(projectDir, baseBranch)
            GhaGitExec.pullRebase(projectDir, "origin", baseBranch)
            GhaGitExec.deleteLocalBranch(projectDir, headBranch, force = true)
            GhaGitExec.deleteRemoteBranch(projectDir, "origin", headBranch)
        } else {
            // User-created branch preservation
            GhaGitExec.checkout(projectDir, baseBranch)
            GhaGitExec.pullRebase(projectDir, "origin", baseBranch)
        }

        return mergeRes.isSuccess
    }

    /**
     * Sweeps and deletes stale remote auto-branches that have no open Pull Request.
     */
    fun sweepStaleRemoteBranches(projectDir: File, token: String?, baseBranch: String = "main"): Int {
        GhaGitExec.fetch(projectDir, "origin", prune = true)

        val remoteResult = GhaGitExec.exec(projectDir, "branch", "-r")
        if (!remoteResult.isSuccess || remoteResult.stdout.isBlank()) return 0

        val remoteBranches = remoteResult.stdout.lines()
            .map { it.trim() }
            .filter { it.startsWith("origin/") }
            .map { it.substringAfter("origin/") }
            .filter { isAutoCreatedBranch(it) && it != baseBranch }

        if (remoteBranches.isEmpty()) return 0

        val openPrs = listOpenPrsTargetingBase(projectDir, token, baseBranch)
        val openPrHeadBranches = openPrs.map { it.headBranch }.toSet()

        var deletedCount = 0
        for (branch in remoteBranches) {
            if (!openPrHeadBranches.contains(branch)) {
                val delRes = GhaGitExec.deleteRemoteBranch(projectDir, "origin", branch)
                if (delRes.isSuccess) {
                    deletedCount++
                }
            }
        }
        return deletedCount
    }

    /**
     * Executes the "0 Effort, 100% Gain" Autonomous AI Workflow.
     * Ported from GhaAiTask for singular orchestration by GMA.
     */
    fun executeAutonomousWorkflow(
        rootDir: File,
        token: String?,
        baseBranch: String = "main",
        customUserBranch: String? = null,
        explicitCommitMessage: String? = null,
        mergeMethod: String = "squash",
        log: (String) -> Unit = { println(it) }
    ): String {
        val method = mergeMethod.lowercase()

        // 0. Auto-Init VCS if not present
        if (!File(rootDir, ".git").exists()) {
            log("🌱 [GMA] Initializing new Git repository...")
            GhaGitExec.exec(rootDir, "init")
            log("✅ [GMA] Git repository initialized.")
        }

        val isDirty = !GhaGitExec.isClean(rootDir)
        val currentBranch = GhaGitExec.currentBranch(rootDir)
        val projectContext = GhaAiManager.detectProjectContext(rootDir)

        log("🤖 [GMA Interactor] Autonomous Workflow — Context: $projectContext")
        log("   ├── Current Branch  : '$currentBranch'")
        log("   └── Working Tree    : ${if (isDirty) "Dirty (Local Changes)" else "Clean"}")

        // Step 1: Ensure safe working branch
        val (headBranch, isAutoBranch) = prepareWorkingBranch(
            projectDir = rootDir,
            requestedBaseBranch = baseBranch,
            customUserBranch = customUserBranch,
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
        var tipRecommendation = "Make code edits anytime and run GMA to auto-save, sync, and push to GitHub."

        if (isDirty) {
            log("📦 [GMA] Detected dirty working tree. Executing local checkin & remote push workflow...")

            // Autonomous Version Bump
            val newVersion = GhaVersionManager.bumpVersion(rootDir)
            log("📈 [GMA Version Bump] Incremented version to $newVersion")

            val smartMsg = GhaAiManager.detectSmartCommitMessage(rootDir, explicitCommitMessage)
            commitSummary = "Committed: \"$smartMsg\""

            log("📦 Staging working tree changes...")
            GhaGitExec.exec(rootDir, "add", "-A")
            GhaGitExec.exec(rootDir, "update-index", "--chmod=+x", "ghai")
            GhaGitExec.exec(rootDir, "update-index", "--chmod=+x", "init/install.sh")
            GhaGitExec.exec(rootDir, "update-index", "--chmod=+x", "gradlew")

            log("📝 Auto-committing: \"$smartMsg\"...")
            GhaGitExec.exec(rootDir, "commit", "-m", smartMsg)

            // Push & PR logic
            syncWithRemoteBase(rootDir, baseBranch)

            log("🚀 Pushing '$headBranch' to origin remote...")
            val pushRes = GhaGitExec.push(rootDir, "origin", headBranch, setUpstream = true)
            pushSummary = if (pushRes.isSuccess) "Pushed to origin/$headBranch" else "Push attempted"

            if (headBranch != baseBranch) {
                log("🔀 Managing Pull Request against protected '$baseBranch'...")
                val (prOk, prInfo) = createOrUpdatePr(
                    projectDir = rootDir,
                    token = token,
                    baseBranch = baseBranch,
                    headBranch = headBranch,
                    title = smartMsg,
                    body = "Automated contribution created by GHA Master Agent.",
                )

                if (prOk && (prInfo != null)) {
                    prSummary = "PR #${prInfo.number} active"
                    prUrlSummary = prInfo.url
                    ciSummary = "PENDING / AUTO-MERGE"

                    log("✅ Pull Request #${prInfo.number} active on GitHub: ${prInfo.url}")
                    val env = if (!token.isNullOrBlank()) mapOf("GITHUB_TOKEN" to token, "GH_TOKEN" to token) else emptyMap()
                    GhaProcessRunner.exec(
                        workingDir = rootDir,
                        command = listOf("gh", "pr", "merge", prInfo.number.toString(), "--$method", "--delete-branch", "--auto"),
                        extraEnv = env,
                        timeoutSeconds = 30L,
                    )
                    log("🤖 GitHub Auto-Merge enabled for PR #${prInfo.number}.")
                }

                if (isAutoBranch) {
                    log("🔄 Work pushed & PR auto-merge active. Returning to clean '$baseBranch' branch...")
                    forceReturnToBaseBranch(rootDir, baseBranch, headBranch)
                    activeHeadBranch = baseBranch
                    activeBranchCategory = "Base Branch"
                }
            } else {
                prSummary = "Direct main push (No PR required)"
                ciSummary = "PASSED / DIRECT"
                log("✅ Direct push to '$baseBranch' completed. Version $newVersion is live!")
            }

            tipRecommendation = "Your changes are saved, pushed, and auto-merging on GitHub."

        } else {
            log("✨ [GMA] Detected clean working tree. Sweeping open PRs and checking CI status...")

            syncWithRemoteBase(rootDir, baseBranch)

            val openPrs = listOpenPrsTargetingBase(rootDir, token, baseBranch)
            if (openPrs.isNotEmpty()) {
                log("🔀 Found ${openPrs.size} open PR(s) targeting '$baseBranch'. Sweeping...")
                var mergedCount = 0
                var pendingCount = 0
                var failedCount = 0

                openPrs.forEach { pr ->
                    log("🔍 Inspecting PR #${pr.number} ('${pr.headBranch}')")
                    val ciStatus = GhaAiManager.checkPrCiStatus(rootDir, token, pr.number)

                    when (ciStatus.ciStatus) {
                        GhaAiManager.CiStatus.PASSED, GhaAiManager.CiStatus.NO_CHECKS -> {
                            val merged = mergeAndCleanup(
                                projectDir = rootDir,
                                token = token,
                                prNumber = pr.number,
                                headBranch = pr.headBranch,
                                baseBranch = baseBranch,
                                mergeMethod = method,
                                autoMerge = true,
                            )
                            if (merged) {
                                mergedCount++
                                log("✅ Merged PR #${pr.number} into '$baseBranch'!")
                            }
                        }
                        GhaAiManager.CiStatus.PENDING -> {
                            pendingCount++
                            val env = if (!token.isNullOrBlank()) mapOf("GITHUB_TOKEN" to token, "GH_TOKEN" to token) else emptyMap()
                            GhaProcessRunner.exec(
                                workingDir = rootDir,
                                command = listOf("gh", "pr", "merge", pr.number.toString(), "--$method", "--delete-branch", "--auto"),
                                extraEnv = env,
                                timeoutSeconds = 30L,
                            )
                        }
                        GhaAiManager.CiStatus.FAILED -> {
                            failedCount++
                            log("❌ PR #${pr.number} CI checks FAILED.")
                        }
                    }
                }

                if (mergedCount > 0) {
                    forceReturnToBaseBranch(rootDir, baseBranch, headBranch)
                    activeHeadBranch = baseBranch
                    activeBranchCategory = "Base Branch"
                    prSummary = "$mergedCount PR(s) MERGED"
                    ciSummary = "PASSED"
                } else if (pendingCount > 0) {
                    if (isAutoBranch && headBranch != baseBranch) {
                        forceReturnToBaseBranch(rootDir, baseBranch, headBranch)
                        activeHeadBranch = baseBranch
                        activeBranchCategory = "Base Branch"
                    }
                    prSummary = "$pendingCount PR(s) PENDING"
                    ciSummary = "PENDING"
                } else if (failedCount > 0) {
                    prSummary = "$failedCount PR(s) FAILED"
                    ciSummary = "FAILED"
                }
            } else {
                if (GhaAiManager.isBranchAheadOfRemote(rootDir, baseBranch) && headBranch != baseBranch) {
                    val lastCommitMsg = GhaGitExec.exec(rootDir, "log", "-1", "--pretty=%s").stdout.ifBlank { "Update $headBranch" }
                    val (prOk, prInfo) = createOrUpdatePr(rootDir, token, baseBranch, headBranch, lastCommitMsg, "Automated contribution.")
                    if (prOk && (prInfo != null)) {
                        prSummary = "PR #${prInfo.number} created"
                        prUrlSummary = prInfo.url
                        log("✅ Created Pull Request #${prInfo.number}: ${prInfo.url}")
                    }
                } else {
                    if (isAutoBranch && headBranch != baseBranch) {
                        forceReturnToBaseBranch(rootDir, baseBranch, headBranch)
                        activeHeadBranch = baseBranch
                        activeBranchCategory = "Base Branch"
                    }
                }
            }
        }

        // Sweepers
        log("🧹 [GMA Sweeper] Sweeping stale workflow runs and branches...")
        val prunedRuns = try {
            GhaWorkflowManager.pruneOldWorkflowRuns(rootDir, token, maxKeep = 5)
        } catch (_: Exception) { 0 }
        
        val prunedBranches = try {
            sweepStaleRemoteBranches(rootDir, token, baseBranch)
        } catch (_: Exception) { 0 }

        val summary = StringBuilder()
        summary.append("\n════════════════════════════════════════════════════════════════════════════════\n")
        summary.append("📋 [GMA Execution Summary]\n")
        summary.append("   • Working Branch : $activeHeadBranch ($activeBranchCategory)\n")
        summary.append("   • Commit Status  : $commitSummary\n")
        summary.append("   • Remote Push    : $pushSummary\n")
        summary.append("   • GitHub PR      : $prSummary ${if (prUrlSummary != "N/A") "($prUrlSummary)" else ""}\n")
        summary.append("   • CI/CD Status   : $ciSummary\n")
        summary.append("   • Swept Data     : $prunedRuns runs pruned, $prunedBranches branches pruned\n")
        summary.append("   • Local Sync     : 100% Synced with origin/$baseBranch\n")
        summary.append("────────────────────────────────────────────────────────────────────────────────\n")
        summary.append("💡 Tip: $tipRecommendation\n")
        summary.append("════════════════════════════════════════════════════════════════════════════════\n")
        
        return summary.toString()
    }

    private fun forceReturnToBaseBranch(rootDir: File, baseBranch: String, autoBranchToDelete: String) {
        if (!GhaGitExec.isClean(rootDir)) {
            GhaGitExec.exec(rootDir, "add", "-A")
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
    }
}
