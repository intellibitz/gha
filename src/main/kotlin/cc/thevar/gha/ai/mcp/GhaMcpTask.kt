package cc.thevar.gha.ai.mcp

import cc.thevar.gha.GhaTask
import org.gradle.api.tasks.TaskAction
import org.gradle.work.DisableCachingByDefault

/**
 * GMCP: GMA Master MCP Interactor Task.
 * Starts a long-running Model Context Protocol (MCP) server over stdio.
 * Implementation: GhaGmcpEngine.
 */
@DisableCachingByDefault(because = "Starts a long-running interactive MCP server")
abstract class GhaMcpTask : GhaTask() {

    @TaskAction
    fun execute() {
        verifySandbox()
        val rootDir = taskRootDirFile
        val engine = GhaGmcpEngine(rootDir)

        logger.lifecycle("🤖 [GMCP] GMA Master MCP Server starting over stdio...")
        logger.lifecycle("   ├── Host Mode   : Enabled (Managing external MCP hubs)")
        logger.lifecycle("   ├── Server Mode : Enabled (Exposing GHA tools to everyone)")
        logger.lifecycle("   └── Client Mode : Active (Used by GMA Master Agents)")

        engine.startServer()
    }
}
