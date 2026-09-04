// Self-contained Gradle Init Script for GitHub Automation (GHA)
// 100% Sandboxed - 0% Modifications to existing project files.
initscript {
    repositories {
        mavenLocal()
        mavenCentral()
        gradlePluginPortal()
    }
    dependencies {
        classpath("cc.thevar.gha:gha:0.1.23-SNAPSHOT")
    }
}

allprojects {
    apply<cc.thevar.gha.GhaPlugin>()
}
