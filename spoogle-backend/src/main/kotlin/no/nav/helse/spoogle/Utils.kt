package no.nav.helse.spoogle

import com.fasterxml.jackson.databind.JsonNode
import java.util.*

internal fun JsonNode.asUUID() = UUID.fromString(asText())