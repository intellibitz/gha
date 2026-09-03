package cc.thevar.gha.git

import cc.thevar.gha.GhaTask
import org.gradle.api.provider.Property
import org.gradle.api.tasks.Input
import org.gradle.api.tasks.Optional
import org.gradle.api.tasks.TaskAction
import org.gradle.work.DisableCachingByDefault

@DisableCachingByDefault(because = "Executes Git checkout operations")
abstract class GhaGitCheckoutTask : GhaTask() {

    @get:Input
    @get:Optional
    abstract val branchName: Property<String>

    @get:Input
    @get:Optional
    abstract val createBranch: Property<String>

    @get:Input
    @get:Optional
    abstract val startPoint: Property<String>

    @get:Input
    @get:Optional
    abstract val stashLocal: Property<String>

    init {
        branchName.convention(project.providers.gradleProperty("branchName").orElse(project.providers.gradleProperty("branch")))
        createBranch.convention(project.providers.gradleProperty("createBranch").orElse("false"))
        startPoint.convention(project.providers.gradleProperty("startPoint"))
        stashLocal.convention(project.providers.gradleProperty("stashLocal").orElse("false"))
    }

    @TaskAction
    fun execute() {
        val dir = projectRootDir.get().asFile
        val target = branchName.orNull

        if (target.isNullOrBlank()) {
            logger.error("❌ Branch name required. Usage: ./gradlew ghaGitCheckout -PbranchName=feature/login")
            return
        }

        val shouldCreate = createBranch.get().lowercase() == "true"
        val shouldStash = stashLocal.get().lowercase() == "true"
        val start = startPoint.orNull

        logger.lifecycle("🌿 [GHA Git Checkout] Switching to branch '$target' (create: $shouldCreate)...")

        if (shouldStash && !GhaGitExec.isClean(dir)) {
            logger.lifecycle("📦 Stashing uncommitted local changes...")
            GhaGitExec.exec(dir, "stash", "push", "-m", "Auto-stashed by GHA Checkout")
        }

        val result = GhaGitExec.checkout(
            workingDir = dir,
            branchName = target,
            createIfMissing = shouldCreate,
            startPoint = start
        )

        if (result.isSuccess) {
            logger.lifecycle("✅ Successfully checked out branch '$target'.")
            if (shouldStash) {
                logger.lifecycle("📦 Unstashing local changes...")
                GhaGitExec.exec(dir, "stash", "pop")
            }
        } else {
            logger.error("❌ Git checkout failed: ${result.stderr.ifEmpty { result.stdout }}")
        }
    }
}
