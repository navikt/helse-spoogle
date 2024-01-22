plugins {
    base
    kotlin("jvm") version "1.9.21" apply false
}

subprojects {
    repositories {
        mavenCentral()
        maven("https://jitpack.io")
    }
}