# Installation & Setup

## Option 1: Self-Contained Init Script (Zero Modifications)

Run `gha` tasks on any cloned project without changing build scripts:

```bash
./gradlew --init-script init/gha.init.gradle.kts ghaInit ghaStatus ghaDependencies
```

## Option 2: Gradle Plugin

Add `cc.thevar.gha` to your project's `build.gradle.kts`:

```kotlin
plugins {
    id("cc.thevar.gha") version "0.1.0"
}
```