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

## 🌟 Component Architecture & How To Use Each Tier

`gha` operates as a 3-tier coordinated ecosystem where each layer provides dedicated capabilities for `gha` users:

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
 │ • Hardware Profiler (-ngl 99)    │  │ • 39+ Coordinated Tools (Port 9090)│
 └──────────────────────────────────┘  └──────────────────────────────────┘
```

### 1. 🤖 Tier 1: GAWD (GMA Master Agent & GMAS Supervisor)
* **What it does**: **GMA (GHA Master Agent)** acts as the **Sole Interactor** for the user. **GMAS (GMA Supervisor)** governs worker agent fleets using **AOA (Agent of Agents)** and **A2A (Agent to Agent)** protocols.
* **How to use it**:
  ```bash
  ghai "orchestrate an autonomous AI workflow"   # Execute natural language AI goal
  ghai :status                                   # Inspect GAWD fleet & workspace health
  ```

### 2. 🧠 Tier 2: GEMI (GHA Engines & Models AI Inference)
* **What it does**: Coordinates local GGUF (`.gguf`) models in `~/.gha/models`, exposes OpenAI-compatible ChatCompletions endpoints, and profiles hardware (CPU cores, Metal/CUDA GPU offloading `-ngl 99`).
* **How to use it**:
  ```bash
  ghai ai models                                 # Catalog local GGUF & cloud AI models
  ghai ai engines                                # Inspect embedded GGUF & cloud inference engines
  ```

### 3. 🔌 Tier 3: GMCP (GMA Master MCP Infrastructure)
* **What it does**: Native JSON-RPC 2.0 MCP Host, Client & Server over stdio and background TCP socket (Port 9090) exposing 39+ coordinated AI tools.
* **How to use it**:
  ```bash
  ghai gmcp, mcp                                 # Start stdio MCP Server for IDE integration
  ghai ai mcp-hub                                # List active MCP tool servers & tools
  ```

---

## 🔌 IDE Integration & Common MCP Setup

`gha` connects directly to **Android Studio**, **VS Code**, **Cursor**, **IntelliJ**, **Claude Desktop**, and **GitHub Copilot Chat** via Model Context Protocol (MCP).

### 1. Android Studio / JetBrains IDEs Setup
Edit or create `.idea/mcp.json` in your workspace or global IDE settings (`~/.config/Google/AndroidStudio2026.1.4/mcp.json`):

```json
{
  "mcpServers": {
    "gha": {
      "command": "ghai",
      "args": ["mcp"],
      "enabled": true,
      "trust": true
    }
  },
  "mcpServersMetadata": {
    "gha": {
      "registryName": "gha",
      "title": "GHA Master Agent (GMA)",
      "description": "gha: Universal Multi-Agent AI Runtime & MCP Engine"
    }
  }
}
```

### 2. VS Code / Cursor Setup
In VS Code or Cursor MCP settings (`mcp.json` or `.vscode/mcp.json`):

```json
{
  "mcpServers": {
    "gha": {
      "command": "ghai",
      "args": ["mcp"]
    }
  }
}
```

### 3. Claude Desktop Setup
Add `gha` to your Claude Desktop configuration file (`~/Library/Application Support/Claude/claude_desktop_config.json` on macOS or `%APPDATA%\Claude\claude_desktop_config.json` on Windows):

```json
{
  "mcpServers": {
    "gha": {
      "command": "ghai",
      "args": ["mcp"]
    }
  }
}
```

---

## 💬 How to Use `gha` Tools from IDE Agents / Chat Window

Once registered, IDE chat agents (Gemini, Copilot, or Claude) discover `gha` tools automatically:

* **Explicit Tool Calls**:
  - `@gha status` — Get workspace health, sandbox status, and hardware profile.
  - `@gha list_models` — Inspect local GGUF and web AI models.
  - `@gha profile_hardware` — Check CPU cores and GPU acceleration (`-ngl 99`).
  - `@gha orchestrate "goal"` — Execute GMA multi-agent mission.

* **Natural Language Invocation**:
  Simply ask your AI assistant: *"Check hardware profile and local GGUF models using gha"*. The assistant invokes `ghai mcp` over stdio in **< 2 ms**.

---

## 🛡️ 2-Layer Sandboxing Model (`~/.gha` & `./.gha`)

* **Global Vault (`~/.gha/`)**: Houses the global `ghai` executable, central GGUF model vault (`~/.gha/models/`), and daemon locks. Shared across all projects to avoid duplicate downloads.
* **Local Workspace Sandbox (`./.gha/`)**: Stores workspace build caches and session logs. Completely git-ignored. Cleaned instantly via `ghai :uninstall`.

---

## Contributors

`gha` is co-created and maintained by **IntelliBitz**, **Muthu Ramadoss**, and **Gemini (Google AI)**. See [CONTRIBUTORS.md](CONTRIBUTORS.md) for details.

## License

This project is licensed under the [MIT License](LICENSE).
