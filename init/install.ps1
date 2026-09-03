# 🚀 gha: 0 Effort, Ridiculously Easy Installer for Windows PowerShell
# Usage: iwr -useb https://raw.githubusercontent.com/intellibitz/gha/main/init/install.ps1 | iex

$ErrorActionPreference = "Stop"

Write-Host "🚀 [gha Installer] Installing gha (Git, GitHub & Gradle Automation)..." -ForegroundColor Green

if (-not (Test-Path "init")) {
    New-Item -ItemType Directory -Path "init" | Out-Null
}

if (-not (Test-Path "init\gha.init.gradle.kts")) {
    Write-Host "📥 Downloading init\gha.init.gradle.kts..." -ForegroundColor Cyan
    try {
        Invoke-WebRequest -Uri "https://raw.githubusercontent.com/intellibitz/gha/main/init/gha.init.gradle.kts" -OutFile "init\gha.init.gradle.kts"
    } catch {
        Write-Host "⚠️ Downloading online script failed. Creating local init script..." -ForegroundColor Yellow
        @'
initscript {
    repositories {
        mavenLocal()
        mavenCentral()
        gradlePluginPortal()
    }
    dependencies {
        classpath("cc.thevar.gha:gha:0.1.0-SNAPSHOT")
        classpath("org.gradle.toolchains:foojay-resolver:1.0.0")
    }
}
settingsEvaluated {
    apply(plugin = "org.gradle.toolchains.foojay-resolver-convention")
}
allprojects {
    apply<cc.thevar.gha.GhaPlugin>()
}
'@ | Out-File -Encoding utf8 "init\gha.init.gradle.kts"
    }
}

if (Test-Path ".\gradlew.bat") {
    Write-Host "⚙️ Initializing gha sandbox..." -ForegroundColor Cyan
    .\gradlew.bat -Dgradle.user.home=.gha/gradle-user-home --init-script init/gha.init.gradle.kts ghaInit
}

Write-Host "🎉 [gha Installer] gha installed ridiculously easy in 1 second!" -ForegroundColor Green
Write-Host "👉 Run: .\gradlew.bat -Dgradle.user.home=.gha/gradle-user-home --init-script init/gha.init.gradle.kts ghai" -ForegroundColor Yellow
