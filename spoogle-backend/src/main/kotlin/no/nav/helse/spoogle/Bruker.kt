@file:UseSerializers(UUIDSerializer::class)

package no.nav.helse.spoogle

import io.ktor.http.HttpStatusCode
import io.ktor.server.application.ApplicationCall
import io.ktor.server.auth.authentication
import kotlinx.serialization.Serializable
import kotlinx.serialization.UseSerializers
import no.nav.security.token.support.v3.TokenValidationContextPrincipal
import java.util.*

@Serializable
class Bruker(
    private val epostadresse: String,
    private val navn: String,
    private val ident: String,
    private val oid: UUID,
) {
    internal fun ident() = ident

    internal companion object {
        internal fun fromCall(
            issuer: String,
            call: ApplicationCall,
        ): Bruker {
            return Bruker(
                epostadresse = call.getClaim(issuer, "preferred_username") ?: throw BrukerException.ManglerEpostadresse(),
                navn = call.getClaim(issuer, "name") ?: throw BrukerException.ManglerNavn(),
                ident = call.getClaim(issuer, "NAVident") ?: throw BrukerException.ManglerIdent(),
                oid = call.getClaim(issuer, "oid")?.let(UUID::fromString) ?: throw BrukerException.ManglerOid(),
            )
        }

        private fun ApplicationCall.getClaim(
            issuer: String,
            name: String,
        ): String? =
            this.authentication.principal<TokenValidationContextPrincipal>()
                ?.context
                ?.getClaims(issuer)
                ?.getStringClaim(name)
    }

    override fun toString(): String {
        return navn
    }

    override fun equals(other: Any?) =
        this === other || (
            other is Bruker &&
                javaClass == other.javaClass &&
                epostadresse == other.epostadresse &&
                navn == other.navn &&
                ident == other.ident &&
                oid == other.oid
        )

    override fun hashCode(): Int {
        var result = epostadresse.hashCode()
        result = 31 * result + navn.hashCode()
        result = 31 * result + ident.hashCode()
        result = 31 * result + oid.hashCode()
        return result
    }
}

internal sealed class BrukerException(message: String) : Exception(message) {
    internal abstract val httpStatusCode: HttpStatusCode

    internal class ManglerEpostadresse :
        BrukerException("Token mangler epostadresse (claim: preferred_username)") {
        override val httpStatusCode: HttpStatusCode = HttpStatusCode.BadRequest
    }

    internal class ManglerNavn :
        BrukerException("Token mangler navn (claim: name)") {
        override val httpStatusCode: HttpStatusCode = HttpStatusCode.BadRequest
    }

    internal class ManglerIdent :
        BrukerException("Token mangler ident (claim: NAVident)") {
        override val httpStatusCode: HttpStatusCode = HttpStatusCode.BadRequest
    }

    internal class ManglerOid :
        BrukerException("Token mangler oid (claim: oid)") {
        override val httpStatusCode: HttpStatusCode = HttpStatusCode.BadRequest
    }
}
