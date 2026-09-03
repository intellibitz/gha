@echo off
rem 🤖 ghai - Autonomous AI Workflow Executable Launcher for Windows
rem 0 Effort, 100% Gain: Works Anywhere, Everywhere.

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
        echo         classpath^("org.gradle.toolchains:foojay-resolver:1.0.0"^)
        echo     }
        echo }
        echo settingsEvaluated {
        echo     apply^(plugin = "org.gradle.toolchains.foojay-resolver-convention"^)
        echo }
        echo allprojects {
        echo     apply^<cc.thevar.gha.GhaPlugin^>^(`)
        echo }
    ) > init\gha.init.gradle.kts
    .\gradlew.bat --init-script init/gha.init.gradle.kts ghaInit
)

.\gradlew.bat --init-script init/gha.init.gradle.kts ghai %*
