#!/usr/bin/env bash
# 🚀 gha: 0 Effort, Ridiculously Easy Installer for Creators & Users
# Usage: curl -sSL https://raw.githubusercontent.com/intellibitz/gha/main/init/install.sh | bash

set -e

echo "🚀 [gha Installer] Installing gha & ghai..."

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

# Create top-level ./ghai executable script
cat << 'EOF' > ghai
#!/usr/bin/env bash
# 🤖 ghai - Autonomous AI Workflow Executable Launcher
# 0 Effort, 100% Gain: Works Anywhere, Everywhere.

set -e

PROJECT_ROOT="$PWD"
while [ "$PROJECT_ROOT" != "/" ]; do
    if [ -f "$PROJECT_ROOT/gradlew" ] || [ -d "$PROJECT_ROOT/.git" ]; then
        break
    fi
    PROJECT_ROOT="$(dirname "$PROJECT_ROOT")"
done

if [ ! -f "$PROJECT_ROOT/gradlew" ]; then
    echo "❌ [ghai] No Gradle wrapper (gradlew) found in project tree."
    exit 1
fi

cd "$PROJECT_ROOT"
chmod +x gradlew ghai 2>/dev/null || true

if [ ! -f "init/gha.init.gradle.kts" ] || [ ! -d ".gha" ]; then
    echo "⚡ [ghai] First-time run detected! Auto-initializing gha sandbox..."
    mkdir -p init
    cat << 'FALLBACK_EOF' > init/gha.init.gradle.kts
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
FALLBACK_EOF
    ./gradlew --init-script init/gha.init.gradle.kts ghaInit
fi

exec ./gradlew --init-script init/gha.init.gradle.kts ghai "$@"
EOF

chmod +x ghai

# Symlink to ~/.local/bin/ghai if directory exists for global 'ghai' CLI access
if [ -d "$HOME/.local/bin" ]; then
    ln -sf "$PWD/ghai" "$HOME/.local/bin/ghai" 2>/dev/null || true
fi

echo "⚙️ Initializing gha sandbox & executing ghai..."
./ghai

echo "🎉 [gha Installer] gha & ghai installed ridiculously easy!"
echo "👉 Creators & Users: Simply type './ghai' anywhere in your project!"
