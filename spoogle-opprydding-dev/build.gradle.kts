private val testcontainersVersion = "1.19.7"
private val cloudSqlVersion = "1.15.2"
private val postgresqlVersion = "42.7.2"
private val hikariVersion = "5.0.1"
private val kotliqueryVersion = "1.9.0"
private val flywayVersion = "10.12.0"
private val junitVersion = "5.10.2"
private val rapidsAndRiversVersion = "2024082313161724411773.1db923f4402d"

val mainClass = "no.nav.helse.opprydding.AppKt"

plugins {
    kotlin("jvm") apply true
}

dependencies {
    api("com.github.navikt:rapids-and-rivers:$rapidsAndRiversVersion")

    implementation("com.google.cloud.sql:postgres-socket-factory:$cloudSqlVersion")
    implementation("org.postgresql:postgresql:$postgresqlVersion")
    implementation("com.github.seratch:kotliquery:$kotliqueryVersion")
    implementation("com.zaxxer:HikariCP:$hikariVersion")

    testImplementation(project(":spoogle-backend")) // for å få  tilgang på db/migrations-filene
    testImplementation("org.flywaydb:flyway-core:$flywayVersion")
    testImplementation("org.flywaydb:flyway-database-postgresql:$flywayVersion")
    testImplementation("org.testcontainers:postgresql:$testcontainersVersion") {
        exclude("com.fasterxml.jackson.core")
    }
    testImplementation("org.junit.jupiter:junit-jupiter-api:$junitVersion")
    testRuntimeOnly("org.junit.jupiter:junit-jupiter-engine:$junitVersion")
}

kotlin {
    jvmToolchain {
        languageVersion.set(JavaLanguageVersion.of(21))
    }
}

tasks {

    test {
        useJUnitPlatform()
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
