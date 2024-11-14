package no.nav.helse.spoogle.plugins

import io.ktor.server.application.Application
import io.ktor.server.application.install
import io.ktor.server.plugins.callid.CallId
import io.ktor.server.plugins.callloging.CallLogging
import io.ktor.server.plugins.forwardedheaders.XForwardedHeaders
import org.slf4j.event.Level
import java.util.*

internal fun Application.configureUtilities() {
    install(CallId) {
        generate {
            UUID.randomUUID().toString()
        }
    }
    install(CallLogging) {
        disableDefaultColors()
        level = Level.DEBUG
    }
    install(XForwardedHeaders)
}
