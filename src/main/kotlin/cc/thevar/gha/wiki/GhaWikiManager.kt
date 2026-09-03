package cc.thevar.gha.wiki

import cc.thevar.gha.git.GhaGitExec
import cc.thevar.gha.safety.GhaProcessRunner
import java.io.File

/**
 * Platform-independent GitHub Wiki manager.
 * Manages local `wiki/` documentation pages and synchronizes them with the remote `<owner>/<repo>.wiki.git` repository.
 */
object GhaWikiManager {

    /**
     * Resolves the remote GitHub Wiki URL based on project's `origin` remote.
     * Handles both HTTPS (`https://github.com/owner/repo.git`) and SSH (`git@github.com:owner/repo.git`).
     */
    fun resolveWikiUrl(projectDir: File, token: String? = null): String? {
        val result = GhaGitExec.exec(projectDir, "config", "--get", "remote.origin.url")
        if (!result.isSuccess || result.stdout.isBlank()) {
            return null
        }

        val rawUrl = result.stdout.trim()
        val cleanedUrl = rawUrl.removeSuffix(".git")

        val wikiBaseUrl = when {
            cleanedUrl.startsWith("https://github.com/") -> {
                val path = cleanedUrl.removePrefix("https://github.com/")
                if (!token.isNullOrBlank()) {
                    "https://x-access-token:$token@github.com/$path.wiki.git"
                } else {
                    "https://github.com/$path.wiki.git"
                }
            }
            cleanedUrl.startsWith("git@github.com:") -> {
                val path = cleanedUrl.removePrefix("git@github.com:")
                if (!token.isNullOrBlank()) {
                    "https://x-access-token:$token@github.com/$path.wiki.git"
                } else {
                    "git@github.com:$path.wiki.git"
                }
            }
            else -> "$cleanedUrl.wiki.git"
        }

        return wikiBaseUrl
    }

    /**
     * Initializes a standard local `wiki/` directory with template pages.
     */
    fun initLocalWiki(wikiDir: File, projectName: String) {
        if (!wikiDir.exists()) {
            wikiDir.mkdirs()
        }

        val homeFile = File(wikiDir, "Home.md")
        if (!homeFile.exists()) {
            homeFile.writeText(
                """
                # Welcome to $projectName Wiki

                Welcome to the official documentation for **$projectName** (`cc.thevar.gha`).

                ## Overview

                **gha** is a 100% self-contained, platform-independent Git and GitHub automation framework written in pure Kotlin.

                ## Quick Navigation

                - [Installation & Quick Start](Installation)
                - [Tasks Reference](Tasks-Reference)
                - [Security & Sandboxing](Security-and-Sandboxing)

                ---
                *Automated and maintained via `ghaWikiPublish`.*
                """.trimIndent()
            )
        }

        val sidebarFile = File(wikiDir, "_Sidebar.md")
        if (!sidebarFile.exists()) {
            sidebarFile.writeText(
                """
                ### Navigation

                - **[Home](Home)**
                - **[Installation](Installation)**
                - **[Tasks Reference](Tasks-Reference)**
                - **[Security](Security-and-Sandboxing)**
                """.trimIndent()
            )
        }

        val footerFile = File(wikiDir, "_Footer.md")
        if (!footerFile.exists()) {
            footerFile.writeText(
                """
                ---
                *Powered by [gha](https://github.com/intellibitz/gha) — 100% Kotlin GitHub Automation.*
                """.trimIndent()
            )
        }

        val installationFile = File(wikiDir, "Installation.md")
        if (!installationFile.exists()) {
            installationFile.writeText(
                """
                # Installation & Setup

                ## Option 1: Self-Contained Init Script (Zero Modifications)

                Run `gha` tasks on any cloned project without changing build scripts:

                ```bash
                ./gradlew --init-script init/gha.init.gradle.kts ghaInit ghaStatus ghaDependencies
                ```

                ## Option 2: Gradle Plugin

                Add `cc.thevar.gha` to your project's `build.gradle.kts`:

                ```kotlin
                plugins {
                    id("cc.thevar.gha") version "0.1.0"
                }
                ```
                """.trimIndent()
            )
        }

        val referenceFile = File(wikiDir, "Tasks-Reference.md")
        if (!referenceFile.exists()) {
            referenceFile.writeText(
                """
                # GHA Tasks Reference

                ## Core Tasks
                - `./gradlew ghaInit`: Initializes sandboxed GitHub Automation environment (`.gha/`).
                - `./gradlew ghaStatus`: Displays current project status and platform details.
                - `./gradlew ghaDependencies`: Prints trusted vendors and dependency versions.
                - `./gradlew ghaWorkflow`: Executes automated workflows.

                ## GitHub Wiki Tasks
                - `./gradlew ghaWikiInit`: Creates local `wiki/` documentation directory and template pages.
                - `./gradlew ghaWikiStatus`: Inspects local wiki pages and remote wiki status.
                - `./gradlew ghaWikiSync`: Pulls remote wiki changes into local `wiki/` directory.
                - `./gradlew ghaWikiPublish`: Commits and pushes local `wiki/` pages to remote GitHub Wiki.

                ## GitHub Operations
                - `./gradlew ghaPrCreate`: Creates Pull Requests.
                - `./gradlew ghaPrList`: Lists open Pull Requests.
                - `./gradlew ghaIssueCreate`: Creates Issues.
                - `./gradlew ghaIssueList`: Lists open Issues.
                - `./gradlew ghaReleaseCreate`: Creates Releases.

                ## Git Operations
                - `./gradlew ghaGitStatus`: Displays Git repository status.
                - `./gradlew ghaGitCommit`: Stages and commits working tree changes.
                - `./gradlew ghaGitPush`: Pushes current branch to origin.
                - `./gradlew ghaGitPull`: Pulls remote changes with rebase.
                - `./gradlew ghaGitTag`: Tagging and pushing.
                - `./gradlew ghaGitLog`: Displays commit log.
                """.trimIndent()
            )
        }

        val securityFile = File(wikiDir, "Security-and-Sandboxing.md")
        if (!securityFile.exists()) {
            securityFile.writeText(
                """
                # Security & Sandboxing

                ## Sandboxed Dependencies
                All Gradle user home caches, toolchain downloads, and execution state are confined to `.gha/gradle-user-home/`. Zero changes are made to `~/.gradle/`.

                ## Secret Isolation
                GitHub tokens are resolved via `GITHUB_TOKEN`, `GH_TOKEN`, or `gh auth token` dynamically and masked (`ghp_...cdef`) in all task output.
                """.trimIndent()
            )
        }
    }

