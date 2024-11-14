plugins {
    base
    kotlin("jvm") version "2.0.21" apply false
}

subprojects {
    repositories {
        mavenCentral()
        maven("https://jitpack.io")
    }
}