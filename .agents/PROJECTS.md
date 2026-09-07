# Project Instructions & Architecture

## System Information

* **Project Name**: `gha`
* **Current Engine Version**: `v0.1.143`
* **Core Paradigm**: EAI (Exponential Intelligence for Any AI) — Standalone Native Rust Multi-Agent Engine

## Core Terminology & Acronyms

* **GHA**: Global Hyper-intelligence Agents — 100% standalone native Rust AI engine & platform.
* **EAI**: Exponential Intelligence for Any AI — Architectural framework for exponential agent scaling.
* **GMA**: GHA Master Agent — Tier 1 universal intent interactor, A2A root supervisor, and mission router.
* **GAWD**: Global Agentic Workspace Domain — Multi-agent swarm ecosystem and agent governance domain.
* **GMAS**: GAWD Multi-agent Supervisor — Cluster supervisor managing inter-agent communications (A2A & AOA protocols).
* **GEMI**: Global Engine for Multi-model Inference — Inference bridge routing across cloud APIs and local runtimes.
* **GMCP**: GHA Model Context Protocol — Native tool execution engine, interop bus, and Model Context Protocol server/client.
* **PKB**: Protocol Knowledge Base — Reflex data synthesizer for Tier 0 training distillation.

## EAI Architecture & Decoupled Modular Dynamics

* **2⁰ Core**: 2⁰ (microsecond reflex engine) is the immutable core of GHA.
* **Modular Decoupling**: Every step taken becomes a standalone, decoupled module. Modules communicate strictly through established protocols (A2A, AOA, GMCP).
* **Scaling Cycle**: $2^0 \text{ (Reflex Core)} \longrightarrow 2^{63} \text{ (63-Step Swarm Pipeline)} \longrightarrow \text{PKB Distillation} \longrightarrow \text{Compounded } 2^0 \text{ Core}$.
* **Decoupled Swarm**: Expanding from $2^0$ to $2^{63}$ and collapsing back to a compounded $2^0$ primitive is executed entirely through decoupled modular components.

## Component Integration & Non-Bloat Verification

* **Core Simplicity**: `gha` core acts strictly as a lightweight protocol router and orchestrator. It does not perform heavy monolithic work itself.
* **Component Synergy**: All execution power is derived dynamically from:
  1. **Installed System Capabilities**: `cargo`, `docker`, `terraform`, `kubectl`, `df`.
  2. **External MCP Tool Servers**: Proxying third-party MCP servers via GMCP JSON-RPC 2.0 (`GmcpClient`).
  3. **Inference Runtimes**: Delegation to Ollama, Candle, and Cloud APIs (Gemini, OpenAI, Anthropic, Groq, DeepSeek).
* **Zero Bloat Guarantee**: 0 heavy GUI dependencies, 0 background worker bloat, < 2ms binary startup.

## Intelligence Tiers

* **Tier 0: GHA-Alpha (Native Reflex Engine)**: Microsecond (<1ms) protocol routing and deterministic reflexes ($2^0$ Primitive).
* **Tier 1: GAWD / GMA (Universal Swarm Supervisor)**: A2A swarm dispatch, governance auditing, and mission supervision ($2^{63}$ Swarm Pipeline).
* **Tier 2: GEMI (Deep Reasoning & Multi-Model Inference)**: Multi-cloud provider scouting and local model fallback inference.

## GMCP Action Substrate & Interop Bus

* **Role**: Universal action substrate, hardware execution layer, and two-way protocol interop bus connecting Tiers 0, 1, and 2 to workspace operations and external AI systems.
* **Inbound (`GmcpServer`)**: Exposes GHA capabilities and swarm orchestration to external clients (Claude Desktop, Cursor, IDEs) over JSON-RPC 2.0 (stdio/TCP 9090).
* **Outbound (`GmcpClient`)**: Proxies external industry MCP servers into GHA agent swarms.
* **Capabilities (`ToolRegistry`)**: Executes system commands, file I/O, hardware profiling, model listing, and swarm flux actions.

## Core Architecture Components

* **`GmaDaemon`**: Persistent background daemon managing local sandbox and swarm tasks.
* **`SandboxManager`**: Isolated runtime execution container in `.gha`.
* **`ToolRegistry`**: Native GMCP capability executor (file I/O, VCS, system, web, MCP tools).
* **`HardwareProfiler`**: Hardware profiler optimizing CPU thread saturation, RAM, and GPU usage.
