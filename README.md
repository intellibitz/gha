# 🌌 gha: AI for AI — Universal Multi-Agent AI Runtime & MCP Engine

> **100% Platform Independent • 100% Sandboxed • 100% IDE Independent • 0% Effort • 100% Gains**

**gha** is a **100% self-contained, standalone native Rust binary engine** powering **AI for AI, anywhere for anything**.

---

## ⚡ 1-Line Universal Installation (0-Effort, 100% Gains)

Install `gha` globally with a single command:

```bash
# Linux, macOS, & WSL (1-Line Universal Installer):
curl -sSL https://raw.githubusercontent.com/intellibitz/gha/main/init/install.sh | bash

# Windows PowerShell:
iwr -useb https://raw.githubusercontent.com/intellibitz/gha/main/init/install.ps1 | iex
```

---

## 🌟 Core Architecture: GAWD, GEMI & GMCP

`gha` operates as a 3-tier coordinated ecosystem where each layer possesses custom intelligence, communicating seamlessly with external agents, IDEs, and LLMs:

1. **🤖 Tier 1: GAWD (GMA Master Agent & GMAS Supervisor)**
   - **GMA (GHA Master Agent)**: The Master, The One, and Sole Interactor for the user.
   - **GMAS (GMA Supervisor)**: AOA (Agent of Agents) and A2A (Agent to Agent) protocol supervisor managing worker agent fleets and web plugins.

2. **🧠 Tier 2: GEMI (GHA Engines & Models AI Inference)**
   - **Local & Cloud AI Inference Coordinator**: Loads GGUF `.gguf` models directly from `~/.gha/models`. Serves OpenAI-compatible Chat Completions API endpoints.
   - **Autonomous Hardware Profiler**: Profiles CPU cores and GPU acceleration (Metal/CUDA) to automatically configure GPU layer offloading (`-ngl 99`).

3. **🔌 Tier 3: GMCP (GMA Master MCP Infrastructure)**
   - **Full MCP Host, Client & Server**: Standalone JSON-RPC 2.0 MCP implementation over stdio and background TCP sockets (Port 9090).
   - **Universal Tool Registry**: Exposes 39+ AI tools to external IDEs (VS Code, Android Studio, IntelliJ, Cursor), LLMs, and agents.

---

## 🚀 The GMA Sole Interactor (`ghai`)

The ultra-fast native `ghai` executable responds in **sub-millisecond (< 2ms)** latency:

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

---

## 🛡️ Core Principles

- **100% Sandboxed & Zero Side Effects**: Everything lives strictly inside `.gha/` or `~/.gha/`. Zero host system pollution.
- **100% IDE & Environment Independent**: Plugs into any editor, terminal, or agent network via standard Model Context Protocol (MCP).
- **Single Standalone Native Binary**: Instant startup in < 2ms without third-party runtime dependencies.

---

## Contributors

`gha` is co-created and maintained by **IntelliBitz**, **Muthu Ramadoss**, and **Gemini (Google AI)**. See [CONTRIBUTORS.md](CONTRIBUTORS.md) for details.

## License

This project is licensed under the [MIT License](LICENSE).
