// Self-contained Gradle Init Script for GitHub Automation (GHA)
// 100% Sandboxed - 0% System Modifications.
initscript {
    repositories {
        mavenLocal()
        mavenCentral()
        gradlePluginPortal()
    }
    dependencies {
        classpath("cc.thevar.gha:gha:0.1.0-SNAPSHOT")
        classpath("org.gradle.toolchains:foojay-resolver:1.0.0")
    }
}

settingsEvaluated {
    try {
        apply(plugin = "org.gradle.toolchains.foojay-resolver-convention")
    } catch (_: Throwable) {
        // Safe fallback if plugin repositories are not yet configured in settings
    }
}

allprojects {
    apply<cc.thevar.gha.GhaPlugin>()
}
