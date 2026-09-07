#!/usr/bin/env bash
# gha installer
set -e

GLOBAL_GHA_DIR="$HOME/.gha"
GLOBAL_BIN_DIR="$GLOBAL_GHA_DIR/bin"
mkdir -p "$GLOBAL_BIN_DIR"
mkdir -p "$GLOBAL_GHA_DIR/models"

echo "Initializing gha environment..."

SCRIPT_DIR_DETECT="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"
if [[ -f "$SCRIPT_DIR_DETECT/Cargo.toml" ]]; then
    SCRIPT_DIR="$SCRIPT_DIR_DETECT"
    echo "Using local source directory..."
else
    echo "Cloning gha repository..."
    TEMP_DIR=$(mktemp -d)
    git clone --depth 1 https://github.com/intellibitz/gha.git "$TEMP_DIR" >/dev/null 2>&1 || { echo "Git clone failed."; exit 1; }
    SCRIPT_DIR="$TEMP_DIR"
fi

INSTALLED=0
rm -f "$GLOBAL_BIN_DIR/gha" "$GLOBAL_BIN_DIR/gha-engine" 2>/dev/null || true

if command -v cargo >/dev/null 2>&1 && [ -f "$SCRIPT_DIR/Cargo.toml" ]; then
    echo "Building release binary..."
    (cd "$SCRIPT_DIR" && cargo build --release >/dev/null 2>&1)
    if [ -f "$SCRIPT_DIR/target/release/gha" ]; then
        cp "$SCRIPT_DIR/target/release/gha" "$GLOBAL_BIN_DIR/gha-engine"
        cp "$SCRIPT_DIR/target/release/gha" "$GLOBAL_BIN_DIR/gha"
        chmod +x "$GLOBAL_BIN_DIR/gha-engine" "$GLOBAL_BIN_DIR/gha"
        INSTALLED=1
        echo "Deployed binary to $GLOBAL_BIN_DIR/gha"
    fi
elif [ -f "$SCRIPT_DIR/target/release/gha" ]; then
    cp "$SCRIPT_DIR/target/release/gha" "$GLOBAL_BIN_DIR/gha-engine"
    cp "$SCRIPT_DIR/target/release/gha" "$GLOBAL_BIN_DIR/gha"
    chmod +x "$GLOBAL_BIN_DIR/gha-engine" "$GLOBAL_BIN_DIR/gha"
    INSTALLED=1
    echo "Deployed pre-built binary to $GLOBAL_BIN_DIR/gha"
fi

if [ "$INSTALLED" = "0" ]; then
    echo "Error: Installation failed. Ensure 'cargo' is available."
    exit 1
fi

if [ -f "$SCRIPT_DIR/version.txt" ]; then
    cp "$SCRIPT_DIR/version.txt" "$GLOBAL_GHA_DIR/gha-engine-version.txt"
fi

if [ -x "$GLOBAL_BIN_DIR/gha" ]; then
    "$GLOBAL_BIN_DIR/gha" install >/dev/null 2>&1 || true
fi

if [[ ":$PATH:" != *":$GLOBAL_BIN_DIR:"* ]]; then
    CONFIG_FILES=("$HOME/.bashrc" "$HOME/.zshrc" "$HOME/.profile")
    for config in "${CONFIG_FILES[@]}"; do
        if [ -f "$config" ] && ! grep -q ".gha/bin" "$config"; then
            echo -e "\nexport PATH=\"\$HOME/.gha/bin:\$PATH\"" >> "$config"
        fi
    done

    FISH_CONFIG="$HOME/.config/fish/config.fish"
    if [ -d "$HOME/.config/fish" ] || command -v fish >/dev/null 2>&1; then
        mkdir -p "$HOME/.config/fish"
        if [ -f "$FISH_CONFIG" ] && ! grep -q ".gha/bin" "$FISH_CONFIG"; then
            echo -e "\nfish_add_path \$HOME/.gha/bin" >> "$FISH_CONFIG"
        elif [ ! -f "$FISH_CONFIG" ]; then
            echo -e "fish_add_path \$HOME/.gha/bin" > "$FISH_CONFIG"
        fi
        if command -v fish >/dev/null 2>&1; then
            fish -c "fish_add_path $GLOBAL_BIN_DIR" >/dev/null 2>&1 || true
        fi
    fi
fi

echo "Installation complete. Run 'gha' to start."
