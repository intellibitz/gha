# gha: Git, GitHub & Gradle Automation (Universal & AI-Native)

**gha** (`cc.thevar.gha`) is a **100% self-contained, 100% Kotlin** automation engine and **GMA Master Interactor ("The Agent of Agents")**. **gha runs on gha**—automating its own development, testing, dependencies, commits, pulls, PRs, releases, security, issues, wikis, insights, and AI workflows.

The **GHA Master Agent (GMA)** serves as the **Sole Interactor** for the GHA User, coordinating every manager, agent, engine, model, and MCP tool across any project, anywhere.

GitHub users and developers across any IDE or terminal can clone or install this project and expect **0% system modifications**. All dependencies, Kotlin libraries, Gradle caches, JDK toolchains, AI models, init scripts, and execution state are strictly sandboxed inside a single `.gha/` directory.

---

## 🌟 Mission: 0 Effort, 100% Gain (`gha: ai, anywhere`)

`gha` creators—**Intellibitz**, **Muthu Ramadoss**, **Gemini (Google AI)**, and other AI agents alongside GitHub community contributors—build platform-independent automation workflows and multi-agent AI tools written purely in **100% Kotlin** to power software development everywhere.

---

## 🌌 GMA 4-Tier Master Architecture

`gha` brings the power of AI to **every user, anywhere, even on limited home hardware**, through a 4-tier coordinated system:

1. **🏛️ Tier 1: GHA Master Agent (GMA)**: The **Master**. The Orchestrator. The One. GMA sits in the front as the singular Sole Interactor for the user. It takes user queries, coordinates GAWD, GEMI, and GMCP to complete work in the best possible execution, and reports back while awaiting the next command.
2. **🤖 Tier 2: GHA Agents Web & Domain (GAWD)**: The **Workers** that execute missions. GAWD makes use of GEMI (Tier 3) for reasoning and thinking, and GMCP (Tier 4) for skills, tools, and capabilities. Includes Gradle, Git, GitHub, System, and Web fleet agents.
3. **🧠 Tier 3: GHA Engines & Models AI Inference (GEMI)**: The **Intelligence** layer. GEMI coordinates all AI inference engines (Ollama, Groq, OpenAI) and autonomous models (DeepSeek R1, Llama 3.3). GEMI provides pure reasoning and reports back to agents without interacting with the infrastructure layer (unless an engine is also an agent).
4. **🔌 Tier 4: Model Context Protocol (GMCP)**: The **Infrastructure** layer. Full MCP implementation (Host, Client, Server) exposing hardware, system tools, and registries. Only Agents (Tier 2 and agent-capable engines) talk to GMCP.

---

### 🔄 GMA Delegation Cascade

Every user instruction (e.g. `./ghai "build an os"`) starts with the GMA and cascades through the 4 tiers:

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

Install `gha` into **any repository** instantly. The installer automatically primes the GMA Master Agent and GMCP Interactor to keep them running and ready:

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
