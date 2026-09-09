#!/usr/bin/env bash
# gha installer - Lightning Fast Intelligence Substrate Onboarding
set -e

GLOBAL_GHA_DIR="$HOME/.gha"
GLOBAL_BIN_DIR="$GLOBAL_GHA_DIR/bin"
mkdir -p "$GLOBAL_BIN_DIR"
mkdir -p "$GLOBAL_GHA_DIR/models"

GHA_REPO="${GHA_REPO:-intellibitz/gha}"

# Try to detect repo from git if available
if command -v git >/dev/null 2>&1 && [ -d ".git" ]; then
    GIT_REMOTE=$(git remote get-url origin 2>/dev/null || true)
    if [[ "$GIT_REMOTE" == *"github.com"* ]]; then
        # Extract owner/repo from https://github.com/owner/repo.git or git@github.com:owner/repo.git
        DETECTED_REPO=$(echo "$GIT_REMOTE" | sed -E 's/.*github\.com[:\/](.*)\.git/\1/' | sed -E 's/.*github\.com[:\/](.*)/\1/')
        if [[ -n "$DETECTED_REPO" ]]; then
            GHA_REPO="$DETECTED_REPO"
        fi
    fi
fi

echo "Initializing gha environment (Repo: $GHA_REPO)..."

# 1. Detect Environment
OS_TYPE="$(uname -s | tr '[:upper:]' '[:lower:]')"
ARCH_TYPE="$(uname -m)"

case "$OS_TYPE" in
    linux*)  PLATFORM="linux" ;;
    darwin*) PLATFORM="macos" ;;
    mingw*|cygwin*|msys*) PLATFORM="windows" ;;
    *)       PLATFORM="unknown" ;;
esac

case "$ARCH_TYPE" in
    x86_64|amd64) ARCH="x86_64" ;;
    aarch64|arm64) ARCH="aarch64" ;;
    *)      ARCH="unknown" ;;
esac

INSTALLED=0

