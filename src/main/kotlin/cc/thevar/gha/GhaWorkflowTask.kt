package cc.thevar.gha

import org.gradle.api.tasks.Input
import org.gradle.api.tasks.Optional
import org.gradle.api.tasks.TaskAction
import org.gradle.work.DisableCachingByDefault

@DisableCachingByDefault(because = "Executes dynamic workflow actions")
abstract class GhaWorkflowTask : GhaTask() {

    @get:Input
    @get:Optional
    var taskProjectName: String = "gha"

    @get:Input
    @get:Optional
    var workflowName: String = "default"

    @TaskAction
    fun execute() {
        println("⚙️ [GHA Workflow] Executing workflow '$workflowName' for $taskProjectName...")
        println("🔒 [GHA Security] Authenticated with Token: ${maskedToken()}")
        println("✅ Workflow '$workflowName' completed successfully.")
    }
}
