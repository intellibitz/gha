plugins {
    `kotlin-dsl`
    `maven-publish`
    id("com.gradle.plugin-publish") version "1.3.1"
}

group = "cc.thevar.gha"
version = "0.1.0-SNAPSHOT"

gradlePlugin {
    website = "https://github.com/intellibitz/gha"
    vcsUrl = "https://github.com/intellibitz/gha.git"

    plugins {
        create("gha") {
            id = "cc.thevar.gha"
            displayName = "GitHub Automation Gradle Plugin"
            description = "100% Kotlin, 100% platform-independent GitHub automation workflow Gradle tasks"
            implementationClass = "cc.thevar.gha.GhaPlugin"
            tags = listOf("github", "automation", "workflow", "ci", "kotlin")
        }
    }
}
