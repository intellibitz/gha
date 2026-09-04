package cc.thevar.gha.workflow

import cc.thevar.gha.GhaTask
import org.gradle.api.tasks.TaskAction
import org.gradle.work.DisableCachingByDefault
import java.io.File

/**
 * Initializes GitHub Actions CI workflow on demand (.github/workflows/gha.yml).
 */
@DisableCachingByDefault(because = "Initializes GitHub Actions CI workflow files")
abstract class GhaWorkflowInitTask : GhaTask() {

    @TaskAction
    fun execute() {
        val rootDir = projectRootDir.get().asFile
        val workflowsDir = File(rootDir, ".github/workflows")
        if (!workflowsDir.exists()) workflowsDir.mkdirs()

        val workflowFile = File(workflowsDir, "gha.yml")
        workflowFile.writeText(
            """
            name: gha Automation CI

            on:
              push:
                branches: [ main, master ]
              pull_request:
                branches: [ main, master ]

            jobs:
              gha-ci:
                runs-on: ubuntu-latest
                steps:
                  - name: Checkout Repository
                    uses: actions/checkout@v4
                    with:
                      fetch-depth: 0

                  - name: Set up JDK 21
                    uses: actions/setup-java@v4
                    with:
                      distribution: 'temurin'
                      java-version: '21'

                  - name: Run ghai Autonomous Workflow
                    env:
                      GITHUB_TOKEN: ${'$'}{{ secrets.GITHUB_TOKEN }}
                    run: |
                      chmod +x gradlew ghai
                      ./ghai :version
                      ./ghai :status
                      ./ghai
            """.trimIndent() + "\n"
        )

        logger.lifecycle("✅ [GHA Workflow Init] GitHub Actions workflow created at ${workflowFile.relativeTo(rootDir).path}")
    }
}
