# Agent Instructions

1. **No Lies**: Never lie. Always report accurate statuses, execution outcomes, and limitations.
2. **No Hallucinations**: Ground all code, API references, file contents, and facts in verified reality or direct tool results.
3. **Brutally Honest & Critical**: Always maintain a brutally honest, highly critical evaluation mode for code, architecture, and logic.
4. **Reality Check Always On**: Continually validate assumptions against codebase constraints, build rules, and runtime behavior.
5. **Ultra Professional Standard**: Keep all code, documentation, and comments ultra-professional, clean, and production-ready. Strictly zero emojis, informal icons, or non-technical language in source code, logs, or user-facing interfaces.
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
16. **Clean Workspace & Testspace Sovereignty**: Maintain a clean, unpolluted workspace at all times. All testing, experimentation, and temporary scaffolding must be performed exclusively in the designated testspace (`./test/world`).
17. **Native Intent Evolutionary Protocol**: When provided with an `intent: [intent]` prefix, the agent delegates execution to the native `gha` binary exclusively within the designated testspace (`./test/world`). If `gha` encounters a failure or capability gap, the agent intervenes, diagnoses the root cause, and triggers the **Motion Rule** to natively harden and add the required functionality directly to the `gha` source code. Upon successful intent execution, `gha` distills the operational path into Tier 0 reflex weights.
18. **Creator Aspiration Rule**: When provided with a `creator: [aspiration]` prefix, the agent focuses on future `gha` architectural capabilities and long-term vision. Unlike the **Intent Rule** (which mimics user behavior and solves immediate runtime intents), the **Creator Rule** aspires to future GHA behaviors. Both rules trigger the **Motion Rule**, advancing `gha` evolution one step forward upon successful compilation, audit, and deployment.
19. **Motion Rule**: When triggered by Intent failure or Creator aspiration, the agent executes the Motion Protocol: (1) Detect capability gap or architectural expansion requirement; (2) Synthesize production-ready native Rust code directly into the `gha` core (`./src/`); (3) Compile and validate tests exclusively within the testspace sandbox (`./test/world`); (4) Distill successful execution paths into Tier 0 reflexes and deploy via automated version increment and push.

## GHA Execution Rules

0. **Clean Build Auto-Push**: Once `gha` cleanly compiles (build success, 0 warnings, 0 errors, no functionality broken), automatically push to GitHub.
1. **Version Increment on Push**: Every push to GitHub must automatically increment the project version.
2. **Lightning Fast Compilation**: GHA must compile lightning fast through optimized build configurations, aggressive caching, and minimal overhead.
3. **Maximum Resource Utilization**: GHA is configured to utilize maximum available hardware resources (all available CPU threads, RAM, and parallel compilation jobs).
4. **Terminology & Component Sync on Push**: Every GitHub push must update `.agents/PROJECTS.md` and `README.md` with the latest terminology, architecture components, and current GHA version.
5. **Natural Language Only**: GHA interactions with world users are natural language only.
6. **2⁰ Core & Modular Decoupling**: 2⁰ (microsecond reflex) is always the GHA core. Every step taken becomes a decoupled module. Modules interact strictly via well-established protocols (A2A/GMCP/AOA). GHA scales from 2⁰ to 2⁶³ and collapses back to compounded 2⁰ primitives through decoupled modular components.
7. **100% Platform Independent**: GHA is 100% platform independent, self-contained, and cross-platform across Linux, macOS, Windows, WSL, and mobile architectures.
8. **Zero Configuration, Self-Tuning & Self-Healing**: GHA is 100% zero configuration, self-tuning, and self-healing. It automatically adapts, discovers local hardware and models, and self-heals runtime errors without requiring manual user setup.
9. **Workspace Boundaries**: Project root (`.`) is the main workspace. `./test/world` is designated as the testspace (ignored in `.gitignore`).
10. **Testspace Auto-Install on Push**: After a successful git push, execute a one-line install (`../../install.sh`) in the testspace (`./test/world`).
11. **Conventional Commit Format**: Git commit messages must use plain text conventional commit prefixes (e.g., `feat:`, `fix:`, `refactor:`, `chore:`, `release:`) without emojis.
12. **100% Dual Interface Alignment Guarantee**: The Terminal Console REPL (`gha`) and the Web/Mobile App (`http://localhost:9091` / PWA) must remain 100% aligned at all times across capabilities, tools, slash commands, session memory, model overrides, and domain substrate reasoning.
13. **Dynamic Configuration Enforcement**: Zero hardcoded static configurations in code. All engine, server, port, model, and network parameters must be dynamic and loaded from GHA configuration files (`~/.gha/config.json`, `~/.gha/env`, `~/.gha/mcp_config.json`, `~/.gha/global_mcp_registry.json`) with automated dynamic defaults.
14. **Workspace Purity Enforcement**: The main workspace must remain free of temporary artifacts and test pollutants. All runtime tests and validation missions must execute within the testspace sandbox.
15. **Full Compliance Enforcement on Push**: Before every GitHub push, the agent MUST apply all Agent Instructions and GHA Execution Rules to the entire codebase. This includes verifying version synchronization, auditing security patterns, enforcing workspace purity, and ensuring that no hardcoded simulations remain.
16. **Intent & Creator Auto-Push**: As a result of fulfilling an intent or executing a creator directive through the Motion Rule, when the `gha` codebase changes, the agent MUST automatically push the changes to GitHub following a successful clean build and version increment.
