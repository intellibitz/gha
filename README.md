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

## 🌟 Dual-Port Native Architecture & Component Overview

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

---

## 🤖 1. How the World Can Use GAWD (Tier 1: Multi-Agent Orchestration)

GAWD exposes **GMA (Master Interactor)** and **GMAS (Supervisor)** to govern agentic workflows across specialized sub-agents (`GhaContextAgent`, `GhaReasoningAgent`, `GhaSystemExecutionAgent`, `GhaWebResearchAgent`, `GhaAutonomousAgent`).

### A. Android Studio / Gemini / Agentic IDEs
When you type a high-level goal into Android Studio's Agents Tab:
1. Gemini delegates the top-level mission to **GMA (GHA Master Agent)** via MCP tool (`orchestrate`) or A2A message.
2. GMA hands off supervision to **GMAS Supervisor**, which dispatches tasks across the specialized GAWD worker fleet using **A2A (`A2AMessage`)** protocols.
3. GAWD workers execute steps natively and return a synthesized **GMA Master Interactor Report** to Android Studio in **< 2ms**.

### B. External Multi-Agent Frameworks (AutoGen, CrewAI, LangGraph)
External agent frameworks send A2A JSON messages to GMA:
```json
{
  "sender": "ExternalAgent",
  "recipient": "GMA-Master-Orchestrator",
  "action": "SUPERVISE",
  "payload": "Orchestrate workspace intelligence analysis"
}
```

### C. Terminal CLI Natural Missions
```bash
ghai "orchestrate an autonomous AI mission"    # GMA natural mission execution
ghai :status                                   # Inspect GAWD fleet & workspace status
```

---

## 🧠 2. How the World Can Use GEMI (Tier 2: AI Inference Engine — Port 9091)

Because GEMI implements the universal OpenAI REST specification (`/v1/chat/completions` and `/v1/models`), **any AI client, IDE, or framework** can use GEMI as its local LLM provider:

### A. Android Studio / Gemini / JetBrains AI / Cursor / VS Code
Set custom OpenAI-compatible endpoint in your IDE settings or AI plugin:
* **Base URL**: `http://127.0.0.1:9091/v1`
* **API Key**: `gha-native-key` (any string)
* **Model**: `deepseek-r1` or `llama-3.3-70b`

### B. cURL / Terminal HTTP Requests
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

### C. Python (OpenAI SDK / LangChain / AutoGen / LlamaIndex)
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

## 🔌 3. How the World Can Use GMCP (Tier 3: MCP Server — Port 9090 / Stdio)

GMCP exposes 39+ action tools (`status`, `reason`, `orchestrate`, `list_models`, `profile_hardware`) to IDEs over stdio or background TCP Port 9090.

### Android Studio / JetBrains IDEs Setup (`~/.config/Google/AndroidStudio*/mcp.json`)
```json
{
  "mcpServers": {
    "gha": {
      "command": "ghai",
      "args": ["mcp"],
      "enabled": true,
      "trust": true
    }
  }
}
```

### VS Code / Cursor / Claude Desktop Setup (`mcp.json`)
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

## 🛡️ 2-Layer Sandboxing Model (`~/.gha` & `./.gha`)

* **Global Vault (`~/.gha/`)**: Houses the global `ghai` executable, central GGUF model vault (`~/.gha/models/`), and daemon locks. Shared across all projects.
* **Local Workspace Sandbox (`./.gha/`)**: Stores workspace build caches and session logs. Completely git-ignored. Cleaned instantly via `ghai :uninstall`.

---

## Contributors

`gha` is co-created and maintained by **IntelliBitz**, **Muthu Ramadoss**, and **Gemini (Google AI)**. See [CONTRIBUTORS.md](CONTRIBUTORS.md) for details.

## License

This project is licensed under the [MIT License](LICENSE).
