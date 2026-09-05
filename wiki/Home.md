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

## 🌟 The GMA "Sole Interactor" Architecture

```text
 👤 GHA USER / IDE / CLIENT ──► 🤖 GMA Master Agent (Sole Interactor)
                                        │
             ┌──────────────────────────┼──────────────────────────┐
             ▼                          ▼                          ▼
   🤖 Tier 1: GAWD             🧠 Tier 2: GEMI           🔌 Tier 3: GMCP
   (A2A Worker Agent Fleet)    (Port 9091 REST API)      (Port 9090 / Stdio MCP)
```

---

## 📖 Integration Summary

### 1. Terminal CLI (`ghai`)
- `ghai "natural language goal"` — GMA Master Interactor goal execution.
- `ghai :status` — Workspace health, hardware profile & daemon status.
- `ghai ai server` — Start GEMI OpenAI REST server on Port 9091.
- `ghai mcp` — Start GMCP stdio MCP server for IDEs.

### 2. IDE Integration (`mcp.json`)
- Android Studio / JetBrains: `~/.config/Google/AndroidStudio*/mcp.json`.
- VS Code / Cursor: `.vscode/mcp.json`.
- Claude Desktop: `claude_desktop_config.json`.
- Trigger tools in IDE chat: `@gha status`, `@gha reason`, `@gha orchestrate`.

### 3. External LLM & Frameworks (Port 9091)
- Base URL: `http://127.0.0.1:9091/v1`
- Compatible with OpenAI Python SDK, LangChain, AutoGen, LlamaIndex, cURL.
