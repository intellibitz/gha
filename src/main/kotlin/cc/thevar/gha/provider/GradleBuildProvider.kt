package cc.thevar.gha.provider

import cc.thevar.gha.config.GhaConfig
import cc.thevar.gha.safety.GhaProcessRunner
import java.io.File

class GradleBuildProvider : GhaBuildProvider {
    override val name = "Gradle"
    override val version = GhaConfig.GRADLE_VERSION
    override val vendor = GhaConfig.GRADLE_VENDOR

    override fun isAvailable(rootDir: File): Boolean = 
        File(rootDir, "gradlew").exists() || File(rootDir, "settings.gradle.kts").exists()

    override fun build(rootDir: File) {
        GhaProcessRunner.exec(rootDir, listOf("./gradlew", "build"))
    }

    override fun clean(rootDir: File) {
        GhaProcessRunner.exec(rootDir, listOf("./gradlew", "clean"))
    }

    override fun test(rootDir: File) {
        GhaProcessRunner.exec(rootDir, listOf("./gradlew", "test"))
    }
}
