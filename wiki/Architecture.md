# 🏗️ GHA Governance Architecture

`gha` is architected around a single, uncompromising principle: **GMA (GHA Master Agent)** is the **Sole Interactor** for the user. 

## 🤖 The "Sole Interactor" Model

You interact strictly with GMA, and GMA governs the 3 tiers behind the scenes in parallel:

```text
                                 👤 GHA USER / IDE / CLIENT
                                         │
                                         │ Single Point of Interaction
                                         ▼
                     ┌───────────────────────────────────────┐
                     │ 🤖 GMA (GHA Master Agent)             │
                     │    • The Master                       │
                     │    • The Orchestrator                 │
                     │    • The One & Sole Interactor        │
                     └───────────────────┬───────────────────┘
                                         │
                 ┌───────────────────────┼───────────────────────┐
                 ▼                       ▼                       ▼
   🤖 Tier 1: GAWD             🧠 Tier 2: GEMI           🔌 Tier 3: GMCP
   (Multi-Threaded A2A         (Cloud API & Local        (MCP Server, Stdio &
    Parallel Worker Fleet)      GGUF Inference Engine)    Port 9090 Tools Hub)
                 │                       │                       │
                 └───────────────────────┼───────────────────────┘
                                         │
                                         ▼
                     🌌 GMA Synthesizes Master Interactor Report
                                         │
                                         ▼
                                 👤 Presented To User
```

---

## 📦 The 3 Coordinated Tiers

### 🤖 Tier 1: GAWD (Agent-to-Agent Worker Fleet)
A multi-threaded parallel execution fleet. Each agent communicates via async channels to fulfill segments of the mission:
*   **GhaContextAgent**: Workspace & Codebase analysis.
*   **GhaReasoningAgent**: Strategic mission decomposition.
*   **GhaSystemExecutionAgent**: System tool orchestration.
*   **GhaAutonomousAgent**: Health verification and self-evolution.

### 🧠 Tier 2: GEMI (Inference & Hardware Profiler)
A protocol-normalized gateway to intelligence:
*   **Zero-Download Cloud**: Dynamic routing to DeepSeek, Groq, Gemini, Mistral, and OpenAI.
*   **Local GGUF Vault**: Native execution of local `.gguf` binaries via hardware-accelerated backends.

### 🔌 Tier 3: GMCP (Model Context Protocol Infrastructure)
The standardized tool hub:
*   **100% Native JSON-RPC 2.0 Server** over `stdio` and TCP Port `9090`.
*   **Dynamic Plugin Engine**: Automatically discovers and executes scripts in `~/.gha/tools/`.
