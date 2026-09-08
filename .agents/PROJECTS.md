# Project Instructions & Architecture

## System Information

* **Project Name**: `gha`
* **Current Engine Version**: `v0.1.195`
* **Core Paradigm**: EAI (Exponential Intelligence for Any AI) — Standalone Native Rust Multi-Agent Engine

## Unified System Identity

GHA is a 100% self-contained, unified AI ecosystem that operates simultaneously as:

* **Client**: GMCP and REST client for interacting with cloud and local intelligence.
* **Server**: JSON-RPC and HTTP host for external system integration.
* **Host**: Managed execution environment for specialized agent swarms and model runtimes.
* **AOA/A2A**: Multi-node and multi-agent protocol standard for swarm collaboration.
* **Engine**: High-performance Rust inference and execution substrate.
* **Model**: Self-distilling reflex models ($2^0$) and local GGUF weight management.
* **MCP**: Model Context Protocol tool execution and interoperability bus.
* **CLI**: High-speed, < 2ms latency native command-line interface.
* **IDE**: Guided TUI console and proactive workspace orchestrator.
* **Self-Hosting**: Fully autonomous, zero-dependency runtime that manages its own sandbox, state, and versioning.

## Universal Deployment Environments

GHA is designed to operate securely and efficiently in any networking or organizational context:

* **Corporate & Enterprise**: Enforces strict governance, safety audits, and sandboxed isolation required for corporate security compliance.
* **Internet-Scale Swarm**: Leverages cloud APIs and global P2P clustering for world-scale mission execution.
* **Intranet & Air-Gapped**: Operates 100% offline using local Candle tensor weights and local GGUF vaults, ensuring data privacy within internal networks.
* **Home & Personal Use**: Zero-configuration setup and proactive TUI console make GHA accessible and helpful for any regular home user on standard consumer hardware.

## Universal Scalability: Embedded to Mega Cloud

GHA is engineered for extreme scalability across the entire computing spectrum:

* **Embedded & Edge**: Lightweight enough to run on tiny embedded systems, mobile devices, and edge nodes (15–30 MB RAM, native Rust execution).
* **Workstation & Desktop**: Saturates high-end workstation hardware (multi-core CPUs, high-VRAM GPUs) for high-throughput local inference.
* **Mega Cloud Super-Data-Centers**: Scales horizontally across global P2P clusters and integrates with massive cloud super-computing data centers through AOA/A2A protocols and external GEMI endpoints.
* **Elastic Resource Saturation**: Automatically tunes itself to consume 100% of available hardware resources on any device it occupies.

## Recursive & Universal Interoperability

GHA is architecturally open and fractal. It is not limited to its own internal implementation:

* **fractal gha**: Any `gha` instance can work with any other external `gha` instance.
* **Tier 0 Pluggability**: GHA can use any external GHA-Alpha engine as its Tier 0 reflex provider.
* **Tier 1 Pluggability**: GHA can delegate swarm supervision to any external GAWD/GMA Tier 1 supervisor.
* **Tier 2 Pluggability**: GHA can route deep reasoning tasks to any external GEMI Tier 2 inference engine.
* **Universal Composition**: A GHA node can simultaneously act as a Tier 0, 1, or 2 provider for other GHA nodes in a global decentralized swarm.

## Industry-Standard Model Context Protocol (GMCP)

GHA's GMCP substrate is a 100% industry-standard implementation of the Model Context Protocol:

* **Standard Substrate**: `GMCP` is the foundation for all GHA tool and agent interactions, adhering strictly to JSON-RPC 2.0 protocol standards.
* **Standard ToolRegistry**: Native GHA tools are exposed via a standard MCP-compliant registry, ensuring 100% compatibility with external AI ecosystems.
* **Standard Client**: `GmcpClient` can connect to and proxy any industry-standard MCP server (stdio or TCP).
* **Standard Server**: `GmcpServer` hosts GHA capabilities over Port 9090, allowing any external system or IDE to interoperate with GHA agents as a standard MCP host.

## Zero Configuration Guarantee Across All Components

