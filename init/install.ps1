# 🚀 gha: Bare minimum 100% Kotlin pass-through installer
# Zero PowerShell logic to prevent platform dependency traps.
New-Item -ItemType Directory -Force -Path "init" | Out-Null
Invoke-WebRequest -Uri "https://raw.githubusercontent.com/intellibitz/gha/main/init/gha.init.gradle.kts" -OutFile "init\gha.init.gradle.kts" -ErrorAction SilentlyContinue
.\gradlew.bat -Dgradle.user.home=.gha/gradle-user-home --init-script init/gha.init.gradle.kts ghaInit $args