# 2. Try Binary Download First (Lightning Fast)
if [[ "$PLATFORM" != "unknown" && "$ARCH" != "unknown" ]]; then
    # Try downloading both launcher and engine
    LAUNCHER_BINARY="gha-$PLATFORM-$ARCH"
    ENGINE_BINARY="gha-engine-$PLATFORM-$ARCH"

    if [[ "$PLATFORM" == "windows" ]]; then
        LAUNCHER_BINARY="${LAUNCHER_BINARY}.exe"
        ENGINE_BINARY="${ENGINE_BINARY}.exe"
    fi

    BASE_URL="https://github.com/$GHA_REPO/releases/latest/download"

    echo "Attempting to download pre-compiled binaries from $GHA_REPO..."

    DEPLOYED=0
    if command -v curl >/dev/null 2>&1; then
        # Download Launcher
        echo "  [1/2] Downloading launcher: $LAUNCHER_BINARY..."
        if curl -sSfL "$BASE_URL/$LAUNCHER_BINARY" -o "$GLOBAL_BIN_DIR/gha-new"; then
            # Download Engine
            echo "  [2/2] Downloading engine: $ENGINE_BINARY..."
            if curl -sSfL "$BASE_URL/$ENGINE_BINARY" -o "$GLOBAL_BIN_DIR/gha-engine-new"; then
                DEPLOYED=1
            fi
        fi
    elif command -v wget >/dev/null 2>&1; then
        # Download Launcher
        echo "  [1/2] Downloading launcher: $LAUNCHER_BINARY..."
        if wget -q "$BASE_URL/$LAUNCHER_BINARY" -O "$GLOBAL_BIN_DIR/gha-new"; then
            # Download Engine
            echo "  [2/2] Downloading engine: $ENGINE_BINARY..."
            if wget -q "$BASE_URL/$ENGINE_BINARY" -O "$GLOBAL_BIN_DIR/gha-engine-new"; then
                DEPLOYED=1
            fi
        fi
    fi

    if [ "$DEPLOYED" = "1" ]; then
        pkill -f gha || true

        BIN_EXE=""
        ENGINE_EXE="-engine"
        if [[ "$PLATFORM" == "windows" ]]; then
            BIN_EXE=".exe"
            ENGINE_EXE="-engine.exe"
        fi

        rm -f "$GLOBAL_BIN_DIR/gha${BIN_EXE}" "$GLOBAL_BIN_DIR/gha${ENGINE_EXE}" 2>/dev/null || true
        mv "$GLOBAL_BIN_DIR/gha-new" "$GLOBAL_BIN_DIR/gha${BIN_EXE}"
        mv "$GLOBAL_BIN_DIR/gha-engine-new" "$GLOBAL_BIN_DIR/gha${ENGINE_EXE}"
        chmod +x "$GLOBAL_BIN_DIR/gha${BIN_EXE}" "$GLOBAL_BIN_DIR/gha${ENGINE_EXE}"
        INSTALLED=1
        echo "Successfully deployed binaries from GitHub ($GHA_REPO)."
    else
        echo "  Binary download unavailable or failed. Falling back to build."
        rm -f "$GLOBAL_BIN_DIR/gha-new" "$GLOBAL_BIN_DIR/gha-engine-new" 2>/dev/null || true
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
        echo "Building release binaries (this may take a moment)..."

        # 100% GPU Hardware Interrogation Build Strategy
        BUILD_FEATURES=""
        if [[ "$PLATFORM" == "macos" ]]; then
            BUILD_FEATURES="--features metal"
        elif command -v nvcc >/dev/null 2>&1 || [ -d "/usr/local/cuda" ]; then
            CUDA_VERSION=$(nvcc --version 2>/dev/null | grep "release" | sed 's/.*release //;s/,.*//' || echo "0")
            if [[ "$CUDA_VERSION" == "11."* ]] || [[ "$CUDA_VERSION" == "12."* ]]; then
                BUILD_FEATURES="--features cuda"
            fi
        fi

        # Build Engine
        echo "  Building engine..."
        (cd "$SCRIPT_DIR" && cargo build --release $BUILD_FEATURES >/dev/null 2>&1)
        # Build Launcher
        echo "  Building launcher..."
        (cd "$SCRIPT_DIR/src/native/gha" && cargo build --release >/dev/null 2>&1)

        ENGINE_SRC="$SCRIPT_DIR/target/release/gha"
        LAUNCHER_SRC="$SCRIPT_DIR/src/native/gha/target/release/gha"

        if [[ "$PLATFORM" == "windows" ]]; then
            ENGINE_SRC="${ENGINE_SRC}.exe"
            LAUNCHER_SRC="${LAUNCHER_SRC}.exe"
        fi

        if [ -f "$ENGINE_SRC" ] && [ -f "$LAUNCHER_SRC" ]; then
            pkill -f gha || true
            rm -f "$GLOBAL_BIN_DIR/gha-engine" "$GLOBAL_BIN_DIR/gha" 2>/dev/null || true
            cp "$ENGINE_SRC" "$GLOBAL_BIN_DIR/gha-engine"
            cp "$LAUNCHER_SRC" "$GLOBAL_BIN_DIR/gha"
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

# 5. Model Substrate Check (Optional but recommended for offline mode)
MODEL_DIR="$GLOBAL_GHA_DIR/models"
ALPHA_MODEL="$MODEL_DIR/gha-alpha.safetensors"
if [ ! -f "$ALPHA_MODEL" ]; then
    echo "Intelligence substrate (gha-alpha.safetensors) missing."
    if [ -z "$NONINTERACTIVE" ]; then
        read -p "Do you want to download the native Tier 0 reflex weights (~150MB)? [Y/n] " -n 1 -r
        echo
        if [[ $REPLY =~ ^[Yy]$ ]] || [[ -z $REPLY ]]; then
            echo "Downloading gha-alpha intelligence substrate..."
            # Placeholder URL - update with actual weights when published
            # curl -sSfL "https://huggingface.co/intellibitz/gha-alpha/resolve/main/gha-alpha.safetensors" -o "$ALPHA_MODEL"
            echo "Note: Native reflex weights can be downloaded later using '/scout_model gha-alpha'."
        fi
    fi
fi

# 6. PATH Management
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
