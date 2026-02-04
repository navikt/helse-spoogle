package no.nav.helse.spoogle.river

import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import no.nav.helse.spoogle.tre.Tre
import org.intellij.lang.annotations.Language
import org.junit.jupiter.api.Assertions.assertEquals
import java.util.*

@Language("JSON")
internal fun behandlingOpprettet(
    vedtaksperiodeId: UUID,
    behandlingId: UUID,
    fødselsnummer: String = "12345678910",
    organisasjonsnummer: String = "987654321",
) = """{
    "@event_name": "behandling_opprettet",
    "organisasjonsnummer": "$organisasjonsnummer",
    "yrkesaktivitetstype": "ARBEIDSTAKER",
    "vedtaksperiodeId": "$vedtaksperiodeId",
    "behandlingId": "$behandlingId",
    "@id": "4c443e35-e993-49d3-a5c1-e230fa32f5e0",
    "@opprettet": "2018-01-01T00:00:00.000",
    "fødselsnummer": "$fødselsnummer"
}"""

@Language("JSON")
internal fun behandlingOpprettetSelvstendig(
    vedtaksperiodeId: UUID,
    behandlingId: UUID,
    fødselsnummer: String = "12345678910",
) = """{
    "@event_name": "behandling_opprettet",
    "yrkesaktivitetstype": "SELVSTENDIG",
    "vedtaksperiodeId": "$vedtaksperiodeId",
    "behandlingId": "$behandlingId",
    "@id": "4c443e35-e993-49d3-a5c1-e230fa32f5e0",
    "@opprettet": "2018-01-01T00:00:00.000",
    "fødselsnummer": "$fødselsnummer"
}"""

@Language("JSON")
internal fun oppgaveOpprettet(
    oppgaveId: String,
    behandlingId: UUID,
    fødselsnummer: String = "12345678910",
) = """{
    "@event_name": "oppgave_opprettet",
    "oppgaveId": "$oppgaveId",
    "tilstand": "AvventerSaksbehandler",
    "behandlingId": "$behandlingId",
    "egenskaper": [],
    "fødselsnummer": "$fødselsnummer"
}
    """

@Language("JSON")
internal fun vedtaksperiodeEndret(
    vedtaksperiodeId: UUID,
    fødselsnummer: String = "12345678910",
    organisasjonsnummer: String = "987654321",
) = """{
    "@event_name": "vedtaksperiode_endret",
    "organisasjonsnummer": "$organisasjonsnummer",
    "vedtaksperiodeId": "$vedtaksperiodeId",
    "gjeldendeTilstand": "START",
    "forrigeTilstand": "AVVENTER_INNTEKTSMELDING",
    "hendelser": [
        "c9214688-b47a-448b-bbc6-d4cb51dc0380"
    ],
    "makstid": "2018-01-01T00:00:00.000",
    "fom": "2018-01-01",
    "tom": "2018-01-31",
    "@id": "4c443e35-e993-49d3-a5c1-e230fa32f5e0",
    "@opprettet": "2018-01-01T00:00:00.000",
    "fødselsnummer": "$fødselsnummer"
}
    """

@Language("JSON")
internal fun vedtaksperiodeForkastet(
    vedtaksperiodeId: UUID,
    fødselsnummer: String = "12345678910",
    organisasjonsnummer: String = "987654321",
) = """{
    "@event_name": "vedtaksperiode_forkastet",
    "organisasjonsnummer": "$organisasjonsnummer",
    "vedtaksperiodeId": "$vedtaksperiodeId",
    "tilstand": "AVVENTER_HISTORIKK",
    "hendelser": [
        "c9214688-b47a-448b-bbc6-d4cb51dc0380"
    ],
    "forlengerPeriode": true,
    "harPeriodeInnenfor16Dager": false,
    "trengerArbeidsgiveropplysninger": false,
    "sykmeldingsperioder": [],
    "makstid": "2018-01-01T00:00:00.000",
    "fom": "2018-01-01",
    "tom": "2018-01-31",
    "@id": "4c443e35-e993-49d3-a5c1-e230fa32f5e0",
    "@opprettet": "2018-01-01T00:00:00.000",
    "fødselsnummer": "$fødselsnummer"
}
    """

internal fun assertJson(expectedJson: String, tre: Tre?) {
    val json = tre?.let { jacksonObjectMapper().readTree(it.toJson()) }
    assertEquals(jacksonObjectMapper().readTree(expectedJson), json)
}