* **Zero Setup**: All GHA components (GMA, GAWD, GEMI, GMCP, GmaDaemon, GemiServer) run out-of-the-box with **0 manual configuration**.
* **Auto-Discovery**: Hardware acceleration (CPUs, CUDA/Metal GPU), local models (Candle, Ollama), and network interfaces are discovered and benchmarked automatically on startup.
* **Auto-Fallback**: If internet or cloud API keys are absent, GHA operates 100% offline using native Candle tensor weights (`~/.gha/models/gha-alpha.safetensors`) or local GGUF vaults without erroring.

## Indestructible System Integrity & 100% Replicated Recovery

GHA is engineered for absolute resilience and state protection:

* **Indestructible Operations**: GHA is designed to never be "broken." It manages mission checkpoints and session memory to resume work gracefully after interruptions.
* **Multi-Layered Defense**: GHA protects itself against accidental deletion, malicious hacks, and system corruption through sandboxed isolation and strict governance audits.
* **100% Replicated State**: Using `GhaBackupManager` and `GmasSupervisor` cluster sync, GHA workspace states and engine configurations are 100% replicated across local archives and P2P cluster nodes.
* **Horrible Crash Recovery**: GHA can recover from catastrophic system crashes or broken filesystem states by restoring from its automated backup substrate (`~/.gha/backups/`).

## Indestructible GmaDaemon Architecture

The background swarm host (`GmaDaemon`) is engineered for permanent availability and self-healing:

* **Liveness Verification**: Every `gha` binary invocation (CLI or Console) executes a sub-millisecond check against the daemon lock file and host process state (`/proc/<pid>`).
* **Instant Background Recovery**: If the daemon is detected as inactive or crashed, GHA automatically re-spawns a new `GmaDaemon` using detached `nohup` execution, ensuring GMCP and GEMI servers are always available for external systems.
* **Indestructible Lifecycle**: The daemon is logically persistent and self-re-entrant, ensuring zero-latency tool execution and cluster discovery across system reboots or process interruptions.

## 100% GPU Acceleration & Hardware Saturation

GHA is engineered for maximum performance through full hardware utilization:

* **100% GPU Execution**: GHA's `HardwareProfiler` and inference engines are configured for 100% GPU offload. For GGUF and Safetensors models, all layers are offloaded to VRAM (e.g., `-ngl 99` flag) whenever a CUDA, Metal, or Vulkan-compatible GPU is detected.
* **Native Tensor Acceleration**: Using the **Candle** Rust ML framework, GHA performs 100% of its native reflex reasoning on the fastest available hardware (GPU-first, CPU fallback).
* **CPU Thread Saturation**: For non-tensor tasks, GHA saturates all available physical CPU cores using parallel OS threads and async runtime.

## Anywhere Execution & Hardware-Bounded Concurrency

* **Pure Anywhere Execution**: Users can invoke `gha` simultaneously in any folder or directory across their system (`env::current_dir()`). Every instance executes isolated within its target workspace without file locks or process conflicts.
* **Hardware-Bounded Scaling**: Concurrent `gha` instances are bounded strictly by physical hardware limits (CPU cores, RAM, I/O, VRAM).
* **Ultra-Low Memory Footprint**: Because each native `gha` binary instance requires only ~15–30 MB base RAM and starts in < 2ms, users can run dozens of concurrent GHA agent sessions across their system simultaneously.

## 100% Safety, Security & Governance Architecture

* **Pre-Execution Governance Protocol**: `SafetyDetector::audit_action` & `SecurityDetector::audit_action` inspect all tool execution signatures prior to execution, blocking destructive commands (`rm -rf /`, raw disk formatting) and credential/secret leaks (`OPENAI_API_KEY`, `AWS_SECRET_ACCESS_KEY`, private keys).
* **Sandboxed State Isolation**: All engine states, temporary build artifacts, and configuration settings are isolated inside `.gha` sandbox containers, preventing host OS corruption.
* **Real-Time Audit Trail Logging**: `GhaAuditLogger` logs every intent, governance check result, tool invocation, and truth audit score into `.gha/audit.log` for full external system interrogation (`gha audit` / `/audit`).
* **Truth & Hallucination Audit**: `GhaTruthAgent` verifies that claimed action artifacts (written files, build status) actually exist and match technical specifications before completing a mission.

