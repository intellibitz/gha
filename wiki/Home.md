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

```text
 🔌 GMCP (Tier 3 MCP Infrastructure Server) : Port 9090 (or stdio)
 🧠 GEMI (Tier 2 AI Inference REST Server)  : Port 9091 (http://127.0.0.1:9091/v1)
```

1. **🤖 Tier 1: GAWD (GMA Master Agent & GMAS Supervisor)**
   - **GMA (GMA Master Agent)**: The Master, The One, and Sole Interactor for the user.
   - **GMAS (GMA Supervisor)**: AOA (Agent of Agents) & A2A (Agent to Agent) protocol governor managing worker agent fleets.
   - **Usage**: `ghai "my AI goal"` or `ghai :status`.

2. **🧠 Tier 2: GEMI (GHA Engines & Models AI Inference) — Unique Port 9091**
   - **Local GGUF & Cloud AI Inference Coordinator**: Loads `.gguf` models directly from `~/.gha/models`. Serves OpenAI-compatible ChatCompletions API endpoints.
   - **Autonomous Hardware Profiler**: Profiles CPU cores and Metal/CUDA GPU acceleration for GPU offloading (`-ngl 99`).
   - **Usage**: `ghai ai server` (starts REST server on `http://127.0.0.1:9091/v1`).

3. **🔌 Tier 3: GMCP (GMA Master MCP Infrastructure) — Port 9090 / Stdio**
   - **Full MCP Host, Client & Server**: Standalone JSON-RPC 2.0 MCP implementation over stdio and background TCP sockets (Port 9090).
   - **Universal Tool Registry**: Exposes 39+ AI tools to external IDEs (VS Code, Android Studio, IntelliJ, Cursor), LLMs, and agents.
   - **Usage**: `ghai mcp` or `ghai ai mcp-hub`.

---

## 🌍 How the World Can Use GEMI (Port 9091)

### 1. Android Studio / Gemini / Cursor / JetBrains AI
Set Base URL to `http://127.0.0.1:9091/v1` and API key to `gha-native-key`.

### 2. cURL
```bash
curl -s http://127.0.0.1:9091/v1/chat/completions \
  -X POST -H "Content-Type: application/json" \
  -d '{"model":"deepseek-r1","messages":[{"role":"user","content":"Hello GEMI"}]}'
```

### 3. Python OpenAI SDK
```python
from openai import OpenAI
client = OpenAI(base_url="http://127.0.0.1:9091/v1", api_key="gha-native-key")
response = client.chat.completions.create(model="deepseek-r1", messages=[{"role": "user", "content": "Hello GEMI"}])
print(response.choices[0].message.content)
```

---

## 🔌 IDE Integration & Common MCP Setup (`ghai mcp`)

### Android Studio / JetBrains IDEs Setup (`mcp.json`)
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

### VS Code / Cursor Setup (`mcp.json`)
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
