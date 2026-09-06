# 🏗️ GHA Purified Architecture: Swarm Flux & EI

`gha` is architected around a single, uncompromising principle: **GMA (GHA Master Agent)** is the **Sole Interactor** for the user. It is a purified architecture that eliminates all project-management scaffolding in favor of direct **Anywhere for Anything** impact.

## 🤖 The "Sole Interactor" Swarm Flux

You interact strictly with GMA using natural language. GMA establishes a mission scope in your **Current Working Directory (CWD)** and governs the 3 tiers in parallel:

```text
                                 👤 GHA USER
                                         │
                                         │ Natural Language Intent
                                         ▼
                     ┌───────────────────────────────────────┐
                     │ 🤖 GMA (GHA Master Agent)             │
                     │    • The Sole Interactor              │
                     │    • The Swarm Flux Root              │
                     │    • Truth & Governance Auditor       │
                     └───────────────────┬───────────────────┘
                                         │
                 ┌───────────────────────┼───────────────────────┐
                 ▼                       ▼                       ▼
   🤖 Tier 1: GAWD             🧠 Tier 2: GEMI           🔌 Tier 3: GMCP
   (Autonomous Swarm           (Universal Inference      (Universal Tool
    Agent Synthesis)            Picking & Benchmarking)   Scouting & Discovery)
                 │                       │                       │
                 └───────────────────────┼───────────────────────┘
                                         │
                                         ▼
                     🌌 GMA Delivers Verified Artifacts in CWD
                                         │
                                         ▼
                                 👤 Mission Accomplished
```

---

## 📦 The 3 Purified Tiers

### 🤖 Tier 1: GAWD (Agent-to-Agent Swarm Synthesis)
A dynamic execution fleet that synthesizes itself based on mission intent.
*   **Dynamic Spawning**: Autonomously spawns specialists (Kernel, Creative, Economic, Linguist) as needed.
*   **A2A Flux**: Multi-threaded async channels for high-throughput vertical scaling.
*   **TruthAgent**: Executes the final Truth Audit to detect hallucinations and verify impact.

### 🧠 Tier 2: GEMI (Inference Picking & Benchmarking)
The gateway to exponential intelligence:
*   **Brain Scouting**: Autonomously finds local (Ollama/GGUF) and cloud brains.
*   **Pulse Benchmarking**: Measures latency and reliability in real-time.
*   **Predictive Picking**: Selects the absolute best brain for the specific mission.

### 🔌 Tier 3: GMCP (Universal Tool Discovery)
The standardized industry-compliant tool hub:
*   **Universal Discovery**: Autonomously scouts and auto-configures Industry Standard MCP servers.
*   **Standard Compliance**: 100% JSON-RPC 2.0 bridge over `stdio` and TCP.
*   **Prefix Proxying**: Seamlessly routes to external tools via `server:tool` syntax.

---

## 🛡️ Governance & Impact Scope
*   **CWD Impact**: `gha` creates no local `.gha` folders. It acts directly on the user's current directory, leaving no trace except for the delivered value.
*   **Safety Pre-Audit**: Every mission is audited for destructive commands (`rm -rf`, `mkfs`) before execution.
*   **Security Pre-Audit**: Every mission is audited for credential leaks (`sk-`, `ghp_`) and exfiltration patterns.
