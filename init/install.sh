#!/usr/bin/env bash
# 🚀 gha: 0 Effort, Ridiculously Easy Installer for Creators & Users
# Usage: curl -sSL https://raw.githubusercontent.com/intellibitz/gha/main/init/install.sh | bash

set -e

echo "🚀 [gha Installer] Installing gha for Creators & Users..."

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

# Create top-level ./ghai executable wrapper script for 0-effort invocation
cat << 'EOF' > ghai
#!/usr/bin/env bash
# 🤖 ghai - Autonomous AI Workflow Execution Script
./gradlew --init-script init/gha.init.gradle.kts ghai "$@"
EOF
chmod +x ghai

# Run ghaInit & ghai immediately
if [ -f "./gradlew" ]; then
    chmod +x ./gradlew
    echo "⚙️ Initializing gha sandbox..."
    ./gradlew --init-script init/gha.init.gradle.kts ghaInit
    echo "🤖 Executing ghai autonomous AI workflow..."
    ./ghai
else
    echo "ℹ️ ./gradlew wrapper not found in current directory. Created ./ghai runner script."
fi

echo "🎉 [gha Installer] gha & ghai installed ridiculously easy in 1 second!"
echo "👉 Creators & Users: Simply type './ghai' anytime to run autonomous AI automation!"
