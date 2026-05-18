plugins {
    id("application")
    alias(libs.plugins.kotlin.jvm)
    alias(libs.plugins.kotlin.serialization)
}

application {
    mainClass.set("no.nav.helse.spoogle.AppKt")
    applicationName = "app"
}

dependencies {
    api(libs.rapids.and.rivers)

    implementation(libs.hikari)
    implementation(libs.kotliquery)
    implementation(libs.postgresql)
    implementation(libs.flyway.core)
    implementation(libs.flyway.database.postgresql)

    implementation(libs.logback.syslog4j) {
        exclude(group = "ch.qos.logback")
    }

    implementation(libs.ktor.server.core.jvm)
    implementation(libs.ktor.server.netty.jvm)
    implementation(libs.ktor.server.content.negotiation)
    implementation(libs.ktor.serialization.kotlinx.json)
    implementation(libs.ktor.network.tls.certificates)
    implementation(libs.ktor.server.auth)
    implementation(libs.ktor.server.auth.jwt) {
        exclude(group = "junit")
    }
    implementation(libs.ktor.server.forwarded.header)
    implementation(libs.ktor.server.cors)
    implementation(libs.ktor.server.call.id)
    implementation(libs.ktor.server.status.pages)
    implementation(libs.ktor.server.call.logging)
    implementation(libs.ktor.server.metrics.micrometer)
    implementation(libs.token.validation.ktor)

    implementation(libs.ktor.client.content.negotiation)
    implementation(libs.ktor.client.core.jvm)
    implementation(libs.ktor.client.apache.jvm)

    testImplementation(libs.tbd.libs.test)
    testImplementation(libs.mock.oauth2.server)
    testImplementation(libs.ktor.server.test.host)
    testImplementation(platform(libs.junit.bom))
    testImplementation(libs.junit.jupiter)
    testImplementation(libs.testcontainers.postgresql)
    testRuntimeOnly(libs.junit.platform.launcher)
}

kotlin {
    jvmToolchain(21)
}

tasks {
    processResources {
        from("${rootProject.projectDir}/spoogle-frontend/dist") {
            into("static")
        }
    }
    test {
        useJUnitPlatform()
        testLogging {
            events("skipped", "failed")
            showStackTraces = true
            exceptionFormat = org.gradle.api.tasks.testing.logging.TestExceptionFormat.FULL
        }
    }
}
