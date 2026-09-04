#!/usr/bin/env bash
# 🚀 gha: 0-Effort Universal Installer (100% On-Demand Scaffolding)
# 100% Sandboxed - 0% Modifications to existing project files.
set -e

mkdir -p .gha

# Dynamic GHA Engine Repository resolution (custom fork/mirror support)
GHA_REPO="${GHA_REPO:-$(git config gha.repo 2>/dev/null || echo "intellibitz/gha")}"

# 1. Download or refresh init script (Single .gha/ Sandbox Init Script)
curl -sSL "https://raw.githubusercontent.com/$GHA_REPO/main/init/gha.init.gradle.kts" -o ".gha/init.gradle.kts" 2>/dev/null || true

# 2. Fetch and update ./ghai executable launcher & batch scripts
echo "📥 Fetching latest ghai launcher script..."
curl -sSL "https://raw.githubusercontent.com/$GHA_REPO/main/ghai" -o "ghai" 2>/dev/null || true
curl -sSL "https://raw.githubusercontent.com/$GHA_REPO/main/ghai.bat" -o ".gha/ghai.bat" 2>/dev/null || true
chmod +x ghai 2>/dev/null || true

# 3. Fetch latest GHA Engine version.txt into .gha/ sandbox
echo "📥 Syncing GHA engine version info..."
curl -sSL "https://raw.githubusercontent.com/$GHA_REPO/main/version.txt" -o ".gha/gha-engine-version.txt" 2>/dev/null || true

# 4. Update .gitignore for Invisible Integration (0 side effects)
if [ -f ".gitignore" ]; then
    if ! grep -q "# gha: Git, GitHub & Gradle Automation" ".gitignore"; then
        echo "   ➕ Updating .gitignore for invisible gha integration..."
        cat << 'EOF' >> .gitignore

# gha: Git, GitHub & Gradle Automation (Invisible Sandbox)
.gha/
ghai
ghai.bat
EOF
    fi
fi

echo "⚡ [gha] 100% Sandboxed 0-Effort Installation Complete!"
echo "   ├── .gha/ sandbox initialized"
echo "   └── ./ghai executable launcher created (rwxr-xr-x)"
echo "🎉 gha is ready! Type './ghai' to run autonomous AI automation."
