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

# 4. PATH Automation (0-Effort Onboarding)
if [[ ":$PATH:" != *":$GLOBAL_BIN_DIR:"* ]]; then
    echo "⚡ [ghai] Automatically adding '$GLOBAL_BIN_DIR' to PATH..."
    EXPORT_CMD="export PATH=\"\$HOME/.gha/bin:\$PATH\""

    # Target common shell config files
    CONFIG_FILES=("$HOME/.bashrc" "$HOME/.zshrc" "$HOME/.profile" "$HOME/.bash_profile")

    ADDED=0
    for config in "${CONFIG_FILES[@]}"; do
        if [ -f "$config" ]; then
            if ! grep -q ".gha/bin" "$config"; then
                echo "" >> "$config"
                echo "# gha: Global Master Agent Launcher" >> "$config"
                echo "$EXPORT_CMD" >> "$config"
                ADDED=1
                echo "   ✅ Added to $config"
            fi
        fi
    done

    if [ "$ADDED" = "1" ]; then
        echo "🎉 PATH updated! Please restart your terminal or run: source ~/.bashrc (or your shell's config)"
    else
        echo "ℹ️  GHA bin already present in shell configs or PATH."
    fi
else
    echo "✅ [ghai] Global launcher already in PATH."
fi

echo "🎉 Global gha is ready! Type 'ghai :version' to verify."