    /**
     * Clones or updates the remote GitHub Wiki into a sandboxed local workspace (`.gha/wiki-workspace`).
     */
    fun prepareWikiWorkspace(projectDir: File, token: String?): Pair<File?, GhaProcessRunner.ProcessResult> {
        val wikiUrl = resolveWikiUrl(projectDir, token)
            ?: return Pair(null, GhaProcessRunner.ProcessResult(-1, "", "Could not resolve remote origin URL for GitHub Wiki."))

        val sandboxedDir = File(projectDir, ".gha/wiki-workspace")
        if (!sandboxedDir.parentFile.exists()) {
            sandboxedDir.parentFile.mkdirs()
        }

        val env = if (!token.isNullOrBlank()) mapOf("GITHUB_TOKEN" to token, "GH_TOKEN" to token) else emptyMap()

        if (!File(sandboxedDir, ".git").exists()) {
            // Attempt to clone existing wiki repo
            val cloneResult = GhaProcessRunner.exec(
                workingDir = sandboxedDir.parentFile,
                command = listOf("git", "clone", wikiUrl, "wiki-workspace"),
                extraEnv = env,
                timeoutSeconds = 45L
            )
            if (!cloneResult.isSuccess) {
                // If clone failed because wiki repo hasn't been created yet, initialize fresh workspace
                if (!sandboxedDir.exists()) {
                    sandboxedDir.mkdirs()
                }
                GhaProcessRunner.exec(sandboxedDir, listOf("git", "init"), extraEnv = env)
                GhaProcessRunner.exec(sandboxedDir, listOf("git", "checkout", "-b", "master"), extraEnv = env)
                GhaProcessRunner.exec(sandboxedDir, listOf("git", "remote", "add", "origin", wikiUrl), extraEnv = env)
            }
        } else {
            // Attempt pull if branch exists
            GhaProcessRunner.exec(
                workingDir = sandboxedDir,
                command = listOf("git", "pull", "--rebase", "origin", "master"),
                extraEnv = env,
                timeoutSeconds = 30L
            )
        }

        return Pair(sandboxedDir, GhaProcessRunner.ProcessResult(0, "Wiki workspace prepared.", ""))
    }

    /**
     * Publishes local `wiki/` contents to the remote GitHub Wiki repository.
     */
    fun publishWiki(projectDir: File, wikiDir: File, token: String?, commitMessage: String): GhaProcessRunner.ProcessResult {
        val (workspace, prepareResult) = prepareWikiWorkspace(projectDir, token)
        if (workspace == null) {
            return prepareResult
        }

        // Copy all files from wikiDir into workspace
        wikiDir.listFiles()?.forEach { file ->
            if (file.isFile) {
                file.copyTo(File(workspace, file.name), overwrite = true)
            }
        }

        val env = if (!token.isNullOrBlank()) mapOf("GITHUB_TOKEN" to token, "GH_TOKEN" to token) else emptyMap()

        // Stage changes
        val addResult = GhaProcessRunner.exec(workspace, listOf("git", "add", "-A"), extraEnv = env)
        if (!addResult.isSuccess) {
            return addResult
        }

        // Check if there are changes to commit
        val statusResult = GhaProcessRunner.exec(workspace, listOf("git", "status", "--porcelain"), extraEnv = env)
        if (statusResult.isSuccess && statusResult.stdout.isBlank()) {
            return GhaProcessRunner.ProcessResult(0, "Wiki is already up to date. No changes to publish.", "")
        }

        // Commit
        val commitResult = GhaProcessRunner.exec(workspace, listOf("git", "commit", "-m", commitMessage), extraEnv = env)
        if (!commitResult.isSuccess) {
            return commitResult
        }

        // Push current branch to origin
        val branch = GhaGitExec.currentBranch(workspace)
        val pushResult = GhaProcessRunner.exec(workspace, listOf("git", "push", "-u", "origin", branch), extraEnv = env, timeoutSeconds = 45L)
        if (!pushResult.isSuccess) {
            val err = pushResult.stderr.ifEmpty { pushResult.stdout }
            if (err.contains("Repository not found")) {
                return GhaProcessRunner.ProcessResult(
                    exitCode = -1,
                    stdout = "",
                    stderr = "Remote Wiki repository not found.\n   💡 Please ensure 'Wikis' is enabled under GitHub Repository Settings (Settings > Features > Wikis)."
                )
            }
        }
        return pushResult
    }
}
