package cc.thevar.gha

import org.gradle.api.provider.ListProperty
import org.gradle.api.tasks.Input
import org.gradle.api.tasks.TaskAction
import org.gradle.work.DisableCachingByDefault

@DisableCachingByDefault(because = "Displays help information for all GHA tasks")
abstract class GhaHelpTask : GhaTask() {

    @get:Input
    abstract val taskDetails: ListProperty<String>

    @TaskAction
    fun execute() {
        logger.lifecycle("📖 [GHA Help] 0 Effort, 100% Gain: Universal Automation CLI")
        logger.lifecycle("   ghai is a universal wrapper for Git, GitHub, and Gradle tasks.")
        
        logger.lifecycle("\n🤖 Core GHA Commands:")
        logger.lifecycle("   ./ghai                - Autonomous AI workflow (Sync/Commit/Push/PR)")
        logger.lifecycle("   ./ghai :version       - Detailed version report")
        logger.lifecycle("   ./ghai :status        - Sandbox health & portability report")
        logger.lifecycle("   ./ghai :install       - Initialize sandboxed environment")
        logger.lifecycle("   ./ghai :reinstall     - Fresh start (wipe & restore sandbox)")
        logger.lifecycle("   ./ghai :update        - Update gha to latest source version")
        logger.lifecycle("   ./ghai :clone <repo>  - Smart clone into current folder")
        logger.lifecycle("   ./ghai :aiContext     - Generate AI Project Context report")

        logger.lifecycle("\n🛠️  Universal Mapping (Power Logic):")
        logger.lifecycle("   1. Known GHA subcommands (above)")
        logger.lifecycle("   2. Gradle tasks (e.g., ./ghai :assemble)")
        logger.lifecycle("   3. GitHub commands (e.g., ./ghai pr list, ./ghai repo view)")
        logger.lifecycle("   4. Git commands (Fallback: e.g., ./ghai log, ./ghai diff)")

        logger.lifecycle("\n📂 Available Gradle Automation Tasks:")
        
        val details = taskDetails.get()
        if (details.isEmpty()) {
            logger.lifecycle("   No GHA tasks found.")
            return
        }

        var currentGroup = ""
        details.sorted().forEach { line ->
            val parts = line.split("|")
            val group = parts[0]
            val name = parts[1]
            val desc = parts[2]

            if (group != currentGroup) {
                logger.lifecycle("\n   [$group]")
                currentGroup = group
            }
            logger.lifecycle("   %-20s - %s".format(name, desc))
        }
        
        logger.lifecycle("\n💡 Usage: ./ghai <command> or ./ghai :<taskName>")
    }
}
