#!/usr/bin/env bash
# 🚀 gha: 1-Line Universal Installer (0-Effort, 100% Gains)
# 100% Sandboxed - 100% Platform Independent - 100% IDE Independent - 0 JVM, 0 Git, 0 Gradle
set -e

GLOBAL_GHA_DIR="$HOME/.gha"
GLOBAL_BIN_DIR="$GLOBAL_GHA_DIR/bin"
mkdir -p "$GLOBAL_BIN_DIR"
mkdir -p "$GLOBAL_GHA_DIR/models"

# ⚡ [gha] Mission: Initialize 100% Sandboxed Native AI Runtime
echo "⚡ [gha] Initializing 100% Sandboxed Native AI Runtime..."

# Determine source location (Local vs Remote Swarm Flux)
if [[ -n "${BASH_SOURCE[0]}" ]] && [[ -f "$(dirname "${BASH_SOURCE[0]}")/../Cargo.toml" ]]; then
    SCRIPT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")/.." && pwd)"
    echo "🏠 [gha Local] Detected local installation from source..."
else
    echo "🌐 [gha Remote] Detected remote installation mission..."
    TEMP_DIR=$(mktemp -d)
    echo "   ├── Cloning gha engine from GitHub to $TEMP_DIR..."
    git clone --depth 1 https://github.com/intellibitz/gha.git "$TEMP_DIR" >/dev/null 2>&1 || { echo "❌ Git clone failed."; exit 1; }
    SCRIPT_DIR="$TEMP_DIR"
fi

INSTALLED=0
rm -f "$GLOBAL_BIN_DIR/ghai" "$GLOBAL_BIN_DIR/ghai-engine" 2>/dev/null || true

# 1. Binary Deployment Protocol
if [ -f "$SCRIPT_DIR/target/release/gha" ]; then
    cp "$SCRIPT_DIR/target/release/gha" "$GLOBAL_BIN_DIR/ghai-engine"
    cp "$SCRIPT_DIR/target/release/gha" "$GLOBAL_BIN_DIR/ghai"
    chmod +x "$GLOBAL_BIN_DIR/ghai-engine" "$GLOBAL_BIN_DIR/ghai"
    INSTALLED=1
    echo "   └── Installed native binary engine to $GLOBAL_BIN_DIR/ghai (< 2ms startup)"
elif command -v cargo >/dev/null 2>&1 && [ -f "$SCRIPT_DIR/Cargo.toml" ]; then
    echo "⚡ [gha Native] Compiling standalone Rust AI engine (High Throughput Optimized)..."
    (cd "$SCRIPT_DIR" && cargo build --release >/dev/null 2>&1)
    if [ -f "$SCRIPT_DIR/target/release/gha" ]; then
        cp "$SCRIPT_DIR/target/release/gha" "$GLOBAL_BIN_DIR/ghai-engine"
        cp "$SCRIPT_DIR/target/release/gha" "$GLOBAL_BIN_DIR/ghai"
        chmod +x "$GLOBAL_BIN_DIR/ghai-engine" "$GLOBAL_BIN_DIR/ghai"
        INSTALLED=1
        echo "   └── Compiled & installed native binary engine to $GLOBAL_BIN_DIR/ghai (< 2ms startup)"
    fi
fi

if [ "$INSTALLED" = "0" ]; then
    echo "❌ [gha] Error: Native binary installation failed. Ensure 'cargo' is installed for first-time native compilation."
    exit 1
fi

# 2. Version Sync
if [ -f "$SCRIPT_DIR/version.txt" ]; then
    cp "$SCRIPT_DIR/version.txt" "$GLOBAL_GHA_DIR/gha-engine-version.txt"
fi

# 3. Prime background GMA Master Daemon
if [ -x "$GLOBAL_BIN_DIR/ghai" ]; then
    "$GLOBAL_BIN_DIR/ghai" install >/dev/null 2>&1 || true
fi

# 4. PATH Integration
if [[ ":$PATH:" != *":$GLOBAL_BIN_DIR:"* ]]; then
    echo "⚡ [ghai] Integrating '$GLOBAL_BIN_DIR' into system PATH..."
    CONFIG_FILES=("$HOME/.bashrc" "$HOME/.zshrc" "$HOME/.profile")
    for config in "${CONFIG_FILES[@]}"; do
        if [ -f "$config" ] && ! grep -q ".gha/bin" "$config"; then
            echo -e "\n# gha: Universal Multi-Agent AI Runtime\nexport PATH=\"\$HOME/.gha/bin:\$PATH\"" >> "$config"
            echo "   ✅ Added to $config"
        fi
    done
fi

echo ""
echo "🎉 Global gha is ready! Type 'ghai \"what is your version?\"' to verify."