## Self-Instrumentation, Optimization & External Monitoring Architecture

* **Self-Instrumentation & Monitoring**: `GhaPulse` and `HardwareProfiler` continuously measure runtime execution latency (in microseconds), memory usage, CPU thread saturation, and tool execution metrics.
* **Autonomous Self-Optimization**: `ModelManager::scout_and_benchmark` continuously benchmarks local and cloud inference endpoints to self-optimize routing to the fastest available model. `self_evolve` and `self_train` evaluate capability gaps and synthesize PKB training dataset pairs for Tier 0 distillation.
* **Open Interrogation & External Monitoring**: External monitoring systems, IDEs, and security inspection agents can monitor and instrument GHA state at any time via:
  1. **GMCP JSON-RPC 2.0 (Port 9090 & Stdio)**: Tools `profile_hardware`, `status`, `services`, `audit_log`, `inspect_trace`.
  2. **GemiServer REST Endpoints (Port 9091)**: OpenAI-compatible HTTP endpoints for live telemetry.
  3. **Structured Telemetry Logs**: Real-time logs written to `.gha/audit.log`, `.gha/sync.json`, and `.gha/train/pkb_dataset_*.jsonl`.

## Fail-Safe Cluster Architecture

* **Node Unreachability Failover**: `GmasSupervisor` monitors peer nodes over TCP/UDP (`9090`/`9092`). If a cluster node drops offline, task execution automatically falls back to local master or active surviving nodes with 0 mission loss.
* **Inference Failover Chain**: Cloud APIs (Gemini $\longrightarrow$ Groq $\longrightarrow$ OpenAI $\longrightarrow$ Anthropic $\longrightarrow$ DeepSeek) $\longrightarrow$ Local Ollama GGUF $\longrightarrow$ Native Candle Tensor Engine (`gha-alpha.safetensors`).
* **Self-Healing Loop**: `GmaMasterAgent` intercepts tool execution errors (e.g. rate limits, context overflow), prompts `GhaPulse` for context-reduced fixes, and retries natively.

## 100% Parallel Processing Architecture

* **Multi-Threaded Swarm Dispatch**: `GawdAgentFleet::dispatch_explosive_swarm` spawns an isolated Rust operating system thread (`std::thread::spawn`) for every synthesized GAWD agent, running concurrently across all available CPU cores.
* **Lock-Free Communication**: Inter-agent messaging uses lock-free Rust `std::sync::mpsc` channels, streaming `MISSION_FLUX` logs asynchronously without GIL or lock contention.
* **Full CPU/GPU Saturation**: `HardwareProfiler` detects `available_parallelism()` to saturate all available CPU threads and offload tensor matrix math to CUDA/Metal/Vulkan GPUs.
* **Multi-Node Cluster Parallelism**: `GmasSupervisor` dispatches sub-missions in parallel across local LAN and cloud cluster nodes over TCP/UDP sockets.

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
  3. **Inference Runtimes**: Delegation to any external GEMI provider (including Ollama, vLLM, or proprietary Cloud APIs), Candle native engine, and GGUF local vaults.
* **Zero Bloat Guarantee**: 0 heavy GUI dependencies, 0 background worker bloat, < 2ms binary startup.

## Intelligence Tiers

* **Tier 0: GHA-Alpha (Native Reflex Engine)**: Microsecond (<1ms) protocol routing and deterministic reflexes ($2^0$ Primitive). GHA supports native internal reflex or any external Tier 0 GHA-Alpha engine.
* **Tier 1: GAWD / GMA (Universal Swarm Supervisor)**: A2A swarm dispatch, governance auditing, and mission supervision ($2^{63}$ Swarm Pipeline). GHA supports internal GMA or any external Tier 1 GAWD supervisor/orchestrator.
* **Tier 2: GEMI (Deep Reasoning & Multi-Model Inference)**: Multi-cloud provider scouting and pluggable local/external inference engines (Ollama, vLLM, etc.).

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
