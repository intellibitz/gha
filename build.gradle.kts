plugins {
    `kotlin-dsl`
    `maven-publish`
    id("com.gradle.plugin-publish") version "2.1.1"
}

group = "cc.thevar.gha"
version = "0.1.0-SNAPSHOT"

java {
    toolchain {
        languageVersion.set(JavaLanguageVersion.of(21))
    }
}

gradlePlugin {
    website = "https://github.com/intellibitz/gha"
    vcsUrl = "https://github.com/intellibitz/gha.git"

    plugins {
        create("gha") {
            id = "cc.thevar.gha"
            displayName = "gha: Git, GitHub & Gradle Automation"
            description = "100% Kotlin, 100% platform-independent Git, GitHub, and Gradle automation workflow tasks"
            implementationClass = "cc.thevar.gha.GhaPlugin"
            tags = listOf("git", "github", "gradle", "automation", "workflow", "ci", "kotlin")
        }
    }
}
