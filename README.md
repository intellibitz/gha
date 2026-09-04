# gha: Git, GitHub & Gradle Automation (Universal & AI-Native)

**gha** (`cc.thevar.gha`) is a **100% self-contained, 100% Kotlin** automation engine and **Universal AI Orchestrator ("Agent of Agents")**. **gha runs on gha**—automating its own development, testing, dependencies, commits, pulls, PRs, releases, security, issues, wikis, insights, and AI workflows.

GitHub users and developers across any IDE or terminal can clone or install this project and expect **0% system modifications**. All dependencies, Kotlin libraries, Gradle caches, JDK toolchains, AI models, init scripts, and execution state are strictly sandboxed inside a single `.gha/` directory.

---

## 🌟 Mission: 0 Effort, 100% Gain

`gha` creators—**Intellibitz**, **Muthu Ramadoss**, **Gemini (Google AI)**, and other AI agents alongside GitHub community contributors—build platform-independent automation workflows and multi-agent AI tools written purely in **100% Kotlin** to power software development everywhere.

---

## 🌌 AI Vision: The Agent of Agents & Universal AI Orchestrator

`gha` brings the power of AI to **every user, anywhere, even on limited home hardware**:

- **💬 Natural Language Interaction**: Interact with `gha` using plain English instructions directly from the command line:
  ```bash
  ./ghai "create a kotlin app"
  ./ghai "create an android app"
  ./ghai "fix build and run tests"
  ./ghai "save my work and create pull request"
  ```
- **🏛️ Master Agent Manager**: `gha` communicates **ONLY with Agents**. Specialized Agents (Scaffolding, Build & Test, VCS & PR, Security, Wiki) handle goals autonomously.
- **🔌 GHA MCP Host**: `gha` serves as the central **Model Context Protocol (MCP) Host**. Agents act as **MCP Clients** that discover and execute tools hosted on `gha`.
- **🧠 Thinking Agents & AI Model Inference**: Agents use AI models/engines (Ollama REST API, Hugging Face CLI `hf`, llama.cpp, local GHA AI reasoner) to **think, reason, and infer solutions** before invoking MCP tools.
- **💻 Limited Hardware Profiling**: Analyzes local RAM, CPU, and GPU hardware constraints to automatically recommend quantized model parameter tiers (e.g. 1B–3B GGUF for <8GB RAM, 7B–8B for 16GB RAM) that run smoothly on home computers without crashing.
- **📦 Single `.gha/` Sandbox**: All init scripts (`.gha/init.gradle.kts`), metadata (`.gha/gha.json`), batch launchers (`.gha/ghai.bat`), model caches (`.gha/models/`), and MCP configs (`.gha/mcp/`) are 100% encapsulated inside `.gha/`.

---

## 🛡️ Core Principles

- **100% Sandboxed & 0 Side Effects**: `gha` **never modifies existing project files** (`settings.gradle.kts`, `build.gradle.kts`). All execution state lives inside `.gha/`.
- **Invisible Integration**: The entire `.gha/` sandbox is automatically git-ignored. Installing, updating, and using `gha` leaves no traces in your Git history.
- **100% Self-Healing**: `gha` should never be in a broken state. It proactively restores missing launcher scripts, Gradle wrappers, and sandbox configurations autonomously.
- **Delivery Agnostic & Nomadic**: Whether delivered via `git clone`, a `zip`, or `curl`, `gha` performs. It auto-initializes non-git folders and heals missing project anchors from the source of truth.
- **100% Portable & "Copy-Paste" Ready**: The entire engine, including JDK toolchains and caches, lives in `.gha/`. Copy `.gha/` and `ghai` anywhere, and it works instantly with **zero installation**.
- **100% IDE & Tooling Independent**: Works in 100% CLI mode across any IDE (Android Studio, VS Code, IntelliJ, Terminal) or CI/CD runner.
- **Autonomous Versioning**: Enforces a rule to autonomously bump patch versions for every push, ensuring 100% visibility.

---

## 🚀 The Universal Mega-CLI (`ghai`)

The `ghai` executable is a universal command processor that acts as a unified interface for your entire project:

```bash
./ghai                          # Autonomous AI Workflow (Sync, Commit, Push, PR)
./ghai "create a kotlin app"    # Natural English instruction execution
./ghai ai orchestrate           # Run Master AI Orchestrator status report
./ghai ai models                # Inspect local AI model cache & RAM compatibility
./ghai ai engines               # Detect active local AI engines (Ollama, HF CLI, llama.cpp)
./ghai ai mcp-hub               # List registered MCP Tool Hub servers
./ghai :version                 # Detailed sandbox and engine version report
./ghai :status                  # Sandbox health & portability report
./ghai :install                 # Initialize/Restore sandboxed environment
./ghai :reinstall               # Clean wipe and fresh sandbox restoration
./ghai :clone <repo>            # Smart clone into current folder or subfolder
```

---

## ⚡ 1-Second Installation

Install `gha` into **any repository** instantly:

```bash
# Linux, macOS, & WSL:
curl -sSL https://raw.githubusercontent.com/intellibitz/gha/main/init/install.sh | bash

# Windows PowerShell:
iwr -useb https://raw.githubusercontent.com/intellibitz/gha/main/init/install.ps1 | iex
```

---

## Contributors

`gha` is co-created and maintained by **IntelliBitz**, **Muthu Ramadoss**, and **Gemini (Google AI)**. See [CONTRIBUTORS.md](CONTRIBUTORS.md) for the full list of project creators and contributors.

## License

This project is licensed under the [MIT License](LICENSE).
