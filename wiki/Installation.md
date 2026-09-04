# Ridiculously Easy 0-Effort Installation

Install `gha` into **any repository** in 1 second without modifying global system settings or existing project files:

## ⚡ 1-Second Universal One-Liner Installers

### macOS, Linux, & WSL
```bash
curl -sSL https://raw.githubusercontent.com/intellibitz/gha/main/init/install.sh | bash
```

### Windows PowerShell
```powershell
iwr -useb https://raw.githubusercontent.com/intellibitz/gha/main/init/install.ps1 | iex
```

---

## 🤖 Executable Launcher Subcommands (`ghai`)

Once installed, `./ghai` works anywhere across Linux, macOS, WSL, and Windows. It is **100% sandboxed** and will never modify your existing `build.gradle.kts` or `settings.gradle.kts`.

```bash
# 1. Primary Autonomous AI Workflow (Auto-detects diffs, commits, pushes, PRs, auto-merges, prunes CI logs):
./ghai

# 2. Smart Git Repository Clone (e.g. ./ghai clone intellibitz -> https://github.com/intellibitz/intellibitz):
./ghai clone intellibitz

# 3. Print Version Report & Verify Engines (100% Sandboxed):
./ghai version

# 4. Fetch & Update gha to Latest Release:
./ghai update

# 5. Completely Uninstall & Clean gha (0 side effects to user files):
./ghai uninstall
```

---

## The 100% Sandbox Principle

`gha` follows a strict **"Never Touch User Files"** policy:

- **Scenario: Existing Project**: `gha` integrates into your build purely in memory via Gradle's init script flag. Your original source files remain pristine and untouched.
- **Scenario: Empty Folder**: `gha` creates its own minimal build and settings files with a clear header. These are safely removed by `./ghai uninstall`.
- **Global Settings**: `gha` uses an isolated Gradle home directory in `.gha/gradle-user-home`, ensuring 0 modifications to your system's `~/.gradle` folder.
