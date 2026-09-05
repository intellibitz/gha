# Ridiculously Easy 0-Effort Installation

Install `gha` globally in 1 second. The installer automatically sets up the **GMA Master Daemon** and adds the `ghai` launcher to your `PATH` for zero-effort access from any directory:

## ⚡ 1-Second Global Installers

### macOS, Linux, & WSL
```bash
curl -sSL https://raw.githubusercontent.com/intellibitz/gha/main/init/install.sh | bash
```

### Windows PowerShell
```powershell
iwr -useb https://raw.githubusercontent.com/intellibitz/gha/main/init/install.ps1 | iex
```

---

## 🤖 Global Launcher Subcommands (`ghai`)

Once installed, `ghai` works globally across Linux, macOS, WSL, and Windows. It is **100% sandboxed** and context-aware—automatically detecting if you are inside a GHA project or offering to initialize one.

```bash
# 1. Trigger AI Automation in the current folder:
ghai

# 2. Initialize a folder for GHA (0-Effort Scaffolding):
ghai :install

# 3. Smart Git Repository Clone:
ghai :clone intellibitz

# 4. Print Version Report & Verify Engines:
ghai :version

# 5. Display Always-On Daemon & Sandbox Health:
ghai :status
```

---

## The 100% Sandbox Principle

`gha` follows a strict **"Never Touch User Files"** policy:

- **Scenario: Existing Project**: `gha` integrates into your build purely in memory via Gradle's init script flag. Your original source files remain pristine and untouched.
- **Scenario: Empty Folder**: `gha` creates its own minimal build and settings files with a clear header. These are safely removed by `./ghai :uninstall`.
- **Global Settings**: `gha` uses an isolated Gradle home directory in `.gha/gradle-user-home`, ensuring 0 modifications to your system's `~/.gradle` folder.
