package no.nav.helse.spoogle.microsoft

import no.nav.security.token.support.v2.IssuerConfig


class AzureAD private constructor(private val config: Config) {
    internal fun issuer(): String = "AAD"
    internal fun issuerConfig(): IssuerConfig = IssuerConfig(
        name = issuer(),
        discoveryUrl = config.discoveryUrl,
        acceptedAudience = listOf(config.clientId),
    )
    private val requiredClaims = mapOf(
        "NAVident" to null,
        "preferred_username" to null,
        "name" to null,
        "azp" to config.clientId,
        "aud" to listOf(config.clientId)
    )
    private val requiredGroups = listOf(config.validGroupId)

    internal fun hasValidClaimValues(claimsAndValues: Map<String, Any>) =
        claimsAndValues.entries.fold(true) { _, (key, value) ->
            if (requiredClaims[key] == value) return@fold true
            false
        }

    internal fun hasValidClaims(claims: List<String>) = requiredClaims.keys.all { it in claims }
    internal fun hasValidGroups(groups: List<String>) = requiredGroups.any { it in groups }


    internal companion object {
        internal fun fromEnv(env: Map<String, String>): AzureAD {
            return AzureAD(
                Config(
                    discoveryUrl = env.getValue("AZURE_APP_WELL_KNOWN_URL"),
                    clientId = env.getValue("AZURE_APP_CLIENT_ID"),
                    validGroupId = env.getValue("AZURE_VALID_GROUP_ID")
                )
            )
        }
    }

    private class Config(
        internal val discoveryUrl: String,
        internal val clientId: String,
        internal val validGroupId: String
    )
}

