package no.nav.helse.spoogle.river

import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import no.nav.helse.rapids_rivers.testsupport.TestRapid
import no.nav.helse.spoogle.TreService
import no.nav.helse.spoogle.db.AbstractDatabaseTest
import org.intellij.lang.annotations.Language
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.Test
import java.util.*

internal class BehandlingOpprettetRiverTest: AbstractDatabaseTest() {
    private val testRapid = TestRapid()
    private val treService = TreService(dataSource)
    private val vedtaksperiodeId = UUID.fromString("62db1d3a-cb36-499d-b280-f7c9cebcf08d")
    private val behandlingId = UUID.fromString("50e49e94-64c2-423f-b492-fe84be47e7c6")

    init {
        BehandlingOpprettetRiver(treService, testRapid)
    }

    @Test
    fun `Les inn behandling_opprettet`() {
        testRapid.sendTestMessage(behandlingOpprettet)
        val tree = treService.finnTre(vedtaksperiodeId.toString())
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
                        "children": [
                          {
                            "id": "$behandlingId",
                            "type": "BEHANDLING_ID",
                            "children": [],
                            "ugyldig_fra": null
                          }
                        ],
                        "ugyldig_fra": null
                    }
                ],
                "ugyldig_fra": null
            }
            ],
            "ugyldig_fra": null
       } 
    """

    @Language("JSON")
    private val behandlingOpprettet = """{
    "@event_name": "behandling_opprettet",
    "organisasjonsnummer": "987654321",
    "vedtaksperiodeId": "$vedtaksperiodeId",
    "behandlingId": "$behandlingId",
    "@id": "4c443e35-e993-49d3-a5c1-e230fa32f5e0",
    "@opprettet": "2018-01-01T00:00:00.000",
    "aktørId": "1234567891011",
    "fødselsnummer": "12345678910"
}
    """
}