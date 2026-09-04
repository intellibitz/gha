package cc.thevar.gha

import cc.thevar.gha.safety.GhaProcessRunner
import cc.thevar.gha.safety.GhaVersionManager
import org.gradle.api.provider.Property
import org.gradle.api.tasks.Input
import org.gradle.api.tasks.Optional
import org.gradle.api.tasks.TaskAction
import org.gradle.work.DisableCachingByDefault
import java.io.File
import java.time.Instant

@DisableCachingByDefault(because = "Initializes sandboxed GitHub Automation environment ridiculously easy for creators & users")
abstract class GhaInitTask : GhaTask() {

    @get:Input
    abstract val projectName: Property<String>

    @get:Input
    @get:Optional
    abstract val targetDirProperty: Property<String>

    init {
        val prov = project.providers
        val cmdTargetDir = project.findProperty("targetDir")?.toString()
        projectName.convention(project.name)
        targetDirProperty.convention(prov.gradleProperty("targetDir").orElse(prov.provider { cmdTargetDir }))
    }

    @TaskAction
    fun execute() {
        val targetDirStr = targetDirProperty.orNull
        val rootDir = if (!targetDirStr.isNullOrBlank()) File(targetDirStr) else projectRootDir.get().asFile

        // 1. Create .gha sandbox directory
        val ghaDir = File(rootDir, ".gha")
        if (!ghaDir.exists()) ghaDir.mkdirs()

        val configFile = File(ghaDir, "gha.json")
        val currentVersion = GhaVersionManager.readVersion(rootDir)

        val pName = ghaProjectName.getOrElse("gha")

        // Ensure .gha/version.txt is present inside the sandbox
        val sandboxVersionFile = File(ghaDir, "version.txt")
        sandboxVersionFile.writeText(currentVersion + "\n")

        // Clean up root version.txt if not in gha source project
        if (pName != "gha" && rootDir.name != "gha") {
            val rootVersionFile = File(rootDir, "version.txt")
            if (rootVersionFile.exists()) {
                rootVersionFile.delete()
            }
        }
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

        // Migration: Clean up legacy init/ directory in favor of unified .gha/
        val legacyInitDir = File(rootDir, "init")
        if (legacyInitDir.exists()) {
            val legacyScript = File(legacyInitDir, "gha.init.gradle.kts")
            if (legacyScript.exists()) {
                legacyScript.delete()
            }
            val remainingFiles = legacyInitDir.listFiles()?.filter { !it.name.startsWith(".") } ?: emptyList()
            if (remainingFiles.isEmpty() || (remainingFiles.size == 1 && remainingFiles[0].name == "install.sh")) {
                legacyInitDir.deleteRecursively()
                logger.lifecycle("   🧹 [GHA Migration] Cleaned up legacy init/ directory in favor of unified .gha/ sandbox.")
            }
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

        // 4. Update .gitignore for Invisible Integration (0 side effects)
        if (pName != "gha" && rootDir.name != "gha") {
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
