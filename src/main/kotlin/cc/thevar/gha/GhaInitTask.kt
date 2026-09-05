package cc.thevar.gha

import cc.thevar.gha.safety.GhaVersionManager
import org.gradle.api.provider.Property
import org.gradle.api.tasks.Input
import org.gradle.api.tasks.Optional
import org.gradle.api.tasks.TaskAction
import org.gradle.work.DisableCachingByDefault
import java.io.File
import java.time.Instant

@DisableCachingByDefault(because = "Initializes GMA Master Interactor environment for any project anywhere")
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
        val rootDir = (if (!targetDirStr.isNullOrBlank()) File(targetDirStr) else projectRootDir.get().asFile).canonicalFile

        // 1. Create .gha sandbox directory
        val ghaDir = File(rootDir, ".gha")
        if (!ghaDir.exists()) ghaDir.mkdirs()

        val configFile = File(ghaDir, "gha.json")
        val currentVersion = GhaVersionManager.readVersion(rootDir)
        val pName = ghaProjectName.getOrElse("gha")

        // Ensure version info in sandbox
        File(ghaDir, "version.txt").writeText(currentVersion + "\n")
        File(ghaDir, "version-$currentVersion.txt").writeText(currentVersion + "\n")

        if (pName != "gha" && rootDir.name != "gha") {
            rootDir.listFiles()?.filter { it.name.startsWith("version") && it.name.endsWith(".txt") }?.forEach { it.delete() }
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

        // 2. Create .gha/init.gradle.kts
        val initScriptText = """
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

        File(ghaDir, "init.gradle.kts").writeText(initScriptText)
        val initDir = File(rootDir, "init")
        if (!initDir.exists()) initDir.mkdirs()
        File(initDir, "gha.init.gradle.kts").writeText(initScriptText)

        // 3. Create ./ghai Sole Interactor Launcher
        val ghaiScript = File(rootDir, "ghai")
        val ghaiContent = """
            #!/usr/bin/env bash
            # 🤖 ghai - GHA Master Agent (GMA) Launcher & Sole Interactor
            # 100% Sandboxed - Any project, anywhere. 0 Effort, 100% Gain.

            set -e
            GHA_REPO="${'$'}{GHA_REPO:-${'$'}(git config gha.repo 2>/dev/null || echo "intellibitz/gha")}"
            REFRESH_FLAG="--refresh-dependencies"
            if [ "${'$'}GHA_NO_REFRESH" = "1" ]; then REFRESH_FLAG=""; fi

            INIT_SCRIPT=".gha/init.gradle.kts"
            if [ ! -f "${'$'}INIT_SCRIPT" ]; then
                if [ -f "init/gha.init.gradle.kts" ]; then
                    mkdir -p .gha; cp "init/gha.init.gradle.kts" "${'$'}INIT_SCRIPT"
                fi
            fi

            if [ ! -f "gradlew" ]; then
                echo "📥 [ghai On-Demand] Bootstrapping Gradle wrapper..."
                mkdir -p "gradle/wrapper"
                curl -fsSL "https://raw.githubusercontent.com/${'$'}GHA_REPO/main/gradlew" -o "gradlew" 2>/dev/null || true
                chmod +x "gradlew" 2>/dev/null || true
            fi

            RAW_ARG="${'$'}1"
            CMD="${'$'}{RAW_ARG#:}"

            if [ -z "${'$'}RAW_ARG" ]; then
                exec ./gradlew ${'$'}REFRESH_FLAG -Dgradle.user.home=.gha/gradle-user-home --init-script "${'$'}INIT_SCRIPT" ghai
            fi

            case "${'$'}CMD" in
                version|--version|-v)
                    exec ./gradlew ${'$'}REFRESH_FLAG -Dgradle.user.home=.gha/gradle-user-home --init-script "${'$'}INIT_SCRIPT" ghai -Pmessage="--version report"
                    ;;
                gmcp|mcp)
                    exec ./gradlew ${'$'}REFRESH_FLAG -Dgradle.user.home=.gha/gradle-user-home --init-script "${'$'}INIT_SCRIPT" gmcp
                    ;;
                help)
                    exec ./gradlew ${'$'}REFRESH_FLAG -Dgradle.user.home=.gha/gradle-user-home --init-script "${'$'}INIT_SCRIPT" ghaHelp
                    ;;
                install)
                    exec ./gradlew --refresh-dependencies -Dgradle.user.home=.gha/gradle-user-home --init-script "${'$'}INIT_SCRIPT" ghaInit
                    ;;
                uninstall)
                    ./gradlew -Dgradle.user.home=.gha/gradle-user-home --init-script "${'$'}INIT_SCRIPT" ghaUninstall
                    exit 0
                    ;;
            esac

            # Delegate known Gradle tasks directly for speed
            KNOWN_GRADLE_TASKS="build test clean assemble check compileKotlin ghaInit ghaStatus ghaBuild ghaTest ghaClean ghaWorkflow ghaUpdate ghaUninstall ghaHelp ghaGitClone ghaGitStatus ghaGitCommit ghaGitPush ghaGitPull ghaGitBranch ghaGitCheckout ghaGitTag ghaGitLog ghaGitReset ghaGitStash ghaGitDiff ghaPrCreate ghaPrList ghaPrView ghaPrMerge ghaPrClose ghaPrReopen ghaPrEdit ghaPrCheckout ghaPrReview ghaIssueCreate ghaIssueList ghaIssueView ghaIssueClose ghaIssueReopen ghaIssueComment ghaIssueEdit ghaReleaseCreate ghaRepoView ghaGistCreate ghaSecretSet ghaProjectInit ghaProjectCreate ghaProjectList ghaProjectView ghaProjectAddItem ghaProjectClose ghaInsights ghaContributors ghaTraffic ghaSecurityInit ghaSecurityStatus ghaDependabotInit ghaDependabotList ghaDependabotMerge ghaDependabotClose ghaDependabotCleanup ghaDependabotRebase ghaCodeScanningInit ghaWikiInit ghaWikiStatus ghaWikiSync ghaWikiPublish ghaParallelWorkflow ghaDevWorkflow ghaAiContext ghaMcp ghaAiVision ghaAiOrchestrate ghaModels ghaEngines ghaMcpHub ghaBumpVersion"

            if [[ "${'$'}RAW_ARG" == ":"* ]] || [[ "${'$'}RAW_ARG" == "-"* ]] || [[ " ${'$'}KNOWN_GRADLE_TASKS " =~ " ${'$'}CMD " ]]; then
                exec ./gradlew ${'$'}REFRESH_FLAG -Dgradle.user.home=.gha/gradle-user-home --init-script "${'$'}INIT_SCRIPT" "${'$'}@"
            fi

            # Default: Pass as Goal to GMA
            echo "🤖 [GMA Master Interactor] Processing Goal: \"${'$'}*\""
            exec ./gradlew ${'$'}REFRESH_FLAG -Dgradle.user.home=.gha/gradle-user-home --init-script "${'$'}INIT_SCRIPT" ghai -Pmessage="${'$'}*"
        """.trimIndent() + "\n"

        ghaiScript.writeText(ghaiContent)
        ghaiScript.setExecutable(true, false)

        val ghaiBatContent = """
            @echo off
            .\gradlew.bat -Dgradle.user.home=.gha/gradle-user-home --init-script .gha\init.gradle.kts ghai %*
        """.trimIndent() + "\n"

        File(rootDir, "ghai.bat").writeText(ghaiBatContent)

        // 4. Pre-push hook
        val gitDir = File(rootDir, ".git")
        if (gitDir.exists()) {
            val prePushHook = File(gitDir, "hooks/pre-push")
            prePushHook.parentFile.mkdirs()
            prePushHook.writeText("#!/usr/bin/env bash\n./ghai :ghaBumpVersion 2>/dev/null || true\n")
            prePushHook.setExecutable(true, false)
        }

        // 5. .gitignore
        if (pName != "gha" && rootDir.name != "gha") {
            val gitignore = File(rootDir, ".gitignore")
            val section = "\n# gha: Master Interactor Sandbox\n.gha/\nghai\nghai.bat\n"
            if (gitignore.exists()) {
                if (!gitignore.readText().contains(".gha/")) gitignore.appendText(section)
            } else gitignore.writeText(section)
        }

        logger.lifecycle("🎉 [GMA] 100% Sandboxed Installation Complete! Type './ghai' to start.")
    }
}
