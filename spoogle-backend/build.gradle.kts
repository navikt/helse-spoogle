private val mainClass = "no.nav.helse.spoogle.AppKt"

private val rapidsAndRiversVersion = "2025110410541762250064.d7e58c3fad81"
private val tbdLibsVersion = "2025.11.04-10.54-c831038e"
private val junitVersion = "6.0.2"
private val flywayVersion = "11.20.2"
private val hikariVersion = "7.0.2"
private val kotliqueryVersion = "1.9.1"
private val postgresqlVersion = "42.7.8"
private val testcontainersPostgresqlVersion = "2.0.3"
private val ktorVersion = "3.3.3"
private val logbackSyslog4jVersion = "1.0.0"

plugins {
    kotlin("jvm") apply true
    id("org.jetbrains.kotlin.plugin.serialization") version "2.3.0" apply true
}

dependencies {
    api("com.github.navikt:rapids-and-rivers:$rapidsAndRiversVersion")

    implementation("com.zaxxer:HikariCP:$hikariVersion")
    implementation("com.github.seratch:kotliquery:$kotliqueryVersion")
    implementation("org.postgresql:postgresql:$postgresqlVersion")
    implementation("org.flywaydb:flyway-core:$flywayVersion")
    implementation("org.flywaydb:flyway-database-postgresql:$flywayVersion")

    implementation("com.papertrailapp:logback-syslog4j:$logbackSyslog4jVersion") {
        exclude(group = "ch.qos.logback")
    }

    implementation("io.ktor:ktor-server-core-jvm:$ktorVersion")
    implementation("io.ktor:ktor-server-netty-jvm:$ktorVersion")
    implementation("io.ktor:ktor-server-content-negotiation:$ktorVersion")
    implementation("io.ktor:ktor-serialization-kotlinx-json:$ktorVersion")
    implementation("io.ktor:ktor-network-tls-certificates:$ktorVersion")
    implementation("io.ktor:ktor-server-auth:$ktorVersion")
    implementation("io.ktor:ktor-server-auth-jwt:$ktorVersion") {
        exclude(group = "junit")
    }
    implementation("io.ktor:ktor-server-forwarded-header:$ktorVersion")
    implementation("io.ktor:ktor-server-cors:$ktorVersion")
    implementation("io.ktor:ktor-server-call-id:$ktorVersion")
    implementation("io.ktor:ktor-server-status-pages:$ktorVersion")
    implementation("io.ktor:ktor-server-call-logging:$ktorVersion")
    implementation("io.ktor:ktor-server-metrics-micrometer:$ktorVersion")
    implementation("no.nav.security:token-validation-ktor-v3:6.0.0")

    implementation("io.ktor:ktor-client-content-negotiation:$ktorVersion")
    implementation("io.ktor:ktor-client-core-jvm:$ktorVersion")
    implementation("io.ktor:ktor-client-apache-jvm:$ktorVersion")

    testImplementation("com.github.navikt.tbd-libs:rapids-and-rivers-test:$tbdLibsVersion")
    testImplementation("no.nav.security:mock-oauth2-server:3.0.1")
    testImplementation("io.ktor:ktor-server-test-host:$ktorVersion")
    testImplementation(platform("org.junit:junit-bom:$junitVersion"))
    testImplementation("org.junit.jupiter:junit-jupiter")
    testImplementation("org.testcontainers:testcontainers-postgresql:$testcontainersPostgresqlVersion")
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

    jar {
        archiveBaseName.set("app")

        manifest {
            attributes["Main-Class"] = mainClass
            attributes["Class-Path"] =
                configurations.runtimeClasspath.get().joinToString(separator = " ") {
                    it.name
                }
        }

        from("${rootProject.projectDir}/spoogle-frontend/dist") {
            into("static")
        }

        doLast {
            configurations.runtimeClasspath.get().forEach {
                val file = File("${layout.buildDirectory.get()}/libs/${it.name}")
                if (!file.exists()) it.copyTo(file)
            }
        }
    }
}
