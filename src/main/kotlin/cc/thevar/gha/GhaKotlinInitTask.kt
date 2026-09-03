package cc.thevar.gha

import cc.thevar.gha.config.GhaConfig
import org.gradle.api.provider.Property
import org.gradle.api.tasks.Input
import org.gradle.api.tasks.Optional
import org.gradle.api.tasks.TaskAction
import org.gradle.work.DisableCachingByDefault
import java.io.File

@DisableCachingByDefault(because = "Scaffolds a new 100% Kotlin project structure")
abstract class GhaKotlinInitTask : GhaTask() {

    @get:Input
    @get:Optional
    abstract val projectName: Property<String>

    @get:Input
    @get:Optional
    abstract val packageName: Property<String>

    init {
        projectName.convention(project.providers.gradleProperty("projectName").orElse(project.name))
        packageName.convention(project.providers.gradleProperty("packageName").orElse("com.example.app"))
    }

    @TaskAction
    fun execute() {
        val rootDir = projectRootDir.get().asFile
        val name = projectName.get()
        val pkg = packageName.get()
        val pkgPath = pkg.replace('.', '/')

        logger.lifecycle("🚀 [GHA Kotlin Project Init] Scaffolding Kotlin project: $name (package: $pkg)...")

        // 1. Create directory structure
        val mainKotlinDir = File(rootDir, "src/main/kotlin/$pkgPath")
        val testKotlinDir = File(rootDir, "src/test/kotlin/$pkgPath")
        val gradleDir = File(rootDir, "gradle")
        val initDir = File(rootDir, "init")
        val ghaDir = File(rootDir, ".gha")

        mainKotlinDir.mkdirs()
        testKotlinDir.mkdirs()
        gradleDir.mkdirs()
        initDir.mkdirs()
        ghaDir.mkdirs()

        // 2. Main.kt
        val mainFile = File(mainKotlinDir, "Main.kt")
        if (!mainFile.exists()) {
            mainFile.writeText(
                """
                package $pkg

                fun main() {
                    println("Hello, $name from 100% Kotlin & GHA!")
                }
                """.trimIndent() + "\n"
            )
            logger.lifecycle("   ➕ Created ${mainFile.relativeTo(rootDir).path}")
        }

        // 3. MainTest.kt
        val testFile = File(testKotlinDir, "MainTest.kt")
        if (!testFile.exists()) {
            testFile.writeText(
                """
                package $pkg

                import kotlin.test.Test
                import kotlin.test.assertTrue

                class MainTest {
                    @Test
                    fun testApp() {
                        assertTrue(true, "Kotlin project $name initialized successfully!")
                    }
                }
                """.trimIndent() + "\n"
            )
            logger.lifecycle("   ➕ Created ${testFile.relativeTo(rootDir).path}")
        }

        // 4. gradle/libs.versions.toml
        val versionCatalogFile = File(gradleDir, "libs.versions.toml")
        if (!versionCatalogFile.exists()) {
            versionCatalogFile.writeText(
                """
                [versions]
                java = "${GhaConfig.JAVA_VERSION}"
                kotlin = "${GhaConfig.KOTLIN_VERSION}"
                gradle = "${GhaConfig.GRADLE_VERSION}"
                foojay-resolver = "${GhaConfig.FOOJAY_RESOLVER_VERSION}"

                [libraries]
                kotlin-stdlib = { module = "org.jetbrains.kotlin:kotlin-stdlib", version.ref = "kotlin" }
                kotlin-test = { module = "org.jetbrains.kotlin:kotlin-test", version.ref = "kotlin" }

                [plugins]
                kotlin-jvm = { id = "org.jetbrains.kotlin.jvm", version.ref = "kotlin" }
                foojay-resolver = { id = "org.gradle.toolchains.foojay-resolver-convention", version.ref = "foojay-resolver" }
                """.trimIndent() + "\n"
            )
            logger.lifecycle("   ➕ Created ${versionCatalogFile.relativeTo(rootDir).path}")
        }

        // 5. build.gradle.kts
        val buildFile = File(rootDir, "build.gradle.kts")
        if (!buildFile.exists()) {
            buildFile.writeText(
                """
                plugins {
                    kotlin("jvm") version "${GhaConfig.KOTLIN_VERSION}"
                    id("cc.thevar.gha") version "0.1.0-SNAPSHOT"
                }

                group = "$pkg"
                version = "0.1.0-SNAPSHOT"

                java {
                    toolchain {
                        languageVersion.set(JavaLanguageVersion.of(${GhaConfig.JAVA_VERSION}))
                    }
                }

                dependencies {
                    testImplementation(kotlin("test"))
                }

                tasks.test {
                    useJUnitPlatform()
                }
                """.trimIndent() + "\n"
            )
            logger.lifecycle("   ➕ Created ${buildFile.relativeTo(rootDir).path}")
        }

        // 6. settings.gradle.kts
        val settingsFile = File(rootDir, "settings.gradle.kts")
        if (!settingsFile.exists()) {
            settingsFile.writeText(
                """
                pluginManagement {
                    repositories {
                        mavenLocal()
                        mavenCentral()
                        gradlePluginPortal()
                    }
                }
                plugins {
                    id("org.gradle.toolchains.foojay-resolver-convention") version "${GhaConfig.FOOJAY_RESOLVER_VERSION}"
                }

                rootProject.name = "$name"
                """.trimIndent() + "\n"
            )
            logger.lifecycle("   ➕ Created ${settingsFile.relativeTo(rootDir).path}")
        }

        // 7. gradle.properties
        val propertiesFile = File(rootDir, "gradle.properties")
        if (!propertiesFile.exists()) {
            propertiesFile.writeText(
                """
                # 100% Sandboxed Gradle User Home
                org.gradle.user.home=.gha/gradle-user-home
                org.gradle.configuration-cache=true
                org.gradle.jvmargs=-Xmx2048m -Dfile.encoding=UTF-8
                kotlin.code.style=official
                """.trimIndent() + "\n"
            )
            logger.lifecycle("   ➕ Created ${propertiesFile.relativeTo(rootDir).path}")
        }

        // 8. .gitignore
        val gitignoreFile = File(rootDir, ".gitignore")
        if (!gitignoreFile.exists()) {
            gitignoreFile.writeText(
                """
                .gradle/
                build/
                .gha/
                .idea/
                .vscode/
                *.iml
                """.trimIndent() + "\n"
            )
            logger.lifecycle("   ➕ Created ${gitignoreFile.relativeTo(rootDir).path}")
        }

        // 9. .gha/gha.json
        val ghaConfigFile = File(ghaDir, "gha.json")
        if (!ghaConfigFile.exists()) {
            ghaConfigFile.writeText(
                """
                {
                  "project": "$name",
                  "package": "$pkg",
                  "version": "0.1.0-SNAPSHOT",
                  "sandboxed": true
                }
                """.trimIndent() + "\n"
            )
            logger.lifecycle("   ➕ Created ${ghaConfigFile.relativeTo(rootDir).path}")
        }

        logger.lifecycle("✅ [GHA Kotlin Project Init] Successfully initialized Kotlin project $name ($pkg)!")
    }
}
