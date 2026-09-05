initscript {
    repositories {
        mavenLocal()
        mavenCentral()
        gradlePluginPortal()
    }
    dependencies {
        classpath("cc.thevar.gha:gha:0.1.64-SNAPSHOT")
    }
}
allprojects {
    apply<cc.thevar.gha.GhaPlugin>()
}
