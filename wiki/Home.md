# gha: Git, GitHub & Gradle Automation (Universal & AI-Native)

**gha** (`cc.thevar.gha`) is a **100% self-contained, 100% Kotlin** automation engine and **GMA Master Interactor ("The Agent of Agents")**. **gha runs on gha**—automating its own development, testing, dependencies, commits, pulls, PRs, releases, security, issues, wikis, insights, and AI workflows.

The **GHA Master Agent (GMA)** serves as the **Sole Interactor** for the GHA User, coordinating every manager, agent, engine, model, and MCP tool across any project, anywhere.

---

## 🌟 Mission: 0 Effort, 100% Gain (`gha: ai, anywhere`)

`gha` creators—**Intellibitz**, **Muthu Ramadoss**, **Gemini (Google AI)**, and other AI agents alongside GitHub community contributors—build platform-independent automation workflows and multi-agent AI tools written purely in **100% Kotlin** to power software development everywhere.

---

## 🌌 GMA 4-Tier Coordinated Intelligence

`gha` is a 4-tier coordinated ecosystem where each layer possesses custom intelligence adhering to standard industry protocols, communicating with each other and external systems:

1. **🏛️ Tier 1: GHA Master Agent (GMA) & GMA Supervisor (GMAS)**:
   - **GMA (GHA Master Agent)**: The **Master**. The Orchestrator. The One. GMA sits in front as the singular **Sole Interactor** for the GHA user, operating as an Agent, Agent of Agents (AOA), Engine, and MCP Client/Host.
   - **GMAS (GMA Supervisor)**: The dedicated AOA Supervisor sitting below GMA at Tier 1. GMAS follows standard AOA protocol (`aoa/init`, `aoa/supervise`, `aoa/plugin/download`) to supervise internal GAWD workers and downloaded web/AOA plugins, reporting directly to GMA.
2. **🤖 Tier 2: GHA Agents Web & Domain (GAWD)**:
   - **Custom GAWD Agent**: Implements standard **Agent Task & Step Execution Protocol** and **Agent-to-Agent (A2A)** communication protocol (`A2AMessage` with FIPA performatives `REQUEST`, `INFORM`, `DELEGATE`, `RESPONSE`).
   - **Worker Fleet**: Gradle, Git, GitHub, System, and Web agents executing domain missions using Tier 3 (GEMI) for reasoning and Tier 4 (GMCP) for tools.
3. **🧠 Tier 3: GHA Engines & Models AI Inference (GEMI)**:
   - **GHA Native Engine**: High-performance local inference coordinator loading `.gguf` models directly in `~/.gha/models`. Implements OpenAI Chat Completions API (`chatCompletion`) and MCP reasoning tools (`gemi_reason`).
   - **Autonomous Hardware Optimization**: Profiles CPU cores and GPU capabilities (Metal/CUDA) to automatically configure threading and GPU layer offloading (`-ngl 99`).
4. **🔌 Tier 4: GMA Master MCP Engine (GMCP)**:
   - **Full MCP Host, Client & Server**: JSON-RPC 2.0 MCP implementation (`GhaGmcpEngine` / `GhaGmcpClient`) over stdio and background TCP sockets (Port 9090).
   - **Tools Registry**: Exposes 20+ GHA tools and system capability servers (`gmcp-tools-user` for ADB, Docker, Python UV, System CLI) to external IDEs, LLMs, and agents.

---

## 🎯 User Interaction & Entry Point Flow

```
   GHA User Prompt / Command (CLI: ./ghai "my goal")
                       │
                       ▼
    [GhaAiTask.kt] / [GhaAiOrchestratorTask.kt]
                       │
                       ▼
 🌌 Tier 1: GHA Master Agent (GMA / GhaAgentOfAgents.kt)
 └── Sole Interactor for User & Top-Level Master Governor
                       │
     ┌─────────────────┴─────────────────┐
     ▼                                   ▼
 [GMAS Supervisor]                [GEMI Engines]
 [GhaGmasAgent.kt]               [GhaGemiEngine.kt]
     │                                   │
     ▼                                   ▼
 [GAWD Workers]                   [GMCP Tools]
 [GhaGawdAgent.kt]               [GhaGmcpEngine.kt]
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

Every user instruction starts with the GMA and cascades through the 4 tiers. GMA runs as a **persistent background daemon** in the global sandbox (`~/.gha`), providing parallel coordination across many projects at once with 0% system modifications.

---

## 📦 Universal Auto-Installer & Bootstrapper (`GhaBootstrapManager`)

* **Zero-Setup On-Demand Bootstrap**: Automatically detects and installs missing AOA Python packages, engines, GGUF models, and MCP servers on-demand when a mission is received.
* **Dynamic Multi-Agent Creation**: Dynamically instantiates and parallelizes custom AOAs and specialized Sub-Agents to solve multi-step user instructions.
* **Hardware Profiling**: Analyzes RAM, CPU cores, and GPU acceleration to recommend optimal quantized model tiers.

---

## 🚀 The GMA Sole Interactor (`ghai`)

```bash
./ghai                                           # Autonomous AI Workflow (Sync, Commit, Push, PR)
./ghai "build an os"                            # GMA Mission: execute natural instruction
./ghai "use aoa Public-AOA-01 to review code"   # Inter-AOA Delegation mission
./ghai "download aoa http://registry.ai/p.zip"  # Download and install AOA standard plugin
./ghai gmcp                                      # Start GMA Master MCP Server (stdio)
./ghai ai orchestrate                            # GMA Master Interactor coordination report
./ghai ai models                                 # Inspect coordinated local & web AI models
./ghai ai engines                                # Detect coordinated local & cloud engines
./ghai ai mcp-hub                                # List GMA-coordinated MCP servers
./ghai :version                                  # GMA detailed version report
./ghai :status                                   # GMA sandbox health report
./ghai :install                                  # Initialize GMA sandboxed environment
./ghai :clone <repo>                             # Smart clone via GMA Git Agent
```

---

## ⚡ 1-Second Installation

Install `gha` into **any repository** instantly. The installer automatically primes the GMA Master Agent and GMCP Interactor to keep them running and ready:

```bash
# Linux, macOS, & WSL:
curl -sSL https://raw.githubusercontent.com/intellibitz/gha/main/init/install.sh | bash

# Windows PowerShell:
iwr -useb https://raw.githubusercontent.com/intellibitz/gha/main/init/install.ps1 | iex
```
