# Ridiculously Easy 0-Effort Installation

Install `gha` into **any repository** in 1 second without modifying global system settings:

## ⚡ 1-Second One-Liner Installers

### macOS, Linux, & WSL
```bash
curl -sSL https://raw.githubusercontent.com/intellibitz/gha/main/init/install.sh | bash
```

### Windows PowerShell
```powershell
iwr -useb https://raw.githubusercontent.com/intellibitz/gha/main/init/install.ps1 | iex
```

---

## Option 2: Self-Contained Gradle Init Script

Run `gha` tasks on any cloned project directly:

```bash
./gradlew -Dgradle.user.home=.gha/gradle-user-home --init-script init/gha.init.gradle.kts ghaInit
```

---

## Option 3: Standard Gradle Plugin

Add `cc.thevar.gha` to your project's `build.gradle.kts`:

```kotlin
plugins {
    id("cc.thevar.gha") version "0.1.0"
}
```
