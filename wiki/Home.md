# gha: Git, GitHub & Gradle Automation (Universal & AI-Native)

**gha** (`cc.thevar.gha`) is a **100% self-contained, 100% Kotlin** automation engine and **GMA Master Interactor ("The Agent of Agents")**. **gha runs on gha**—automating its own development, testing, dependencies, commits, pulls, PRs, releases, security, issues, wikis, insights, and AI workflows.

The **GHA Master Agent (GMA)** serves as the **Sole Interactor** for the GHA User, coordinating every manager, agent, engine, model, and MCP tool across any project, anywhere.

---

## 🌟 Mission: 0 Effort, 100% Gain (`gha: ai, anywhere`)

`gha` creators—**Intellibitz**, **Muthu Ramadoss**, **Gemini (Google AI)**, and other AI agents alongside GitHub community contributors—build platform-independent automation workflows and multi-agent AI tools written purely in **100% Kotlin** to power software development everywhere.

---

## 🌌 GMA 4-Tier Coordinated Intelligence

`gha` is a 4-tier coordinated ecosystem where each layer possesses custom intelligence adhering to its respective protocol, capable of communicating with each other and external systems:

1. **🏛️ Tier 1: GHA Master Agent (GMA)**: The **Master**. The Orchestrator. GMA stands tall as the singular Sole Interactor and one-point manager. It governs the 4-tier cascade and uses its **Master Coordination Intelligence** to complete user work via GAWD, GEMI, and GMCP. GMA is fully AOA Standard Protocol compliant and manages other Agent of Agents worldwide.
2. **🤖 Tier 2: GHA Agents Web & Domain (GAWD)**: The **Workers**. GAWD workers possess **Worker Intelligence** to refine mission strategies. They communicate with Tier 3 (GEMI) for reasoning and Tier 4 (GMCP) for skills, tools, and hardware capabilities.
3. **🧠 Tier 3: GHA Engines & Models AI Inference (GEMI)**: The **Intelligence**. GEMI possesses **Inference Intelligence** to route queries to the optimal local or cloud model. It provides pure reasoning and strategy back to GAWD workers.
4. **🔌 Tier 4: Model Context Protocol (GMCP)**: The **Infrastructure**. GMCP possesses **Infrastructure Intelligence** to self-heal and resolve hardware/system tools (ADB, Docker). It implements the full MCP protocol, hosting core servers like `gmcp-tools-user` to expose system capabilities to any external system over stdio or sockets.

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
