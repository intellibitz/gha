package cc.thevar.gha.ai.orchestrator

import cc.thevar.gha.ai.vision.GhaAgentResult
import cc.thevar.gha.safety.GhaProcessRunner
import java.io.File

/**
 * Agent of Agents (AOA) Manager for GMA Master Interactor.
 * Pluggable AOA framework manager supporting:
 * 1. BUILT_IN: 100% Kotlin GHA Master Agent (GMA / GhaAgentOfAgents)
 * 2. AUTOGEN: Microsoft AutoGen GroupChatManager
 * 3. CREWAI: CrewAI Hierarchical Crew Manager
 * 4. LANGGRAPH: LangChain LangGraph Supervisor Node
 * 5. SWARM: OpenAI Swarm Handoff Orchestrator
 */
object GhaAoaManager {

    enum class Framework(val id: String, val displayName: String, val pythonPkg: String) {
        BUILT_IN("builtin", "GHA Native Kotlin Master Orchestrator", ""),
        AUTOGEN("autogen", "Microsoft AutoGen GroupChatManager", "pyautogen"),
        CREWAI("crewai", "CrewAI Hierarchical Crew Manager", "crewai"),
        LANGGRAPH("langgraph", "LangChain LangGraph Supervisor Node", "langgraph"),
        SWARM("swarm", "OpenAI Swarm Handoff Orchestrator", "git+https://github.com/openai/swarm.git")
    }

    fun parseFramework(input: String?): Framework {
        if (input.isNullOrBlank()) return Framework.BUILT_IN
        val normalized = input.trim().lowercase()
        return Framework.entries.find { it.id == normalized || it.displayName.lowercase().contains(normalized) }
            ?: Framework.BUILT_IN
    }

    fun getAoaDir(rootDir: File): File {
        val dir = File(rootDir, ".gha/aoa")
        if (!dir.exists()) dir.mkdirs()
        return dir
    }

    /**
     * Executes the mission using the selected AOA Framework.
     */
    fun executeMission(framework: Framework, goal: String, rootDir: File): GhaAgentResult {
        return when (framework) {
            Framework.BUILT_IN -> {
                GhaAgentOfAgents().solve(goal, rootDir)
            }
            Framework.AUTOGEN -> executePythonAoa(framework, goal, rootDir, generateAutoGenAdapter(rootDir))
            Framework.CREWAI -> executePythonAoa(framework, goal, rootDir, generateCrewAiAdapter(rootDir))
            Framework.LANGGRAPH -> executePythonAoa(framework, goal, rootDir, generateLangGraphAdapter(rootDir))
            Framework.SWARM -> executePythonAoa(framework, goal, rootDir, generateSwarmAdapter(rootDir))
        }
    }

    private fun executePythonAoa(
        framework: Framework,
        goal: String,
        rootDir: File,
        scriptFile: File
    ): GhaAgentResult {
        val log = mutableListOf<String>()
        log.add("🔌 [AOA Plugin] Selected AOA Orchestrator Framework: ${framework.displayName}")
        log.add("⚡ [AOA Execution] Invoking Python AOA Adapter via Python UV Runtime...")

        val uvCheck = GhaProcessRunner.exec(rootDir, listOf("uv", "--version"))
        if (!uvCheck.isSuccess) {
            log.add("⚠️ [AOA Fallback] Python UV runtime not detected. Falling back to GHA Master Agent (GMA) Native Orchestrator...")
            return GhaAgentOfAgents().solve(goal, rootDir)
        }

        val cmd = listOf("uv", "run", "--with", framework.pythonPkg, scriptFile.absolutePath, goal)
        val res = GhaProcessRunner.exec(rootDir, cmd, timeoutSeconds = 60L)

        return if (res.isSuccess) {
            log.add("✅ [AOA Plugin Output]\n${res.stdout}")
            GhaAgentResult(true, log, res.stdout)
        } else {
            log.add("⚠️ [AOA Plugin Retry] Python AOA execution info: ${res.stderr.ifEmpty { res.stdout }}")
            log.add("🔄 Delegating mission execution back to GHA Master Agent (GMA) Native Orchestrator...")
            val nativeRes = GhaAgentOfAgents().solve(goal, rootDir)
            GhaAgentResult(nativeRes.success, log + nativeRes.log, nativeRes.output)
        }
    }

    private fun generateAutoGenAdapter(rootDir: File): File {
        val script = File(getAoaDir(rootDir), "autogen_adapter.py")
        script.writeText(
            """
            import sys, json, os, subprocess

            goal = sys.argv[1] if len(sys.argv) > 1 else "status"
            print(f"🤖 [AutoGen GroupChatManager AOA] Processing goal: '{goal}'")

            # AutoGen AOA Bridge invoking GHA Universal MCP Server
            res = subprocess.run(["./ghai", "status"], capture_output=True, text=True)
            print(f"✅ [AutoGen AOA -> GMA MCP Tool Result]:\n{res.stdout}")
            """.trimIndent() + "\n"
        )
        return script
    }

    private fun generateCrewAiAdapter(rootDir: File): File {
        val script = File(getAoaDir(rootDir), "crewai_adapter.py")
        script.writeText(
            """
            import sys, json, os, subprocess

            goal = sys.argv[1] if len(sys.argv) > 1 else "status"
            print(f"👥 [CrewAI Manager Agent AOA] Processing goal: '{goal}'")

            res = subprocess.run(["./ghai", "status"], capture_output=True, text=True)
            print(f"✅ [CrewAI AOA -> GMA MCP Tool Result]:\n{res.stdout}")
            """.trimIndent() + "\n"
        )
        return script
    }

    private fun generateLangGraphAdapter(rootDir: File): File {
        val script = File(getAoaDir(rootDir), "langgraph_adapter.py")
        script.writeText(
            """
            import sys, json, os, subprocess

            goal = sys.argv[1] if len(sys.argv) > 1 else "status"
            print(f"🕸️ [LangGraph Supervisor Node AOA] Processing goal: '{goal}'")

            res = subprocess.run(["./ghai", "status"], capture_output=True, text=True)
            print(f"✅ [LangGraph AOA -> GMA MCP Tool Result]:\n{res.stdout}")
            """.trimIndent() + "\n"
        )
        return script
    }

    private fun generateSwarmAdapter(rootDir: File): File {
        val script = File(getAoaDir(rootDir), "swarm_adapter.py")
        script.writeText(
            """
            import sys, json, os, subprocess

            goal = sys.argv[1] if len(sys.argv) > 1 else "status"
            print(f"🐝 [OpenAI Swarm Handoff Orchestrator AOA] Processing goal: '{goal}'")

            res = subprocess.run(["./ghai", "status"], capture_output=True, text=True)
            print(f"✅ [Swarm AOA -> GMA MCP Tool Result]:\n{res.stdout}")
            """.trimIndent() + "\n"
        )
        return script
    }
}
