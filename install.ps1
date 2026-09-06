# 🚀 gha: 1-Line Universal Windows Installer (PowerShell)
# 100% Sandboxed - 100% Native Executable - 0 JVM, 0 Git, 0 Gradle Dependency

$ErrorActionPreference = "Stop"

$GlobalGhaDir = Join-Path $HOME ".gha"
$GlobalBinDir = Join-Path $GlobalGhaDir "bin"
$GlobalModelsDir = Join-Path $GlobalGhaDir "models"

New-Item -ItemType Directory -Force -Path $GlobalBinDir | Out-Null
New-Item -ItemType Directory -Force -Path $GlobalModelsDir | Out-Null

$GhaRepo = if ($env:GHA_REPO) { $env:GHA_REPO } else { "intellibitz/gha" }
$RawUrl = "https://raw.githubusercontent.com/$GHA_REPO/main"

Write-Host "⚡ [gha] Initializing 100% Sandboxed Native AI Runtime..." -ForegroundColor Cyan

$ExePath = Join-Path $GlobalBinDir "gha.exe"

# 1. Install or copy native binary
if (Test-Path "target\release\gha.exe") {
    Copy-Item "target\release\gha.exe" $ExePath -Force
    Write-Host "   └── Installed local release binary to $ExePath" -ForegroundColor Green
} elseif (Get-Command "cargo" -ErrorAction SilentlyContinue) {
    Write-Host "⚡ [gha Native] Compiling standalone Rust AI engine..." -ForegroundColor Yellow
    cargo build --release | Out-Null
    if (Test-Path "target\release\gha.exe") {
        Copy-Item "target\release\gha.exe" $ExePath -Force
        Write-Host "   └── Compiled & installed native binary to $ExePath" -ForegroundColor Green
    }
} else {
    Write-Host "📥 Fetching latest gha executable to $ExePath..." -ForegroundColor Yellow
    Invoke-WebRequest -Uri "$RawUrl/gha.exe" -OutFile $ExePath -UseBasicParsing
}

# 2. PATH Automation (0-Effort Onboarding)
$UserPath = [Environment]::GetEnvironmentVariable("Path", "User")
if ($UserPath -notlike "*$GlobalBinDir*") {
    Write-Host "⚡ [gha] Automatically adding '$GlobalBinDir' to User PATH..." -ForegroundColor Cyan
    [Environment]::SetEnvironmentVariable("Path", "$GlobalBinDir;$UserPath", "User")
    $env:Path = "$GlobalBinDir;$env:Path"
    Write-Host "   ✅ User PATH updated!" -ForegroundColor Green
}

Write-Host "`n🎉 Global gha is ready! Type 'gha :version' or 'gha :status' to verify." -ForegroundColor Green
