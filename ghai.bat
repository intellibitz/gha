@echo off
.\gradlew.bat -Dgradle.user.home=.gha/gradle-user-home --init-script .gha\init.gradle.kts ghai %*
