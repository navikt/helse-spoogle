private val mainClass = "no.nav.helse.spoogle.AppKt"

private val rapidsAndRiversVersion = "2023050308441683096263.f5a276d7bd28"
private val junitVersion = "5.9.0"
private val flywayVersion = "9.3.0"
private val hikariVersion = "5.0.1"
private val kotliqueryVersion = "1.9.0"
private val postgresqlVersion = "42.5.1"
private val testcontainersPostgresqlVersion = "1.17.3"

plugins {
    kotlin("jvm") apply true
}

dependencies {
    api("com.github.navikt:rapids-and-rivers:$rapidsAndRiversVersion")

    implementation("com.zaxxer:HikariCP:$hikariVersion")
    implementation("com.github.seratch:kotliquery:$kotliqueryVersion")
    implementation("org.postgresql:postgresql:$postgresqlVersion")
    implementation("org.flywaydb:flyway-core:$flywayVersion")

    testImplementation("org.junit.jupiter:junit-jupiter-api:$junitVersion")
    testImplementation("org.testcontainers:postgresql:$testcontainersPostgresqlVersion")
    testRuntimeOnly("org.junit.jupiter:junit-jupiter-engine:$junitVersion")
}

tasks {
    test {
        useJUnitPlatform()
    }

    compileKotlin {
        kotlinOptions.jvmTarget = "17"
    }

    compileTestKotlin {
        kotlinOptions.jvmTarget = "17"
    }

    jar {
        archiveBaseName.set("app")

        manifest {
            attributes["Main-Class"] = mainClass
            attributes["Class-Path"] = configurations.runtimeClasspath.get().joinToString(separator = " ") {
                it.name
            }
        }

        doLast {
            configurations.runtimeClasspath.get().forEach {
                val file = File("$buildDir/libs/${it.name}")
                if (!file.exists())
                    it.copyTo(file)
            }
        }
    }
}