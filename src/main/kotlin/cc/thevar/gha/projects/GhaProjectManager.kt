package cc.thevar.gha.projects

import cc.thevar.gha.git.GhaGitExec
import cc.thevar.gha.safety.GhaProcessRunner
import java.io.File

/**
 * Manager for creating, listing, viewing, linking, and maintaining GitHub Projects (v2).
 */
object GhaProjectManager {

    /**
     * Resolves the owner (user or organization) from remote origin URL.
     * Example: "https://github.com/intellibitz/gha.git" -> "intellibitz"
     */
    fun resolveOwner(projectDir: File): String? {
        val result = GhaGitExec.exec(projectDir, "config", "--get", "remote.origin.url")
        if (!result.isSuccess || result.stdout.isBlank()) return null

        val rawUrl = result.stdout.trim().removeSuffix(".git").removeSuffix("/")
        val path = when {
            rawUrl.startsWith("https://github.com/") -> rawUrl.removePrefix("https://github.com/")
            rawUrl.startsWith("git@github.com:") -> rawUrl.removePrefix("git@github.com:")
            else -> return null
        }

        val parts = path.split("/")
        return if (parts.size >= 2) parts[0] else null
    }

    /**
     * Creates a new GitHub Project board.
     */
    fun createProject(
        projectDir: File,
        token: String?,
        title: String,
        owner: String? = null
    ): GhaProcessRunner.ProcessResult {
        val targetOwner = owner ?: resolveOwner(projectDir)
            ?: return GhaProcessRunner.ProcessResult(-1, "", "Could not resolve GitHub owner from git remote.")

        val env = if (!token.isNullOrBlank()) mapOf("GITHUB_TOKEN" to token, "GH_TOKEN" to token) else emptyMap()

        return GhaProcessRunner.exec(
            workingDir = projectDir,
            command = listOf("gh", "project", "create", "--owner", targetOwner, "--title", title),
            extraEnv = env,
            timeoutSeconds = 30L
        )
    }

    /**
     * Lists GitHub Project boards for an owner.
     */
    fun listProjects(projectDir: File, token: String?, owner: String? = null): GhaProcessRunner.ProcessResult {
        val targetOwner = owner ?: resolveOwner(projectDir)
            ?: return GhaProcessRunner.ProcessResult(-1, "", "Could not resolve GitHub owner from git remote.")

        val env = if (!token.isNullOrBlank()) mapOf("GITHUB_TOKEN" to token, "GH_TOKEN" to token) else emptyMap()

        return GhaProcessRunner.exec(
            workingDir = projectDir,
            command = listOf("gh", "project", "list", "--owner", targetOwner),
            extraEnv = env,
            timeoutSeconds = 30L
        )
    }

    /**
     * Displays details and items for a GitHub Project board.
     */
    fun viewProject(projectDir: File, token: String?, projectNumber: Int, owner: String? = null): GhaProcessRunner.ProcessResult {
        val targetOwner = owner ?: resolveOwner(projectDir)
            ?: return GhaProcessRunner.ProcessResult(-1, "", "Could not resolve GitHub owner from git remote.")

        val env = if (!token.isNullOrBlank()) mapOf("GITHUB_TOKEN" to token, "GH_TOKEN" to token) else emptyMap()

        return GhaProcessRunner.exec(
            workingDir = projectDir,
            command = listOf("gh", "project", "view", projectNumber.toString(), "--owner", targetOwner),
            extraEnv = env,
            timeoutSeconds = 30L
        )
    }

    /**
     * Adds an Issue or Pull Request URL to a GitHub Project board.
     */
    fun addItemToProject(
        projectDir: File,
        token: String?,
        projectNumber: Int,
        itemUrl: String,
        owner: String? = null
    ): GhaProcessRunner.ProcessResult {
        val targetOwner = owner ?: resolveOwner(projectDir)
            ?: return GhaProcessRunner.ProcessResult(-1, "", "Could not resolve GitHub owner from git remote.")

        val env = if (!token.isNullOrBlank()) mapOf("GITHUB_TOKEN" to token, "GH_TOKEN" to token) else emptyMap()

        return GhaProcessRunner.exec(
            workingDir = projectDir,
            command = listOf("gh", "project", "item-add", projectNumber.toString(), "--owner", targetOwner, "--url", itemUrl),
            extraEnv = env,
            timeoutSeconds = 30L
        )
    }

    /**
     * Closes or archives a GitHub Project board.
     */
    fun closeProject(projectDir: File, token: String?, projectNumber: Int, owner: String? = null): GhaProcessRunner.ProcessResult {
        val targetOwner = owner ?: resolveOwner(projectDir)
            ?: return GhaProcessRunner.ProcessResult(-1, "", "Could not resolve GitHub owner from git remote.")

        val env = if (!token.isNullOrBlank()) mapOf("GITHUB_TOKEN" to token, "GH_TOKEN" to token) else emptyMap()

        return GhaProcessRunner.exec(
            workingDir = projectDir,
            command = listOf("gh", "project", "close", projectNumber.toString(), "--owner", targetOwner),
            extraEnv = env,
            timeoutSeconds = 30L
        )
    }

    /**
     * Initializes default GitHub Project boards for a repository.
     */
    fun initDefaultProjects(projectDir: File, token: String?, projectName: String): List<String> {
        val owner = resolveOwner(projectDir) ?: return emptyList()
        val created = mutableListOf<String>()

        val defaultBoards = listOf(
            "$projectName - Development Roadmap",
            "$projectName - Bug & Issue Tracker",
            "$projectName - Releases & Deployments"
        )

        defaultBoards.forEach { title ->
            val result = createProject(projectDir, token, title, owner)
            if (result.isSuccess) {
                created.add(title)
            }
        }

        return created
    }
}
