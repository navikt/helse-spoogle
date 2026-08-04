plugins {
    id("no.nav.helse.sas.sas-deployable")
}

sasDeployable {
    mainClass = "no.nav.helse.opprydding.AppKt"
    imageName = "${rootProject.name}-opprydding-dev"
}

dependencies {
    api(libs.rapids.and.rivers)

    implementation(libs.google.cloud.sql.socket.factory)
    implementation(libs.postgresql)
    implementation(libs.kotliquery)
    implementation(libs.hikari)

    testImplementation(project(":spoogle-backend")) // for å få tilgang på db/migrations-filene
    testImplementation(libs.tbd.libs.test)
    testImplementation(libs.flyway.core)
    testImplementation(libs.flyway.database.postgresql)
    testImplementation(libs.testcontainers.postgresql) {
        exclude("com.fasterxml.jackson.core")
    }
}
