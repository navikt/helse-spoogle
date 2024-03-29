package no.nav.helse.spoogle.routes

import io.ktor.http.HttpStatusCode
import io.ktor.server.application.call
import io.ktor.server.auth.jwt.JWTPrincipal
import io.ktor.server.auth.principal
import io.ktor.server.request.path
import io.ktor.server.response.respond
import io.ktor.server.routing.Route
import io.ktor.server.routing.get
import no.nav.helse.spoogle.ITreeService
import org.intellij.lang.annotations.Language
import org.slf4j.LoggerFactory

private val auditlogg = LoggerFactory.getLogger("auditLogger")

internal fun Route.treRoutes(service: ITreeService) {
    get("/api/sok/{id}") {
        val id = call.parameters["id"]
            ?: return@get call.respond(HttpStatusCode.BadRequest, "Id må være satt")
        call.principal<JWTPrincipal>()?.let { principal ->
            val path = call.request.path()
            val navIdent = principal.payload.getClaim("NAVident").asString()
            auditlogg.info("CEF:0|Vedtaksløsning for sykepenger|Spoogle|1.0|audit:access|Sporingslogg|INFO|end=${System.currentTimeMillis()} duid=${id} suid=$navIdent request=$path")
        }
        val tree = service.finnTre(id)
        val treeJson = tree?.toJson() ?: return@get call.respond(HttpStatusCode.NotFound)

        val path = tree.pathTo(id)
        @Language("JSON")
        val response = """
            {
                "tree": $treeJson,
                "path": ${path.map { """"$it"""" }}
            }
        """
        call.respond(HttpStatusCode.OK, response)
    }
}