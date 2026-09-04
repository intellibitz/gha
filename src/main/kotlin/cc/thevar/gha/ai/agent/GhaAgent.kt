package cc.thevar.gha.ai.agent

import java.io.File

/**
 * Interface for gha Autonomous Agents.
 */
interface GhaAgent {
    val name: String
    val role: String
    
    /**
     * Executes an autonomous mission within the project sandbox.
     */
    fun executeMission(projectDir: File, prompt: String): String
}
