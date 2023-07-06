package no.nav.helse.spoogle

import kotlinx.serialization.Serializable

@Serializable
data class Feilrespons(
    val errorId: String,
    val description: String?
)