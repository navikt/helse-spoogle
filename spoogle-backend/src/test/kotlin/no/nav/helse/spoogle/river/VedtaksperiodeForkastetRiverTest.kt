package no.nav.helse.spoogle.river

import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import kotliquery.queryOf
import kotliquery.sessionOf
import no.nav.helse.rapids_rivers.testsupport.TestRapid
import no.nav.helse.spoogle.TreService
import no.nav.helse.spoogle.db.AbstractDatabaseTest
import org.intellij.lang.annotations.Language
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertNotNull
import org.junit.jupiter.api.Test
import java.time.LocalDateTime
import java.util.*

internal class VedtaksperiodeForkastetRiverTest: AbstractDatabaseTest() {
    private val testRapid = TestRapid()
    private val treService = TreService(dataSource)
    private val vedtaksperiodeId = UUID.fromString("d100e098-8f77-4985-bd6b-bb067dbaaf37")

    init {
        VedtaksperiodeEndretRiver(treService, testRapid)
        VedtaksperiodeForkastetRiver(treService, testRapid)
    }

    @Test
    fun `Les inn vedtaksperiode_forkastet`() {
        testRapid.sendTestMessage(vedtaksperiodeEndret)
        testRapid.sendTestMessage(vedtaksperiodeForkastet)
        val tree = treService.finnTre(vedtaksperiodeId.toString())
        assertNotNull(tree)

        val ugyldigFra = finnUgyldigFra("987654321+12345678910", vedtaksperiodeId.toString())
        assertNotNull(ugyldigFra)

        val json = tree?.let { jacksonObjectMapper().readTree(it.toJson()) }
        val expectedJson = jacksonObjectMapper().readTree(expectedJson(ugyldigFra))

        assertEquals(expectedJson, json)
    }

    private fun finnUgyldigFra(parentId: String, childId: String): LocalDateTime? {
        @Language("PostgreSQL")
        val query = """
           SELECT ugyldig FROM edge WHERE node_a = (SELECT node_id FROM node WHERE id = ?) AND node_b = (SELECT node_id FROM node WHERE id = ?) 
        """

        return sessionOf(dataSource).use {
            it.run(queryOf(query, parentId, childId).map { it.localDateTimeOrNull("ugyldig") }.asSingle)
        }
    }

    @Language("JSON")
    private fun expectedJson(ugyldigFra: LocalDateTime? = null) = """
       {
            "id": "12345678910",
            "type": "FØDSELSNUMMER",
            "children": [
            {
                "id": "1234567891011",
                "type": "AKTØR_ID",
                "children": [],
                "ugyldig_fra": null
            },
            {
                "id": "987654321",
                "type": "ORGANISASJONSNUMMER",
                "children": [
                    {
                        "id": "$vedtaksperiodeId",
                        "type": "VEDTAKSPERIODE_ID",
                        "children": [],
                        "ugyldig_fra": ${ugyldigFra?.let { """ "$it" """ }}
                    }
                ],
                "ugyldig_fra": null
            }
            ],
            "ugyldig_fra": null
       } 
    """

    @Language("JSON")
    private val vedtaksperiodeForkastet = """{
    "@event_name": "vedtaksperiode_forkastet",
    "organisasjonsnummer": "987654321",
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
    "aktørId": "1234567891011",
    "fødselsnummer": "12345678910"
}
    """

    @Language("JSON")
    private val vedtaksperiodeEndret = """{
    "@event_name": "vedtaksperiode_endret",
    "organisasjonsnummer": "987654321",
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
    "aktørId": "1234567891011",
    "fødselsnummer": "12345678910"
}
    """
}

