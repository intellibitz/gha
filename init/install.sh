#!/usr/bin/env bash
# 🚀 gha: Bare minimum 100% Kotlin pass-through installer
# Zero shell logic to prevent platform dependency traps.
mkdir -p init
curl -sSL "https://raw.githubusercontent.com/intellibitz/gha/main/init/gha.init.gradle.kts" -o "init/gha.init.gradle.kts" 2>/dev/null || true
chmod +x gradlew 2>/dev/null || true
exec ./gradlew -Dgradle.user.home=.gha/gradle-user-home --init-script init/gha.init.gradle.kts ghaInit "$@"
