#!/usr/bin/env bash
# 🚀 gha: 0-Effort Universal Installer
set -e

mkdir -p init gradle/wrapper

# 1. Download init script if missing
if [ ! -f "init/gha.init.gradle.kts" ]; then
    curl -sSL "https://raw.githubusercontent.com/intellibitz/gha/main/init/gha.init.gradle.kts" -o "init/gha.init.gradle.kts" 2>/dev/null || true
fi

# 2. Bootstrap Gradle wrapper if missing in current folder
if [ ! -f "gradlew" ]; then
    echo "📥 Bootstrapping Gradle wrapper (gradlew)..."
    curl -sSL "https://raw.githubusercontent.com/intellibitz/gha/main/gradlew" -o "gradlew" 2>/dev/null || true
    curl -sSL "https://raw.githubusercontent.com/intellibitz/gha/main/gradlew.bat" -o "gradlew.bat" 2>/dev/null || true
    curl -sSL "https://raw.githubusercontent.com/intellibitz/gha/main/gradle/wrapper/gradle-wrapper.properties" -o "gradle/wrapper/gradle-wrapper.properties" 2>/dev/null || true
    curl -sSL "https://raw.githubusercontent.com/intellibitz/gha/main/gradle/wrapper/gradle-wrapper.jar" -o "gradle/wrapper/gradle-wrapper.jar" 2>/dev/null || true
fi

chmod +x gradlew 2>/dev/null || true

# 3. Create ./ghai executable wrapper if missing
if [ ! -f "ghai" ]; then
    curl -sSL "https://raw.githubusercontent.com/intellibitz/gha/main/ghai" -o "ghai" 2>/dev/null || true
    chmod +x ghai 2>/dev/null || true
fi

# 4. Delegate to gradlew
exec ./gradlew -Dgradle.user.home=.gha/gradle-user-home --init-script init/gha.init.gradle.kts ghaInit "$@"
