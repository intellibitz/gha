@echo off
rem 🤖 ghai - Autonomous AI Workflow & Universal Command Processor for Windows
rem 100% Sandboxed - 0% Modifications to existing project files.

setlocal enabledelayedexpansion

set "RAW_ARG=%~1"
set "CMD=%RAW_ARG%"
if defined CMD if "%CMD:~0,1%"==":" set "CMD=%CMD:~1%"

rem 1. CORE GHA SUBCOMMANDS
if /i "%CMD%"=="version" goto :run_ghai_version
if /i "%RAW_ARG%"=="--version" goto :run_ghai_version
if /i "%RAW_ARG%"=="-v" goto :run_ghai_version
if /i "%CMD%"=="status" goto :run_status
if /i "%CMD%"=="clone" goto :run_clone
if /i "%CMD%"=="help" goto :run_help
if /i "%CMD%"=="install" goto :run_install
if /i "%CMD%"=="reinstall" goto :run_reinstall
if /i "%CMD%"=="update" goto :run_update
if /i "%CMD%"=="uninstall" goto :run_uninstall
if /i "%CMD%"=="ai" goto :run_ai

rem 2. GRADLE TASK DETECTION
if defined RAW_ARG if "%RAW_ARG:~0,1%"==":" (
    call .\gradlew.bat -Dgradle.user.home=.gha/gradle-user-home --init-script init/gha.init.gradle.kts %*
    exit /b %ERRORLEVEL%
)

rem Find project root
set "PROJECT_ROOT=%CD%"
:find_root
if exist "%PROJECT_ROOT%\gradlew.bat" goto :found_root
if exist "%PROJECT_ROOT%\.git" goto :found_root
set "OLD_ROOT=%PROJECT_ROOT%"
for %%i in ("%PROJECT_ROOT%") do set "PROJECT_ROOT=%%~dpi"
set "PROJECT_ROOT=%PROJECT_ROOT:~0,-1%"
if "%PROJECT_ROOT%"=="" (
    set "PROJECT_ROOT=%CD%"
    goto :found_root
)
if "%PROJECT_ROOT%"=="%OLD_ROOT%" (
    set "PROJECT_ROOT=%CD%"
    goto :found_root
)
goto :find_root

:found_root
cd /d "%PROJECT_ROOT%"

rem 3. SELF-HEALING BOOTSTRAP
if not exist "gradlew.bat" (
    echo 📥 [ghai Self-Heal] Bootstrapping missing or broken Gradle wrapper...
    if not exist "gradle\wrapper" mkdir "gradle\wrapper"
    powershell -Command "Invoke-WebRequest -Uri 'https://raw.githubusercontent.com/intellibitz/gha/main/gradlew.bat' -OutFile 'gradlew.bat'"
    powershell -Command "Invoke-WebRequest -Uri 'https://raw.githubusercontent.com/intellibitz/gha/main/gradle/wrapper/gradle-wrapper.properties' -OutFile 'gradle\wrapper\gradle-wrapper.properties'"
    powershell -Command "Invoke-WebRequest -Uri 'https://raw.githubusercontent.com/intellibitz/gha/main/gradle/wrapper/gradle-wrapper.jar' -OutFile 'gradle\wrapper\gradle-wrapper.jar'"
)

if not exist "init\gha.init.gradle.kts" (
    echo ⚡ [ghai Self-Heal] Restoring gha sandbox...
    if not exist "init" mkdir "init"
    powershell -Command "Invoke-WebRequest -Uri 'https://raw.githubusercontent.com/intellibitz/gha/main/init/gha.init.gradle.kts' -OutFile 'init\gha.init.gradle.kts'"
    powershell -Command "Invoke-WebRequest -Uri 'https://raw.githubusercontent.com/intellibitz/gha/main/version.txt' -OutFile 'version.txt'"
    call .\gradlew.bat --refresh-dependencies -Dgradle.user.home=.gha/gradle-user-home --init-script init/gha.init.gradle.kts ghaInit
)

