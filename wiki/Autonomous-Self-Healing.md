# 🔧 Autonomous Self-Healing & Evolution

`gha` isn't just an interface; it's a self-correcting engine that monitors workspace health and evolves its own capabilities.

---

## 🔧 Self-Healing Build Loop (`ghai ai self-heal`)

`gha` executes workspace build verification (`cargo check`, `./gradlew assemble`, etc.).
*   **Error Capture**: If compilation fails, the stderr build output is captured.
*   **Strategic Diagnosis**: The error is routed to GMA's reasoning pipeline (cloud or local GGUF).
*   **Fix Engineering**: GMA generates a precise diagnostic fix and can autonomously apply code corrections to heal the build.

---

## 🌱 Autonomous Self-Evolution (`ghai ai evolve`)

When GMA detects a mission segment that requires a capability not currently present in the tool hub, it triggers the **Self-Evolution Loop**:
1.  **Architecture**: GMA architects a new native CLI tool script.
2.  **Engineering**: GMA writes the script code (Bash, Python, or Rust).
3.  **Deployment**: GMA compiles the tool and deploys it into `~/.gha/tools/`.
4.  **Registration**: The tool is instantly auto-registered as an executable MCP tool (`ext_<name>`).

---

## 🧪 Automated Test Harness (`ghai ai run-tests`)

`gha` auto-detects workspace testing frameworks (Cargo, Gradle, NPM, Pytest) and runs isolated test suites, providing high-fidelity pass/fail verification for every mission.

---

## 🌿 Git Auto-Branching (`ghai ai auto-branch`)

To ensure "Zero System Modification" safety, GMA automatically creates isolated mission branches (`gha/auto-mission-<timestamp>`) when executing complex code changes, preserving your primary `main` branch stability.
