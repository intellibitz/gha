package cc.thevar.gha.ai

import cc.thevar.gha.GhaTask
import cc.thevar.gha.safety.GhaVersionManager
import org.gradle.api.tasks.TaskAction
import org.gradle.work.DisableCachingByDefault
import java.io.File

/**
 * Generates a comprehensive AI Context report for the current project.
 * Designed to be piped into LLMs or Agent context windows.
 */
@DisableCachingByDefault(because = "Generates real-time project context for AI agents")
abstract class GhaAiContextTask : GhaTask() {

    @TaskAction
    fun execute() {
        verifySandbox()
        val rootDir = projectRootDir.get().asFile
        
        val context = StringBuilder()
        context.append("# [gha] AI Project Context Report\n\n")
        context.append("## Project Metadata\n")
        context.append("- Name: ${taskProjectNameStr}\n")
        context.append("- Path: ${rootDir.absolutePath}\n")
        context.append("- GHA Version: ${GhaVersionManager.readVersion(rootDir)}\n\n")

        context.append("## VCS Context (${vcs.name})\n")
        context.append("- Branch: ${vcs.currentBranch(rootDir)}\n")
        context.append("- Dirty: ${vcs.isDirty(rootDir)}\n\n")

        context.append("## Build Context (${build.name})\n")
        context.append("- Version: ${build.version}\n")
        context.append("- Healthy: ${GhaAiManager.detectProjectContext(rootDir)}\n\n")

        context.append("## Directory Structure\n")
        context.append("```\n")
        val structure = rootDir.walk()
            .maxDepth(3)
            .filter { it.isDirectory && !it.name.startsWith(".") && it.name != "build" && it.name != "gradle" }
            .map { it.relativeTo(rootDir).path }
            .filter { it.isNotBlank() }
            .joinToString("\n")
        context.append(structure)
        context.append("\n```\n")

        val outputFile = File(rootDir, ".gha/ai-context.artifact.md")
        outputFile.writeText(context.toString())
        
        logger.lifecycle("🤖 [gha] AI Context generated at ${outputFile.absolutePath}")
        println(context.toString())
    }
}
