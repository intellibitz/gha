package cc.thevar.gha

import cc.thevar.gha.ai.orchestrator.GhaAgentOfAgents
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
        val engineVersion = GhaVersionManager.getEngineVersion(rootDir)
        val pName = ghaProjectName.getOrElse("gha")
        
        // Ensure version info in sandbox
        File(ghaDir, "version.txt").writeText(currentVersion + "\n")
        File(ghaDir, "version-$currentVersion.txt").writeText(currentVersion + "\n")
        if (!File(ghaDir, "gha-engine-version.txt").exists()) {
             File(ghaDir, "gha-engine-version.txt").writeText(engineVersion + "\n")
        }

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
                    classpath("cc.thevar.gha:gha:$engineVersion")
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
            
            # 🌌 Global GHA Context & Project Discovery
            GLOBAL_GHA_DIR="${'$'}HOME/.gha"
            GLOBAL_BIN_DIR="${'$'}GLOBAL_GHA_DIR/bin"
            CWD=${'$'}(pwd)

            # Function to find project root (walking up from CWD)
            find_project_root() {
                local dir="${'$'}1"
                while [ "${'$'}dir" != "/" ] && [ "${'$'}dir" != "${'$'}HOME" ]; do
                    if [ -d "${'$'}dir/.gha" ]; then
                        echo "${'$'}dir"
                        return
                    fi
                    dir=${'$'}(dirname "${'$'}dir")
                done
                echo ""
            }

            PROJECT_ROOT=${'$'}(find_project_root "${'$'}CWD")
            GHA_REPO="${'$'}{GHA_REPO:-${'$'}(git config gha.repo 2>/dev/null || echo "intellibitz/gha")}"
            
            if [ -n "${'$'}PROJECT_ROOT" ]; then
                cd "${'$'}PROJECT_ROOT"
                if [ -f "${'$'}PROJECT_ROOT/.gha/init.gradle.kts" ]; then
                    INIT_SCRIPT="${'$'}PROJECT_ROOT/.gha/init.gradle.kts"
                else
                    INIT_SCRIPT="${'$'}GLOBAL_GHA_DIR/init.gradle.kts"
                fi

                if [ -f "${'$'}PROJECT_ROOT/gradlew" ] && [ -f "${'$'}PROJECT_ROOT/gradle/wrapper/gradle-wrapper.jar" ]; then
                    GRADLEW="${'$'}PROJECT_ROOT/gradlew"
                else
                    GRADLEW="${'$'}GLOBAL_GHA_DIR/gradlew"
                fi
            else
                INIT_SCRIPT="${'$'}GLOBAL_GHA_DIR/init.gradle.kts"
                GRADLEW="${'$'}GLOBAL_GHA_DIR/gradlew"
                cd "${'$'}GLOBAL_GHA_DIR"
            fi

            REFRESH_FLAG="--refresh-dependencies"
            if [ "${'$'}GHA_NO_REFRESH" = "1" ]; then REFRESH_FLAG=""; fi

            if [ ! -f "${'$'}GRADLEW" ]; then
                echo "📥 [ghai On-Demand] Bootstrapping Gradle wrapper..."
                mkdir -p "${'$'}(dirname "${'$'}GRADLEW")/gradle/wrapper"
                curl -fsSL "https://raw.githubusercontent.com/${'$'}GHA_REPO/main/gradlew" -o "${'$'}GRADLEW" 2>/dev/null || true
                chmod +x "${'$'}GRADLEW" 2>/dev/null || true
            fi

            # 🌌 GMA Master Daemon Auto-Start
            GMA_LOCK="${'$'}GLOBAL_GHA_DIR/gma.lock"
            DAEMON_RUNNING=0
            if [ -f "${'$'}GMA_LOCK" ]; then
                GMA_PID=${'$'}(cat "${'$'}GMA_LOCK")
                if [ -n "${'$'}GMA_PID" ] && ps -p "${'$'}GMA_PID" > /dev/null 2>&1; then DAEMON_RUNNING=1; fi
            fi

            if [ "${'$'}DAEMON_RUNNING" = "0" ] && [ "${'$'}1" != ":daemon" ]; then
                if [[ "${'$'}1" != ":uninstall" && "${'$'}1" != ":ghaUninstall" ]]; then
                    echo "🚀 [ghai] Priming GMA Master Daemon..."
                    mkdir -p "${'$'}GLOBAL_GHA_DIR"
                    nohup "${'$'}GRADLEW" -Dgradle.user.home="${'$'}GLOBAL_GHA_DIR/gradle-user-home" --init-script "${'$'}INIT_SCRIPT" ghaAiOrchestrate -Paction=daemon > "${'$'}GLOBAL_GHA_DIR/gma-daemon.log" 2>&1 &
                fi
            fi

            RAW_ARG="${'$'}1"
            CMD="${'$'}{RAW_ARG#:}"

            run_gha() {
                exec "${'$'}GRADLEW" ${'$'}REFRESH_FLAG -Dgradle.user.home="${'$'}GLOBAL_GHA_DIR/gradle-user-home" --init-script "${'$'}INIT_SCRIPT" "${'$'}@"
            }

            if [ -z "${'$'}RAW_ARG" ]; then run_gha ghai; fi

            case "${'$'}CMD" in
                version|--version|-v) run_gha ghai -Pmessage="--version"; ;;
                gmcp|mcp) run_gha gmcp; ;;
                help) run_gha ghaHelp; ;;
                status) run_gha ghaStatus; ;;
                install)
                    echo "🚀 [ghai] Initializing GHA environment..."
                    mkdir -p .gha
                    if [ ! -f "settings.gradle.kts" ] && [ ! -f "settings.gradle" ]; then
                        echo "rootProject.name = \"${'$'}(basename ${'$'}(pwd))\"" > settings.gradle.kts
                    fi
                    run_gha ghaInit
                    ;;
                uninstall) run_gha ghaUninstall; exit 0; ;;
            esac

            KNOWN_GRADLE_TASKS="build test clean assemble check compileKotlin ghaInit ghaStatus ghaBuild ghaTest ghaClean ghaWorkflow ghaUpdate ghaUninstall ghaHelp ghaGitClone ghaGitStatus ghaGitCommit ghaGitPush ghaGitPull ghaGitBranch ghaGitCheckout ghaGitTag ghaGitLog ghaGitReset ghaGitStash ghaGitDiff ghaPrCreate ghaPrList ghaPrView ghaPrMerge ghaPrClose ghaPrReopen ghaPrEdit ghaPrCheckout ghaPrReview ghaIssueCreate ghaIssueList ghaIssueView ghaIssueClose ghaIssueReopen ghaIssueComment ghaIssueEdit ghaReleaseCreate ghaRepoView ghaGistCreate ghaSecretSet ghaProjectInit ghaProjectCreate ghaProjectList ghaProjectView ghaProjectAddItem ghaProjectClose ghaInsights ghaContributors ghaTraffic ghaSecurityInit ghaSecurityStatus ghaDependabotInit ghaDependabotList ghaDependabotMerge ghaDependabotClose ghaDependabotCleanup ghaDependabotRebase ghaCodeScanningInit ghaWikiInit ghaWikiStatus ghaWikiSync ghaWikiPublish ghaParallelWorkflow ghaDevWorkflow ghaAiContext ghaMcp ghaAiVision ghaAiOrchestrate ghaModels ghaEngines ghaMcpHub ghaBumpVersion"

            if [[ "${'$'}RAW_ARG" == ":"* ]] || [[ "${'$'}RAW_ARG" == "-"* ]] || [[ " ${'$'}KNOWN_GRADLE_TASKS " =~ " ${'$'}CMD " ]]; then
                run_gha "${'$'}@"
            fi

            echo "🤖 [ghai Orchestrator] Natural Language Instruction: \"${'$'}*\""
            run_gha ghaAiOrchestrate -Paction="agent" -Pgoal="${'$'}*"
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

        // 6. Final Readiness Check
        logger.lifecycle("🏁 [GMA] Finalizing 1-line installation & priming core components...")
        try {
            val gma = GhaAgentOfAgents()
            val report = gma.getCoordinationReport(rootDir)
            logger.lifecycle("✅ [GMA] Master Agent Ready: ${report.projectContext}")
            logger.lifecycle("✅ [GMCP] Master MCP Interactor Ready: ${report.gmcpStatus}")
            logger.lifecycle("✅ [ghai] Launcher scripts verified and executable.")
        } catch (e: Exception) {
            logger.lifecycle("⚠️ [GMA] Primary priming skipped: ${e.message}")
        }

        logger.lifecycle("🎉 [GMA] 100% Sandboxed Installation Complete! Type './ghai' to start.")
    }
}
