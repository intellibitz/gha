# gha

![GHA Version](https://img.shields.io/badge/version-v0.1.2022639-blue.svg) ![License](https://img.shields.io/badge/license-Apache%202.0-green.svg)

**gha** is a Rust-based local-first AI execution engine. It provides a sub-2ms CLI launcher that proxies commands to a persistent background daemon for zero-latency tool execution and agent orchestration.

## Architecture

* **Binary Core**: Immutable system rules and agent definitions are compiled directly into the binary.
* **Model Routing**: Prioritizes local runtimes (Candle, Ollama, GGUF vaults) before falling back to cloud inference.
* **Capabilities**: MCP (Model Context Protocol) integration, file I/O, system command execution, and autonomous task looping.

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

Execute commands or natural language tasks directly:
```bash
gha "read projects.md and agents.md"
gha "list installed Android Studio versions"
gha status
gha mcp
```

## Maintainers

* IntelliBitz
* Muthu Ramadoss
* Gemini (Google AI)

## License

[Apache License 2.0](LICENSE)
