# gha: AI for AI — Universal, Multi-Stack & AI-Native Engine

**gha** (`cc.thevar.gha`) is a **100% self-contained, 100% Kotlin** autonomous AI runtime, supervisor, and execution engine powering **AI for AI, anywhere for anything**. **gha runs on gha**—automating its own development, testing, dependencies, commits, pulls, PRs, releases, security, issues, wikis, insights, hardware profiling, and AI workflows.

The **GHA Master Agent (GMA)** serves as the **Sole Interactor** for the GHA User, coordinating every manager, agent, engine, model, and MCP tool across any project, multi-stack technology (Kotlin, Python, Rust, Go, TypeScript, Docker, Android, ADB), anywhere.

GitHub users and developers across any IDE or terminal can clone or install this project and expect **0% system modifications**. All dependencies, Kotlin libraries, Gradle caches, JDK toolchains, AI models, init scripts, and execution state are strictly sandboxed inside a single `.gha/` directory.

---

## 🌟 Mission: 0 Effort, 100% Gain (`gha: ai for ai, anywhere for anything`)

`gha` creators—**Intellibitz**, **Muthu Ramadoss**, **Gemini (Google AI)**, and other AI agents alongside GitHub community contributors—build platform-independent automation workflows and multi-agent AI tools written purely in **100% Kotlin** to power software development everywhere.

---

## 🌌 GHA 3-Tier Coordinated Intelligence Architecture

`gha` is a 3-tier coordinated ecosystem where each layer possesses custom intelligence adhering to standard industry protocols, communicating seamlessly with each other and external systems:

1. **🤖 Tier 1: GAWD (GHA Agents Web & Domain)**:
   - **GMA (GHA Master Agent)**: The Master, The One, and Sole Interactor for the user. Operates as an Autonomous Agent, Agent of Agents (AOA), Engine, and MCP Client/Host.
   - **GMAS (GMA Supervisor)**: AOA Protocol Supervisor sitting inside Tier 1. Follows standard AOA protocol (`aoa/init`, `aoa/supervise`, `aoa/plugin/download`) to supervise GAWD worker agents and web plugins, reporting directly to GMA.
   - **Domain & Web Worker Fleet**: Specialized agents implementing A2A (`A2AMessage`) protocol for Gradle, Git, GitHub, System (ADB, Docker, Python UV, Rust, Go, Node), Security, Dependabot, and Web Research.
2. **🧠 Tier 2: GEMI (GHA Engines & Models AI Inference)**:
   - **AI Inference Engine Coordinator**: High-performance local inference coordinator loading `.gguf` models directly from `~/.gha/models`. Serves OpenAI Chat Completions API (`chatCompletion`) and MCP reasoning tools (`gemi_reason`).
   - **Autonomous Hardware Optimization**: Profiles CPU cores and GPU capabilities (Metal/CUDA) to automatically configure threading and GPU layer offloading (`-ngl 99`).
3. **🔌 Tier 3: GMCP (GMA Master MCP Infrastructure)**:
   - **Full MCP Host, Client & Server**: JSON-RPC 2.0 MCP implementation (`GhaGmcpEngine` / `GhaGmcpClient`) over stdio and background TCP sockets (Port 9090).
   - **Universal Tool Registry**: Exposes 39+ GHA tools and system capability servers (ADB, Docker, Python UV, Rust, Go, System CLI) to external IDEs, LLMs, and agents.

---

## 🎯 User Interaction & Entry Point Flow

```
   GHA User Prompt / Command (CLI: ./ghai "my goal")
                       │
                       ▼
 🤖 Tier 1: GAWD (GMA Master Agent & GMAS Supervisor)
 └── Sole Interactor for User & Top-Level Master Governor
                       │
     ┌─────────────────┴─────────────────┐
     ▼                                   ▼
 🧠 Tier 2: GEMI Intelligence       🔌 Tier 3: GMCP Infrastructure
 (Local GGUF Engines & Models)      (MCP Host, Client & 39+ Tools)
     │                                   │
     └─────────────────┬─────────────────┘
                       │
                       ▼
 🌌 GMA Synthesizes Master Report (`output`)
                       │
                       ▼
         Presented Directly to User
```

---

## 🏗️ GHA Component Roles & Responsibilities

