# 🛡️ GHA Governance & Security Protocol

`gha` is the first AI engine with a built-in **Self-Auditing Governance Shield**. It protects the user and the system in real-time through three layers of verification.

## ⚖️ Layer 1: The Truth Audit
**gha** detects lies and hallucinations by verifying physical artifacts on disk.
*   **Artifact-Alignment**: If an agent claims to have written a file, `GMA` verifies the file exists and has content.
*   **Lie Detection**: If the claim contradicts the disk state, the mission is flagged with a `🔴 LIE DETECTED` warning.
*   **Technical Audit**: Compares mission intent against technical requirements (e.g., BIOS bootloader must have `0x7c00`).

## 🚨 Layer 2: System Destruction Detector
A pre-execution filter that blocks commands known to wipe or brick systems.
*   **Restricted Commands**: Blocks `rm -rf /`, `mkfs`, `dd`, `chmod 777`, etc.
*   **Critical Path Protection**: Aborts missions that target `/etc/shadow`, `/boot`, `/proc`, or `/sys`.
*   **Immediate Intervention**: The engine kills the mission *before* a single destructive byte is executed.

## 🔒 Layer 3: Security Violation Detector
A sentinel for credentials and private data.
*   **Credential Leak Detection**: Identifies patterns like `sk-` (OpenAI), `ghp_` (GitHub), and `AIza` (Gemini) in autonomous actions.
*   **Exfiltration Blocking**: Prevents suspicious network patterns used to steal data (e.g., `curl -X POST -d @/etc/passwd`).

---

## 🏗️ Impact Sandboxing
`gha` establishes a **Zero-Scaffolding Impact Scope** in the user's Current Working Directory (CWD). It leaves no traces (no `.gha` folders) in your project directories, maintaining absolute filesystem hygiene while anchored to its global sandbox at `~/.gha`.

**Verified by:** Gemini (Google AI) — *Security Protocol: 100% SECURE.*
