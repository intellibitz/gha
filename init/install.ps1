# 🚀 gha: 0-Effort Universal Installer (PowerShell)
$ErrorActionPreference = "Stop"

if (-not (Test-Path "init")) { New-Item -ItemType Directory -Force -Path "init" | Out-Null }
if (-not (Test-Path "gradle\wrapper")) { New-Item -ItemType Directory -Force -Path "gradle\wrapper" | Out-Null }

# 1. Scaffold or repair settings.gradle.kts with Foojay Toolchain Resolver
if ((-not (Test-Path "settings.gradle.kts")) -and (-not (Test-Path "settings.gradle"))) {
    $folderName = Split-Path -Path $PWD -Leaf
    Write-Host "📥 Creating settings.gradle.kts (rootProject.name = '$folderName')..." -ForegroundColor Cyan
    @'
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
'@ + "`nrootProject.name = `"$folderName`"" | Out-File -Encoding utf8 "settings.gradle.kts"
} elseif ((Test-Path "settings.gradle.kts") -and (-not (Select-String -Path "settings.gradle.kts" -Pattern "foojay-resolver-convention" -Quiet))) {
    Write-Host "📥 Injecting Foojay Toolchain Resolver into settings.gradle.kts..." -ForegroundColor Cyan
    $existing = Get-Content "settings.gradle.kts" -Raw
    @'
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
'@ + "`n" + $existing | Out-File -Encoding utf8 "settings.gradle.kts"
}

# 2. Scaffold minimal build.gradle.kts if missing
if ((-not (Test-Path "build.gradle.kts")) -and (-not (Test-Path "build.gradle"))) {
    Write-Host "📥 Creating build.gradle.kts..." -ForegroundColor Cyan
    @'
plugins {
    `kotlin-dsl`
}
'@ | Out-File -Encoding utf8 "build.gradle.kts"
}

# 3. Download or refresh init script
Invoke-WebRequest -Uri "https://raw.githubusercontent.com/intellibitz/gha/main/init/gha.init.gradle.kts" -OutFile "init\gha.init.gradle.kts" -ErrorAction SilentlyContinue

# 4. Bootstrap Gradle wrapper if missing
if (-not (Test-Path ".\gradlew.bat")) {
    Write-Host "📥 Bootstrapping Gradle wrapper..." -ForegroundColor Cyan
    Invoke-WebRequest -Uri "https://raw.githubusercontent.com/intellibitz/gha/main/gradlew" -OutFile "gradlew" -ErrorAction SilentlyContinue
    Invoke-WebRequest -Uri "https://raw.githubusercontent.com/intellibitz/gha/main/gradlew.bat" -OutFile "gradlew.bat" -ErrorAction SilentlyContinue
    Invoke-WebRequest -Uri "https://raw.githubusercontent.com/intellibitz/gha/main/gradle/wrapper/gradle-wrapper.properties" -OutFile "gradle\wrapper\gradle-wrapper.properties" -ErrorAction SilentlyContinue
    Invoke-WebRequest -Uri "https://raw.githubusercontent.com/intellibitz/gha/main/gradle/wrapper/gradle-wrapper.jar" -OutFile "gradle\wrapper\gradle-wrapper.jar" -ErrorAction SilentlyContinue
}

# 5. Fetch latest ghai.bat wrapper
Write-Host "📥 Fetching latest ghai.bat launcher script..." -ForegroundColor Cyan
Invoke-WebRequest -Uri "https://raw.githubusercontent.com/intellibitz/gha/main/ghai.bat" -OutFile "ghai.bat" -ErrorAction SilentlyContinue

.\gradlew.bat -Dgradle.user.home=.gha/gradle-user-home --init-script init/gha.init.gradle.kts ghaInit $args
