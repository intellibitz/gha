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

## 🌟 The 3-Tier Coordinated Intelligence Architecture

```text
 🤖 Tier 1: GAWD (GMA Master Agent & GMAS Supervisor)  : Multi-Agent AOA / A2A Orchestration
 🧠 Tier 2: GEMI (AI Inference REST Server)           : Port 9091 (http://127.0.0.1:9091/v1)
 🔌 Tier 3: GMCP (MCP Infrastructure Server)           : Port 9090 / Stdio
```

### 1. 🤖 Tier 1: GAWD (Multi-Agent Orchestration)
- **GMA Master Agent (Sole Interactor)** & **GMAS Supervisor** govern worker agent fleets (`GhaContextAgent`, `GhaReasoningAgent`, `GhaSystemExecutionAgent`, `GhaWebResearchAgent`, `GhaAutonomousAgent`).
- **Usage in Android Studio**: Type goals into Agents Tab; Gemini delegates to GMA via MCP tool (`orchestrate`). GMA synthesizes the Master Report in < 2ms.
- **Usage in Multi-Agent Frameworks**: AutoGen, CrewAI, LangGraph send A2A messages (`{"sender": "...", "recipient": "GMA-Master", "action": "SUPERVISE"}`).

### 2. 🧠 Tier 2: GEMI (AI Inference Engine — Port 9091)
- **OpenAI-Compatible REST API**: `http://127.0.0.1:9091/v1/chat/completions`.
- **Usage**: Android Studio, Gemini, Cursor, Python OpenAI SDK, cURL.

### 3. 🔌 Tier 3: GMCP (MCP Server — Port 9090 / Stdio)
- **JSON-RPC 2.0 MCP Server**: Exposes 39+ tools (`status`, `reason`, `orchestrate`, `list_models`, `profile_hardware`).
- **Usage**: Android Studio, VS Code, Cursor, Claude Desktop (`mcp.json`).
