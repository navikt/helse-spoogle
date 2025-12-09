private val testcontainersPostgresqlVersion = "1.21.3"
private val socketFactryVersion = "1.27.0"
private val postgresqlVersion = "42.7.8"
private val hikariVersion = "7.0.2"
private val kotliqueryVersion = "1.9.1"
private val flywayVersion = "11.19.0"
private val junitVersion = "6.0.0"
private val rapidsAndRiversVersion = "2025110410541762250064.d7e58c3fad81"
private val tbdLibsVersion = "2025.11.04-10.54-c831038e"

val mainClass = "no.nav.helse.opprydding.AppKt"

plugins {
    kotlin("jvm") apply true
}

dependencies {
    api("com.github.navikt:rapids-and-rivers:$rapidsAndRiversVersion")

    implementation("com.google.cloud.sql:postgres-socket-factory:$socketFactryVersion")
    implementation("org.postgresql:postgresql:$postgresqlVersion")
    implementation("com.github.seratch:kotliquery:$kotliqueryVersion")
    implementation("com.zaxxer:HikariCP:$hikariVersion")

    testImplementation(project(":spoogle-backend")) // for å få  tilgang på db/migrations-filene
    testImplementation("com.github.navikt.tbd-libs:rapids-and-rivers-test:$tbdLibsVersion")
    testImplementation("org.flywaydb:flyway-core:$flywayVersion")
    testImplementation("org.flywaydb:flyway-database-postgresql:$flywayVersion")
    testImplementation("org.testcontainers:postgresql:$testcontainersPostgresqlVersion") {
        exclude("com.fasterxml.jackson.core")
    }
    testImplementation(platform("org.junit:junit-bom:$junitVersion"))
    testImplementation("org.junit.jupiter:junit-jupiter")
    testRuntimeOnly("org.junit.platform:junit-platform-launcher")
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
