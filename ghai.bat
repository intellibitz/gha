@echo off
set "CMD=%~1"
if defined CMD if "%CMD:~0,1%"==":" set "CMD=%CMD:~1%"
set "INIT_SCRIPT=.gha\init.gradle.kts"
if not exist "%INIT_SCRIPT%" if exist "init\gha.init.gradle.kts" set "INIT_SCRIPT=init\gha.init.gradle.kts"
if /i "%CMD%"=="version" (
    .\gradlew.bat -Dgradle.user.home=.gha/gradle-user-home --init-script "%INIT_SCRIPT%" ghai -Pmessage="--version"
    exit /b 0
)
.\gradlew.bat -Dgradle.user.home=.gha/gradle-user-home --init-script "%INIT_SCRIPT%" ghai %*
