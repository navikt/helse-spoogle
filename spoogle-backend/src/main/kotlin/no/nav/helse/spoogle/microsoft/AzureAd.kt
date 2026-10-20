package no.nav.helse.spoogle.microsoft

import kotlinx.serialization.json.Json
import kotlinx.serialization.json.jsonArray
import kotlinx.serialization.json.jsonObject
import kotlinx.serialization.json.jsonPrimitive
import no.nav.security.token.support.v3.IssuerConfig

class AzureAD private constructor(
    private val config: Config,
) {
    internal fun issuer(): String = "AAD"

    internal fun issuerConfig(): IssuerConfig =
        IssuerConfig(
            name = issuer(),
            discoveryUrl = config.discoveryUrl,
            acceptedAudience = listOf(config.clientId),
        )

    private val requiredClaims = listOf("NAVident", "preferred_username", "name", "azp", "aud")
    private val requiredGroups = config.validGroupId
    private val authorizedParties = setOf(config.clientId) + config.preAuthorizedClientIds

    internal fun hasValidClaimValues(
        audience: List<String>,
        authorizedParty: String?,
    ) = config.clientId in audience && authorizedParty in authorizedParties

    internal fun hasValidClaims(claims: List<String>) = requiredClaims.all { it in claims }

    internal fun hasValidGroups(groups: List<String>) = requiredGroups.any { it in groups }

    internal companion object {
        internal fun fromEnv(env: Map<String, String>): AzureAD =
            AzureAD(
                Config(
                    discoveryUrl = env.getValue("AZURE_APP_WELL_KNOWN_URL"),
                    clientId = env.getValue("AZURE_APP_CLIENT_ID"),
                    validGroupId = env.getValue("AZURE_VALID_GROUP_IDS").split(","),
                    preAuthorizedClientIds = env["AZURE_APP_PRE_AUTHORIZED_APPS"]?.let(::clientIds) ?: emptySet(),
                ),
            )

        private fun clientIds(preAuthorizedApps: String): Set<String> =
            Json
                .parseToJsonElement(preAuthorizedApps)
                .jsonArray
                .map {
                    it.jsonObject
                        .getValue("clientId")
                        .jsonPrimitive.content
                }.toSet()
    }

    private class Config(
        val discoveryUrl: String,
        val clientId: String,
        val validGroupId: List<String>,
        val preAuthorizedClientIds: Set<String>,
    )
}
