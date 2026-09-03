package cc.thevar.gha.git

import cc.thevar.gha.GhaTask
import org.gradle.api.provider.Property
import org.gradle.api.tasks.Input
import org.gradle.api.tasks.Optional
import org.gradle.api.tasks.TaskAction
import org.gradle.work.DisableCachingByDefault

@DisableCachingByDefault(because = "Displays recent Git commits")
abstract class GhaGitLogTask : GhaTask() {

    @get:Input
    @get:Optional
    abstract val maxCount: Property<Int>

    @get:Input
    @get:Optional
    abstract val targetRef: Property<String>

    init {
        maxCount.convention(1)
        targetRef.convention(project.providers.gradleProperty("targetRef").orElse("origin/main"))
    }

    @TaskAction
    fun execute() {
        val dir = projectRootDir.get().asFile
        val count = maxCount.getOrElse(1)
        val ref = targetRef.getOrElse("origin/main")

        println("📜 [GHA Git Log] Showing last $count commit(s) for '$ref'...")
        val logResult = GhaGitExec.exec(dir, "log", "-n", count.toString(), "--format=commit %H%nAuthor: %an <%ae>%nDate:   %ad%n%n    %s%n", ref)
        if (logResult.isSuccess) {
            println(logResult.stdout.prependIndent("   "))
        } else {
            println("❌ Git log failed: ${logResult.stderr.ifEmpty { logResult.stdout }}")
        }
    }
}
