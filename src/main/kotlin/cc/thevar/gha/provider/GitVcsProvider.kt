package cc.thevar.gha.provider

import cc.thevar.gha.git.GhaGitExec
import cc.thevar.gha.safety.GhaVersionManager
import java.io.File

class GitVcsProvider : GhaVcsProvider {
    override val name = "Git"
    override val version = "Official VCS"
    override val vendor = "Software Freedom Conservancy"

    override fun isAvailable(rootDir: File): Boolean = GhaGitExec.isGitRepo(rootDir)

    override fun currentBranch(rootDir: File): String = GhaGitExec.currentBranch(rootDir)

    override fun isDirty(rootDir: File): Boolean = !GhaGitExec.isClean(rootDir)

    override fun init(rootDir: File) {
        GhaGitExec.init(rootDir)
    }

    override fun commit(rootDir: File, message: String) {
        GhaGitExec.exec(rootDir, "add", "-A")
        GhaGitExec.exec(rootDir, "commit", "-m", message)
    }

    override fun push(rootDir: File, remote: String, branch: String) {
        GhaVersionManager.bumpAndCommitVersion(rootDir)
        GhaGitExec.push(rootDir, remote, branch)
    }

    override fun pull(rootDir: File, remote: String, branch: String, rebase: Boolean) {
        if (rebase) {
            GhaGitExec.pullRebase(rootDir, remote, branch)
        } else {
            GhaGitExec.exec(rootDir, "pull", remote, branch)
        }
    }
}
