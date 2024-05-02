package no.nav.helse.spoogle.river

import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import no.nav.helse.rapids_rivers.testsupport.TestRapid
import no.nav.helse.spoogle.TreService
import no.nav.helse.spoogle.db.AbstractDatabaseTest
import org.intellij.lang.annotations.Language
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.Test

internal class OppgaveEndretRiverTest : AbstractDatabaseTest() {
    private val testRapid = TestRapid()
    private val treService = TreService(dataSource)
    private val fødselsnummer = "12345678910"
    private val oppgaveId = "1234"

    init {
        OppgaveEndretRiver(treService, testRapid)
    }

    @Test
    fun `Les inn oppgave_endret`() {
        testRapid.sendTestMessage(oppgaveOpprettet)
        val tree = treService.finnTre(fødselsnummer)
        assertNotNull(tree)

        val json = tree?.let { jacksonObjectMapper().readTree(it.toJson()) }
        val expectedJson = jacksonObjectMapper().readTree(expectedJson)

        assertEquals(expectedJson, json)
    }

    @Language("JSON")
    private val expectedJson = """{
  "id": "$fødselsnummer",
  "type": "FØDSELSNUMMER",
  "children": [
    {
      "id": "$oppgaveId",
      "type": "OPPGAVE_ID",
      "ugyldig_fra": null,
      "children": []
    }
  ],
  "ugyldig_fra": null
} 
    """

    @Language("JSON")
    private val oppgaveOpprettet = """{
    "@event_name": "oppgave_opprettet",
    "oppgaveId": "$oppgaveId",
    "tilstand": "AvventerSaksbehandler",
    "egenskaper": [],
    "fødselsnummer": "12345678910"
}
    """
}
