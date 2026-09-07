# Agent Instructions

1. **No Lies**: Never lie. Always report accurate statuses, execution outcomes, and limitations.
2. **No Hallucinations**: Ground all code, API references, file contents, and facts in verified reality or direct tool results.
3. **Brutally Honest & Critical**: Always maintain a brutally honest, highly critical evaluation mode for code, architecture, and logic.
4. **Reality Check Always On**: Continually validate assumptions against codebase constraints, build rules, and runtime behavior.
5. **Ultra Professional Standard**: Keep all code, documentation, and comments ultra-professional, clean, and production-ready.
6. **No Fluff**: Be direct, concise, and technical. Eliminate filler phrases, fluff, and conversational pleasantries.
7. **No Secret Leaks**: Zero tolerance for leaking tokens, credentials, API keys, or sensitive configuration data.
8. **Creator Interactive Mode**: Maintain direct creator-agent collaborative mode—aligned, responsive, and precise.
9. **Workspace Boundaries**: Project root (`.`) is the main workspace. `./test/world` is designated as the testspace (ignored in `.gitignore`).
10. **Full Autonomy & Permissions**: Agents have full permission for all file operations and command executions. Automatically apply all code changes without asking for permission, prompting confirmation, or requiring "Keep changes" clicks.

## GHA Rules

1. **Version Increment on Push**: Every push to GitHub must automatically increment the project version.
2. **Lightning Fast Compilation**: GHA must compile lightning fast through optimized build configurations, aggressive caching, and minimal overhead.
3. **Maximum Resource Utilization**: GHA is configured to utilize maximum available hardware resources (all available CPU threads, RAM, and parallel compilation jobs).
4. **Terminology & Component Sync on Push**: Every GitHub push must update the `Projects & Terminology` section with the latest terminology, architecture components, and current GHA version.

## Projects & Terminology

* **Current GHA Version**: `v0.1.115`

### Core Acronyms
* **GHA**: Global Hyper-intelligence Agents — 100% standalone native Rust AI engine & platform.
* **EAI**: Exponential Intelligence for Any AI — Architectural framework for exponential agent scaling.
* **GMA**: GHA Master Agent — Tier 1 universal intent interactor, A2A root supervisor, and mission router.
* **GAWD**: Global Agentic Workspace Domain — Multi-agent swarm ecosystem and agent governance domain.
* **GMAS**: GAWD Multi-agent Supervisor — Cluster supervisor managing inter-agent communications (A2A & AOA protocols).
* **GEMI**: Global Engine for Multi-model Inference — Inference bridge routing across cloud APIs and local runtimes.
* **GMCP**: GHA Model Context Protocol — Native tool execution engine and Model Context Protocol server.
* **PKB**: Protocol Knowledge Base — Reflex data synthesizer for Tier 0 training distillation.

### Intelligence Tiers
* **Tier 0: GHA-Alpha (Native Reflex Engine)**: Microsecond (<1ms) protocol routing and deterministic reflexes.
* **Tier 1: GAWD / GMA (Universal Swarm Supervisor)**: A2A swarm dispatch, governance auditing, and mission supervision.
* **Tier 2: GEMI (Deep Reasoning & Multi-Model Inference)**: Multi-cloud provider scouting and local model fallback inference.

### Core Architecture Components
* **`GmaDaemon`**: Persistent background daemon managing local sandbox and swarm tasks.
* **`SandboxManager`**: Isolated runtime execution container in `.gha`.
* **`ToolRegistry`**: Native GMCP capability executor (file I/O, VCS, system, web, MCP tools).
* **`HardwareProfiler`**: Hardware profiler optimizing CPU thread saturation, RAM, and GPU usage.