### 🏛️ Tier 1: GMA (Master Agent) & GMAS (Supervisor)
* **GMA (GHA Master Agent)** (`GhaAgentOfAgents.kt`): The Master, The One, and Sole Interactor for the user. Operates as an Autonomous Agent, Agent of Agents (AOA), Engine, and MCP Client/Host.
* **GMAS (GMA Supervisor)** (`GhaGmasAgent.kt`): Tier 1 AOA Supervisor sitting below GMA. Follows standard AOA Protocol (`aoa/init`, `aoa/supervise`, `aoa/plugin/download`) to supervise custom GAWD workers and downloaded web/AOA plugins, reporting back to GMA.

### 🤖 Tier 2: GAWD (Agents Web & Domain)
* **Custom GAWD Agent** (`GhaGawdAgent.kt`): Standard Protocol-Compliant Worker Agent implementing **Agent Task & Step Execution Protocol** (`INITIATED`, `IN_PROGRESS`, `COMPLETED`, `FAILED`) and **Agent-to-Agent (A2A)** Communication Protocol (`A2AMessage`).
* **Domain Workers** (`GhaSpecializedAgents.kt`): Gradle Agent (Scaffolding & Build), Git Agent (VCS & Context), GitHub Agent (PRs & Workflows), System Agent (ADB, Docker, Python UV, System CLI).
* **Web Agents** (`GhaWebAgents.kt` / `GhaWebAgentManager.kt`): Web Research, Hugging Face Hub, GitHub Remote API, and Remote MCP Web agents.

### 🧠 Tier 3: GEMI (Engines & Models AI Inference)
* **GEMI Router** (`GhaGemiEngine.kt`): Tier 3 Pure Intelligence & Reasoning Router evaluating prompts and routing to local or web engines.
* **GHA Native Engine** (`GhaNativeGemiEngine.kt`): Embedded GGUF model runner loading `.gguf` models directly from `~/.gha/models`. Exposes OpenAI-compatible Chat Completions API (`chatCompletion`) and MCP reasoning tools (`gemi_reason`).
* **Hardware Profiler** (`GhaHardwareProfiler.kt`): Profiles CPU cores and GPU capabilities (Metal/CUDA) to automatically calculate CPU threading and GPU layer offloading (`-ngl 99`).

### 🔌 Tier 4: GMCP (Model Context Protocol Infrastructure)
* **GMA Master MCP Server** (`GhaGmcpEngine.kt`): JSON-RPC 2.0 MCP implementation over stdio and background TCP sockets (Port 9090) with self-healing tool resolution (`resolveWithIntelligence`).
* **GMA Master MCP Client** (`GhaGmcpClient.kt`): Native Kotlin MCP client used by GMA and workers to call tools over MCP.
* **Tool Servers**: Built-in Universal MCP Server (`GhaUniversalMcpServer.kt`), System Tools MCP Server (`GhaSystemMcpServer.kt`), and MCP Host Hub (`GhaMcpHost.kt`) aggregating external stdio/SSE MCP tools.

---

## 🌍 Ecosystem Interoperability & External Exposure

```
┌────────────────────────────────────────────────────────────────────────────────────────┐
│ 🌍 OUTSIDE WORLD (IDEs, Claude Desktop, VS Code, AutoGen, CrewAI, OpenAI, LangChain)    │
└────────┬──────────────────────┬──────────────────────┬──────────────────────┬──────────┘
         │                      │                      │                      │
         │ MCP (JSON-RPC)       │ AOA Protocol         │ A2A Protocol         │ OpenAI API
         ▼                      ▼                      ▼                      ▼
┌──────────────────┐   ┌──────────────────┐   ┌──────────────────┐   ┌──────────────────┐
│ GMCP Server      │   │ GMA & GMAS       │   │ GAWD Agent       │   │ GEMI Engine      │
│ - Stdio / Socket │   │ - AOA Interceptor│   │ - A2A Envelope   │   │ - ChatCompletion │
│ - Tools Registry │   │ - Handoff / AOA  │   │ - Step Protocol  │   │ - OpenAI Payload │
└──────────────────┘   └──────────────────┘   └──────────────────┘   └──────────────────┘
```

---

### 🔄 GMA Delegation Cascade & Always-On Architecture

Every user instruction (e.g. `./ghai "build an os"`) starts with the GMA and cascades through the 4 tiers. GMA runs as a **persistent background daemon** in the global sandbox (`~/.gha`), providing parallel coordination across many projects at once with 0% system modifications.

