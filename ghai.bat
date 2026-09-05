@echo off
set "GLOBAL_GHA_BIN=%USERPROFILE%\.gha\bin\ghai.exe"
if exist "%GLOBAL_GHA_BIN%" (
    "%GLOBAL_GHA_BIN%" %*
) else if exist "%~dp0target\release\gha.exe" (
    "%~dp0target\release\gha.exe" %*
) else (
    cargo run --release -- %*
)
