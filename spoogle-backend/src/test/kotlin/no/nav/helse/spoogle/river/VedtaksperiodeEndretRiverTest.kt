package no.nav.helse.spoogle.river

import no.nav.helse.rapids_rivers.testsupport.TestRapid
import no.nav.helse.spoogle.TreService
import no.nav.helse.spoogle.db.AbstractDatabaseTest
import org.intellij.lang.annotations.Language
import org.junit.jupiter.api.Assertions.assertNotNull
import org.junit.jupiter.api.Test
import java.util.*

internal class VedtaksperiodeEndretRiverTest: AbstractDatabaseTest() {
    private val testRapid = TestRapid()
    private val treService = TreService(dataSource)
    private val vedtaksperiodeId = UUID.fromString("d100e098-8f77-4985-bd6b-bb067dbaaf37")

    init {
        VedtaksperiodeEndretRiver(treService, testRapid)
    }

    @Test
    fun `Les inn vedtaksperiode_endret`() {
        testRapid.sendTestMessage(vedtaksperiodeEndret(vedtaksperiodeId))
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
                        "children": [],
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
