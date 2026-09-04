# gha: Universal Automation & AI Orchestrator ("Agent of Agents")

**gha** (`cc.thevar.gha`) is a **100% self-contained, 100% Kotlin** automation engine and **Universal AI Orchestrator**. **gha runs on gha**—automating its own development, testing, and lifecycle across Git, GitHub, Gradle, and multi-agent AI systems.

Developers across any IDE or terminal can expect **0% system modifications**. All dependencies, models, and execution state are strictly sandboxed inside `.gha/`.

## 🛡️ The gha Promise

- **100% Sandboxed**: Zero changes to global `~/.gradle` or system settings.
- **Invisible Integration**: Automatically git-ignored; leaves no traces in your Git history.
- **100% Self-Healing**: Proactively restores its own launchers, wrappers, and sandbox integrity.
- **100% Portable**: Copy `.gha/` and `ghai` anywhere; it works instantly with zero setup.
- **Agent of Agents Architecture**: GHA acts as the MCP Host and Agent Manager. GHA communicates ONLY with Agents; Agents act as MCP Clients and call tools on the MCP Host.
- **Limited Hardware AI**: Hardware profiler customizes AI model selection for home hardware constraints.

---

## 🚀 Universal Mega-CLI (`ghai`)

The `ghai` launcher is the single entry point for all project automation and natural language instructions.

```bash
./ghai                          # Autonomous AI Workflow (Sync, Commit, Push, PR)
./ghai "create a kotlin app"    # Natural English Language Instruction
./ghai ai orchestrate           # Master AI Orchestrator report
./ghai ai models                # Local AI model cache & RAM profile
./ghai ai engines               # Local AI engines (Ollama, HF CLI, llama.cpp, PyTorch/UV)
./ghai ai mcp-hub               # Community MCP Tool Hub
./ghai :version                 # Engine version report
./ghai :status                  # Sandbox health report
./ghai :install                 # Initialize sandboxed environment
./ghai :clone <repo>            # Smart clone into workspace
```

---

## Installation

```bash
curl -sSL https://raw.githubusercontent.com/intellibitz/gha/main/init/install.sh | bash
```

See [Installation](Installation) and [Tasks Reference](Tasks-Reference) for more details.
