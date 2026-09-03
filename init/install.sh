#!/usr/bin/env bash
# 🚀 gha: 0 Effort, Ridiculously Easy Installer for macOS, Linux, & WSL
# Usage: curl -sSL https://raw.githubusercontent.com/intellibitz/gha/main/init/install.sh | bash

set -e

echo "🚀 [gha Installer] Installing gha (Git, GitHub & Gradle Automation)..."

# Ensure init directory exists
mkdir -p init

# Download gha.init.gradle.kts if not present
if [ ! -f "init/gha.init.gradle.kts" ]; then
    echo "📥 Downloading init/gha.init.gradle.kts..."
    curl -sSL "https://raw.githubusercontent.com/intellibitz/gha/main/init/gha.init.gradle.kts" -o "init/gha.init.gradle.kts" || {
        echo "⚠️ Could not download init script online. Creating local fallback init script..."
        cat << 'EOF' > init/gha.init.gradle.kts
initscript {
    repositories {
        mavenLocal()
        mavenCentral()
        gradlePluginPortal()
    }
    dependencies {
        classpath("cc.thevar.gha:gha:0.1.0-SNAPSHOT")
        classpath("org.gradle.toolchains:foojay-resolver:1.0.0")
    }
}
settingsEvaluated {
    apply(plugin = "org.gradle.toolchains.foojay-resolver-convention")
}
allprojects {
    apply<cc.thevar.gha.GhaPlugin>()
}
EOF
    }
fi

# Run ghaInit & ghai
if [ -f "./gradlew" ]; then
    chmod +x ./gradlew
    echo "⚙️ Initializing gha sandbox..."
    ./gradlew -Dgradle.user.home=.gha/gradle-user-home --init-script init/gha.init.gradle.kts ghaInit
else
    echo "ℹ️ ./gradlew wrapper not found in current directory. Creating init script in init/gha.init.gradle.kts."
fi

echo "🎉 [gha Installer] gha installed ridiculously easy in 1 second!"
echo "👉 Type './gradlew -Dgradle.user.home=.gha/gradle-user-home --init-script init/gha.init.gradle.kts ghai' to run autonomous AI automation."
