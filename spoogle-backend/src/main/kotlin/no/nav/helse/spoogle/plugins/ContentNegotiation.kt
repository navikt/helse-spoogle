package no.nav.helse.spoogle.plugins

import io.ktor.client.HttpClientConfig
import io.ktor.client.engine.HttpClientEngineConfig
import io.ktor.serialization.kotlinx.json.json
import io.ktor.server.application.Application
import io.ktor.server.application.install
import io.ktor.client.plugins.contentnegotiation.ContentNegotiation as ClientContentNegotiation
import io.ktor.server.plugins.contentnegotiation.ContentNegotiation as ServerContentNegotation

internal fun Application.configureServerContentNegotiation() {
    install(ServerContentNegotation) {
        json()
    }
}

internal fun <T: HttpClientEngineConfig> HttpClientConfig<T>.configureClientContentNegotiation() {
    install(ClientContentNegotiation) {
        json()
    }
}