rem 4. GITHUB CLI (gh) MAPPING
set "GH_CMDS= pr issue repo gist release workflow secret project run auth browse codespace variable "
if defined CMD (
    echo %GH_CMDS% | findstr /i " %CMD% " >nul
    if !ERRORLEVEL! EQU 0 (
        for /f "usebackq tokens=*" %%i in (`.\gradlew.bat -q -Dgradle.user.home=.gha/gradle-user-home --init-script init/gha.init.gradle.kts :ghai -Pmessage^="--token-only"`) do set "GITHUB_TOKEN=%%i"
        where gh >nul 2>&1
        if !ERRORLEVEL! EQU 0 (
            gh %*
            exit /b !ERRORLEVEL!
        ) else (
            echo ⚠️ GitHub CLI (gh) not found in PATH. Attempting via Gradle task...
            call .\gradlew.bat -Dgradle.user.home=.gha/gradle-user-home --init-script init/gha.init.gradle.kts ghai %*
            exit /b !ERRORLEVEL!
        )
    )
)

rem 5. UNIVERSAL GIT FALLBACK
if not defined RAW_ARG (
    call .\gradlew.bat --refresh-dependencies -Dgradle.user.home=.gha/gradle-user-home --init-script init/gha.init.gradle.kts ghai
    exit /b %ERRORLEVEL%
)

rem If it's a known Gradle task
if "%CMD:~0,3%"=="gha" (
    call .\gradlew.bat -Dgradle.user.home=.gha/gradle-user-home --init-script init/gha.init.gradle.kts %*
    exit /b %ERRORLEVEL%
)

echo 🛠️  [ghai] Command not recognized. Passing to Git engine...
git %*
exit /b %ERRORLEVEL%

:run_ghai_version
call .\gradlew.bat --refresh-dependencies -Dgradle.user.home=.gha/gradle-user-home --init-script init/gha.init.gradle.kts ghai -Pmessage="--version"
exit /b %ERRORLEVEL%

:run_status
call .\gradlew.bat --refresh-dependencies -Dgradle.user.home=.gha/gradle-user-home --init-script init/gha.init.gradle.kts ghaStatus
exit /b %ERRORLEVEL%

:run_clone
call .\gradlew.bat --refresh-dependencies -Dgradle.user.home=.gha/gradle-user-home --init-script init/gha.init.gradle.kts ghaGitClone -PtargetRepo="%~2" -Pdir="%~3"
exit /b %ERRORLEVEL%

:run_help
call .\gradlew.bat --refresh-dependencies -Dgradle.user.home=.gha/gradle-user-home --init-script init/gha.init.gradle.kts ghaHelp
exit /b %ERRORLEVEL%

:run_install
call .\gradlew.bat --refresh-dependencies -Dgradle.user.home=.gha/gradle-user-home --init-script init/gha.init.gradle.kts ghaInit
exit /b %ERRORLEVEL%

:run_reinstall
echo 🧹 [ghai Reinstall] Performing clean removal...
call .\gradlew.bat -Dgradle.user.home=.gha/gradle-user-home --init-script init/gha.init.gradle.kts ghaUninstall
echo ⚡ [ghai Reinstall] Restoring sandbox...
if not exist "init" mkdir "init"
powershell -Command "Invoke-WebRequest -Uri 'https://raw.githubusercontent.com/intellibitz/gha/main/init/gha.init.gradle.kts' -OutFile 'init\gha.init.gradle.kts'"
powershell -Command "Invoke-WebRequest -Uri 'https://raw.githubusercontent.com/intellibitz/gha/main/version.txt' -OutFile 'version.txt'"
call .\gradlew.bat --refresh-dependencies -Dgradle.user.home=.gha/gradle-user-home --init-script init/gha.init.gradle.kts ghaInit
exit /b %ERRORLEVEL%

:run_update
echo 🚀 [ghai Update] Fetching and updating gha to latest version...
powershell -Command "iwr -useb https://raw.githubusercontent.com/intellibitz/gha/main/init/install.ps1 | iex"
echo 🎉 [ghai Update] Updated gha & ghai to latest version successfully!
exit /b 0

:run_uninstall
echo 🧹 [ghai Uninstall] Completely removing gha sandbox, runner scripts, and workflows...
call .\gradlew.bat -Dgradle.user.home=.gha/gradle-user-home --init-script init/gha.init.gradle.kts ghaUninstall
echo ✨ [ghai Uninstall] gha removed completely with 0 lingering system modifications!
exit /b 0

:run_ai
call .\gradlew.bat -Dgradle.user.home=.gha/gradle-user-home --init-script init/gha.init.gradle.kts ghaAiVision -Pmode="%~2" -Pgoal="%~3"
exit /b %ERRORLEVEL%
