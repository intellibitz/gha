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
        logger.lifecycle("📖 [GHA Help] Available GitHub Automation Tasks:")
        
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
                logger.lifecycle("\n📂 $group:")
                currentGroup = group
            }
            logger.lifecycle("   %-20s - %s".format(name, desc))
        }
        
        logger.lifecycle("\n💡 Usage: ./gradlew --init-script init/gha.init.gradle.kts <taskName>")
    }
}
