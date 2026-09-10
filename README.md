![GHA Version](https://img.shields.io/badge/version-v0.1.2022631-blue.svg) ![License](https://img.shields.io/badge/license-Apache%202.0-green.svg)

**GHA (Exponential Intelligence for Any AI Substrate)** is an Intelligence Reflex & Execution Substrate (EAI) empowering any world user to execute any intent through a native Rust intelligence engine. It features an ultra-lightweight launcher (<2ms startup) managing a persistent background daemon for zero-latency tool execution and local hardware sovereignty.

## The Unified Neural Paradigm

GHA operates through a dual-section neural architecture:
1. **Alpha-Self**: Immutable system constitution, architectural rules, and component topologies compiled directly into binary instructions (`src/gawd/self_core.rs`), providing instant self-awareness across all engine components.
2. **Alpha-User**: Experiential memory and distilled intent reflexes stored in local neural weights (`gha-alpha.safetensors`), expanding exponentially through continuous Procedural Knowledge Base (PKB) distillation.
3. **Local-First Fallback**: Automatically prioritizes local execution (Ollama, local GGUF vaults, and Candle tensor substrates) before scouting cloud providers, ensuring strict hardware sovereignty.

## Installation

### Linux / macOS / WSL
```bash
curl -sSL https://raw.githubusercontent.com/intellibitz/gha/main/install.sh | bash
```

### Windows (PowerShell)
```powershell
iwr -useb https://raw.githubusercontent.com/intellibitz/gha/main/install.ps1 | iex
```

## Usage

Invoking `gha` with no arguments exits quietly with zero overhead. To execute intents or commands, provide them directly:

```bash
gha "read projects.md and agents.md"
gha "list the number of android studio installed in my system"
gha status
gha mcp
```

## Maintainers

* IntelliBitz
* Muthu Ramadoss
* Gemini (Google AI)

## License

[Apache License 2.0](LICENSE).
