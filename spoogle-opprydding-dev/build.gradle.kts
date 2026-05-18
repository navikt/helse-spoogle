plugins {
    id("application")
    alias(libs.plugins.kotlin.jvm)
}

application {
    mainClass.set("no.nav.helse.opprydding.AppKt")
    applicationName = "app"
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
    jvmToolchain(21)
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
}
