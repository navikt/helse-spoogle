package no.nav.helse.spoogle.routes

import io.ktor.http.HttpStatusCode
import io.ktor.server.application.call
import io.ktor.server.response.respond
import io.ktor.server.routing.Route
import io.ktor.server.routing.get
import no.nav.helse.spoogle.ITreeService

internal fun Route.treeRoutes(service: ITreeService) {
    get("/api/sok/{id}") {
        val id = call.parameters["id"]
            ?: return@get call.respond(HttpStatusCode.BadRequest, "Id må være satt")
        call.respond(HttpStatusCode.OK, service.finnTre(id)?.toJson() ?: "{}")
    }
}