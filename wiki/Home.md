# 🌌 gha: AI for AI — Universal Multi-Agent AI Runtime & MCP Engine

> **100% Platform Independent • 100% Sandboxed • 100% IDE Independent • 0% Effort • 100% Gains**

**gha** is a **100% self-contained, standalone native Rust binary engine** powering **AI for AI, anywhere for anything**.

---

## ⚡ 1-Line Universal Installation

```bash
# Linux, macOS, & WSL:
curl -sSL https://raw.githubusercontent.com/intellibitz/gha/main/init/install.sh | bash

# Windows PowerShell:
iwr -useb https://raw.githubusercontent.com/intellibitz/gha/main/init/install.ps1 | iex
```

---

## 🌟 The 3-Tier Coordinated Intelligence Architecture: GAWD, GEMI & GMCP

1. **🤖 Tier 1: GAWD (GMA Master Agent & GMAS Supervisor)**
   - **GMA (GHA Master Agent)**: The Master, The One, and Sole Interactor for the user.
   - **GMAS (GMA Supervisor)**: AOA (Agent of Agents) & A2A (Agent to Agent) protocol governor managing worker agent fleets.

2. **🧠 Tier 2: GEMI (GHA Engines & Models AI Inference)**
   - **Local GGUF & Cloud AI Inference Coordinator**: Loads `.gguf` models directly from `~/.gha/models`. Serves OpenAI-compatible ChatCompletions API endpoints.
   - **Autonomous Hardware Profiler**: Profiles CPU cores and Metal/CUDA GPU acceleration for GPU offloading (`-ngl 99`).

3. **🔌 Tier 3: GMCP (GMA Master MCP Infrastructure)**
   - **Full MCP Host, Client & Server**: Standalone JSON-RPC 2.0 MCP implementation over stdio and background TCP sockets (Port 9090).
   - **Universal Tool Registry**: Exposes 39+ AI tools to external IDEs (VS Code, Android Studio, IntelliJ, Cursor), LLMs, and agents.

---

## 🚀 The GMA Sole Interactor (`ghai`)

```bash
ghai                                           # Interactive GMA Interactor
ghai "build an AI workflow"                    # GMA Mission: execute natural AI instruction
ghai gmcp, mcp                                 # Start native GMA Master MCP Server over stdio
ghai ai orchestrate                            # GMA Master Interactor coordination report
ghai ai models                                 # Inspect GGUF & web AI models
ghai ai engines                                # Detect local & cloud AI inference engines
ghai ai mcp-hub                                # List GMA-coordinated MCP tool servers
ghai :version                                  # Print engine architecture report
ghai :status                                   # Print workspace health & daemon status
ghai :install                                  # Initialize sandboxed .gha environment offline
```
