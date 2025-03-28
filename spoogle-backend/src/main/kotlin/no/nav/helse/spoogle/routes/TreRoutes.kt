package no.nav.helse.spoogle.routes

import io.ktor.http.HttpStatusCode
import io.ktor.server.request.path
import io.ktor.server.response.respond
import io.ktor.server.routing.Route
import io.ktor.server.routing.get
import no.nav.helse.spoogle.Bruker
import no.nav.helse.spoogle.BrukerException
import no.nav.helse.spoogle.ITreeService
import org.intellij.lang.annotations.Language
import org.slf4j.LoggerFactory

private val auditlogg = LoggerFactory.getLogger("auditLogger")

internal fun Route.treRoutes(
    service: ITreeService,
    identityIssuer: String,
) {
    get("/api/sok/{id}") {
        val id =
            call.parameters["id"]
                ?: return@get call.respond(HttpStatusCode.BadRequest, "Id må være satt")
        val bruker =
            try {
                Bruker.fromCall(identityIssuer, call)
            } catch (e: BrukerException) {
                return@get call.respond(e.httpStatusCode, e.message!!)
            }
        val requestPath = call.request.path()

        val tre = service.finnTre(id)
        auditlogg.info("end=${System.currentTimeMillis()} duid=$id suid=${bruker.ident()} request=$requestPath")
        val treJson = tre?.toJson() ?: return@get call.respond(HttpStatusCode.NotFound)

        val stiFraRotTilId = tre.pathTo(id)

        @Language("JSON")
        val response = """
            {
                "tree": $treJson,
                "path": ${stiFraRotTilId.map { """"$it"""" }}
            }
        """
        call.respond(HttpStatusCode.OK, response)
    }
}
