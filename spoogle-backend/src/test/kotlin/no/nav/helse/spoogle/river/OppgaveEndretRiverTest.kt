package no.nav.helse.spoogle.river

import com.github.navikt.tbd_libs.rapids_and_rivers.test_support.TestRapid
import no.nav.helse.spoogle.TreService
import no.nav.helse.spoogle.db.AbstractDatabaseTest
import org.intellij.lang.annotations.Language
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.Test
import java.util.*

internal class OppgaveEndretRiverTest : AbstractDatabaseTest() {
    private val testRapid = TestRapid()
    private val treService = TreService(dataSource)
    private val oppgaveId = "1234"
    private val behandlingId = UUID.randomUUID()
    private val vedtaksperiodeId = UUID.randomUUID()

    init {
        OppgaveEndretRiver(treService, testRapid)
        BehandlingOpprettetRiver(treService, testRapid)
    }

    @Test
    fun `Les inn oppgave_endret`() {
        testRapid.sendTestMessage(behandlingOpprettet(vedtaksperiodeId, behandlingId))
        testRapid.sendTestMessage(oppgaveOpprettet(oppgaveId, behandlingId))
        val tree = treService.finnTre(behandlingId.toString())
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
                            "children": [
                              {
                                "id": "$oppgaveId",
                                "type": "OPPGAVE_ID",
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
            ],
            "ugyldig_fra": null
       } 
    """


}
