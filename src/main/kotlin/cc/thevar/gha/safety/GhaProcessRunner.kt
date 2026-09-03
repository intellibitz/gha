package cc.thevar.gha.safety

import java.io.File
import java.util.concurrent.Executors
import java.util.concurrent.TimeUnit

/**
 * Safe process execution engine for GHA:
 * 1. Enforces strict timeouts (default 30 seconds) to prevent infinite waiting.
 * 2. Enforces non-interactive environment variables (GIT_TERMINAL_PROMPT=0, GH_NO_PROMPT=1).
 * 3. Prevents infinite recursion loops via re-entry depth tracking.
 */
object GhaProcessRunner {

    private const val MAX_RECURSION_DEPTH = 3
    private const val DEFAULT_TIMEOUT_SECONDS = 30L

    data class ProcessResult(
        val exitCode: Int,
        val stdout: String,
        val stderr: String,
        val timedOut: Boolean = false
    ) {
        val isSuccess: Boolean get() = exitCode == 0 && !timedOut
    }

    fun exec(
        workingDir: File,
        command: List<String>,
        extraEnv: Map<String, String> = emptyMap(),
        timeoutSeconds: Long = DEFAULT_TIMEOUT_SECONDS
    ): ProcessResult {

        // Check recursion guard
        val currentDepth = System.getenv("GHA_RECURSION_DEPTH")?.toIntOrNull() ?: 0
        if (currentDepth >= MAX_RECURSION_DEPTH) {
            return ProcessResult(
                exitCode = -1,
                stdout = "",
                stderr = "⚠️ [GHA Loop Guard] Maximum recursion depth ($MAX_RECURSION_DEPTH) reached. Aborting to prevent infinite loop.",
                timedOut = false
            )
        }

        val executor = Executors.newFixedThreadPool(2)
        return try {
            val pb = ProcessBuilder(command)
                .directory(workingDir)

            val env = pb.environment()
            // Non-interactive enforcements
            env["GIT_TERMINAL_PROMPT"] = "0"
            env["GH_NO_PROMPT"] = "1"
            env["CI"] = "true"
            env["GHA_RECURSION_DEPTH"] = (currentDepth + 1).toString()
            extraEnv.forEach { (k, v) -> env[k] = v }

            val process = pb.start()

            val stdoutFuture = executor.submit<String> {
                process.inputStream.bufferedReader().readText().trim()
            }
            val stderrFuture = executor.submit<String> {
                process.errorStream.bufferedReader().readText().trim()
            }

            val completed = process.waitFor(timeoutSeconds, TimeUnit.SECONDS)

            if (!completed) {
                process.destroyForcibly()
                ProcessResult(
                    exitCode = -1,
                    stdout = "",
                    stderr = "⏳ [GHA Timeout] Command '${command.firstOrNull()}' timed out after $timeoutSeconds seconds.",
                    timedOut = true
                )
            } else {
                val stdout = try { stdoutFuture.get(2, TimeUnit.SECONDS) } catch (_: Exception) { "" }
                val stderr = try { stderrFuture.get(2, TimeUnit.SECONDS) } catch (_: Exception) { "" }
                ProcessResult(process.exitValue(), stdout, stderr)
            }
        } catch (e: Exception) {
            ProcessResult(-1, "", e.message ?: "Unknown process execution error")
        } finally {
            executor.shutdownNow()
        }
    }
}
