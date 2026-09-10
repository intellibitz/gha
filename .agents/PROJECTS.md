# Project Instructions & Architecture

## System Information

* **Project Name**: `gha`
* **Current Engine Version**: `v0.1.2022637`

## Architecture

`gha` is a local-first, native Rust AI execution engine designed for high-throughput, low-latency agent orchestration.

### Engine Architecture

* **Alpha-Self**: Immutable system rules (`AGENTS.md`), and component topologies (`PROJECTS.md`) compiled directly into strongly-typed Rust data structures (`AlphaSelf`). Eliminates runtime string parsing and provides instantaneous self-awareness to all engine components.
* **Alpha-User**: Experiential memory and learned actions stored in local neural tensor weights (`gha-alpha.safetensors`), trained continuously.
* **Local-First Fallback**: Automatic fallback strategy that prioritizes local execution (Ollama, local GGUF vaults, and Candle tensor substrates) before calling cloud APIs.

### Unified Identity

* **Client**: MCP and REST client for interacting with cloud and local intelligence.
* **Server**: JSON-RPC and HTTP host for external system integration.
* **Host**: Managed execution environment for specialized agents and model runtimes.
* **CLI**: High-speed, < 2ms latency native command-line interface.

## Industry-Standard Model Context Protocol (MCP)

* **Protocol Support**: Strictly adheres to the MCP JSON-RPC 2.0 standard.
* **Tool Registry**: Native tools are exposed via a standard MCP-compliant registry, ensuring 100% compatibility with external AI ecosystems.
* **Client Implementation**: Can connect to and proxy any industry-standard MCP server (stdio or TCP).
* **Server Implementation**: Hosts capabilities over Port 9090, allowing any external system or IDE to interoperate with local agents as an MCP host.

## Components & Concurrency

* **Zero Setup**: All components run out-of-the-box with zero manual configuration.
* **Dynamic Intelligence**: Tools and agents are implemented via dynamic traits and thread-safe registries, ensuring architectural flexibility.
* **GPU Acceleration**: Inference engines are configured for 100% GPU offload (e.g., `-ngl 99` flag) whenever a CUDA, Metal, or Vulkan-compatible GPU is detected, utilizing the Candle Rust ML framework.
* **CPU Thread Saturation**: For non-tensor tasks, `gha` saturates available physical CPU cores using parallel OS threads and async runtimes.
* **Sandboxed State Isolation**: All engine states, temporary build artifacts, and configuration settings are isolated inside `.gha` sandbox containers, preventing host OS corruption.

## Process Architecture

* **GHA Launcher (`native/gha`)**: A micro-binary (<3MB) written in native Rust that serves as the high-speed entry point. It spawns the background daemon and proxies MCP JSON-RPC streams to the persistent engine over local TCP (Port 9090).
* **GHA Engine (`gha`)**: The heavy-throughput intelligence engine that manages swarm orchestration, inference routing, local model management, and the dynamic tool registry.

## Security & Governance

* **Pre-Execution Governance**: Inspects tool execution signatures prior to execution, blocking destructive commands (`rm -rf /`, raw disk formatting) and credential/secret leaks (`OPENAI_API_KEY`, `AWS_SECRET_ACCESS_KEY`, private keys).
* **Audit Logging**: Logs every intent, governance check result, and tool invocation into `.gha/audit.log`.
* **State Verification**: Verifies that claimed action artifacts (written files, build status) actually exist and match technical specifications before completing tasks.
