# gha: Git, GitHub & Gradle Automation (Universal & AI-Native)

**gha** (`cc.thevar.gha`) is a **100% self-contained, 100% Kotlin** automation engine and **GMA Master Interactor ("The Agent of Agents")**. **gha runs on gha**—automating its own development, testing, dependencies, commits, pulls, PRs, releases, security, issues, wikis, insights, and AI workflows.

The **GHA Master Agent (GMA)** serves as the **Sole Interactor** for the GHA User, coordinating every manager, agent, engine, model, and MCP tool across any project, anywhere.

GitHub users and developers across any IDE or terminal can clone or install this project and expect **0% system modifications**. All dependencies, Kotlin libraries, Gradle caches, JDK toolchains, AI models, init scripts, and execution state are strictly sandboxed inside a single `.gha/` directory.

---

## 🌟 Mission: 0 Effort, 100% Gain (`gha: ai, anywhere`)

`gha` creators—**Intellibitz**, **Muthu Ramadoss**, **Gemini (Google AI)**, and other AI agents alongside GitHub community contributors—build platform-independent automation workflows and multi-agent AI tools written purely in **100% Kotlin** to power software development everywhere.

---

## 🌌 GMA Master Interactor Architecture

`gha` brings the power of AI to **every user, anywhere, even on limited home hardware**, through a singular master agent:

1. **🏛️ GMA Master Interactor & Pluggable AOA (`GhaAgentOfAgents`)**: The singular one-point manager and worker for the GHA user. It coordinates every system component and optionally delegates to frameworks like Microsoft AutoGen, CrewAI, LangGraph, or Swarm.
2. **🌐 Specialized Agents (`GhaAgentManager`)**: Domain-specific workers for Gradle, Git, GitHub, and System tasks, plus a fleet of Web Agents for research and remote APIs.
3. **⚡ AI Inference Engines (`GhaEngineManager`)**: Coordinated local (Ollama, llama.cpp, Python UV) and web-based AI engines (OpenRouter, Groq, HF, Gemini, OpenAI, Anthropic).
4. **🧠 AI Model Discovery & Resolution (`GhaModelManager`)**: Searches, resolves, and auto-downloads open-weights and cloud models across multiple registries.
5. **🔌 GMA Master MCP Interactor (GMCP)**: Full Model Context Protocol (MCP) implementation (Host, Client, Server) exposing 40+ tools to anyone over stdio. GMA hosts GMCP.

---

### 🔄 GMA Delegation Cascade

Every user instruction (e.g. `./ghai "build an os"`) starts with the GMA and cascades through the system:

```
┌─────────────────────────────────────────────────────────────────────────────┐
│ 1. GMA Master Interactor (Sole User Agent)                                  │
│ - Singular management of missions, context, and coordination.               │
└──────────────────────────────────────┬──────────────────────────────────────┘
                                       │
                                       ▼
┌─────────────────────────────────────────────────────────────────────────────┐
│ 2. Domain Agents & Web Agents (The Workers)                                 │
│ - Specialized agents (Gradle, Git, GitHub, System) and Web Agents fleet.    │
└──────────────────────────────────────┬──────────────────────────────────────┘
                                       │
                                       ▼
┌─────────────────────────────────────────────────────────────────────────────┐
│ 3. AI Inference Engines & Web Models                                        │
│ - Reasoning queries routed to local (Ollama) or cloud (Groq, OpenAI) engines.│
└──────────────────────────────────────┬──────────────────────────────────────┘
                                       │
                                       ▼
┌─────────────────────────────────────────────────────────────────────────────┐
│ 4. MCP Host & Tool Execution (The Hardware)                                 │
│ - GMA hosts MCP servers exposing 40+ system, build, git, and browser tools. │
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

Install `gha` into **any repository** instantly:

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
