@echo off
rem 🤖 ghai - Autonomous AI Workflow Executable Launcher for Windows

if "%1"=="--version" (
    .\gradlew.bat -Dgradle.user.home=.gha/gradle-user-home --init-script init/gha.init.gradle.kts ghai -Pmessage="--version"
    exit /b 0
)

if "%1"=="-v" (
    .\gradlew.bat -Dgradle.user.home=.gha/gradle-user-home --init-script init/gha.init.gradle.kts ghai -Pmessage="--version"
    exit /b 0
)

if "%1"=="version" (
    .\gradlew.bat -Dgradle.user.home=.gha/gradle-user-home --init-script init/gha.init.gradle.kts ghai -Pmessage="--version"
    exit /b 0
)

if "%1"=="clone" (
    .\gradlew.bat -Dgradle.user.home=.gha/gradle-user-home --init-script init/gha.init.gradle.kts ghaGitClone -PtargetRepo="%2" -Pdir="%3"
    exit /b 0
)

if "%1"=="update" (
    echo 🚀 [ghai Update] Fetching and updating gha to latest version...
    powershell -Command "iwr -useb https://raw.githubusercontent.com/intellibitz/gha/main/init/install.ps1 | iex"
    echo 🎉 [ghai Update] Updated gha & ghai to latest version successfully!
    exit /b 0
)

if "%1"=="uninstall" (
    echo 🧹 [ghai Uninstall] Completely removing gha sandbox, runner scripts, and workflows...
    if exist ".gha" rmdir /s /q .gha
    if exist "init\gha.init.gradle.kts" del /q init\gha.init.gradle.kts
    if exist ".github\workflows\gha.yml" del /q .github\workflows\gha.yml
    echo ✨ [ghai Uninstall] gha removed completely with 0 lingering system modifications!
    if exist "ghai.bat" del /q ghai.bat
    if exist "ghai" del /q ghai
    exit /b 0
)

if not exist "init\gha.init.gradle.kts" (
    echo ⚡ [ghai] First-time run detected! Auto-initializing gha sandbox...
    if not exist "init" mkdir init
    (
        echo initscript {
        echo     repositories {
        echo         mavenLocal^(^)
        echo         mavenCentral^(^)
        echo         gradlePluginPortal^(^)
        echo     }
        echo     dependencies {
        echo         classpath^("cc.thevar.gha:gha:0.1.0-SNAPSHOT"^)
        echo     }
        echo }
        echo allprojects {
        echo     apply^<cc.thevar.gha.GhaPlugin^>^(`)
        echo }
    ) > init\gha.init.gradle.kts
    .\gradlew.bat -Dgradle.user.home=.gha/gradle-user-home --init-script init/gha.init.gradle.kts ghaInit
)

.\gradlew.bat -Dgradle.user.home=.gha/gradle-user-home --init-script init/gha.init.gradle.kts ghai %*
