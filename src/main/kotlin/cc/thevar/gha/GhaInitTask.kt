package cc.thevar.gha

import cc.thevar.gha.safety.GhaProcessRunner
import cc.thevar.gha.safety.GhaVersionManager
import org.gradle.api.provider.Property
import org.gradle.api.tasks.Input
import org.gradle.api.tasks.TaskAction
import org.gradle.work.DisableCachingByDefault
import java.io.File
import java.time.Instant

@DisableCachingByDefault(because = "Initializes sandboxed GitHub Automation environment ridiculously easy for creators & users")
abstract class GhaInitTask : GhaTask() {

    @get:Input
    abstract val projectName: Property<String>

    init {
        projectName.convention(project.name)
    }

    @TaskAction
    fun execute() {
        val rootDir = projectRootDir.get().asFile

        // 1. Create .gha sandbox directory
        val ghaDir = File(rootDir, ".gha")
        if (!ghaDir.exists()) ghaDir.mkdirs()

        val configFile = File(ghaDir, "gha.json")
        val currentVersion = GhaVersionManager.readVersion(rootDir)
        // Always refresh gha.json to ensure latest version and metadata
        configFile.writeText(
            """
            {
              "project": "${projectName.get()}",
              "version": "$currentVersion",
              "sandboxed": true,
              "lastHealed": "${Instant.now()}"
            }
            """.trimIndent() + "\n"
        )

        // 2. Create .gha/init.gradle.kts (Single Sandbox File)
        val initScriptText = """
            // Self-contained Gradle Init Script for GitHub Automation (GHA)
            // 100% Sandboxed - 0% Modifications to existing project files.
            initscript {
                repositories {
                    mavenLocal()
                    mavenCentral()
                    gradlePluginPortal()
                }
                dependencies {
                    classpath("cc.thevar.gha:gha:$currentVersion")
                }
            }

            allprojects {
                apply<cc.thevar.gha.GhaPlugin>()
            }
        """.trimIndent() + "\n"

        val ghaInitScript = File(ghaDir, "init.gradle.kts")
        ghaInitScript.writeText(initScriptText)

        // Backwards compatibility for existing legacy init/ directory
        val legacyInitDir = File(rootDir, "init")
        if (legacyInitDir.exists()) {
            File(legacyInitDir, "gha.init.gradle.kts").writeText(initScriptText)
        }

        // 3. Create top-level ./ghai executable launcher (rwxr-xr-x mode 100755)
        val ghaiScript = File(rootDir, "ghai")
        val ghaiContent = """
            #!/usr/bin/env bash
            # 🤖 ghai - Autonomous AI Workflow Execution Script
            # 100% Sandboxed & Self-Healing - 0% Modifications to user files.
            INIT_SCRIPT=".gha/init.gradle.kts"
            if [ ! -f "${'$'}INIT_SCRIPT" ] && [ -f "init/gha.init.gradle.kts" ]; then
                INIT_SCRIPT="init/gha.init.gradle.kts"
            fi
            if [ "${'$'}1" = "--version" ] || [ "${'$'}1" = "-v" ] || [ "${'$'}1" = "version" ] || [ "${'$'}1" = ":version" ]; then
                exec ./gradlew -Dgradle.user.home=.gha/gradle-user-home --init-script "${'$'}INIT_SCRIPT" ghai -Pmessage="--version"
            fi
            exec ./gradlew -Dgradle.user.home=.gha/gradle-user-home --init-script "${'$'}INIT_SCRIPT" ghai "${'$'}@"
        """.trimIndent() + "\n"

        if (!ghaiScript.exists() || ghaiScript.readText() != ghaiContent) {
            ghaiScript.writeText(ghaiContent)
            ghaiScript.setExecutable(true, false)
        }

        val ghaiBatContent = """
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
        """.trimIndent() + "\n"

        val ghaBatInSandbox = File(ghaDir, "ghai.bat")
        ghaBatInSandbox.writeText(ghaiBatContent)

        val ghaiBatRoot = File(rootDir, "ghai.bat")
        if (!ghaiBatRoot.exists() || ghaiBatRoot.readText() != ghaiBatContent) {
            ghaiBatRoot.writeText(ghaiBatContent)
        }

        // 3a. Self-Heal: Ensure Gradle wrapper and settings files are present and valid
        val gradlew = File(rootDir, "gradlew")
        if (!gradlew.exists() || gradlew.length() == 0L) {
            logger.lifecycle("   📥 [Self-Heal] Restoring broken or missing Gradle wrapper...")
            GhaProcessRunner.exec(
                rootDir,
                listOf(
                    "curl",
                    "-sSL",
                    "https://raw.githubusercontent.com/intellibitz/gha/main/gradlew",
                    "-o",
                    "gradlew"
                )
            )
            GhaProcessRunner.exec(
                rootDir,
                listOf(
                    "curl",
                    "-sSL",
                    "https://raw.githubusercontent.com/intellibitz/gha/main/gradlew.bat",
                    "-o",
                    "gradlew.bat"
                )
            )
            val wrapperDir = File(rootDir, "gradle/wrapper")
            wrapperDir.mkdirs()
            GhaProcessRunner.exec(
                rootDir,
                listOf(
                    "curl",
                    "-sSL",
                    "https://raw.githubusercontent.com/intellibitz/gha/main/gradle/wrapper/gradle-wrapper.properties",
                    "-o",
                    "gradle/wrapper/gradle-wrapper.properties"
                )
            )
            GhaProcessRunner.exec(
                rootDir,
                listOf(
                    "curl",
                    "-sSL",
                    "https://raw.githubusercontent.com/intellibitz/gha/main/gradle/wrapper/gradle-wrapper.jar",
                    "-o",
                    "gradle/wrapper/gradle-wrapper.jar"
                )
            )
            gradlew.setExecutable(true, false)
        }

        val settingsFile = File(rootDir, "settings.gradle.kts")
        val settingsFileGroovy = File(rootDir, "settings.gradle")
        if (!settingsFile.exists() && !settingsFileGroovy.exists()) {
            logger.lifecycle("   📥 [Self-Heal] Scaffolding missing settings.gradle.kts...")
            settingsFile.writeText(
                """
                // Generated by gha - Safe to delete during uninstall
                pluginManagement {
                    repositories {
                        google()
                        mavenCentral()
                        gradlePluginPortal()
                    }
                }
                plugins {
                    id("org.gradle.toolchains.foojay-resolver-convention") version "1.0.0"
                }
                rootProject.name = "${projectName.get()}"
                """.trimIndent() + "\n"
            )
        }

        // 4. Create GitHub Actions Workflow (.github/workflows/gha.yml)
        val workflowsDir = File(rootDir, ".github/workflows")
        if (!workflowsDir.exists()) workflowsDir.mkdirs()

        val workflowFile = File(workflowsDir, "gha.yml")
        if (!workflowFile.exists()) {
            workflowFile.writeText(
                """
                name: gha Automation CI

                on:
                  push:
                    branches: [ main, master ]
                  pull_request:
                    branches: [ main, master ]

                jobs:
                  gha-ci:
                    runs-on: ubuntu-latest
                    steps:
                      - name: Checkout Repository
                        uses: actions/checkout@v4
                        with:
                          fetch-depth: 0

                      - name: Set up JDK 21
                        uses: actions/setup-java@v4
                        with:
                          distribution: 'temurin'
                          java-version: '21'

                      - name: Run ghai Autonomous Workflow
                        env:
                          GITHUB_TOKEN: ${'$'}{{ secrets.GITHUB_TOKEN }}
                        run: |
                          chmod +x gradlew ghai
                          ./ghai :version
                          ./ghai :status
                          ./ghai
                """.trimIndent() + "\n"
            )
        }

        // 5. Update .gitignore for Invisible Integration (0 side effects)
        if (projectName.get() != "gha") {
            val gitignore = File(rootDir, ".gitignore")
            val ghaIgnoreSection = """
                
                # gha: Git, GitHub & Gradle Automation (Invisible Sandbox)
                .gha/
                ghai
                ghai.bat
            """.trimIndent()

            if (gitignore.exists()) {
                val content = gitignore.readText()
                if (!content.contains("# gha: Git, GitHub & Gradle Automation")) {
                    gitignore.appendText(ghaIgnoreSection + "\n")
                    logger.lifecycle("   ➕ Updated .gitignore for invisible gha integration")
                }
            } else {
                gitignore.writeText(ghaIgnoreSection + "\n")
                logger.lifecycle("   ➕ Created .gitignore for invisible gha integration")
            }
        }

        logger.lifecycle("⚡ [gha] 100% Sandboxed 0-Effort Installation Complete!")
        logger.lifecycle("   ├── .gha/ sandbox initialized (.gha/init.gradle.kts)")
        logger.lifecycle("   ├── ./ghai & ./ghai.bat executable runner scripts created (rwxr-xr-x)")
        logger.lifecycle("   └── .github/workflows/gha.yml CI workflow created")
        logger.lifecycle("🎉 gha is ready! Type './ghai' to run autonomous AI automation.")
    }
}
