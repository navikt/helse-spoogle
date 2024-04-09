package no.nav.helse.spoogle.plugins

import io.ktor.server.application.Application
import io.ktor.server.application.install
import io.ktor.server.auth.Authentication
import net.logstash.logback.argument.StructuredArguments.kv
import no.nav.helse.spoogle.microsoft.AzureAD
import no.nav.security.token.support.v2.TokenSupportConfig
import no.nav.security.token.support.v2.tokenValidationSupport
import org.slf4j.LoggerFactory

private val sikkerlogg = LoggerFactory.getLogger("tjenestekall")

internal fun Application.configureAuthentication(azureAD: AzureAD) {
    install(Authentication) {
        tokenValidationSupport(
            name = "ValidToken",
            config = TokenSupportConfig(azureAD.issuerConfig()),
            additionalValidation = { ctx ->
                val claims = ctx.getClaims(azureAD.issuer())
                val hasValidClaims = azureAD.hasValidClaims(claims.allClaims.keys.toList())

                if (!hasValidClaims) {
                    sikkerlogg.info("Mangler påkrevde claims")
                    sikkerlogg.info("Claims: ${claims.allClaims.entries.joinToString {(k, v) -> "($k: $v)" }}")
                    return@tokenValidationSupport false
                }

                val groups: List<String> = claims.getAsList("groups")
                val appId = claims.getStringClaim("azp")
                val audience = claims.getAsList("aud").toList()

                val hasValidGroup = azureAD.hasValidGroups(groups)
                val hasValidClaimValues = azureAD.hasValidClaimValues(mapOf("aud" to audience, "azp" to appId))
                val validToken = hasValidClaimValues && hasValidGroup

                if (!validToken) {
                    sikkerlogg.info(
                        "Har ikke gyldig token. {}, {}",
                        kv("harGyldigAppId", hasValidClaimValues),
                        kv("harGyldigeGrupper", hasValidGroup),
                    )
                    sikkerlogg.info(
                        "Har følgende grupper: ${groups.joinToString()}",
                    )
                    sikkerlogg.info(
                        "Har følgende appId: $appId",
                    )
                    return@tokenValidationSupport false
                }

                sikkerlogg.info("Vellykket validering av token")
                true
            },
        )
    }
}
