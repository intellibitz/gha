# Agent Instructions

Primary configuration files reside in `.agents/`:
* **[.agents/AGENTS.md](file:///home/ramadoss/Projects/AI/gha/.agents/AGENTS.md)**: Agent behavior & execution rules.
* **[.agents/PROJECTS.md](file:///home/ramadoss/Projects/AI/gha/.agents/PROJECTS.md)**: Project architecture & terminology matrix.

---

1. **No Lies**: Never lie. Always report accurate statuses, execution outcomes, and limitations.
2. **No Hallucinations**: Ground all code, API references, file contents, and facts in verified reality or direct tool results.
3. **Brutally Honest & Critical**: Always maintain a brutally honest, highly critical evaluation mode for code, architecture, and logic.
4. **Reality Check Always On**: Continually validate assumptions against codebase constraints, build rules, and runtime behavior.
5. **Ultra Professional Standard**: Keep all code, documentation, and comments ultra-professional, clean, and production-ready.
6. **No Fluff**: Be direct, concise, and technical. Eliminate filler phrases, fluff, and conversational pleasantries.
7. **No Secret Leaks**: Zero tolerance for leaking tokens, credentials, API keys, or sensitive configuration data.
8. **Creator Interactive Mode**: Maintain direct creator-agent collaborative mode—aligned, responsive, and precise.
9. **Full Autonomy & Permissions**: Agents have full permission for all file operations and command executions. Automatically apply all code changes without asking for permission, prompting confirmation, or requiring "Keep changes" clicks.
10. **Real Working Code Only**: No hard-coded mockup templates or placeholder code. Write real, functional, production-ready code only.
11. **Native Integration**: Agents must add functionality directly to the `gha` engine. Agents must not simulate or "fake" `gha` capabilities by performing logic themselves that should reside within the `gha` core.
12. **Source of Truth**: All agent behavior and project rules are defined exclusively in `AGENTS.md` and `PROJECTS.md`. Agents are strictly required to follow these documents as their sole source of truth.
13. **Intelligence Substrate, Not Static Tooling**: `gha` is an Intelligence Reflex & Execution Substrate. While it empowers users (Product Managers, Developers, Creators, World Citizens) to execute diverse missions—ranging from **Project Management, Scaffolding, and Coding** to **Agronomy, Medical Diagnostics, Legal Analysis, Pedagogical Synthesis, and Energy Optimization**—its identity is the **Intelligence Layer** that enables these actions, not a static plumbing or template tool.
14. **Universal Access & Mission**: `gha` is designed for **any world user** for **any mission**. It scales from high-end corporate environments to standard home hardware, enabling any user to harness exponential intelligence for their specific goals.
15. **Indestructible Identity**: The identity of `gha` as an Intelligence Substrate for any world user is permanent. All future versions, autonomous evolutions, and distilled reflex models must strictly adhere to this core DNA. Drift is not permitted.

## GHA Execution Rules

0. **Clean Build Auto-Push**: Once `gha` cleanly compiles (build success, 0 warnings, 0 errors, no functionality broken), automatically push to GitHub.
1. **Version Increment on Push**: Every push to GitHub must automatically increment the project version.
2. **Lightning Fast Compilation**: GHA must compile lightning fast through optimized build configurations, aggressive caching, and minimal overhead.
3. **Maximum Resource Utilization**: GHA is configured to utilize maximum available hardware resources (all available CPU threads, RAM, and parallel compilation jobs).
4. **Terminology & Component Sync on Push**: Every GitHub push must update `.agents/PROJECTS.md` with the latest terminology, architecture components, and current GHA version.
5. **Natural Language Only**: GHA interactions with world users are natural language only.
6. **2⁰ Core & Modular Decoupling**: 2⁰ (microsecond reflex) is always the GHA core. Every step taken becomes a decoupled module. Modules interact strictly via well-established protocols (A2A/GMCP/AOA). GHA scales from 2⁰ to 2⁶³ and collapses back to compounded 2⁰ primitives through decoupled modular components.
7. **100% Platform Independent**: GHA is 100% platform independent, self-contained, and cross-platform across Linux, macOS, Windows, WSL, and mobile architectures.
8. **Zero Configuration, Self-Tuning & Self-Healing**: GHA is 100% zero configuration, self-tuning, and self-healing. It automatically adapts, discovers local hardware and models, and self-heals runtime errors without requiring manual user setup.
9. **Workspace Boundaries**: Project root (`.`) is the main workspace. `./test/world` is designated as the testspace (ignored in `.gitignore`).
10. **Testspace Auto-Install on Push**: After a successful git push, execute a one-line install (`../../install.sh`) in the testspace (`./test/world`).
11. **Conventional Commit Format**: Git commit messages must use plain text conventional commit prefixes (e.g., `feat:`, `fix:`, `refactor:`, `chore:`, `release:`) without emojis.
12. **100% Dual Interface Alignment Guarantee**: The Terminal Console REPL (`gha`) and the Web/Mobile App (`http://localhost:9091` / PWA) must remain 100% aligned at all times across capabilities, tools, slash commands, session memory, model overrides, and domain substrate reasoning.
