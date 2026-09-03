// Self-contained Gradle Init Script for GitHub Automation (GHA)
// 100% Sandboxed - 0% System Modifications.
// Usage: ./gradlew --init-script init/gha.init.gradle.kts ghaInit
initscript {
    repositories {
        mavenLocal()
        mavenCentral()
        gradlePluginPortal()
    }
    dependencies {
        classpath("cc.thevar.gha:gha:0.1.0-SNAPSHOT")
    }
}

allprojects {
    apply<cc.thevar.gha.GhaPlugin>()
}
