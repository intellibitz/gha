#!/usr/bin/env bash
# gha installer - Lightning Fast Intelligence Substrate Onboarding
set -e

GLOBAL_GHA_DIR="$HOME/.gha"
GLOBAL_BIN_DIR="$GLOBAL_GHA_DIR/bin"
mkdir -p "$GLOBAL_BIN_DIR"
mkdir -p "$GLOBAL_GHA_DIR/models"

GHA_REPO="${GHA_REPO:-intellibitz/gha}"

echo "Initializing gha environment (Repo: $GHA_REPO)..."

# 1. Detect Environment
OS_TYPE="$(uname -s | tr '[:upper:]' '[:lower:]')"
ARCH_TYPE="$(uname -m)"

case "$OS_TYPE" in
    linux*)  PLATFORM="linux" ;;
    darwin*) PLATFORM="macos" ;;
    *)       PLATFORM="unknown" ;;
esac

case "$ARCH_TYPE" in
    x86_64) ARCH="x86_64" ;;
    aarch64|arm64) ARCH="aarch64" ;;
    *)      ARCH="unknown" ;;
esac

INSTALLED=0

# 2. Try Binary Download First (Lightning Fast)
if [[ "$PLATFORM" != "unknown" && "$ARCH" != "unknown" ]]; then
    BINARY_NAME="gha-$PLATFORM-$ARCH"
    DOWNLOAD_URL="https://github.com/$GHA_REPO/releases/latest/download/$BINARY_NAME"

    echo "Attempting to download pre-compiled binary: $BINARY_NAME..."

    if command -v curl >/dev/null 2>&1; then
        if curl -sSfL "$DOWNLOAD_URL" -o "$GLOBAL_BIN_DIR/gha-engine-new"; then
            pkill -f gha || true
            rm -f "$GLOBAL_BIN_DIR/gha-engine" "$GLOBAL_BIN_DIR/gha" 2>/dev/null || true
            mv "$GLOBAL_BIN_DIR/gha-engine-new" "$GLOBAL_BIN_DIR/gha-engine"
            cp "$GLOBAL_BIN_DIR/gha-engine" "$GLOBAL_BIN_DIR/gha"
            chmod +x "$GLOBAL_BIN_DIR/gha-engine" "$GLOBAL_BIN_DIR/gha"
            INSTALLED=1
            echo "Successfully deployed binary from GitHub ($GHA_REPO)."
        fi
    elif command -v wget >/dev/null 2>&1; then
        if wget -q "$DOWNLOAD_URL" -O "$GLOBAL_BIN_DIR/gha-engine-new"; then
            pkill -f gha || true
            rm -f "$GLOBAL_BIN_DIR/gha-engine" "$GLOBAL_BIN_DIR/gha" 2>/dev/null || true
            mv "$GLOBAL_BIN_DIR/gha-engine-new" "$GLOBAL_BIN_DIR/gha-engine"
            cp "$GLOBAL_BIN_DIR/gha-engine" "$GLOBAL_BIN_DIR/gha"
            chmod +x "$GLOBAL_BIN_DIR/gha-engine" "$GLOBAL_BIN_DIR/gha"
            INSTALLED=1
            echo "Successfully deployed binary from GitHub ($GHA_REPO)."
        fi
    fi
fi

