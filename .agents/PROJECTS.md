# Project Instructions & Architecture

## System Information

* **Project Name**: `gha`
* **Current Engine Version**: `v0.1.125`
* **Architecture**: EAI (Exponential Intelligence for Any AI) — Standalone Native Rust Multi-Agent Engine

## Core Terminology & Acronyms

* **GHA**: Global Hyper-intelligence Agents — 100% standalone native Rust AI engine & platform.
* **EAI**: Exponential Intelligence for Any AI — Architectural framework for exponential agent scaling.
* **GMA**: GHA Master Agent — Tier 1 universal intent interactor, A2A root supervisor, and mission router.
* **GAWD**: Global Agentic Workspace Domain — Multi-agent swarm ecosystem and agent governance domain.
* **GMAS**: GAWD Multi-agent Supervisor — Cluster supervisor managing inter-agent communications (A2A & AOA protocols).
* **GEMI**: Global Engine for Multi-model Inference — Inference bridge routing across cloud APIs and local runtimes.
* **GMCP**: GHA Model Context Protocol — Native tool execution engine, interop bus, and Model Context Protocol server/client.
* **PKB**: Protocol Knowledge Base — Reflex data synthesizer for Tier 0 training distillation.

## Intelligence Tiers

* **Tier 0: GHA-Alpha (Native Reflex Engine)**: Microsecond (<1ms) protocol routing and deterministic reflexes.
* **Tier 1: GAWD / GMA (Universal Swarm Supervisor)**: A2A swarm dispatch, governance auditing, and mission supervision.
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
