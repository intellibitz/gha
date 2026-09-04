# gha: Universal & AI-Native Automation

**gha** (`cc.thevar.gha`) is a **100% self-contained, 100% Kotlin** automation engine. **gha runs on gha**—automating its own development, testing, and lifecycle across Git, GitHub, and Gradle.

GitHub users and developers across any IDE or terminal can expect **0% system modifications**. All dependencies and execution state are strictly sandboxed inside `.gha/`.

## 🛡️ The gha Promise

- **100% Sandboxed**: Zero changes to global `~/.gradle` or system settings.
- **Invisible Integration**: Automatically git-ignored; leaves no traces in your Git history.
- **100% Self-Healing**: Proactively restores its own launchers and sandbox integrity.
- **100% Portable**: Copy `.gha/` and `ghai` anywhere; it works instantly with zero setup.
- **AI-Native**: Ready for the **Model Context Protocol (MCP)** and autonomous agents.

---

## 🚀 Universal Mega-CLI (`ghai`)

The `ghai` launcher is the single entry point for all project automation. It supports both high-level AI workflows and direct tool mapping.

```bash
./ghai                # Autonomous AI Workflow (Sync, Commit, Push, PR)
./ghai :version        # Detailed engine version report
./ghai :status         # Sandbox health report
./ghai :install        # Initialize sandboxed environment
./ghai :clone <repo>   # Smart clone into workspace
```

### Power Mapping Logic
1. **Core GHA Intelligence**: Priority subcommands for sandbox management.
2. **Gradle Routing**: Any `:task` goes to the sandboxed Gradle engine.
3. **GitHub CLI Mirroring**: Understands `pr`, `issue`, `repo`, etc.
4. **Git Fallback**: Transparently passes unrecognized commands to Git.

---

## 🤖 AI Vision

`gha` provides the infrastructure for AI to build software:
- **MCP Bridge**: Exposes tasks as tools for LLMs.
- **Autonomous Agents**: Solve project goals with the GHA Ghost Agent.

---

## Installation

```bash
curl -sSL https://raw.githubusercontent.com/intellibitz/gha/main/init/install.sh | bash
```

See the [Installation](Installation) page for more details.
