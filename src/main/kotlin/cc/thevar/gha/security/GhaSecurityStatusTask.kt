package cc.thevar.gha.security

import cc.thevar.gha.GhaTask
import org.gradle.api.provider.Property
import org.gradle.api.tasks.Input
import org.gradle.api.tasks.TaskAction
import org.gradle.work.DisableCachingByDefault
import java.io.File

@DisableCachingByDefault(because = "Displays current GitHub security, scanning, and Dependabot status")
abstract class GhaSecurityStatusTask : GhaTask() {

    @get:Input
    abstract val projectName: Property<String>

    init {
        projectName.convention(project.name)
    }

    @TaskAction
    fun execute() {
        val rootDir = projectRootDir.get().asFile
        val githubDir = File(rootDir, ".github")

        val dependabotExists = File(githubDir, "dependabot.yml").exists()
        val codeqlExists = File(githubDir, "workflows/codeql.yml").exists()
        val securityAuditExists = File(githubDir, "workflows/security.yml").exists()
        val securityPolicyExists = File(githubDir, "SECURITY.md").exists()

        logger.lifecycle("🛡️ [GHA Security Status] Project: ${projectName.get()}")
        logger.lifecycle("   Dependabot Config (.github/dependabot.yml): ${if (dependabotExists) "✅ Present" else "⚠️ Missing"}")
        logger.lifecycle("   CodeQL Scanning Workflow (.github/workflows/codeql.yml): ${if (codeqlExists) "✅ Present" else "⚠️ Missing"}")
        logger.lifecycle("   Security Audit Workflow (.github/workflows/security.yml): ${if (securityAuditExists) "✅ Present" else "⚠️ Missing"}")
        logger.lifecycle("   Security Policy (.github/SECURITY.md): ${if (securityPolicyExists) "✅ Present" else "⚠️ Missing"}")

        if (!dependabotExists || !codeqlExists || !securityAuditExists || !securityPolicyExists) {
            logger.lifecycle("💡 Tip: Run './gradlew ghaSecurityInit' to automatically generate missing security workflows.")
        }
    }
}
