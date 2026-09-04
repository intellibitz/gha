package cc.thevar.gha

import org.gradle.api.provider.Property
import org.gradle.api.tasks.Input
import org.gradle.api.tasks.TaskAction
import org.gradle.work.DisableCachingByDefault
import java.io.File

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
        if (!configFile.exists()) {
            configFile.writeText(
                """
                {
                  "project": "${projectName.get()}",
                  "version": "0.1.0-SNAPSHOT",
                  "sandboxed": true
                }
                """.trimIndent() + "\n"
            )
        }

        // 2. Create init/ directory and gha.init.gradle.kts
        val initDir = File(rootDir, "init")
        if (!initDir.exists()) initDir.mkdirs()

        val initScript = File(initDir, "gha.init.gradle.kts")
        if (!initScript.exists()) {
            initScript.writeText(
                """
                // Self-contained Gradle Init Script for GitHub Automation (GHA)
                // 100% Sandboxed - 0% Modifications to existing project files.
                initscript {
                    repositories {
                        mavenLocal()
                        mavenCentral()
                        gradlePluginPortal()
                    }
                    dependencies {
                        classpath("cc.thevar.gha:gha:0.1.0-SNAPSHOT")
                    }
                }

                allprojects {
                    apply<cc.thevar.gha.GhaPlugin>()
                }
                """.trimIndent() + "\n"
            )
        }

        val installSh = File(initDir, "install.sh")
        if (installSh.exists()) {
            installSh.setExecutable(true, false)
        }

        // 3. Create top-level ./ghai executable launcher (rwxr-xr-x mode 100755)
        val ghaiScript = File(rootDir, "ghai")
        if (!ghaiScript.exists()) {
            ghaiScript.writeText(
                """
                #!/usr/bin/env bash
                # 🤖 ghai - Autonomous AI Workflow Execution Script
                if [ "${'$'}1" = "--version" ] || [ "${'$'}1" = "-v" ] || [ "${'$'}1" = "version" ] || [ "${'$'}1" = ":version" ]; then
                    exec ./gradlew -Dgradle.user.home=.gha/gradle-user-home --init-script init/gha.init.gradle.kts ghai -Pmessage="--version"
                fi
                exec ./gradlew -Dgradle.user.home=.gha/gradle-user-home --init-script init/gha.init.gradle.kts ghai "${'$'}@"
                """.trimIndent() + "\n"
            )
        }
        ghaiScript.setExecutable(true, false)

        val ghaiBat = File(rootDir, "ghai.bat")
        if (!ghaiBat.exists()) {
            ghaiBat.writeText(
                """
                @echo off
                set "CMD=%~1"
                if defined CMD if "%CMD:~0,1%"==":" set "CMD=%CMD:~1%"
                if /i "%CMD%"=="version" (
                    .\gradlew.bat -Dgradle.user.home=.gha/gradle-user-home --init-script init/gha.init.gradle.kts ghai -Pmessage="--version"
                    exit /b 0
                )
                .\gradlew.bat -Dgradle.user.home=.gha/gradle-user-home --init-script init/gha.init.gradle.kts ghai %*
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

        logger.lifecycle("⚡ [gha] 100% Sandboxed 0-Effort Installation Complete!")
        logger.lifecycle("   ├── .gha/ sandbox initialized")
        logger.lifecycle("   ├── init/gha.init.gradle.kts refreshed")
        logger.lifecycle("   ├── ./ghai & ./ghai.bat executable runner scripts created (rwxr-xr-x)")
        logger.lifecycle("   └── .github/workflows/gha.yml CI workflow created")
        logger.lifecycle("🎉 gha is ready! Type './ghai' to run autonomous AI automation.")
    }
}
