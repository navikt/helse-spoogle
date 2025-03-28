package no.nav.helse.spoogle.river

import com.github.navikt.tbd_libs.rapids_and_rivers.test_support.TestRapid
import no.nav.helse.spoogle.TreService
import no.nav.helse.spoogle.db.AbstractDatabaseTest
import org.intellij.lang.annotations.Language
import org.junit.jupiter.api.Assertions.assertNotNull
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
        testRapid.sendTestMessage(behandlingOpprettet(vedtaksperiodeId, behandlingId))
        val tree = treService.finnTre(vedtaksperiodeId.toString())
        assertNotNull(tree)
        assertJson(expectedJson, tree)
    }

    @Language("JSON")
    private val expectedJson = """
       {
            "id": "12345678910",
            "type": "FØDSELSNUMMER",
            "children": [
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
}
