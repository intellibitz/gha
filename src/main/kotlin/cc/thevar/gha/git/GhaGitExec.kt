package cc.thevar.gha.git

import cc.thevar.gha.safety.GhaProcessRunner
import java.io.File

/**
 * 100% Kotlin, platform-independent Git execution engine with loop prevention and timeouts.
 */
object GhaGitExec {

    fun exec(workingDir: File, vararg args: String, timeoutSeconds: Long = 30L): GhaProcessRunner.ProcessResult {
        return GhaProcessRunner.exec(
            workingDir = workingDir,
            command = listOf("git") + args.toList(),
            timeoutSeconds = timeoutSeconds
        )
    }

    fun currentBranch(workingDir: File): String {
        val result = exec(workingDir, "rev-parse", "--abbrev-ref", "HEAD")
        return if (result.isSuccess) result.stdout else "unknown"
    }

    fun isClean(workingDir: File): Boolean {
        val result = exec(workingDir, "status", "--porcelain")
        return result.isSuccess && result.stdout.isBlank()
    }

    fun localBranchExists(workingDir: File, branchName: String): Boolean {
        val result = exec(workingDir, "show-ref", "--verify", "--quiet", "refs/heads/$branchName")
        return result.isSuccess
    }

    fun remoteBranchExists(workingDir: File, remoteName: String, branchName: String): Boolean {
        val result = exec(workingDir, "show-ref", "--verify", "--quiet", "refs/remotes/$remoteName/$branchName")
        return result.isSuccess
    }

    fun fetch(workingDir: File, remoteName: String = "origin", prune: Boolean = false): GhaProcessRunner.ProcessResult {
        val args = mutableListOf("fetch")
        if (prune) args.add("--prune")
        args.add(remoteName)
        return exec(workingDir, *args.toTypedArray(), timeoutSeconds = 45L)
    }

    fun checkout(
        workingDir: File,
        branchName: String,
        createIfMissing: Boolean = false,
        startPoint: String? = null
    ): GhaProcessRunner.ProcessResult {
        val exists = localBranchExists(workingDir, branchName)
        val args = mutableListOf("checkout")
        if (!exists && createIfMissing) {
            args.add("-b")
        }
        args.add(branchName)
        if (!exists && createIfMissing && !startPoint.isNullOrBlank()) {
            args.add(startPoint)
        }
        return exec(workingDir, *args.toTypedArray())
    }

    fun deleteLocalBranch(workingDir: File, branchName: String, force: Boolean = false): GhaProcessRunner.ProcessResult {
        val flag = if (force) "-D" else "-d"
        return exec(workingDir, "branch", flag, branchName)
    }

    fun deleteRemoteBranch(workingDir: File, remoteName: String, branchName: String): GhaProcessRunner.ProcessResult {
        return exec(workingDir, "push", remoteName, "--delete", branchName, timeoutSeconds = 45L)
    }

    fun pullRebase(workingDir: File, remoteName: String = "origin", branchName: String): GhaProcessRunner.ProcessResult {
        return exec(workingDir, "pull", "--rebase", remoteName, branchName, timeoutSeconds = 60L)
    }

    fun push(workingDir: File, remoteName: String = "origin", branchName: String, setUpstream: Boolean = true): GhaProcessRunner.ProcessResult {
        val args = mutableListOf("push")
        if (setUpstream) {
            args.add("-u")
        }
        args.add(remoteName)
        args.add(branchName)
        return exec(workingDir, *args.toTypedArray(), timeoutSeconds = 60L)
    }
}
