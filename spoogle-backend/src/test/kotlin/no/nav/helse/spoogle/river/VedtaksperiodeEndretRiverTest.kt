package no.nav.helse.spoogle.river

import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import no.nav.helse.rapids_rivers.testsupport.TestRapid
import no.nav.helse.spoogle.TreeService
import no.nav.helse.spoogle.db.AbstractDatabaseTest
import org.intellij.lang.annotations.Language
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.Test
import java.util.*

internal class VedtaksperiodeEndretRiverTest: AbstractDatabaseTest() {
    private val testRapid = TestRapid()
    private val treeService = TreeService(dataSource)
    private val vedtaksperiodeId = UUID.fromString("d100e098-8f77-4985-bd6b-bb067dbaaf37")

    init {
        VedtaksperiodeEndretRiver(treeService, testRapid)
    }

    @Test
    fun `Les inn vedtaksperiode_endret`() {
        testRapid.sendTestMessage(vedtaksperiodeEndret)
        val tree = treeService.finnTre(vedtaksperiodeId.toString())
        assertNotNull(tree)

        val json = tree?.let { jacksonObjectMapper().readTree(it.toJson()) }
        val expectedJson = jacksonObjectMapper().readTree(expectedJson)

        assertEquals(expectedJson, json)
    }

    @Language("JSON")
    private val expectedJson = """
       {
            "id": "12345678910",
            "type": "FØDSELSNUMMER",
            "children": [
            {
                "id": "1234567891011",
                "type": "AKTØR_ID",
                "children": []
            },
            {
                "id": "987654321",
                "type": "ORGANISASJONSNUMMER",
                "children": [
                    {
                        "id": "$vedtaksperiodeId",
                        "type": "VEDTAKSPERIODE_ID",
                        "children": []
                    }
                ]
            }
            ]
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