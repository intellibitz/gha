plugins {
    `kotlin-dsl`
    `maven-publish`
    id("com.gradle.plugin-publish") version "2.1.1"
}

group = "cc.thevar.gha"
val projectVersion = file("version.txt").readText().trim()
version = projectVersion

tasks.register("pushDocs") {
    doLast {
        ProcessBuilder("git", "add", "-A").directory(file(".")).start().waitFor()
        ProcessBuilder("git", "commit", "-m", "docs: update architecture documentation for GMA, GMAS, GAWD, GEMI, and GMCP").directory(file(".")).start().waitFor()
        ProcessBuilder("git", "push", "origin", "main").directory(file(".")).start().waitFor()
    }
}

tasks.register("syncInitScript") {
    val projectDir = project.layout.projectDirectory
    val v = projectVersion
    doLast {
        val initScriptFile = projectDir.file(".gha/init.gradle.kts").asFile
        if (initScriptFile.exists()) {
            val content = initScriptFile.readText()
            val newContent = content.replace(Regex("""classpath\("cc.thevar.gha:gha:.*"\)"""), "classpath(\"cc.thevar.gha:gha:$v\")")
            if (content != newContent) {
                initScriptFile.writeText(newContent)
                println("✅ Updated .gha/init.gradle.kts to version $v")
            }
        }
        val legacyInitFile = projectDir.file("init/gha.init.gradle.kts").asFile
        if (legacyInitFile.exists()) {
            val content = legacyInitFile.readText()
            val newContent = content.replace(Regex("""classpath\("cc.thevar.gha:gha:.*"\)"""), "classpath(\"cc.thevar.gha:gha:$v\")")
            if (content != newContent) {
                legacyInitFile.writeText(newContent)
            }
        }
    }
}

tasks.named("publishToMavenLocal") {
    dependsOn("syncInitScript")
}

java {
    toolchain {
        languageVersion.set(JavaLanguageVersion.of(21))
    }
}

dependencies {
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-core:1.10.1")
    testImplementation(kotlin("test"))
}

tasks.named<org.jetbrains.kotlin.gradle.tasks.KotlinCompile>("compileKotlin") {
    exclude("**/com/example/**")
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
