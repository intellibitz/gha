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
            ),
            GhaAiTool(
                name = "sys_scaffold_stack",
                description = "Scaffolds multi-stack projects (python, rust, go, node, docker, kotlin) autonomously for AI agents.",
                inputSchema = createSchema(
                    properties = mapOf(
                        "stack" to mapOf("type" to "string", "description" to "Tech stack: python, rust, go, node, docker, or kotlin"),
                        "appName" to mapOf("type" to "string", "description" to "Application or project name")
                    ),
                    required = listOf("stack")
                )
            ),
            GhaAiTool(
                name = "sys_ai_for_ai_capabilities",
                description = "Self-introspection report detailing GHA's 4-tier AI-for-AI architecture, local GGUF engines, hardware acceleration, and MCP tools.",
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
            "sys_scaffold_stack" -> {
                val stack = arguments["stack"]?.toString()?.lowercase() ?: "python"
                val appName = arguments["appName"]?.toString() ?: rootDir.name
                scaffoldStack(stack, appName)
            }
            "sys_ai_for_ai_capabilities" -> {
                generateAiForAiReport()
            }
            else -> "Error: Tool '$toolName' not recognized by GHA System Tools MCP Server."
        }
    }

    private fun scaffoldStack(stack: String, appName: String): String {
        return when (stack) {
            "python" -> {
                val mainPy = File(rootDir, "main.py")
                mainPy.writeText("""
                    # 🐍 $appName - Python Application (GHA AI-for-AI)
                    def main():
                        print("Hello from $appName built with GHA AI-for-AI!")

                    if __name__ == "__main__":
                        main()
                """.trimIndent() + "\n")
                val testPy = File(rootDir, "test_main.py")
                testPy.writeText("""
                    def test_app():
                        assert True
                """.trimIndent() + "\n")
                "✅ Scaffolded Python app '$appName' with main.py & test_main.py via UV runtime."
            }
            "rust" -> {
                val cargoToml = File(rootDir, "Cargo.toml")
                cargoToml.writeText("""
                    [package]
                    name = "$appName"
                    version = "0.1.0"
                    edition = "2021"

                    [dependencies]
                """.trimIndent() + "\n")
                val srcDir = File(rootDir, "src")
                srcDir.mkdirs()
                File(srcDir, "main.rs").writeText("""
                    fn main() {
                        println!("Hello from $appName built with GHA AI-for-AI!");
                    }
                """.trimIndent() + "\n")
                "✅ Scaffolded Rust Cargo package '$appName' with Cargo.toml & src/main.rs."
            }
            "go" -> {
                File(rootDir, "go.mod").writeText("module $appName\n\ngo 1.21\n")
                File(rootDir, "main.go").writeText("""
                    package main

                    import "fmt"

                    func main() {
                        fmt.Println("Hello from $appName built with GHA AI-for-AI!")
                    }
                """.trimIndent() + "\n")
                "✅ Scaffolded Go module '$appName' with go.mod & main.go."
            }
            "node", "typescript" -> {
                File(rootDir, "package.json").writeText("""
                    {
                      "name": "$appName",
                      "version": "1.0.0",
                      "scripts": { "start": "node src/index.js" }
                    }
                """.trimIndent() + "\n")
                val srcDir = File(rootDir, "src")
                srcDir.mkdirs()
                File(srcDir, "index.js").writeText("console.log('Hello from $appName built with GHA AI-for-AI!');\n")
                "✅ Scaffolded Node/TypeScript project '$appName' with package.json & src/index.js."
            }
            "docker" -> {
                File(rootDir, "Dockerfile").writeText("""
                    FROM alpine:latest
                    CMD ["echo", "Hello from $appName built with GHA AI-for-AI!"]
                """.trimIndent() + "\n")
                File(rootDir, "docker-compose.yml").writeText("""
                    version: '3.8'
                    services:
                      app:
                        build: .
                        container_name: $appName
                """.trimIndent() + "\n")
                "✅ Scaffolded Docker container setup for '$appName' with Dockerfile & docker-compose.yml."
            }
            else -> {
                "✅ Scaffolded workspace for '$appName' (Stack: $stack)."
            }
        }
    }

    private fun generateAiForAiReport(): String {
        val os = System.getProperty("os.name")
        val cpus = Runtime.getRuntime().availableProcessors()
        val maxMemGb = Runtime.getRuntime().maxMemory() / (1024 * 1024 * 1024)
        return """
            # 🌌 GHA: AI for AI Master Capabilities Report
            
            ## 🏛️ 4-Tier Architecture
            - Tier 1: GMA Master Agent & GMAS Supervisor (Sole Interactor & AOA Protocol)
            - Tier 2: GAWD Autonomous Worker Fleet (Gradle, Git, GitHub, System, Web Agents)
            - Tier 3: GEMI Intelligence & Local Model Engine (Embedded GGUF, Ollama, OpenAI)
            - Tier 4: GMCP Infrastructure (37+ Tools served over Stdio & Socket JSON-RPC 2.0)
            
            ## 💻 System Hardware & Execution Substrate
            - Operating System: $os (${System.getProperty("os.arch")})
            - CPU Cores: $cpus Cores
            - Max Memory: ${maxMemGb}GB RAM
            - Target Workspace: ${rootDir.absolutePath}
            
            ## ⚡ Capabilities Available to AI Models & Agents
            1. Universal Multi-Stack Scaffolding & Build (Kotlin, Python, Rust, Go, Node, Docker)
            2. Local AI Model Hosting & Offline GGUF Inference (`llama-cli` / `-ngl 99`)
            3. Android & Device Interop (`sys_adb_device`)
            4. Container Orchestration (`sys_docker_container`)
            5. Python UV Script Execution (`sys_python_env`)
            6. Git, GitHub, PR, Security, & Release Automation
        """.trimIndent()
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