# 3. Fallback to Local Source or Clone & Build
if [ "$INSTALLED" = "0" ]; then
    echo "Binary download unavailable or failed. Falling back to build from source..."

    SCRIPT_DIR_DETECT="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"
    if [[ -f "$SCRIPT_DIR_DETECT/Cargo.toml" ]]; then
        SCRIPT_DIR="$SCRIPT_DIR_DETECT"
        echo "Using local source directory..."
    else
        echo "Downloading gha source archive ($GHA_REPO)..."
        TEMP_DIR=$(mktemp -d)
        SOURCE_URL="https://github.com/$GHA_REPO/archive/refs/heads/main.tar.gz"
        if command -v curl >/dev/null 2>&1 && command -v tar >/dev/null 2>&1; then
            curl -sSfL "$SOURCE_URL" | tar -xzC "$TEMP_DIR" --strip-components=1 || { echo "Source download failed."; exit 1; }
            SCRIPT_DIR="$TEMP_DIR"
        elif command -v wget >/dev/null 2>&1 && command -v tar >/dev/null 2>&1; then
            wget -qO- "$SOURCE_URL" | tar -xzC "$TEMP_DIR" --strip-components=1 || { echo "Source download failed."; exit 1; }
            SCRIPT_DIR="$TEMP_DIR"
        else
            echo "Error: 'tar' and either 'curl' or 'wget' are required for source fallback."
            exit 1
        fi
    fi

    if command -v cargo >/dev/null 2>&1 && [ -f "$SCRIPT_DIR/Cargo.toml" ]; then
        echo "Building release binary (this may take a moment)..."

        # 100% GPU Hardware Interrogation Build Strategy
        BUILD_FEATURES=""
        if [[ "$PLATFORM" == "macos" ]]; then
            BUILD_FEATURES="--features metal"
        elif command -v nvcc >/dev/null 2>&1 || [ -d "/usr/local/cuda" ]; then
            # Check for cudarc support (currently fails on CUDA 13.3)
            CUDA_VERSION=$(nvcc --version | grep "release" | sed 's/.*release //;s/,.*//')
            if [[ "$CUDA_VERSION" == "11."* ]] || [[ "$CUDA_VERSION" == "12."* ]]; then
                BUILD_FEATURES="--features cuda"
            fi
        fi

        (cd "$SCRIPT_DIR" && cargo build --release $BUILD_FEATURES >/dev/null 2>&1)
        (cd "$SCRIPT_DIR/src/native/gha" && cargo build --release >/dev/null 2>&1)

        if [ -f "$SCRIPT_DIR/target/release/gha" ] && [ -f "$SCRIPT_DIR/src/native/gha/target/release/gha" ]; then
            pkill -f gha || true
            rm -f "$GLOBAL_BIN_DIR/gha-engine" "$GLOBAL_BIN_DIR/gha" 2>/dev/null || true
            cp "$SCRIPT_DIR/target/release/gha" "$GLOBAL_BIN_DIR/gha-engine"
            cp "$SCRIPT_DIR/src/native/gha/target/release/gha" "$GLOBAL_BIN_DIR/gha"
            chmod +x "$GLOBAL_BIN_DIR/gha-engine" "$GLOBAL_BIN_DIR/gha"
            INSTALLED=1
            echo "Deployed engine and launcher binaries to $GLOBAL_BIN_DIR"
        fi
    fi
fi

if [ "$INSTALLED" = "0" ]; then
    echo "Error: Installation failed. Ensure 'cargo' or 'curl' is available and you have internet access."
    exit 1
fi

# 4. Engine Initialization
if [ -x "$GLOBAL_BIN_DIR/gha" ]; then
    "$GLOBAL_BIN_DIR/gha" install >/dev/null 2>&1 || true
fi

# 5. PATH Management
if [[ ":$PATH:" != *":$GLOBAL_BIN_DIR:"* ]]; then
    CONFIG_FILES=("$HOME/.bashrc" "$HOME/.zshrc" "$HOME/.profile")
    for config in "${CONFIG_FILES[@]}"; do
        if [ -f "$config" ] && ! grep -q ".gha/bin" "$config"; then
            echo -e "\n# gha path initialization\nexport PATH=\"\$HOME/.gha/bin:\$PATH\"" >> "$config"
        fi
    done

    FISH_CONFIG="$HOME/.config/fish/config.fish"
    if [ -d "$HOME/.config/fish" ] || command -v fish >/dev/null 2>&1; then
        mkdir -p "$HOME/.config/fish"
        if [ -f "$FISH_CONFIG" ] && ! grep -q ".gha/bin" "$FISH_CONFIG"; then
            echo -e "\n# gha path initialization\nfish_add_path \$HOME/.gha/bin" >> "$FISH_CONFIG"
        elif [ ! -f "$FISH_CONFIG" ]; then
            echo -e "fish_add_path \$HOME/.gha/bin" > "$FISH_CONFIG"
        fi
        if command -v fish >/dev/null 2>&1; then
            fish -c "fish_add_path $GLOBAL_BIN_DIR" >/dev/null 2>&1 || true
        fi
    fi
fi

# 6. Finalize
if [ -t 0 ] && [ -t 1 ] && [ -z "$NONINTERACTIVE" ] && [ -x "$GLOBAL_BIN_DIR/gha" ]; then
    echo "Installation complete. Starting interactive gha session..."
    echo ""
    exec "$GLOBAL_BIN_DIR/gha"
else
    echo "Installation complete. Run 'gha' to start."
fi