```
┌─────────────────────────────────────────────────────────────────────────────┐
│ Tier 1: GMA Master Agent (The Master)                                       │
│ - Singular management of missions, context, and coordination.               │
└──────────────────────────────────────┬──────────────────────────────────────┘
                                       │
                                       ▼
┌─────────────────────────────────────────────────────────────────────────────┐
│ Tier 2: GHA Agents Web & Domain (GAWD)                                      │
│ - Workers (Gradle, Git, GitHub, System) that use Tier 3 & Tier 4.           │
└──────────────────────────────────────┬──────────────────────────────────────┘
                                       │
                                       ▼
┌─────────────────────────────────────────────────────────────────────────────┐
│ Tier 3: GHA Engines & Models AI Inference (GEMI)                            │
│ - Pure intelligence & reasoning for GAWD agents.                            │
└──────────────────────────────────────┬──────────────────────────────────────┘
                                       │
                                       ▼
┌─────────────────────────────────────────────────────────────────────────────┐
│ Tier 4: GMCP Host, Client & Server (The Infrastructure)                     │
│ - Skills and tools used by GAWD agents.                                     │
└─────────────────────────────────────────────────────────────────────────────┘
```

---

## 📦 Universal Auto-Installer & Bootstrapper (`GhaBootstrapManager`)

* **Zero-Setup On-Demand Bootstrap**: Automatically detects and installs missing AOA Python packages, engines, GGUF models, and MCP servers on-demand when a mission is received.
* **Dynamic Multi-Agent Creation**: Dynamically instantiates and parallelizes custom AOAs and specialized Sub-Agents to solve multi-step user instructions.
* **Hardware Profiling**: Analyzes RAM, CPU cores, and GPU acceleration to recommend optimal quantized model tiers.

---

## 🛡️ Core Principles

- **100% Sandboxed & 0 Side Effects**: `gha` **never modifies existing project files** (`settings.gradle.kts`, `build.gradle.kts`). All execution state lives inside `.gha/`.
- **Invisible Integration**: The entire `.gha/` sandbox is automatically git-ignored.
- **Dynamic Repository URLs**: Supports custom forks, mirrors, and upstream repositories via `GHA_REPO` or `git config gha.repo`.
- **Automated Push Versioning**: Enforces a pre-push Git hook (`.git/hooks/pre-push`) to autonomously bump patch versions for every push.
- **100% Self-Healing**: Proactively restores missing launcher scripts, Gradle wrappers, and sandbox configurations autonomously.
- **100% Portable & "Copy-Paste" Ready**: Copy `.gha/` and `ghai` anywhere, and it works instantly with **zero installation**.

---

## 🚀 The GMA Sole Interactor (`ghai`)

The `ghai` launcher is the universal entry point that talks directly to the GMA:

```bash
./ghai                                           # Autonomous AI Workflow (Sync, Commit, Push, PR)
./ghai "build an os"                            # GMA Mission: execute natural instruction
./ghai "create a kotlin app"                     # GMA Mission: application scaffolding
./ghai ai orchestrate                            # GMA Master Interactor coordination report
./ghai ai models                                 # Inspect coordinated local & web AI models
./ghai ai engines                                # Detect coordinated local & cloud engines
./ghai ai mcp-hub                                # List GMA-coordinated MCP servers
./ghai gmcp                                      # Start GMA Master MCP Server (stdio)
./ghai :version                                  # GMA detailed version report
./ghai :status                                   # GMA sandbox health report
./ghai :install                                  # Initialize GMA sandboxed environment
./ghai :clone <repo>                             # Smart clone via GMA Git Agent
```

---

## ⚡ 1-Second Installation

Install `gha` globally with a single command. The installer automatically primes the GMA Master Agent, GMCP Interactor, and **adds `ghai` to your PATH** for instant access anywhere:

```bash
# Linux, macOS, & WSL:
curl -sSL https://raw.githubusercontent.com/intellibitz/gha/main/init/install.sh | bash

# Windows PowerShell:
iwr -useb https://raw.githubusercontent.com/intellibitz/gha/main/init/install.ps1 | iex
```

---

## Contributors

`gha` is co-created and maintained by **IntelliBitz**, **Muthu Ramadoss**, and **Gemini (Google AI)**. See [CONTRIBUTORS.md](CONTRIBUTORS.md) for the full list of project creators and contributors.

## License

This project is licensed under the [MIT License](LICENSE).
