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

## 🌟 Component Architecture & Port Mapping

`gha` operates a dual-port native architecture for tools and AI inference:

```text
 ┌────────────────────────────────────────────────────────────────────────┐
 │ 🤖 Tier 1: GAWD (GMA Master Agent & GMAS Supervisor)                   │
 │ • Sole Interactor for User & Multi-Agent Fleet Governor                │
 └─────────────────┬────────────────────────────────────┬─────────────────┘
                   │                                    │
                   ▼                                    ▼
 ┌──────────────────────────────────┐  ┌──────────────────────────────────┐
 │ 🧠 Tier 2: GEMI Inference        │  │ 🔌 Tier 3: GMCP MCP Infrastructure│
 │ • Local GGUF Model Runner        │  │ • Native JSON-RPC 2.0 MCP Server │
 │ • Unique Port 9091 REST API      │  │ • 39+ Tools (Stdio & Port 9090)  │
 └──────────────────────────────────┘  └──────────────────────────────────┘
```

### 1. 🤖 Tier 1: GAWD (GMA Master Agent & GMAS Supervisor)
* **What it does**: **GMA (GHA Master Agent)** acts as the **Sole Interactor** for the user. **GMAS (GMA Supervisor)** governs worker agent fleets using **AOA (Agent of Agents)** and **A2A (Agent to Agent)** protocols.
* **How to use it**:
  ```bash
  ghai "orchestrate an autonomous AI workflow"   # Execute natural language AI goal
  ghai :status                                   # Inspect GAWD fleet & workspace health
  ```

### 2. 🧠 Tier 2: GEMI (GHA Engines & Models AI Inference) — Unique Port 9091
* **What it does**: Coordinates local GGUF (`.gguf`) models in `~/.gha/models`, exposes a unique OpenAI-compatible ChatCompletions endpoint (`http://127.0.0.1:9091/v1`), and profiles hardware (CPU cores, Metal/CUDA GPU offloading `-ngl 99`).
* **How to use it**:
  ```bash
  ghai ai server                                 # Start GEMI REST Server on Port 9091
  ghai ai models                                 # Catalog local GGUF & cloud AI models
  ghai ai engines                                # Inspect embedded GGUF & cloud inference engines
  ```

### 3. 🔌 Tier 3: GMCP (GMA Master MCP Infrastructure) — Port 9090 / Stdio
* **What it does**: Native JSON-RPC 2.0 MCP Host, Client & Server over stdio and background TCP sockets (Port 9090) exposing 39+ coordinated AI tools.
* **How to use it**:
  ```bash
  ghai gmcp, mcp                                 # Start stdio MCP Server for IDE integration
  ghai ai mcp-hub                                # List active MCP tool servers & tools
  ```

---

## 🔌 Connecting Android Studio / Custom LLM Clients to GEMI (Port 9091)

In Android Studio, JetBrains AI Assistant, Cursor, or Python OpenAI SDK:
* **Base URL**: `http://127.0.0.1:9091/v1`
* **API Key**: `gha-native-key` (any string)
* **Model**: `deepseek-r1` or `llama-3.3-70b`

---

## 🛡️ 2-Layer Sandboxing Model (`~/.gha` & `./.gha`)

* **Global Vault (`~/.gha/`)**: Houses the global `ghai` executable, central GGUF model vault (`~/.gha/models/`), and daemon locks. Shared across all projects.
* **Local Workspace Sandbox (`./.gha/`)**: Stores workspace build caches and session logs. Completely git-ignored. Cleaned instantly via `ghai :uninstall`.

---

## Contributors

`gha` is co-created and maintained by **IntelliBitz**, **Muthu Ramadoss**, and **Gemini (Google AI)**. See [CONTRIBUTORS.md](CONTRIBUTORS.md) for details.

## License

This project is licensed under the [MIT License](LICENSE).
