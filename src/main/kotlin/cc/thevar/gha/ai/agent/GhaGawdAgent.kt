package cc.thevar.gha.ai.agent

import cc.thevar.gha.ai.mcp.GhaGmcpClient
import cc.thevar.gha.ai.orchestrator.GhaGemiEngine
import cc.thevar.gha.ai.vision.GhaAgentResult
import cc.thevar.gha.ai.vision.GhaAiAgent
import groovy.json.JsonOutput
import groovy.json.JsonSlurper
import java.io.File
import java.util.UUID

/**
 * GAWD Agent (GHA Agents Web & Domain - Tier 2 Worker).
 * Operates following industry established standard protocols for AI Agents:
 * 1. Agent Task & Step Execution Protocol (create, step, status).
 * 2. Agent-to-Agent (A2A) Communication Protocol (FIPA-inspired performatives: REQUEST, INFORM, DELEGATE, RESPONSE).
 * 3. Model Context Protocol (MCP) Interoperability for tool calling and capability discovery.
 * 4. Multi-Engine Interoperability (works with GEMI, GHA Native Engine, OpenAI, Anthropic, Ollama, Groq, OpenRouter).
 */
class GhaGawdAgent(
    override val identity: String = "GAWD-Worker-01",
    override val name: String = "GHA Custom GAWD Agent",
    override val role: String = "Standard Agent Protocol Compliant Worker for Web & Domain Tasks"
) : GhaAiAgent, GhaAgent {

    enum class TaskStatus { INITIATED, IN_PROGRESS, COMPLETED, FAILED }
    enum class A2APerformative { REQUEST, INFORM, DELEGATE, PROPOSE, AGREE, REFUSE, RESPONSE }

    data class AgentTaskStep(
        val stepId: String = UUID.randomUUID().toString(),
        val action: String,
        val input: String,
        val output: String,
        val isSuccess: Boolean
    )

    data class AgentTask(
        val taskId: String = "task-gawd-${UUID.randomUUID().toString().take(8)}",
        val goal: String,
        val status: TaskStatus = TaskStatus.INITIATED,
        val steps: MutableList<AgentTaskStep> = mutableListOf(),
        val resultSummary: String = ""
    )

    data class A2AMessage(
        val messageId: String = "a2a-${UUID.randomUUID().toString().take(8)}",
        val sender: String,
        val recipient: String,
        val performative: A2APerformative,
        val taskId: String? = null,
        val content: String,
        val metadata: Map<String, Any> = emptyMap()
    )

    private val tasks = mutableMapOf<String, AgentTask>()
    private val slurper = JsonSlurper()

    /**
     * Standard Agent Task Creation Protocol.
     */
    fun createTask(goal: String): AgentTask {
        val task = AgentTask(goal = goal)
        tasks[task.taskId] = task
        return task
    }

    /**
     * Standard Agent-to-Agent (A2A) Protocol Handler.
     * Allows other agents/engines (GMA, external A2A agents, LangChain, AutoGen, CrewAI) to send messages to GAWD.
     */
    fun handleA2AMessage(message: A2AMessage, rootDir: File): A2AMessage {
        System.err.println("🤖 [GAWD A2A Protocol] Received ${message.performative} from '${message.sender}': ${message.content.take(60)}...")

        return when (message.performative) {
            A2APerformative.REQUEST, A2APerformative.DELEGATE -> {
                val task = createTask(message.content)
                val result = executeMissionWithProtocol(task.taskId, rootDir)

                A2AMessage(
                    sender = identity,
                    recipient = message.sender,
                    performative = if (result.success) A2APerformative.RESPONSE else A2APerformative.REFUSE,
                    taskId = task.taskId,
                    content = result.output,
                    metadata = mapOf("status" to (if (result.success) "COMPLETED" else "FAILED"))
                )
            }
            A2APerformative.INFORM -> {
                A2AMessage(
                    sender = identity,
                    recipient = message.sender,
                    performative = A2APerformative.INFORM,
                    taskId = message.taskId,
                    content = "GAWD Agent acknowledged info: ${message.content.take(100)}"
                )
            }
            else -> {
                A2AMessage(
                    sender = identity,
                    recipient = message.sender,
                    performative = A2APerformative.RESPONSE,
                    taskId = message.taskId,
                    content = "A2A Message processed successfully."
                )
            }
        }
    }

    /**
     * Processes JSON-RPC formatted Agent Protocol requests.
     */
    fun handleJsonRpcRequest(jsonRequest: String, rootDir: File): String {
        return try {
            val req = slurper.parseText(jsonRequest) as? Map<String, Any> ?: return createErrorJson(null, -32600, "Invalid Request")
            val id = req["id"]
            val method = req["method"] as? String ?: return createErrorJson(id, -32601, "Method required")
            val params = req["params"] as? Map<String, Any> ?: emptyMap()

            val result = when (method) {
                "agent/info" -> mapOf(
                    "identity" to identity,
                    "name" to name,
                    "role" to role,
                    "protocol" to "Agent Protocol 1.0 (A2A + MCP + JSON-RPC)"
                )
                "agent/task/create" -> {
                    val goal = params["goal"]?.toString() ?: ""
                    val task = createTask(goal)
                    mapOf("taskId" to task.taskId, "status" to task.status.name)
                }
                "agent/task/execute" -> {
                    val taskId = params["taskId"]?.toString()
                    val goal = params["goal"]?.toString()
                    val targetTaskId = if (!taskId.isNullOrBlank()) taskId else createTask(goal ?: "General GAWD Task").taskId
                    val res = executeMissionWithProtocol(targetTaskId, rootDir)
                    mapOf(
                        "taskId" to targetTaskId,
                        "success" to res.success,
                        "output" to res.output,
                        "log" to res.log
                    )
                }
                "agent/a2a/send" -> {
                    val sender = params["sender"]?.toString() ?: "ExternalAgent"
                    val content = params["content"]?.toString() ?: ""
                    val perfStr = params["performative"]?.toString() ?: "REQUEST"
                    val perf = try { A2APerformative.valueOf(perfStr.uppercase()) } catch (_: Exception) { A2APerformative.REQUEST }

                    val a2aReq = A2AMessage(sender = sender, recipient = identity, performative = perf, content = content)
                    val a2aResp = handleA2AMessage(a2aReq, rootDir)

                    mapOf(
                        "messageId" to a2aResp.messageId,
                        "sender" to a2aResp.sender,
                        "performative" to a2aResp.performative.name,
                        "content" to a2aResp.content
                    )
                }
                else -> return createErrorJson(id, -32601, "Method '$method' not found")
            }

            JsonOutput.toJson(mapOf("jsonrpc" to "2.0", "id" to id, "result" to result))
        } catch (e: Exception) {
            createErrorJson(null, -32603, "Internal error: ${e.message}")
        }
    }

    /**
     * Executes a mission using standard Agent Step Protocol and Tier 3 / Tier 4 infrastructure.
     */
    fun executeMissionWithProtocol(taskId: String, rootDir: File): GhaAgentResult {
        val task = tasks[taskId] ?: createTask("Mission for $taskId")
        tasks[task.taskId] = task.copy(status = TaskStatus.IN_PROGRESS)

        val log = mutableListOf<String>()
        log.add("🤖 [GAWD Agent] Active under Standard Agent Protocol (Task: ${task.taskId})")

        val gemi = GhaGemiEngine(rootDir)
        val mcpClient = GhaGmcpClient(rootDir)

        // Step 1: Thinking Phase (Tier 3 Engine Intelligence)
        val reasoning = gemi.reason(task.goal)
        log.addAll(reasoning.log)
        
        val step1 = AgentTaskStep(
            action = "reasoning",
            input = task.goal,
            output = reasoning.output,
            isSuccess = reasoning.success
        )
        task.steps.add(step1)

        // Step 2: Doing Phase (Tier 2 Dispatch to Specialized Workers)
        val workerResult = GhaAgentManager.dispatchMission(task.goal, rootDir, gemi, mcpClient)
        log.addAll(workerResult.log)

        val step2 = AgentTaskStep(
            action = "dispatch_worker",
            input = task.goal,
            output = workerResult.output,
            isSuccess = workerResult.success
        )
        task.steps.add(step2)

        val finalStatus = if (workerResult.success) TaskStatus.COMPLETED else TaskStatus.FAILED
        tasks[task.taskId] = task.copy(status = finalStatus, resultSummary = workerResult.output)

        return GhaAgentResult(workerResult.success, log, workerResult.output)
    }

    override fun executeMission(projectDir: File, prompt: String): String {
        val task = createTask(prompt)
        return executeMissionWithProtocol(task.taskId, projectDir).output
    }

    override fun solve(goal: String, rootDir: File): GhaAgentResult {
        val task = createTask(goal)
        return executeMissionWithProtocol(task.taskId, rootDir)
    }

    private fun createErrorJson(id: Any?, code: Int, message: String): String {
        return JsonOutput.toJson(mapOf(
            "jsonrpc" to "2.0",
            "id" to id,
            "error" to mapOf("code" to code, "message" to message)
        ))
    }
}
