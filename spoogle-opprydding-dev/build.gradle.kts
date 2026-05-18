val mainClass = "no.nav.helse.opprydding.AppKt"

plugins {
    alias(libs.plugins.kotlin.jvm) apply true
}

dependencies {
    api(libs.rapids.and.rivers)

    implementation(libs.google.cloud.sql.socket.factory)
    implementation(libs.postgresql)
    implementation(libs.kotliquery)
    implementation(libs.hikari)

    testImplementation(project(":spoogle-backend")) // for å få  tilgang på db/migrations-filene
    testImplementation(libs.tbd.libs.test)
    testImplementation(libs.flyway.core)
    testImplementation(libs.flyway.database.postgresql)
    testImplementation(libs.testcontainers.postgresql) {
        exclude("com.fasterxml.jackson.core")
    }
    testImplementation(platform(libs.junit.bom))
    testImplementation(libs.junit.jupiter)
    testRuntimeOnly(libs.junit.platform.launcher)
}

kotlin {
    jvmToolchain {
        languageVersion.set(JavaLanguageVersion.of(21))
    }
}

tasks {

    test {
        useJUnitPlatform()
        testLogging {
            events("skipped", "failed")
            showStackTraces = true
            exceptionFormat = org.gradle.api.tasks.testing.logging.TestExceptionFormat.FULL
        }
    }

    named<Jar>("jar") {
        archiveBaseName.set("app")

        manifest {
            attributes["Main-Class"] = mainClass
            attributes["Class-Path"] =
                configurations.runtimeClasspath.get().joinToString(separator = " ") {
                    it.name
                }
        }

        doLast {
            configurations.runtimeClasspath.get().forEach {
                val file = File("${layout.buildDirectory.get()}/libs/${it.name}")
                if (!file.exists()) {
                    it.copyTo(file)
                }
            }
        }
    }
}
