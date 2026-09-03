#!/usr/bin/env bash
# 🚀 gha: 0-Effort Universal Installer
set -e

mkdir -p init gradle/wrapper

# 1. Scaffold minimal settings.gradle.kts if no settings file exists
if [ ! -f "settings.gradle.kts" ] && [ ! -f "settings.gradle" ]; then
    PROJECT_NAME="$(basename "$PWD")"
    echo "📥 Creating settings.gradle.kts (rootProject.name = \"$PROJECT_NAME\")..."
    echo "rootProject.name = \"$PROJECT_NAME\"" > settings.gradle.kts
fi

# 2. Scaffold minimal build.gradle.kts if no build file exists
if [ ! -f "build.gradle.kts" ] && [ ! -f "build.gradle" ]; then
    echo "📥 Creating build.gradle.kts..."
    cat << 'EOF' > build.gradle.kts
plugins {
    `kotlin-dsl`
}
EOF
fi

# 3. Download init script if missing
if [ ! -f "init/gha.init.gradle.kts" ]; then
    curl -sSL "https://raw.githubusercontent.com/intellibitz/gha/main/init/gha.init.gradle.kts" -o "init/gha.init.gradle.kts" 2>/dev/null || true
fi

# 4. Bootstrap Gradle wrapper if missing in current folder
if [ ! -f "gradlew" ]; then
    echo "📥 Bootstrapping Gradle wrapper (gradlew)..."
    curl -sSL "https://raw.githubusercontent.com/intellibitz/gha/main/gradlew" -o "gradlew" 2>/dev/null || true
    curl -sSL "https://raw.githubusercontent.com/intellibitz/gha/main/gradlew.bat" -o "gradlew.bat" 2>/dev/null || true
    curl -sSL "https://raw.githubusercontent.com/intellibitz/gha/main/gradle/wrapper/gradle-wrapper.properties" -o "gradle/wrapper/gradle-wrapper.properties" 2>/dev/null || true
    curl -sSL "https://raw.githubusercontent.com/intellibitz/gha/main/gradle/wrapper/gradle-wrapper.jar" -o "gradle/wrapper/gradle-wrapper.jar" 2>/dev/null || true
fi

chmod +x gradlew 2>/dev/null || true

# 5. Create ./ghai executable wrapper if missing
if [ ! -f "ghai" ]; then
    curl -sSL "https://raw.githubusercontent.com/intellibitz/gha/main/ghai" -o "ghai" 2>/dev/null || true
    chmod +x ghai 2>/dev/null || true
fi

# 6. Delegate to gradlew
exec ./gradlew -Dgradle.user.home=.gha/gradle-user-home --init-script init/gha.init.gradle.kts ghaInit "$@"
