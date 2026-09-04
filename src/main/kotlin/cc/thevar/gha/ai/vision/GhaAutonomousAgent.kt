package cc.thevar.gha.ai.vision

import cc.thevar.gha.provider.GhaProviderRegistry
import java.io.File

/**
 * The Ghost in the Machine: An autonomous GHA Agent that solves project goals.
 */
class GhaAutonomousAgent(override val identity: String = "GHA-Agent-01") : GhaAiAgent {

    override fun solve(goal: String, rootDir: File): GhaAgentResult {
        val log = mutableListOf<String>()
        log.add("🤖 Agent '$identity' starting task: $goal")
        
        val vcs = GhaProviderRegistry.getVcsProvider(rootDir)
        val build = GhaProviderRegistry.getBuildProvider(rootDir)
        
        log.add("🔍 Analyzing environment: VCS=${vcs.name}, Root=${rootDir.name}")
        
        return try {
            if (goal.contains("save") || goal.contains("push") || goal.contains("sync")) {
                log.add("📦 Goal identified as 'sync'. Executing VCS commit & push...")
                vcs.commit(rootDir, "chore: autonomous agent contribution")
                GhaAgentResult(true, log, "Agent successfully synced changes to GitHub.")
            } else if (goal.contains("fix") || goal.contains("build")) {
                log.add("🛠️ Goal identified as 'fix/build'. Running build engine...")
                build.build(rootDir)
                GhaAgentResult(true, log, "Agent verified project build integrity.")
            } else {
                log.add("⚠️ Goal not fully understood. Defaulting to status report.")
                GhaAgentResult(true, log, "Project is ${if (vcs.isDirty(rootDir)) "dirty" else "clean"}.")
            }
        } catch (e: Exception) {
            log.add("❌ Error during execution: ${e.message}")
            GhaAgentResult(false, log, "Agent failed to achieve goal.")
        }
    }
}
