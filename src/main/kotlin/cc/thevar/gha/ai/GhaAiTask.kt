package cc.thevar.gha.ai

import cc.thevar.gha.GhaTask
import cc.thevar.gha.config.GhaConfig
import cc.thevar.gha.git.GhaGitExec
import cc.thevar.gha.safety.GhaProcessRunner
import cc.thevar.gha.safety.GhaVersionManager
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
 * auto-sweeping GitHub Actions workflows on https://github.com/intellibitz/gha/actions every run and keeping history clean.
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
        val prov = project.providers
        baseBranch.convention(prov.gradleProperty("baseBranch").orElse("main"))
        userBranch.convention(prov.gradleProperty("userBranch").orElse(prov.gradleProperty("branch")))
        commitMessage.convention(
            prov.gradleProperty("commitMessage")
                .orElse(prov.gradleProperty("message")),
        )
        autoMerge.convention(prov.gradleProperty("autoMerge").orElse("true"))
        mergeMethod.convention(prov.gradleProperty("mergeMethod").orElse("squash"))
    }

    @TaskAction
    fun execute() {
        val explicitMsg = commitMessage.orNull
        val isTokenOnly = explicitMsg == "--token-only"
        
        if (isTokenOnly) {
            print(resolveToken())
            return
        }

        verifySandbox()
        val rootDir = projectRootDir.get().asFile
        val token = resolveToken()
        val base = baseBranch.getOrElse("main")
        val customBranch = userBranch.orNull
        val method = mergeMethod.getOrElse("squash")

        // 0. Auto-Init VCS if not present (Universal Vision)
        if (!vcs.isAvailable(rootDir)) {
            println("🌱 [ghai] No ${vcs.name} repository detected. Initializing new ${vcs.name} repository...")
            vcs.init(rootDir)
            println("✅ [ghai] ${vcs.name} repository initialized.")
        }

        // 0a. Auto-Init GitHub Remote if missing (0 Effort, 100% Gain)
        val remoteCheck = GhaGitExec.exec(rootDir, "remote")
        if (remoteCheck.isSuccess && remoteCheck.stdout.isBlank() && !token.isNullOrBlank()) {
            val repoName = rootDir.name
            val env = mapOf("GITHUB_TOKEN" to token, "GH_TOKEN" to token)
            
            // Check if repository already exists on GitHub for the user
            println("🔍 [ghai] No remote 'origin' detected. Checking GitHub for '$repoName'...")
            val viewRes = GhaProcessRunner.exec(
                workingDir = rootDir,
                command = listOf("gh", "repo", "view", repoName, "--json", "url", "--template", "{{.url}}"),
                extraEnv = env,
                timeoutSeconds = 30L
            )

            if (viewRes.isSuccess && viewRes.stdout.isNotBlank()) {
                val existingUrl = viewRes.stdout.trim()
                println("🌐 [ghai] Found existing GitHub repository: $existingUrl")
                println("🔗 [ghai] Linking 'origin' to existing repository...")
                GhaGitExec.exec(rootDir, "remote", "add", "origin", existingUrl)
                println("🔄 [ghai] Fetching and syncing with origin...")
                GhaGitExec.fetch(rootDir, "origin")
                
                // If local repo is empty (except for gha scaffolding), attempt to sync with remote main
                val localFiles = rootDir.listFiles()?.filter { 
                    it.name != ".git" && it.name != ".gha" && it.name != "ghai" && it.name != "init" 
                } ?: emptyList()
                
                if (localFiles.isEmpty() || (localFiles.size == 2 && localFiles.any { it.name == "settings.gradle.kts" } && localFiles.any { it.name == "build.gradle.kts" })) {
                    println("🔀 [ghai] Workspace is empty. Restoring project from 'origin/main'...")
                    GhaGitExec.exec(rootDir, "reset", "--hard", "origin/main")
                }
            } else {
                println("✨ [ghai] No existing repository found. Creating new GitHub repository '$repoName'...")
                val createRes = GhaProcessRunner.exec(
                    workingDir = rootDir,
                    command = listOf("gh", "repo", "create", repoName, "--source=.", "--public", "--push"),
                    extraEnv = env,
                    timeoutSeconds = 60L
                )
                if (createRes.isSuccess) {
                    println("✅ [ghai] GitHub repository '$repoName' created and linked as 'origin'.")
                } else {
                    println("⚠️ [ghai] Could not auto-create GitHub repository: ${createRes.stderr}")
                }
            }
        }
        val isVersionQuery = explicitMsg == "--version" || explicitMsg == "-v" || explicitMsg == "version"

        if (isVersionQuery) {
            val gitResult = GhaGitExec.exec(rootDir, "version")
            val gitVersion = if (gitResult.isSuccess) gitResult.stdout else "Not found"
            val ghResult = GhaProcessRunner.exec(rootDir, listOf("gh", "version"))
            val ghVersion = if (ghResult.isSuccess) ghResult.stdout.lines().firstOrNull() ?: "Active" else "Not found"
            val currentVersion = GhaVersionManager.readVersion(rootDir)

            println("🤖 [ghai Version Report]")
            println("   ├── gha Version     : $currentVersion (cc.thevar.gha)")
            println("   ├── Kotlin Version  : ${KotlinVersion.CURRENT} (${GhaConfig.KOTLIN_VENDOR})")
            println("   ├── Gradle Version  : ${GhaConfig.GRADLE_VERSION} (${GhaConfig.GRADLE_VENDOR})")
            println("   ├── Java JDK        : ${System.getProperty("java.version")} (${GhaConfig.JAVA_VENDOR})")
            println("   ├── Git VCS Engine  : $gitVersion (${GhaConfig.GIT_VENDOR})")
            println("   └── GitHub CLI (gh) : $ghVersion (${GhaConfig.GH_CLI_VENDOR})")
            println("✅ All engines verified & official stable.")
            return
        }

        val isDirty = !GhaGitExec.isClean(rootDir)
        val currentBranch = GhaGitExec.currentBranch(rootDir)
        val projectContext = GhaAiManager.detectProjectContext(rootDir)

        println("🤖 [ghai] 0 Effort, 100% Gain — Context: $projectContext")
        println("   ├── Current Branch  : '$currentBranch'")
        println("   └── Working Tree    : ${if (isDirty) "Dirty (Local Changes)" else "Clean"}")

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
            println("📦 [ghai] Detected dirty working tree. Executing local checkin & remote push workflow...")

            // Autonomous Version Bump for every push (0 Effort, 100% Gain)
            val newVersion = GhaVersionManager.bumpVersion(rootDir)
            println("📈 [ghai Version Bump] Incremented version to $newVersion")

            val smartMsg = GhaAiManager.detectSmartCommitMessage(rootDir, explicitMsg)
            commitSummary = "Committed: \"$smartMsg\""

            println("📦 Staging working tree changes & enforcing executable flags...")
            GhaGitExec.exec(rootDir, "add", "-A")
            GhaGitExec.exec(rootDir, "update-index", "--chmod=+x", "ghai")
            GhaGitExec.exec(rootDir, "update-index", "--chmod=+x", "init/install.sh")
            GhaGitExec.exec(rootDir, "update-index", "--chmod=+x", "gradlew")

            println("📝 Auto-committing: \"$smartMsg\"...")
            GhaGitExec.exec(rootDir, "commit", "-m", smartMsg)

            println("🔄 Rebase sync with 'origin/$base'...")
            GhaParallelWorkflowManager.syncWithRemoteBase(rootDir, base)

            println("🚀 Pushing '$headBranch' to origin remote...")
            val pushRes = GhaGitExec.push(rootDir, "origin", headBranch, setUpstream = true)
            pushSummary = if (pushRes.isSuccess) "Pushed to origin/$headBranch" else "Push attempted"

            println("🔀 Managing Pull Request against protected '$base'...")
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

                println("✅ Pull Request #${prInfo.number} active on GitHub: ${prInfo.url}")
                val env = if (!token.isNullOrBlank()) mapOf("GITHUB_TOKEN" to token, "GH_TOKEN" to token) else emptyMap()
                GhaProcessRunner.exec(
                    workingDir = rootDir,
                    command = listOf("gh", "pr", "merge", prInfo.number.toString(), "--squash", "--delete-branch", "--auto"),
                    extraEnv = env,
                    timeoutSeconds = 30L,
                )
                println("🤖 GitHub Auto-Merge enabled for PR #${prInfo.number}.")
            }

            // Immediately switch creator terminal back to clean base branch if on an auto-created branch
            if (isAutoBranch && headBranch != base) {
                println("🔄 [ghai] Work pushed & PR auto-merge active. Returning terminal to clean '$base' branch...")
                forceReturnToBaseBranch(rootDir, base, headBranch)
                activeHeadBranch = base
                activeBranchCategory = "Base Branch"
                println("✅ Creator terminal successfully returned to clean '$base' branch!")
            }

            tipRecommendation = "Your changes are saved, pushed, and auto-merging on GitHub. Terminal is clean on $base!"

        } else {
            // WORKFLOW B: CLEAN WORKING TREE (Post-push or clean local state)
            println("✨ [ghai] Detected clean working tree. Sweeping open PRs and checking CI check status...")

            // Rebase sync with remote base first
            GhaParallelWorkflowManager.syncWithRemoteBase(rootDir, base)

            val openPrs = GhaParallelWorkflowManager.listOpenPrsTargetingBase(rootDir, token, base)
            if (openPrs.isNotEmpty()) {
                println("🔀 Found ${openPrs.size} open PR(s) targeting '$base'. Sweeping and verifying CI status...")
                var mergedCount = 0
                var pendingCount = 0
                var failedCount = 0

                openPrs.forEach { pr ->
                    println("🔍 Inspecting PR #${pr.number} ('${pr.headBranch}'): ${pr.url}")
                    val ciStatus = GhaAiManager.checkPrCiStatus(rootDir, token, pr.number)

                    when (ciStatus.ciStatus) {
                        GhaAiManager.CiStatus.PASSED, GhaAiManager.CiStatus.NO_CHECKS -> {
                            println("🎉 [ghai Sweeper] CI checks PASSED for PR #${pr.number}! Merging into '$base'...")
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
                                println("✅ Merged PR #${pr.number} ('${pr.headBranch}') into '$base'!")
                            }
                        }
                        GhaAiManager.CiStatus.PENDING -> {
                            pendingCount++
                            println("⏳ [ghai Sweeper] PR #${pr.number} ('${pr.headBranch}') CI checks in progress. Enabling auto-merge...")
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
                            println("❌ [ghai Sweeper] PR #${pr.number} ('${pr.headBranch}') CI checks FAILED.")
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
                println("ℹ️ No open PRs found targeting '$base'. Checking commit log relative to origin/$base...")
                if (GhaAiManager.isBranchAheadOfRemote(rootDir, base) && headBranch != base) {
                    println("🚀 Local branch '$headBranch' is ahead of '$base'. Creating Pull Request...")
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
                        println("✅ Created Pull Request #${prInfo.number}: ${prInfo.url}")
                        tipRecommendation = "PR #${prInfo.number} created! Run './ghai' after CI builds finish to merge into $base."
                    }
                } else {
                    if (isAutoBranch && headBranch != base) {
                        println("🗑️ [ghai] Auto-created branch '$headBranch' PR is complete/merged. Returning to base branch '$base'...")
                        forceReturnToBaseBranch(rootDir, base, headBranch)
                        activeHeadBranch = base
                        activeBranchCategory = "Base Branch"
                        println("✅ Returned to clean '$base' branch and cleaned up local auto-branch '$headBranch'.")
                        tipRecommendation = "Start your next feature or type './ghai' anytime to auto-save and push changes."
                    } else {
                        println("✅ Local repository is 100% clean and fully synced with origin/$base.")
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

        // Step 2: Proactively sweep GitHub Actions workflows every run
        println("🧹 [ghai Workflow Sweeper] Sweeping GitHub Actions workflows at https://github.com/intellibitz/gha/actions...")
        val prunedRuns = try {
            GhaWorkflowManager.pruneOldWorkflowRuns(rootDir, token, maxKeep = 5)
        } catch (_: Exception) { 0 }

        if (prunedRuns > 0) {
            println("✅ [ghai Workflow Sweeper] Auto-pruned $prunedRuns old GitHub Actions workflow run(s) from history!")
        } else {
            println("✅ [ghai Workflow Sweeper] GitHub Actions history is lean and clean (Top 5 active runs kept).")
        }

        // Step 3: Sweep stale remote auto-branches
        println("🧹 [ghai Branch Sweeper] Sweeping stale remote auto-branches...")
        val prunedBranches = try {
            GhaParallelWorkflowManager.sweepStaleRemoteBranches(rootDir, token, base)
        } catch (_: Exception) { 0 }

        if (prunedBranches > 0) {
            println("✅ [ghai Branch Sweeper] Auto-pruned $prunedBranches stale remote auto-branch(es)!")
        } else {
            println("✅ [ghai Branch Sweeper] Remote branches are clean.")
        }

        // Print Structured Summary & Actionable One-Line Tip
        println("")
        println("════════════════════════════════════════════════════════════════════════════════")
        println("📋 [ghai Execution Summary]")
        println("   • Working Branch : $activeHeadBranch ($activeBranchCategory)")
        println("   • Commit Status  : $commitSummary")
        println("   • Remote Push    : $pushSummary")
        println("   • GitHub PR      : $prSummary ${if (prUrlSummary != "N/A") "($prUrlSummary)" else ""}")
        println("   • CI/CD Status   : $ciSummary")
        println("   • CI Workflows   : Swept https://github.com/intellibitz/gha/actions ${if (prunedRuns > 0) "($prunedRuns pruned)" else "(Lean)"}")
        println("   • Remote Branches: Swept origin/gha-auto/* ${if (prunedBranches > 0) "($prunedBranches pruned)" else "(Clean)"}")
        println("   • Local Sync     : 100% Synced with origin/$base")
        println("────────────────────────────────────────────────────────────────────────────────")
        println("💡 Tip: $tipRecommendation")
        println("════════════════════════════════════════════════════════════════════════════════")
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
