# gha: Git, GitHub & Gradle Automation (Universal & AI-Native)

**gha** (`cc.thevar.gha`) is a **100% self-contained, 100% Kotlin** automation engine and **GMA Master Interactor ("The Agent of Agents")**. **gha runs on gha**—automating its own development, testing, dependencies, commits, pulls, PRs, releases, security, issues, wikis, insights, and AI workflows.

The **GHA Master Agent (GMA)** serves as the **Sole Interactor** for the GHA User, coordinating every manager, agent, engine, model, and MCP tool across any project, anywhere.

GitHub users and developers across any IDE or terminal can clone or install this project and expect **0% system modifications**. All dependencies, Kotlin libraries, Gradle caches, JDK toolchains, AI models, init scripts, and execution state are strictly sandboxed inside a single `.gha/` directory.

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
