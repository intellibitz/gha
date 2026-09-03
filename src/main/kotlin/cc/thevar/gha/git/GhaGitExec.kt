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
}
