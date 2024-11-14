package no.nav.helse.spoogle.river

import kotliquery.queryOf
import kotliquery.sessionOf
import no.nav.helse.rapids_rivers.testsupport.TestRapid
import no.nav.helse.spoogle.TreService
import no.nav.helse.spoogle.db.AbstractDatabaseTest
import org.intellij.lang.annotations.Language
import org.junit.jupiter.api.Assertions.assertNotNull
import org.junit.jupiter.api.Test
import java.time.LocalDateTime
import java.util.*

internal class VedtaksperiodeForkastetRiverTest : AbstractDatabaseTest() {
    private val testRapid = TestRapid()
    private val treService = TreService(dataSource)
    private val vedtaksperiodeId = UUID.fromString("d100e098-8f77-4985-bd6b-bb067dbaaf37")

    init {
        VedtaksperiodeEndretRiver(treService, testRapid)
        VedtaksperiodeForkastetRiver(treService, testRapid)
    }

    @Test
    fun `Les inn vedtaksperiode_forkastet`() {
        testRapid.sendTestMessage(vedtaksperiodeEndret(vedtaksperiodeId))
        testRapid.sendTestMessage(vedtaksperiodeForkastet(vedtaksperiodeId))
        val tree = treService.finnTre(vedtaksperiodeId.toString())
        assertNotNull(tree)

        val ugyldigFra = finnUgyldigFra("987654321+12345678910", vedtaksperiodeId.toString())
        assertNotNull(ugyldigFra)
        assertJson(expectedJson(ugyldigFra), tree)
    }

    private fun finnUgyldigFra(
        forelderId: String,
        barnId: String,
    ): LocalDateTime? {
        @Language("PostgreSQL")
        val query = """
           SELECT ugyldig FROM relasjon WHERE forelder = ? AND node = ? 
        """

        return sessionOf(dataSource).use { session ->
            session.run(queryOf(query, forelderId, barnId).map { it.localDateTimeOrNull("ugyldig") }.asSingle)
        }
    }

    @Language("JSON")
    private fun expectedJson(ugyldigFra: LocalDateTime? = null) =
        """
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
                        "ugyldig_fra": ${ugyldigFra?.let { """ "$it" """ }}
                    }
                ],
                "ugyldig_fra": null
            }
            ],
            "ugyldig_fra": null
       } 
    """
}
