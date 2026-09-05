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

## 🌟 The GMA "Sole Interactor" Governance Architecture

`gha` is architected around a single, uncompromising principle: **GMA (GHA Master Agent)** is the **Sole Interactor** for the user. You interact strictly with GMA, and GMA governs the 3 tiers behind the scenes:

```text
                                 👤 GHA USER / IDE / CLIENT
                                         │
                                         │ Single Point of Interaction
                                         ▼
                     ┌───────────────────────────────────────┐
                     │ 🤖 GMA (GHA Master Agent)             │
                     │    • The Master                       │
                     │    • The Orchestrator                 │
                     │    • The One & Sole Interactor        │
                     └───────────────────┬───────────────────┘
                                         │
                 ┌───────────────────────┼───────────────────────┐
                 ▼                       ▼                       ▼
   🤖 Tier 1: GAWD             🧠 Tier 2: GEMI           🔌 Tier 3: GMCP
   (GMAS Supervisor &          (GGUF Local Inference &   (MCP Server, Stdio &
   A2A Worker Agent Fleet)     Metal/CUDA Port 9091)     Port 9090 Tools)
                 │                       │                       │
                 └───────────────────────┼───────────────────────┘
                                         │
                                         ▼
                     🌌 GMA Synthesizes Master Interactor Report
                                         │
                                         ▼
                                 👤 Presented To User
```

---

## 📖 How the World Can Use `gha` (Complete Integration Guide)

---

### A. Terminal CLI Usage (`ghai`)

The ultra-fast native `ghai` executable responds in **sub-millisecond (< 2ms)** latency:

```bash
# 1. Natural Language Mission Execution
ghai "orchestrate an autonomous AI mission"    # GMA Master Interactor goal execution
ghai "print workspace health and disk usage"   # GMA system execution task

# 2. Workspace Status & Sandbox Management
ghai :status                                   # Inspect workspace health, GAWD fleet & daemon
ghai :version                                  # Print engine architecture & version report
ghai :install                                  # Initialize sandboxed .gha environment offline
ghai :uninstall                                # Clean up local .gha workspace sandbox

# 3. AI Engines, Models & Servers Management
ghai ai models                                 # Inspect GGUF models (~/.gha/models) & web models
ghai ai engines                                # Inspect embedded GGUF & cloud inference engines
ghai ai mcp-hub                                # List active MCP tool servers & exposed tools
ghai ai server                                 # Start GEMI OpenAI-compatible REST server (Port 9091)
ghai gmcp, mcp                                 # Start native GMA Master MCP Server over stdio
ghai :daemon                                   # Inspect or start background GMA Master Daemon
```

---

### B. IDE Integration (Android Studio, JetBrains, VS Code, Cursor, Claude)

`gha` plugs directly into any IDE via standard **Model Context Protocol (MCP)**.

#### 1. Android Studio / JetBrains IDEs Setup
Edit or create `.idea/mcp.json` or your global IDE settings (`~/.config/Google/AndroidStudio*/mcp.json`):

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

#### 2. VS Code / Cursor Setup (`mcp.json` or `.vscode/mcp.json`)
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

#### 3. Claude Desktop Setup (`claude_desktop_config.json`)
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

#### 4. How to Invoke `gha` Tools in IDE Agents / Chat Window:
* **Explicit Tool Triggers**:
  - `@gha status` — Get workspace health, sandbox status, and hardware profile.
  - `@gha reason "prompt"` — Execute deep reasoning via GEMI engine.
  - `@gha list_models` — Inspect local GGUF and web AI models.
  - `@gha profile_hardware` — Check CPU cores and GPU offload (`-ngl 99`).
  - `@gha orchestrate "goal"` — Execute GMA multi-agent mission.
* **Natural Language Invocation**:
  Ask your assistant: *"Check hardware profile and local GGUF models using gha"*. The assistant automatically invokes `ghai mcp` over stdio in **< 2 ms**.

---

### C. External LLM & Framework Integration (GEMI REST Server — Port 9091)

GEMI implements the universal OpenAI REST specification (`/v1/chat/completions` and `/v1/models`). Any client or framework can use `gha` as a local, hardware-accelerated LLM provider.

#### 1. Connect Android Studio / Gemini / Cursor to GEMI (Port 9091)
In IDE custom OpenAI provider settings:
* **Base URL**: `http://127.0.0.1:9091/v1`
* **API Key**: `gha-native-key` (any string)
* **Model**: `deepseek-r1` or `llama-3.3-70b`

#### 2. cURL / Terminal HTTP Requests
```bash
# List Available GGUF & Cloud Models
curl -s http://127.0.0.1:9091/v1/models

# Execute ChatCompletions Prompt
curl -s http://127.0.0.1:9091/v1/chat/completions \
  -X POST \
  -H "Content-Type: application/json" \
  -d '{
    "model": "deepseek-r1",
    "messages": [{"role": "user", "content": "Explain GHA GEMI architecture."}]
  }'
```

#### 3. Python Integration (OpenAI SDK / LangChain / AutoGen / LlamaIndex)
```python
from openai import OpenAI

client = OpenAI(
    base_url="http://127.0.0.1:9091/v1",
    api_key="gha-native-key"
)

response = client.chat.completions.create(
    model="deepseek-r1",
    messages=[{"role": "user", "content": "Optimize this Rust function."}]
)

print(response.choices[0].message.content)
```

---

### D. External Multi-Agent Protocol Integration (GAWD AOA / A2A)

Multi-agent frameworks (AutoGen, CrewAI, LangGraph) can send A2A JSON message envelopes directly to GMA:

```json
{
  "sender": "ExternalAgent",
  "recipient": "GMA-Master-Orchestrator",
  "action": "SUPERVISE",
  "payload": "Orchestrate workspace intelligence analysis"
}
```

---

## 🛡️ 2-Layer Sandboxing Model (`~/.gha` & `./.gha`)

* **Global Vault (`~/.gha/`)**: Houses the global `ghai` executable, central GGUF model vault (`~/.gha/models/`), and daemon locks. Shared across all projects.
* **Local Workspace Sandbox (`./.gha/`)**: Stores workspace build caches and session logs. Completely git-ignored. Cleaned instantly via `ghai :uninstall`.

---

## Contributors

`gha` is co-created and maintained by **IntelliBitz**, **Muthu Ramadoss**, and **Gemini (Google AI)**. See [CONTRIBUTORS.md](CONTRIBUTORS.md) for details.

## License

This project is licensed under the [MIT License](LICENSE).
