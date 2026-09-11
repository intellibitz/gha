# aeon: 1-Line Universal Windows Installer (PowerShell)
# 100% Sandboxed - 100% Native Executable - 0 JVM, 0 Git, 0 Gradle Dependency

$ErrorActionPreference = "Stop"

$GlobalAeonDir = Join-Path $HOME ".aeon"
$GlobalBinDir = Join-Path $GlobalAeonDir "bin"
$GlobalModelsDir = Join-Path $GlobalAeonDir "models"

New-Item -ItemType Directory -Force -Path $GlobalBinDir | Out-Null
New-Item -ItemType Directory -Force -Path $GlobalModelsDir | Out-Null

$AeonRepo = if ($env:AEON_REPO) { $env:AEON_REPO } else { "intellibitz/aeon" }
$ReleaseUrl = "https://github.com/$AeonRepo/releases/latest/download"

Write-Host "[aeon] Initializing 100% Sandboxed Native AI Runtime (Repo: $AeonRepo)..." -ForegroundColor Cyan

$LauncherExePath = Join-Path $GlobalBinDir "aeon.exe"
$EngineExePath = Join-Path $GlobalBinDir "aeon-engine.exe"

$Installed = $false

# 1. Try Binary Download First (Lightning Fast)
if ($null -eq $env:LOCAL_SOURCE) {
    Write-Host "🔐 Fetching latest aeon executables..." -ForegroundColor Yellow

    $LauncherBinary = "aeon-windows-x86_64.exe"
    $EngineBinary = "aeon-engine-windows-x86_64.exe"

    Stop-Process -Name "aeon" -ErrorAction SilentlyContinue
    Stop-Process -Name "aeon-engine" -ErrorAction SilentlyContinue

    try {
        Invoke-WebRequest -Uri "$ReleaseUrl/$LauncherBinary" -OutFile $LauncherExePath -UseBasicParsing
        Invoke-WebRequest -Uri "$ReleaseUrl/$EngineBinary" -OutFile $EngineExePath -UseBasicParsing
        $Installed = $true
        Write-Host "  ✅ Downloaded & installed binaries from GitHub." -ForegroundColor Green
    } catch {
        Write-Host "  ⚠️ Binary download failed. Falling back to source build." -ForegroundColor Yellow
    }
}

# 2. Fallback to Source Build
if (-not $Installed -and (Get-Command "cargo" -ErrorAction SilentlyContinue)) {
    Write-Host "[aeon Native] Compiling standalone Rust AI engine & launcher..." -ForegroundColor Yellow

    Stop-Process -Name "aeon" -ErrorAction SilentlyContinue
    Stop-Process -Name "aeon-engine" -ErrorAction SilentlyContinue

    # Build Engine
    Write-Host "  Building engine..." -ForegroundColor Gray
    cargo build --release | Out-Null

    # Build Launcher
    Write-Host "  Building launcher..." -ForegroundColor Gray
    Push-Location "src/native/aeon"
    cargo build --release | Out-Null
    Pop-Location

    $EngineSrc = "target\release\aeon-engine.exe"
    $LauncherSrc = "src\native\aeon\target\release\aeon.exe"

    if ((Test-Path $EngineSrc) -and (Test-Path $LauncherSrc)) {
        Copy-Item $EngineSrc $EngineExePath -Force
        Copy-Item $LauncherSrc $LauncherExePath -Force
        $Installed = $true
        Write-Host "  ✅ Compiled & installed native binaries." -ForegroundColor Green
    }
}

if (-not $Installed) {
    Write-Error "Installation failed. Ensure 'cargo' is available or binary downloads are accessible."
    exit 1
}

# 3. PATH Automation (0-Effort Onboarding)
$UserPath = [Environment]::GetEnvironmentVariable("Path", "User")
if ($UserPath -notlike "*$GlobalBinDir*") {
    Write-Host "[aeon] Automatically adding '$GlobalBinDir' to User PATH..." -ForegroundColor Cyan
    [Environment]::SetEnvironmentVariable("Path", "$GlobalBinDir;$UserPath", "User")
    $env:Path = "$GlobalBinDir;$env:Path"
    Write-Host "  ✅ User PATH updated!" -ForegroundColor Green
}

Write-Host "`n🎉 Global aeon is ready! Type 'aeon status' or 'aeon mcp' to verify." -ForegroundColor Green
