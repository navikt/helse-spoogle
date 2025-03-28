package no.nav.helse.spoogle.routes

import io.ktor.http.HttpStatusCode
import io.ktor.server.response.respond
import io.ktor.server.routing.Route
import io.ktor.server.routing.get
import no.nav.helse.spoogle.Bruker
import no.nav.helse.spoogle.BrukerException

internal fun Route.brukerRoutes(identityIssuer: String) {
    get("/api/bruker") {
        val bruker = try {
            Bruker.fromCall(identityIssuer, call)
        } catch (e: BrukerException) {
            return@get call.respond(e.httpStatusCode, e.message!!)
        }
        call.respond(HttpStatusCode.OK, bruker)
    }
}

