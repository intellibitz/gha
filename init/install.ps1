# 🚀 gha: 0-Effort Universal Installer (PowerShell)
$ErrorActionPreference = "Stop"

if (-not (Test-Path "init")) { New-Item -ItemType Directory -Force -Path "init" | Out-Null }
if (-not (Test-Path "gradle\wrapper")) { New-Item -ItemType Directory -Force -Path "gradle\wrapper" | Out-Null }

if (-not (Test-Path "init\gha.init.gradle.kts")) {
    Invoke-WebRequest -Uri "https://raw.githubusercontent.com/intellibitz/gha/main/init/gha.init.gradle.kts" -OutFile "init\gha.init.gradle.kts" -ErrorAction SilentlyContinue
}

if (-not (Test-Path ".\gradlew.bat")) {
    Write-Host "📥 Bootstrapping Gradle wrapper..." -ForegroundColor Cyan
    Invoke-WebRequest -Uri "https://raw.githubusercontent.com/intellibitz/gha/main/gradlew" -OutFile "gradlew" -ErrorAction SilentlyContinue
    Invoke-WebRequest -Uri "https://raw.githubusercontent.com/intellibitz/gha/main/gradlew.bat" -OutFile "gradlew.bat" -ErrorAction SilentlyContinue
    Invoke-WebRequest -Uri "https://raw.githubusercontent.com/intellibitz/gha/main/gradle/wrapper/gradle-wrapper.properties" -OutFile "gradle\wrapper\gradle-wrapper.properties" -ErrorAction SilentlyContinue
    Invoke-WebRequest -Uri "https://raw.githubusercontent.com/intellibitz/gha/main/gradle/wrapper/gradle-wrapper.jar" -OutFile "gradle\wrapper\gradle-wrapper.jar" -ErrorAction SilentlyContinue
}

if (-not (Test-Path ".\ghai.bat")) {
    Invoke-WebRequest -Uri "https://raw.githubusercontent.com/intellibitz/gha/main/ghai.bat" -OutFile "ghai.bat" -ErrorAction SilentlyContinue
}

.\gradlew.bat -Dgradle.user.home=.gha/gradle-user-home --init-script init/gha.init.gradle.kts ghaInit $args
