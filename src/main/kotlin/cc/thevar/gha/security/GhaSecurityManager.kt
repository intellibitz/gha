package cc.thevar.gha.security

import java.io.File

/**
 * Manager for generating, publishing, and maintaining default GitHub Actions security workflows,
 * Dependabot configurations, CodeQL code scanning, and vulnerability policies.
 */
object GhaSecurityManager {

    private const val DOLLAR = "$"

    /**
     * Initializes all default security workflows, Dependabot configurations, and security policy.
     */
    fun initAllSecurityWorkflows(projectDir: File, projectName: String): List<String> {
        val createdFiles = mutableListOf<String>()

        if (initDependabotConfig(projectDir)) {
            createdFiles.add(".github/dependabot.yml")
        }
        if (initCodeScanningWorkflow(projectDir)) {
            createdFiles.add(".github/workflows/codeql.yml")
        }
        if (initSecurityAuditWorkflow(projectDir)) {
            createdFiles.add(".github/workflows/security.yml")
        }
        if (initSecurityPolicy(projectDir, projectName)) {
            createdFiles.add(".github/SECURITY.md")
        }

        return createdFiles
    }

    /**
     * Generates `.github/dependabot.yml` for automated dependency updates.
     */
    fun initDependabotConfig(projectDir: File): Boolean {
        val githubDir = File(projectDir, ".github")
        if (!githubDir.exists()) githubDir.mkdirs()

        val dependabotFile = File(githubDir, "dependabot.yml")
        if (dependabotFile.exists()) return false

        dependabotFile.writeText(
            """
            version: 2
            updates:
              # Maintain dependencies for GitHub Actions
              - package-ecosystem: "github-actions"
                directory: "/"
                schedule:
                  interval: "weekly"
                open-pull-requests-limit: 10
                labels:
                  - "dependencies"
                  - "github-actions"

              # Maintain dependencies for Gradle
              - package-ecosystem: "gradle"
                directory: "/"
                schedule:
                  interval: "weekly"
                open-pull-requests-limit: 10
                labels:
                  - "dependencies"
                  - "gradle"
            """.trimIndent()
        )
        return true
    }

    /**
     * Generates CodeQL code scanning workflow `.github/workflows/codeql.yml`.
     */
    fun initCodeScanningWorkflow(projectDir: File): Boolean {
        val workflowsDir = File(projectDir, ".github/workflows")
        if (!workflowsDir.exists()) workflowsDir.mkdirs()

        val codeqlFile = File(workflowsDir, "codeql.yml")
        if (codeqlFile.exists()) return false

        codeqlFile.writeText(
            """
            name: "CodeQL Security Analysis"

            on:
              push:
                branches: [ "main" ]
              pull_request:
                branches: [ "main" ]
              schedule:
                - cron: '0 0 * * 0' # Weekly security scan

            jobs:
              analyze:
                name: Analyze Code Quality & Security
                runs-on: ubuntu-latest
                permissions:
                  actions: read
                  contents: read
                  security-events: write

                strategy:
                  fail-fast: false
                  matrix:
                    language: [ 'java-kotlin' ]

                steps:
                - name: Checkout repository
                  uses: actions/checkout@v4

                - name: Set up Java Toolchain
                  uses: actions/setup-java@v4
                  with:
                    distribution: 'temurin'
                    java-version: '25'

                - name: Initialize CodeQL
                  uses: github/codeql-action/init@v3
                  with:
                    languages: ${DOLLAR}{{ matrix.language }}

                - name: Autobuild
                  uses: github/codeql-action/autobuild@v3

                - name: Perform CodeQL Analysis
                  uses: github/codeql-action/analyze@v3
                  with:
                    category: "/language:${DOLLAR}{{matrix.language}}"
            """.trimIndent()
        )
        return true
    }

    /**
     * Generates security audit and vulnerability scanning workflow `.github/workflows/security.yml`.
     */
    fun initSecurityAuditWorkflow(projectDir: File): Boolean {
        val workflowsDir = File(projectDir, ".github/workflows")
        if (!workflowsDir.exists()) workflowsDir.mkdirs()

        val securityFile = File(workflowsDir, "security.yml")
        if (securityFile.exists()) return false

        securityFile.writeText(
            """
            name: "Security & Vulnerability Audit"

            on:
              push:
                branches: [ "main" ]
              pull_request:
                branches: [ "main" ]
              workflow_dispatch:

            jobs:
              security-audit:
                name: Vulnerability & Secret Audit
                runs-on: ubuntu-latest
                permissions:
                  contents: read
                  security-events: write

                steps:
                - name: Checkout repository
                  uses: actions/checkout@v4

                - name: Set up Java Toolchain
                  uses: actions/setup-java@v4
                  with:
                    distribution: 'temurin'
                    java-version: '25'

                - name: Validate Gradle Wrapper
                  uses: gradle/actions/wrapper-validation@v4

                - name: Build & Publish Plugin to MavenLocal
                  run: ./gradlew publishToMavenLocal

                - name: Execute GHA Security & Dependency Audit
                  run: ./gradlew --init-script init/gha.init.gradle.kts ghaSecurityStatus ghaDependencies
            """.trimIndent()
        )
        return true
    }

    /**
     * Generates `.github/SECURITY.md` security policy and vulnerability reporting guidelines.
     */
    fun initSecurityPolicy(projectDir: File, projectName: String): Boolean {
        val githubDir = File(projectDir, ".github")
        if (!githubDir.exists()) githubDir.mkdirs()

        val securityMdFile = File(githubDir, "SECURITY.md")
        if (securityMdFile.exists()) return false

        securityMdFile.writeText(
            """
            # Security Policy for $projectName

            ## Supported Versions

            | Version | Supported          |
            | ------- | ------------------ |
            | 0.1.x   | :white_check_mark: |

            ## Reporting a Vulnerability

            If you discover a security vulnerability within **$projectName**, please **do not** open a public issue.

            Instead, report the security vulnerability via GitHub Private Vulnerability Reporting or email the maintainers directly.

            All vulnerability reports will be acknowledged within 24 hours, and security patches will be released promptly.
            """.trimIndent()
        )
        return true
    }
}
