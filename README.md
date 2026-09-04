# gha: Git, GitHub & Gradle Automation (Universal & AI-Native)

**gha** (`cc.thevar.gha`) is a **100% self-contained, 100% Kotlin** automation engine and **Universal AI Orchestrator ("Agent of Agents")**. **gha runs on gha**—automating its own development, testing, dependencies, commits, pulls, PRs, releases, security, issues, wikis, insights, and AI workflows.

GitHub users and developers across any IDE or terminal can clone or install this project and expect **0% system modifications**. All dependencies, Kotlin libraries, Gradle caches, JDK toolchains, AI models, init scripts, and execution state are strictly sandboxed inside a single `.gha/` directory.

---

## 🌟 Mission: 0 Effort, 100% Gain (`gha: ai, anywhere`)

`gha` creators—**Intellibitz**, **Muthu Ramadoss**, **Gemini (Google AI)**, and other AI agents alongside GitHub community contributors—build platform-independent automation workflows and multi-agent AI tools written purely in **100% Kotlin** to power software development everywhere.

---

## 🌌 5-Tier Evolutionary AI Architecture

`gha` brings the power of AI to **every user, anywhere, even on limited home hardware**:

1. **🏛️ Pluggable AOA Frameworks (`GhaAoaManager`)**: Select your preferred Agent of Agents framework via `-Paoa=autogen|crewai|langgraph|swarm|builtin`. Supports Microsoft AutoGen, CrewAI, LangGraph, OpenAI Swarm, and native GHA Kotlin AOA.
2. **🌐 Specialized & Web Agents (`GhaWebAgentManager` & `GhaAgentManager`)**: Integrates Web Research Agent, Hugging Face Web Agent, GitHub Remote Web API Agent, and Remote MCP Web Agents. Dynamically creates custom multi-agent fleets to complete complex user work.
3. **⚡ Local & Cloud AI Inference Engines (`GhaEngineManager`)**: Detects and interfaces with local engines (Ollama, llama.cpp, Python UV) and web-based AI engines (OpenRouter, Groq LPU, Hugging Face Serverless, Google Gemini, OpenAI, Anthropic Claude).
4. **🧠 AI Model Discovery & Resolution (`GhaModelManager`)**: Searches, resolves, and auto-downloads open-weights and cloud models (DeepSeek R1, Llama 3.3, Qwen 2.5 Coder, Claude 3.5 Sonnet, GPT-4o) across Hugging Face Hub, OpenRouter, Ollama Library, and Groq.
5. **🔌 GHA MCP Host & Custom System Tools (`GhaMcpHost` & `GhaSystemMcpServer`)**: `gha` hosts 10 MCP servers and exposes 33+ system, build, git, GitHub, and browser tools (`ADB`, `Docker`, `Python UV`, `Node/NPM`, `System CLIs`, `GitHub API`, `Hugging Face`, `Brave Search`, `Puppeteer`).

---

### 🔄 Top-to-Bottom Cascade Delegation Flow

Every user instruction (e.g. `./ghai "build an os"`) delegates top-down through all 5 layers:

