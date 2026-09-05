#!/usr/bin/env bash
# 🚀 gha: 0-Effort Universal Installer (100% On-Demand Scaffolding)
# 100% Sandboxed - 0% Modifications to existing project files.
set -e

# 🌌 Global GHA Setup
GLOBAL_GHA_DIR="$HOME/.gha"
GLOBAL_BIN_DIR="$GLOBAL_GHA_DIR/bin"
mkdir -p "$GLOBAL_BIN_DIR"

# Dynamic GHA Engine Repository resolution
GHA_REPO="${GHA_REPO:-$(git config gha.repo 2>/dev/null || echo "intellibitz/gha")}"

# 1. Fetch and update global ghai executable launcher
echo "📥 Fetching latest global ghai launcher script..."
curl -sSL "https://raw.githubusercontent.com/$GHA_REPO/main/ghai" -o "$GLOBAL_BIN_DIR/ghai" 2>/dev/null || true
chmod +x "$GLOBAL_BIN_DIR/ghai" 2>/dev/null || true

# 2. Download or refresh global init script
curl -sSL "https://raw.githubusercontent.com/$GHA_REPO/main/init/gha.init.gradle.kts" -o "$GLOBAL_GHA_DIR/init.gradle.kts" 2>/dev/null || true

# 3. Fetch latest GHA Engine version info
echo "📥 Syncing GHA engine version info..."
curl -sSL "https://raw.githubusercontent.com/$GHA_REPO/main/version.txt" -o "$GLOBAL_GHA_DIR/gha-engine-version.txt" 2>/dev/null || true

echo "⚡ [gha] Global 0-Effort Installation Complete!"
echo "   ├── $GLOBAL_GHA_DIR/ sandbox initialized"
echo "   └── $GLOBAL_BIN_DIR/ghai global launcher created"

# 4. PATH Check & Recommendation
if [[ ":$PATH:" != *":$GLOBAL_BIN_DIR:"* ]]; then
    echo "⚠️  [ghai] Action Required: Add '$GLOBAL_BIN_DIR' to your PATH to use 'ghai' anywhere!"
    echo "   Run this command: echo 'export PATH=\"\$HOME/.gha/bin:\$PATH\"' >> ~/.bashrc && source ~/.bashrc"
    echo "   (Or update your .zshrc if using Zsh)"
fi

echo "🎉 Global gha is ready! Type 'ghai :version' to verify."
