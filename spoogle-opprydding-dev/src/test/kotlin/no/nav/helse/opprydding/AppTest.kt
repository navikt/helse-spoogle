package no.nav.helse.opprydding

import com.github.navikt.tbd_libs.rapids_and_rivers.test_support.TestRapid
import kotliquery.queryOf
import kotliquery.sessionOf
import org.intellij.lang.annotations.Language
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test
import java.time.LocalDateTime
import java.util.*

internal class AppTest : AbstractDatabaseTest() {
    private val testRapid = TestRapid()

    init {
        SlettPersonRiver(testRapid, personDao)
    }

    @Test
    fun `slettemelding medfører at person slettes fra databasen`() {
        opprettTre("12345678910", "987654321")
        val fnrNodeKey = finnNodeKey("12345678910")
        val orgnrNodeKey = finnNodeKey("987654321")
        testRapid.sendTestMessage(slettemelding("12345678910"))
        assertFinnesIkke(fnrNodeKey)
        assertFinnesIkke(orgnrNodeKey)
    }

    @Test
    fun `sletter kun aktuelt fnr`() {
        val fnr1 = "12345678910"
        val fnr2 = "99999999999"
        opprettTre(fnr1, "987654321")
        opprettTre(fnr2, "999999999")
        val nodeId1 = finnNodeKey(fnr1)
        val nodeId2 = finnNodeKey(fnr2)
        testRapid.sendTestMessage(slettemelding(fnr1))
        assertFinnesIkke(nodeId1)
        assertFinnes(nodeId2)
    }

    @Language("JSON")
    private fun slettemelding(fødselsnummer: String) =
        """
        {
          "@event_name": "slett_person",
          "@id": "${UUID.randomUUID()}",
          "opprettet": "${LocalDateTime.now()}",
          "fødselsnummer": "$fødselsnummer"
        }
        """.trimIndent()

    private fun assertFinnesIkke(node: String?) {
        assertRelasjonAntall(node, 0)
    }

    private fun assertFinnes(node: String?) {
        assertRelasjonAntall(node, 1)
    }

    private fun assertRelasjonAntall(
        node: String?,
        forventetAntall: Int,
    ) {
        @Language("PostgreSQL")
        val query = """SELECT COUNT(1) FROM relasjon WHERE node = ?"""
        val antall =
            sessionOf(dataSource).use { session ->
                session.run(queryOf(query, node).map { it.int(1) }.asSingle)
            }
        assertEquals(forventetAntall, antall)
    }

    private fun finnNodeKey(fødselsnummer: String): String? {
        @Language("PostgreSQL")
        val query = """
           SELECT node FROM relasjon WHERE node = ? AND type = 'FØDSELSNUMMER'
        """
        return sessionOf(dataSource).use { session ->
            session.run(queryOf(query, fødselsnummer).map { it.string("node") }.asSingle)
        }
    }
}
