package cc.thevar.gha

import org.gradle.api.tasks.TaskAction
import org.gradle.work.DisableCachingByDefault
import java.io.File

@DisableCachingByDefault(because = "Removes Android project files, plugins, and dependencies to convert to a pure Kotlin project")
abstract class GhaAndroidRemoveTask : GhaTask() {

    @TaskAction
    fun execute() {
        val rootDir = projectRootDir.get().asFile
        logger.lifecycle("🧹 [GHA Android Remove] Removing Android project files, plugins, and dependencies for pure Kotlin environment...")

        var removedCount = 0

        // 1. Remove Android Manifests, Resources, and Assets across project
        rootDir.walkTopDown().forEach { file ->
            if (file.isFile && (file.name == "AndroidManifest.xml" || file.name == "proguard-rules.pro")) {
                file.delete()
                logger.lifecycle("   🗑️ Removed ${file.relativeTo(rootDir).path}")
                removedCount++
            } else if (file.isDirectory && (file.name == "res" || file.name == "assets" || file.name == "jniLibs") && file.parentFile?.name == "main") {
                file.deleteRecursively()
                logger.lifecycle("   🗑️ Removed Android directory ${file.relativeTo(rootDir).path}")
                removedCount++
            }
        }

        // 2. Clean up build.gradle.kts files
        rootDir.walkTopDown().filter { it.isFile && (it.name == "build.gradle.kts" || it.name == "build.gradle") }.forEach { buildFile ->
            var content = buildFile.readText()
            val original = content

            // Remove Android plugins
            content = content.replace(Regex("""id\s*\(\s*["']com\.android\.(application|library|test)["']\s*\)(\s*version\s*["'][^"']+["'])?"""), "")
            content = content.replace(Regex("""alias\s*\(\s*libs\.plugins\.android[a-zA-Z.]*\s*\)"""), "")
            content = content.replace(Regex("""kotlin\s*\(\s*["']android["']\s*\)"""), "kotlin(\"jvm\")")

            // Ensure kotlin("jvm") plugin is present if no kotlin plugin is present
            if (!content.contains("kotlin(\"jvm\")") && !content.contains("kotlin-jvm") && !content.contains("org.jetbrains.kotlin.jvm")) {
                if (content.contains("plugins {")) {
                    content = content.replace("plugins {", "plugins {\n    kotlin(\"jvm\")")
                }
            }

            // Remove android { ... } block
            content = content.replace(Regex("""android\s*\{[\s\S]*?\n\}"""), "")

            // Remove androidx / com.google.android dependencies
            content = content.lines().filterNot { line ->
                line.contains("androidx.") ||
                line.contains("com.google.android.") ||
                line.contains("com.android.support") ||
                line.contains("libs.androidx")
            }.joinToString("\n")

            // Remove multiple blank lines
            content = content.replace(Regex("""\n{3,}"""), "\n\n")

            if (content != original) {
                buildFile.writeText(content)
                logger.lifecycle("   ✏️ Cleaned Android plugins/dependencies in ${buildFile.relativeTo(rootDir).path}")
                removedCount++
            }
        }

        // 3. Clean up settings.gradle.kts
        val settingsFile = File(rootDir, "settings.gradle.kts")
        if (settingsFile.exists()) {
            var settingsContent = settingsFile.readText()
            val origSettings = settingsContent

            // Remove com.android / androidx repository filter blocks
            settingsContent = settingsContent.lines().filterNot { line ->
                line.contains("com\\.android.*") || line.contains("androidx.*")
            }.joinToString("\n").replace(Regex("""\n{3,}"""), "\n\n")

            if (settingsContent != origSettings) {
                settingsFile.writeText(settingsContent)
                logger.lifecycle("   ✏️ Cleaned Android repository filters in settings.gradle.kts")
                removedCount++
            }
        }

        // 4. Ensure src/main/kotlin structure exists for pure Kotlin execution
        val mainKotlinDir = File(rootDir, "src/main/kotlin")
        if (!mainKotlinDir.exists()) {
            mainKotlinDir.mkdirs()
            logger.lifecycle("   📁 Created pure Kotlin directory src/main/kotlin")
        }

        logger.lifecycle("✅ [GHA Android Remove] Pure Kotlin conversion complete! ($removedCount items updated/removed)")
        logger.lifecycle("💡 You can now develop purely in Kotlin inside Android Studio or any IDE.")
    }
}
