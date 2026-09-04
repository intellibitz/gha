@echo off
rem 🤖 ghai - Autonomous AI Workflow Executable Launcher for Windows
rem 100% Sandboxed - 0% Modifications to existing project files.

set "CMD=%~1"
if defined CMD if "%CMD:~0,1%"==":" set "CMD=%CMD:~1%"

if /i "%CMD%"=="version" (
    .\gradlew.bat --refresh-dependencies -Dgradle.user.home=.gha/gradle-user-home --init-script init/gha.init.gradle.kts ghai -Pmessage="--version"
    exit /b 0
)

if /i "%1"=="--version" (
    .\gradlew.bat --refresh-dependencies -Dgradle.user.home=.gha/gradle-user-home --init-script init/gha.init.gradle.kts ghai -Pmessage="--version"
    exit /b 0
)

if /i "%1"=="-v" (
    .\gradlew.bat --refresh-dependencies -Dgradle.user.home=.gha/gradle-user-home --init-script init/gha.init.gradle.kts ghai -Pmessage="--version"
    exit /b 0
)

if /i "%CMD%"=="clone" (
    .\gradlew.bat --refresh-dependencies -Dgradle.user.home=.gha/gradle-user-home --init-script init/gha.init.gradle.kts ghaGitClone -PtargetRepo="%2" -Pdir="%3"
    exit /b 0
)

if /i "%CMD%"=="status" (
    .\gradlew.bat --refresh-dependencies -Dgradle.user.home=.gha/gradle-user-home --init-script init/gha.init.gradle.kts ghaStatus
    exit /b 0
)

if /i "%CMD%"=="help" (
    .\gradlew.bat --refresh-dependencies -Dgradle.user.home=.gha/gradle-user-home --init-script init/gha.init.gradle.kts ghaHelp
    exit /b 0
)

if /i "%CMD%"=="update" (
    echo 🚀 [ghai Update] Fetching and updating gha to latest version...
    powershell -Command "iwr -useb https://raw.githubusercontent.com/intellibitz/gha/main/init/install.ps1 | iex"
    echo 🎉 [ghai Update] Updated gha & ghai to latest version successfully!
    exit /b 0
)

if /i "%CMD%"=="uninstall" (
    echo 🧹 [ghai Uninstall] Completely removing gha sandbox, runner scripts, and workflows...
    .\gradlew.bat -Dgradle.user.home=.gha/gradle-user-home --init-script init/gha.init.gradle.kts ghaUninstall
    echo ✨ [ghai Uninstall] gha removed completely with 0 lingering system modifications!
    exit /b 0
)

if not exist "init\gha.init.gradle.kts" (
    echo ⚡ [ghai] Initializing gha sandbox...
    if not exist "init" mkdir init
    powershell -Command "Invoke-WebRequest -Uri https://raw.githubusercontent.com/intellibitz/gha/main/init/gha.init.gradle.kts -OutFile init\gha.init.gradle.kts"
    .\gradlew.bat --refresh-dependencies -Dgradle.user.home=.gha/gradle-user-home --init-script init\gha.init.gradle.kts ghaInit
)

.\gradlew.bat --refresh-dependencies -Dgradle.user.home=.gha/gradle-user-home --init-script init/gha.init.gradle.kts ghai %*
