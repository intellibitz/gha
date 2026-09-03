#!/usr/bin/env bash
# 🚀 gha: 0-Effort Universal Installer
set -e

mkdir -p init gradle/wrapper

# 1. Scaffold or repair settings.gradle.kts with Foojay Toolchain Resolver
if [ ! -f "settings.gradle.kts" ] && [ ! -f "settings.gradle" ]; then
    PROJECT_NAME="$(basename "$PWD")"
    echo "📥 Creating settings.gradle.kts with Foojay Toolchain Resolver..."
    cat << 'EOF' > settings.gradle.kts
pluginManagement {
    repositories {
        mavenLocal()
        mavenCentral()
        gradlePluginPortal()
    }
}
plugins {
    id("org.gradle.toolchains.foojay-resolver-convention") version "1.0.0"
}
EOF
    echo "rootProject.name = \"$PROJECT_NAME\"" >> settings.gradle.kts
elif [ -f "settings.gradle.kts" ] && ! grep -q "foojay-resolver-convention" settings.gradle.kts; then
    echo "📥 Injecting Foojay Toolchain Resolver into settings.gradle.kts..."
    cat << 'EOF' > settings.gradle.kts.tmp
pluginManagement {
    repositories {
        mavenLocal()
        mavenCentral()
        gradlePluginPortal()
    }
}
plugins {
    id("org.gradle.toolchains.foojay-resolver-convention") version "1.0.0"
}
EOF
    cat settings.gradle.kts >> settings.gradle.kts.tmp
    mv settings.gradle.kts.tmp settings.gradle.kts
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

# 3. Download or refresh init script
curl -sSL "https://raw.githubusercontent.com/intellibitz/gha/main/init/gha.init.gradle.kts" -o "init/gha.init.gradle.kts" 2>/dev/null || true

# 4. Bootstrap Gradle wrapper if missing in current folder
if [ ! -f "gradlew" ]; then
    echo "📥 Bootstrapping Gradle wrapper (gradlew)..."
    curl -sSL "https://raw.githubusercontent.com/intellibitz/gha/main/gradlew" -o "gradlew" 2>/dev/null || true
    curl -sSL "https://raw.githubusercontent.com/intellibitz/gha/main/gradlew.bat" -o "gradlew.bat" 2>/dev/null || true
    curl -sSL "https://raw.githubusercontent.com/intellibitz/gha/main/gradle/wrapper/gradle-wrapper.properties" -o "gradle/wrapper/gradle-wrapper.properties" 2>/dev/null || true
    curl -sSL "https://raw.githubusercontent.com/intellibitz/gha/main/gradle/wrapper/gradle-wrapper.jar" -o "gradle/wrapper/gradle-wrapper.jar" 2>/dev/null || true
fi

chmod +x gradlew 2>/dev/null || true

# 5. Fetch and update ./ghai executable wrapper with latest handler
echo "📥 Fetching latest ghai launcher script..."
curl -sSL "https://raw.githubusercontent.com/intellibitz/gha/main/ghai" -o "ghai" 2>/dev/null || true
curl -sSL "https://raw.githubusercontent.com/intellibitz/gha/main/ghai.bat" -o "ghai.bat" 2>/dev/null || true
chmod +x ghai 2>/dev/null || true

# 6. Delegate to gradlew
exec ./gradlew -Dgradle.user.home=.gha/gradle-user-home --init-script init/gha.init.gradle.kts ghaInit "$@"
