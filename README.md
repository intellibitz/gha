# gha: Git, GitHub & Gradle Automation (Universal & AI-Native)

**gha** (`cc.thevar.gha`) is a **100% self-contained, 100% Kotlin** automation engine. **gha runs on gha**—automating its own development, testing, dependencies, commits, pulls, PRs, releases, security, issues, wikis, and insights.

GitHub users and developers across any IDE or terminal can clone this project and expect **0% system modifications**. All dependencies, Kotlin libraries, Gradle caches, JDK toolchains, and execution state are strictly sandboxed inside the local repository folder (`.gha/`).

## 🌟 Mission: 0 Effort, 100% Gain

`gha` creators—**Intellibitz**, **Gemini**, and other AI agents alongside GitHub community contributors—build platform-independent Gradle tasks and plugins written purely in **100% Kotlin** to power end-to-end Git and GitHub automation workflows.

## 🛡️ Core Principles

- **100% Sandboxed & 0 Side Effects**: `gha` **never modifies existing project files** (`settings.gradle.kts`, `build.gradle.kts`). All execution state is strictly sandboxed inside `.gha/`.
- **Invisible Integration**: The entire sandbox environment is automatically git-ignored. Installing, updating, and using `gha` leaves no traces in your Git history.
- **100% Self-Healing**: `gha` should never be in a broken state. It proactively restores missing launcher scripts, Gradle wrappers, and sandbox configurations autonomously.
- **Delivery Agnostic & Nomadic**: Whether delivered via `git clone`, a `zip`, or `curl`, `gha` performs. It auto-initializes non-git folders and heals missing project anchors from the source of truth.
- **100% Portable & "Copy-Paste" Ready**: The entire engine, including JDK toolchains and caches, lives in `.gha/`. Copy it anywhere, and it works instantly with **zero installation**.
- **Provider-Agnostic Vision**: Architected with a pluggable provider system (`GhaVcsProvider`, `GhaBuildProvider`, `GhaRemoteProvider`) to support any toolset (GitLab, Bitbucket, Maven, NPM) in the future.
- **AI-Native (MCP & Agents)**: Built for the AI ecosystem. Supports **Model Context Protocol (MCP)** for discovery by LLMs and hosts autonomous **GHA Agents** to solve project goals.
- **Strict Official Stability**: Exclusively uses official stable releases from verified trusted vendors (`JetBrains`, `Gradle Inc.`, `Eclipse Adoptium`, `Oracle`, `GitHub Inc.`).
- **Autonomous Versioning**: Enforces a rule to autonomously bump patch versions for every push, ensuring 100% visibility.

---

## 🚀 The Universal Mega-CLI (`ghai`)

The `ghai` executable is a universal command processor that acts as a unified interface for your entire project:

```bash
./ghai                # Autonomous AI Workflow (Sync, Commit, Push, PR)
./ghai :version        # Detailed sandbox and engine version report
./ghai :status         # Sandbox health & portability report
./ghai :install        # Initialize/Restore sandboxed environment
./ghai :reinstall      # Clean wipe and fresh sandbox restoration
./ghai :clone <repo>   # Smart clone into current folder or subfolder
```

### ⚡ Universal Power Mapping
`ghai` intelligently routes your commands through a tiered logic:
1. **GHA Subcommands**: Priority handling for `install`, `status`, `update`, etc.
2. **Gradle Tasks**: Any argument starting with `:` (e.g., `./ghai :assemble`) goes to the sandboxed Gradle engine.
3. **GitHub CLI (gh)**: Maps subcommands like `pr`, `issue`, `repo` directly to the GitHub CLI with autonomous token resolution.
4. **Git Fallback**: If unrecognized, the command is transparently passed to the Git engine (e.g., `./ghai log`, `./ghai diff`).

---

## 🤖 AI Vision: MCP & Autonomous Agents

`gha` is the foundational layer for AI models to interact with software projects.

- **MCP Discovery**: Run `./ghai ai mcp` to expose GHA tasks as high-level tools for AI models via the Model Context Protocol.
- **Autonomous Agents**: Run `./ghai ai agent "fix build"` to trigger a sandboxed agent that analyzes your project, fixes issues, and syncs to GitHub.

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

`gha` is co-created and maintained by **IntelliBitz**, **Muthu Ramadoss**, and **Gemini (Google AI)**. See [CONTRIBUTORS.md](file:///home/ramadoss/Projects/AI/gha/CONTRIBUTORS.md) for the full list of project creators and contributors.

## License

This project is licensed under the [MIT License](LICENSE).
