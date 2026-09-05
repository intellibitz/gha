package cc.thevar.gha.ai.vision

import cc.thevar.gha.ai.GhaAiManager
import cc.thevar.gha.ai.agent.GhaAgent
import cc.thevar.gha.ai.mcp.GhaGmcpClient
import cc.thevar.gha.ai.orchestrator.GhaGemiEngine
import cc.thevar.gha.provider.GhaBuildProvider
import cc.thevar.gha.provider.GhaProviderRegistry
import java.io.File

/**
 * GAWD: GHA Agents Web & Domain (Tier 2).
 * The Ghost in the Machine: An autonomous GHA Agent that solves project goals.
 * Uses GEMI for reasoning and GMCP for capabilities.
 */
class GhaAutonomousAgent(
    override val identity: String = "GHA-Agent-01",
    override val name: String = "GHA Autonomous Agent",
    override val role: String = "Autonomous Project Automation Worker"
) : GhaAiAgent, GhaAgent {

    override fun executeMission(projectDir: File, prompt: String): String {
        val res = solve(prompt, projectDir)
        return "Mission Success: ${res.success}\nOutput:\n${res.output}\nLog:\n${res.log.joinToString("\n")}"
    }

    override fun solve(goal: String, rootDir: File): GhaAgentResult {
        return solveWithT3T4(goal, rootDir, GhaGemiEngine(rootDir), GhaGmcpClient(rootDir))
    }

    fun solveWithT3T4(goal: String, rootDir: File, gemi: GhaGemiEngine, mcpClient: GhaGmcpClient): GhaAgentResult {
        val log = mutableListOf<String>()
        val lowerGoal = goal.lowercase().trim()
        log.add("🤖 Agent '$identity' ($name) active (Tier 2 Worker)")

        // 1. Thinking Phase (Tier 3)
        val reasoning = gemi.reason(goal)
        log.addAll(reasoning.log)
        log.add("   💭 Thinking (GEMI): ${reasoning.output}")
        
        val vcs = GhaProviderRegistry.getVcsProvider(rootDir)
        val build = GhaProviderRegistry.getBuildProvider(rootDir)
        val context = GhaAiManager.detectProjectContext(rootDir)

        log.add("🔍 Phase 1 [Environment Analysis]: Context='$context', VCS=${vcs.name}, Branch='${vcs.currentBranch(rootDir)}', Dirty=${vcs.isDirty(rootDir)}")

        // 2. Planning Phase
        val plan = formulatePlan(lowerGoal, vcs.isDirty(rootDir))
        log.add("📋 Phase 2 [Plan Formulation]: Created ${plan.size}-step execution plan:")
        plan.forEachIndexed { idx, step -> log.add("   ├── Step ${idx + 1}: ${step.description}") }

        // 3. Doing Phase (Tier 4)
        log.add("⚙️ Phase 3 [Autonomous Execution via GMCP]:")
        var overallSuccess = true
        val outputs = mutableListOf<String>()

        for ((idx, step) in plan.withIndex()) {
            log.add("   ► Executing Step ${idx + 1}/${plan.size}: ${step.name} (${step.description})")
            val result = executeStepWithSelfHealing(step, mcpClient, build, rootDir, log)
            if (!result.success) {
                log.add("   ❌ Step ${idx + 1} failed: ${result.message}")
                overallSuccess = false
                outputs.add("Step ${step.name} failed: ${result.message}")
                break
            } else {
                log.add("   ✅ Step ${idx + 1} completed: ${result.message}")
                outputs.add(result.message)
            }
        }

        val finalSummary = if (overallSuccess) {
            "Agent successfully completed goal '$goal'. All ${plan.size} plan step(s) executed."
        } else {
            "Agent encountered issues executing goal '$goal'. Check execution trace log for details."
        }

        log.add("✨ Phase 4 [Mission Conclusion]: $finalSummary")
        return GhaAgentResult(overallSuccess, log, outputs.joinToString("\n"))
    }
    
    private data class AgentStep(
        val name: String,
        val toolName: String,
        val args: Map<String, Any>,
        val description: String
    )

    private data class StepResult(
        val success: Boolean,
        val message: String
    )

    private fun formulatePlan(goal: String, isDirty: Boolean): List<AgentStep> {
        val steps = mutableListOf<AgentStep>()

        if (goal.contains("python")) {
            return listOf(
                AgentStep("scaffold_python", "sys_scaffold_stack", mapOf("stack" to "python"), "Scaffold Python app with UV & pytest"),
                AgentStep("run_python", "sys_python_env", mapOf("codeOrScript" to "import main; main.main()"), "Execute Python entrypoint via UV"),
                AgentStep("status_check", "status", emptyMap(), "Verify workspace health")
            )
        }

        if (goal.contains("rust")) {
            return listOf(
                AgentStep("scaffold_rust", "sys_scaffold_stack", mapOf("stack" to "rust"), "Scaffold Rust Cargo package"),
                AgentStep("status_check", "status", emptyMap(), "Verify workspace health")
            )
        }

        if (goal.contains("go")) {
            return listOf(
                AgentStep("scaffold_go", "sys_scaffold_stack", mapOf("stack" to "go"), "Scaffold Go module"),
                AgentStep("status_check", "status", emptyMap(), "Verify workspace health")
            )
        }

        if (goal.contains("docker") || goal.contains("container")) {
            return listOf(
                AgentStep("scaffold_docker", "sys_scaffold_stack", mapOf("stack" to "docker"), "Scaffold Dockerfile and docker-compose.yml"),
                AgentStep("docker_status", "sys_docker_container", mapOf("action" to "ps"), "Inspect active Docker containers")
            )
        }

        if (goal.contains("adb") || goal.contains("device")) {
            return listOf(
                AgentStep("inspect_adb", "sys_adb_device", mapOf("subCommand" to "devices"), "Inspect connected ADB devices")
            )
        }

        if (goal.contains("android")) {
            return listOf(
                AgentStep("scaffold_android_app", "scaffold_android", emptyMap(), "Scaffold 100% Kotlin/Compose Android project"),
                AgentStep("status_check", "status", emptyMap(), "Verify project structure and VCS state"),
                AgentStep("build_project", "build", emptyMap(), "Verify Android build integrity")
            )
        }

        if (goal.contains("kotlin") || (goal.contains("create") && (goal.contains("app") || goal.contains("project"))) || goal.contains("scaffold")) {
            return listOf(
                AgentStep("scaffold_kotlin_app", "scaffold_kotlin", emptyMap(), "Scaffold 100% Kotlin JVM project"),
                AgentStep("status_check", "status", emptyMap(), "Verify project structure and VCS state"),
                AgentStep("build_project", "build", emptyMap(), "Compile and verify build integrity"),
                AgentStep("verify_tests", "test", emptyMap(), "Run unit tests")
            )
        }
        
        if (isDirty && (goal.contains("sync") || goal.contains("save") || goal.contains("push"))) {
            steps.add(AgentStep("status_check", "status", emptyMap(), "Verify current project health and VCS state"))
            steps.add(AgentStep("sync_code", "sync", emptyMap(), "Commit changes, rebase from main, push to GitHub, and auto-merge PR"))
            return steps
        }

        return when {
            goal.contains("save") || goal.contains("push") || goal.contains("sync") -> listOf(
                AgentStep("status_check", "status", emptyMap(), "Verify current project health and VCS state"),
                AgentStep("sync_code", "sync", emptyMap(), "Commit changes, rebase from main, push to GitHub, and auto-merge PR")
            )
            goal.contains("fix") || goal.contains("build") || goal.contains("repair") -> listOf(
                AgentStep("status_check", "status", emptyMap(), "Inspect repository status"),
                AgentStep("clean_workspace", "clean", emptyMap(), "Clean leftover build artifacts"),
                AgentStep("build_project", "build", emptyMap(), "Execute project build engine"),
                AgentStep("verify_tests", "test", emptyMap(), "Run unit test suite")
            )
            goal.contains("test") -> listOf(
                AgentStep("build_project", "build", emptyMap(), "Compile project sources"),
                AgentStep("run_tests", "test", emptyMap(), "Execute test suite")
            )
            goal.contains("security") || goal.contains("audit") || goal.contains("dependabot") -> listOf(
                AgentStep("check_security", "security_status", emptyMap(), "Audit Dependabot and GitHub security alerts")
            )
            goal.contains("context") || goal.contains("report") -> listOf(
                AgentStep("generate_context", "context", emptyMap(), "Generate AI context report")
            )
            else -> listOf(
                AgentStep("status_check", "status", emptyMap(), "Generate project health report"),
                AgentStep("context_report", "context", emptyMap(), "Export AI context report")
            )
        }
    }

    private fun executeStepWithSelfHealing(
        step: AgentStep,
        mcpClient: GhaGmcpClient,
        build: GhaBuildProvider,
        rootDir: File,
        log: MutableList<String>
    ): StepResult {
        return try {
            val output = mcpClient.callTool(step.toolName, step.args)
            StepResult(true, output)
        } catch (e: Exception) {
            log.add("   🩹 [Self-Healing] Attempting recovery for step '${step.name}' following exception: ${e.message}")
            try {
                // Attempt self-healing recovery: clean workspace then retry tool
                build.clean(rootDir)
                val retryOutput = mcpClient.callTool(step.toolName, step.args)
                StepResult(true, "Recovered via self-healing clean: $retryOutput")
            } catch (retryException: Exception) {
                StepResult(false, "Self-healing failed: ${retryException.message}")
            }
        }
    }
}