```
┌─────────────────────────────────────────────────────────────────────────────┐
│ 1. AOA Framework Delegation (GhaAoaManager)                                 │
│ - Delegates top-level mission to AutoGen | CrewAI | LangGraph | Swarm | Native│
└──────────────────────────────────────┬──────────────────────────────────────┘
                                       │
                                       ▼
┌─────────────────────────────────────────────────────────────────────────────┐
│ 2. Sub-Agent & Web Agent Delegation (GhaAgentManager & GhaWebAgent)         │
│ - Delegates tasks down to specialized agents (GhaScaffolder, GhaBuilder,    │
│   GhaVcsAgent, GhaWebResearchAgent, GhaHfWebAgent, GhaGitHubWebAgent, etc.) │
└──────────────────────────────────────┬──────────────────────────────────────┘
                                       │
                                       ▼
┌─────────────────────────────────────────────────────────────────────────────┐
│ 3. AI Inference Engine Delegation (GhaEngineManager)                        │
│ - Delegates reasoning queries to active cloud/web engines (OpenRouter, Groq,│
│   HF Serverless, Gemini, Claude, OpenAI) or local engines (Ollama, UV).     │
└──────────────────────────────────────┬──────────────────────────────────────┘
                                       │
                                       ▼
┌─────────────────────────────────────────────────────────────────────────────┐
│ 4. Web Model Discovery & Resolution (GhaModelManager)                       │
│ - Resolves/downloads model specifications (DeepSeek R1, Llama 3.3, Qwen 2.5, │
│   Claude 3.5 Sonnet, GPT-4o) from Hugging Face Hub, OpenRouter, and Groq.   │
└──────────────────────────────────────┬──────────────────────────────────────┘
                                       │
                                       ▼
┌─────────────────────────────────────────────────────────────────────────────┐
│ 5. MCP Server & Tool Execution (GhaMcpHost & GhaSystemMcpServer)             │
│ - Delegates tool execution to 10 connected MCP servers exposing 33+ tools   │
│   (System CLIs, ADB, Docker, Python UV, Node, Git, GitHub, Scaffolding, Build).│
└─────────────────────────────────────────────────────────────────────────────┘
```

---

## 📦 Universal Auto-Installer & Bootstrapper (`GhaBootstrapManager`)

* **Zero-Setup On-Demand Bootstrap**: Automatically detects and installs missing AOA Python packages, engines, GGUF models, and MCP servers on-demand when a mission is received.
* **Dynamic Multi-Agent Creation**: Dynamically instantiates and parallelizes custom AOAs and specialized Sub-Agents to solve multi-step user instructions.
* **Hardware Profiling**: Analyzes RAM, CPU cores, and GPU acceleration to recommend optimal quantized model tiers.

---

## 🛡️ Core Principles

- **100% Sandboxed & 0 Side Effects**: `gha` **never modifies existing project files** (`settings.gradle.kts`, `build.gradle.kts`). All execution state lives inside `.gha/`.
- **Invisible Integration**: The entire `.gha/` sandbox is automatically git-ignored.
- **Dynamic Repository URLs**: Supports custom forks, mirrors, and upstream repositories via `GHA_REPO` or `git config gha.repo`.
- **Automated Push Versioning**: Enforces a pre-push Git hook (`.git/hooks/pre-push`) to autonomously bump patch versions for every push.
- **100% Self-Healing**: Proactively restores missing launcher scripts, Gradle wrappers, and sandbox configurations autonomously.
- **100% Portable & "Copy-Paste" Ready**: Copy `.gha/` and `ghai` anywhere, and it works instantly with **zero installation**.

---

## 🚀 The Universal Mega-CLI (`ghai`)

The `ghai` executable is a universal command processor that acts as a unified interface for your entire project:

```bash
./ghai                                           # Autonomous AI Workflow (Sync, Commit, Push, PR)
./ghai "build an os"                            # Natural English instruction execution
./ghai "create a kotlin app"                     # Natural English application scaffolding
./ghai "build and run tests" -Paoa=autogen      # Execute mission using Microsoft AutoGen AOA
./ghai "perform security audit" -Paoa=crewai     # Execute mission using CrewAI Manager Agent
./ghai ai orchestrate                            # Run Master AI Orchestrator status report
./ghai ai models                                 # Inspect local & available web AI models
./ghai ai engines                                # Detect local & cloud AI inference engines
./ghai ai mcp-hub                                # List connected MCP Tool Hub servers
./ghai :version                                  # Detailed sandbox and engine version report
./ghai :status                                   # Sandbox health & portability report
./ghai :install                                  # Initialize/Restore sandboxed environment
./ghai :clone <repo>                             # Smart clone into current folder or subfolder
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
