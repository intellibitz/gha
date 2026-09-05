package cc.thevar.gha.ai

import cc.thevar.gha.GhaTask
import cc.thevar.gha.ai.orchestrator.GhaAgentOfAgents
import org.gradle.api.provider.Property
import org.gradle.api.tasks.Input
import org.gradle.api.tasks.Optional
import org.gradle.api.tasks.TaskAction
import org.gradle.work.DisableCachingByDefault
import java.io.File

/**
 * GMA Master Interactor Entry Point (`ghai`).
 * Sole interactor for the GHA User, delegating missions to the GHA Master Agent (GMA).
 */
@DisableCachingByDefault(because = "Executes autonomous AI missions via GMA")
abstract class GhaAiTask : GhaTask() {

    @get:Input
    @get:Optional
    abstract val commitMessage: Property<String>

    init {
        val prov = project.providers
        commitMessage.convention(
            prov.gradleProperty("commitMessage")
                .orElse(prov.gradleProperty("message")),
        )
    }

    @TaskAction
    fun execute() {
        val explicitMsg = commitMessage.orNull
        val isTokenOnly = explicitMsg == "--token-only"
        
        if (isTokenOnly) {
            val token = resolveToken()
            logger.quiet(token)
            return
        }

        verifySandbox()
        val targetDirProp = project.providers.gradleProperty("targetDir")
            .orElse(project.providers.gradleProperty("dir"))
            .orNull
        val rootDir = if (!targetDirProp.isNullOrBlank()) File(targetDirProp) else projectRootDir.get().asFile

        // GHA Master Agent (GMA) is the sole interactor for the user.
        val gma = GhaAgentOfAgents()
        val goal = explicitMsg ?: "autonomous sync and push"
        
        println("🤖 [GMA Master Interactor] Mission starting for target: ${rootDir.absolutePath}")
        val result = gma.solve(goal, rootDir)
        
        // Report back to the user
        result.log.forEach { logger.lifecycle(it) }
        println("\n${result.output}")
    }
}
