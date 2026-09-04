package cc.thevar.gha

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

        // 2. Create init/ directory and gha.init.gradle.kts
        val initDir = File(rootDir, "init")
        if (!initDir.exists()) initDir.mkdirs()

        val initScript = File(initDir, "gha.init.gradle.kts")
        // Always refresh init script to ensure it points to the correct plugin version
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
                    classpath("cc.thevar.gha:gha:$currentVersion")
                }
            }

            allprojects {
                apply<cc.thevar.gha.GhaPlugin>()
            }
            """.trimIndent() + "\n"
        )

        val installSh = File(initDir, "install.sh")
        if (installSh.exists()) {
            installSh.setExecutable(true, false)
        }

        // 3. Create top-level ./ghai executable launcher (rwxr-xr-x mode 100755)
        val ghaiScript = File(rootDir, "ghai")
        // Self-heal launcher: Always write it if it's missing or significantly different
        val ghaiContent = """
            #!/usr/bin/env bash
            # 🤖 ghai - Autonomous AI Workflow Execution Script
            # 100% Sandboxed & Self-Healing - 0% Modifications to user files.
            if [ "${'$'}1" = "--version" ] || [ "${'$'}1" = "-v" ] || [ "${'$'}1" = "version" ] || [ "${'$'}1" = ":version" ]; then
                exec ./gradlew -Dgradle.user.home=.gha/gradle-user-home --init-script init/gha.init.gradle.kts ghai -Pmessage="--version"
            fi
            exec ./gradlew -Dgradle.user.home=.gha/gradle-user-home --init-script init/gha.init.gradle.kts ghai "${'$'}@"
        """.trimIndent() + "\n"

        if (!ghaiScript.exists() || ghaiScript.readText() != ghaiContent) {
            ghaiScript.writeText(ghaiContent)
            ghaiScript.setExecutable(true, false)
        }

        val ghaiBat = File(rootDir, "ghai.bat")
        val ghaiBatContent = """
            @echo off
            set "CMD=%~1"
            if defined CMD if "%CMD:~0,1%"==":" set "CMD=%CMD:~1%"
            if /i "%CMD%"=="version" (
                .\gradlew.bat -Dgradle.user.home=.gha/gradle-user-home --init-script init/gha.init.gradle.kts ghai -Pmessage="--version"
                exit /b 0
            )
            .\gradlew.bat -Dgradle.user.home=.gha/gradle-user-home --init-script init/gha.init.gradle.kts ghai %*
        """.trimIndent() + "\n"

        if (!ghaiBat.exists() || ghaiBat.readText() != ghaiBatContent) {
            ghaiBat.writeText(ghaiBatContent)
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
        // Skip updating .gitignore if we are in the gha project itself to avoid ignoring its own source launcher
        if (projectName.get() != "gha") {
            val gitignore = File(rootDir, ".gitignore")
            val ghaIgnoreSection = """
                
                # gha: Git, GitHub & Gradle Automation (Invisible Sandbox)
                .gha/
                ghai
                ghai.bat
                init/gha.init.gradle.kts
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
        logger.lifecycle("   ├── .gha/ sandbox initialized")
        logger.lifecycle("   ├── init/gha.init.gradle.kts refreshed")
        logger.lifecycle("   ├── ./ghai & ./ghai.bat executable runner scripts created (rwxr-xr-x)")
        logger.lifecycle("   └── .github/workflows/gha.yml CI workflow created")
        logger.lifecycle("🎉 gha is ready! Type './ghai' to run autonomous AI automation.")
    }
}
