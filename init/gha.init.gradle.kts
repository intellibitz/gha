// Zero-effort Gradle Init Script for GitHub Automation (GHA)
// Copy to ~/.gradle/init.d/gha.init.gradle.kts or run gradle with --init-script init/gha.init.gradle.kts
initscript {
    repositories {
        mavenLocal()
        mavenCentral()
        gradlePluginPortal()
    }
    dependencies {
        classpath("com.intellibitz.gha:gha:0.1.0-SNAPSHOT")
    }
}

allprojects {
    apply<com.intellibitz.gha.GhaPlugin>()
}
