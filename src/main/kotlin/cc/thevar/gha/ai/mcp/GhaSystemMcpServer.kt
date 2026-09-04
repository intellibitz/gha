package cc.thevar.gha.ai.mcp

import cc.thevar.gha.ai.vision.GhaAiTool
import cc.thevar.gha.ai.vision.GhaMcpServer
import cc.thevar.gha.safety.GhaProcessRunner
import java.io.File

/**
 * Custom System Tools MCP Server created by GHA.
 * Detects, exposes, and executes available User System Tools (ADB, Docker, Python/UV, Node/NPM, System CLIs, System Profilers)
 * as Model Context Protocol (MCP) tools for MCP Clients (AOA & Sub-Agents).
 */
class GhaSystemMcpServer(private val rootDir: File) : GhaMcpServer {

    override fun exposeTools(): List<GhaAiTool> {
        return listOf(
            GhaAiTool(
                name = "sys_detect_tools",
                description = "Detects installed user system tools, SDKs, compilers, and runtimes on the user OS.",
                inputSchema = createSchema()
            ),
            GhaAiTool(
                name = "sys_exec_command",
                description = "Executes a user system CLI command safely inside the workspace.",
                inputSchema = createSchema(
                    properties = mapOf(
                        "command" to mapOf("type" to "string", "description" to "System command line string to execute")
                    ),
                    required = listOf("command")
                )
            ),
            GhaAiTool(
                name = "sys_adb_device",
                description = "Inspects attached Android physical or virtual devices via ADB.",
                inputSchema = createSchema(
                    properties = mapOf(
                        "subCommand" to mapOf("type" to "string", "description" to "ADB subcommand: devices, logcat, shell, or install (default: devices)")
                    )
                )
            ),
            GhaAiTool(
                name = "sys_docker_container",
                description = "Inspects or executes Docker containers on the user system.",
                inputSchema = createSchema(
                    properties = mapOf(
                        "action" to mapOf("type" to "string", "description" to "Docker action: ps, images, info, or run (default: ps)")
                    )
                )
            ),
            GhaAiTool(
                name = "sys_python_env",
                description = "Executes Python scripts or packages via embedded Python UV runtime.",
                inputSchema = createSchema(
                    properties = mapOf(
                        "codeOrScript" to mapOf("type" to "string", "description" to "Python code snippet or script path")
                    ),
                    required = listOf("codeOrScript")
                )
            ),
            GhaAiTool(
                name = "sys_system_profile",
                description = "Inspects OS hardware, CPU, RAM, environment variables, and system resources.",
                inputSchema = createSchema()
            )
        )
    }

    override fun executeTool(toolName: String, arguments: Map<String, Any>): String {
        return when (toolName.lowercase()) {
            "sys_detect_tools" -> detectInstalledUserTools()
            "sys_exec_command" -> {
                val cmdStr = arguments["command"]?.toString()
                    ?: return "Error: 'command' argument required for sys_exec_command."
                val cmdParts = cmdStr.split("\\s+".toRegex())
                val res = GhaProcessRunner.exec(rootDir, cmdParts)
                if (res.isSuccess) "System Command Executed:\n${res.stdout}" else "Command failed: ${res.stderr}"
            }
            "sys_adb_device" -> {
                val subCmd = arguments["subCommand"]?.toString() ?: "devices"
                val res = GhaProcessRunner.exec(rootDir, listOf("adb", subCmd))
                if (res.isSuccess) "ADB Device Output ($subCmd):\n${res.stdout}" else "ADB execution info: ${res.stderr.ifEmpty { "ADB CLI not detected on system PATH." }}"
            }
            "sys_docker_container" -> {
                val action = arguments["action"]?.toString() ?: "ps"
                val res = GhaProcessRunner.exec(rootDir, listOf("docker", action))
                if (res.isSuccess) "Docker Output ($action):\n${res.stdout}" else "Docker execution info: ${res.stderr.ifEmpty { "Docker CLI not detected or daemon not running." }}"
            }
            "sys_python_env" -> {
                val code = arguments["codeOrScript"]?.toString() ?: return "Error: 'codeOrScript' argument required."
                val res = GhaProcessRunner.exec(rootDir, listOf("uv", "run", "python", "-c", code))
                if (res.isSuccess) "Python UV Output:\n${res.stdout}" else "Python execution info: ${res.stderr}"
            }
            "sys_system_profile" -> {
                val os = System.getProperty("os.name")
                val arch = System.getProperty("os.arch")
                val cpus = Runtime.getRuntime().availableProcessors()
                val maxMemMb = Runtime.getRuntime().maxMemory() / (1024 * 1024)
                "User System Profile: OS=$os ($arch), CPU Cores=$cpus, Max JVM Memory=${maxMemMb}MB, Path=${rootDir.absolutePath}"
            }
            else -> "Error: Tool '$toolName' not recognized by GHA System Tools MCP Server."
        }
    }

    private fun detectInstalledUserTools(): String {
        val toolsToTest = listOf(
            "git" to "git --version",
            "gh" to "gh --version",
            "gradle" to "gradle --version",
            "adb" to "adb version",
            "docker" to "docker --version",
            "uv" to "uv --version",
            "node" to "node --version",
            "npm" to "npm --version",
            "java" to "java -version",
            "curl" to "curl --version"
        )

        val detected = mutableListOf<String>()
        toolsToTest.forEach { (name, cmd) ->
            val parts = cmd.split(" ")
            val res = GhaProcessRunner.exec(rootDir, parts, timeoutSeconds = 5L)
            if (res.isSuccess) {
                val ver = res.stdout.lines().firstOrNull()?.trim() ?: "Installed"
                detected.add("✅ $name: $ver")
            } else {
                detected.add("❌ $name: Not Installed")
            }
        }

        return "User System Tools Detected on OS (${detected.count { it.startsWith("✅") }}/${toolsToTest.size} Available):\n" + detected.joinToString("\n")
    }

    private fun createSchema(
        properties: Map<String, Map<String, String>> = emptyMap(),
        required: List<String> = emptyList()
    ): Map<String, Any> {
        val propsMap = properties.mapValues { (_, spec) ->
            mapOf(
                "type" to (spec["type"] ?: "string"),
                "description" to (spec["description"] ?: "")
            )
        }
        val schema = mutableMapOf<String, Any>(
            "type" to "object",
            "properties" to propsMap
        )
        if (required.isNotEmpty()) {
            schema["required"] = required
        }
        return schema
    }
}
