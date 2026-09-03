package cc.thevar.gha.git

import java.io.File

/**
 * 100% Kotlin, platform-independent Git execution engine.
 */
object GhaGitExec {

    data class GitResult(
        val exitCode: Int,
        val stdout: String,
        val stderr: String
    ) {
        val isSuccess: Boolean get() = exitCode == 0
    }

    fun exec(workingDir: File, vararg args: String): GitResult {
        return try {
            val process = ProcessBuilder("git", *args)
                .directory(workingDir)
                .start()

            val stdout = process.inputStream.bufferedReader().readText().trim()
            val stderr = process.errorStream.bufferedReader().readText().trim()
            val exitCode = process.waitFor()

            GitResult(exitCode, stdout, stderr)
        } catch (e: Exception) {
            GitResult(-1, "", e.message ?: "Unknown git execution error")
        }
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
