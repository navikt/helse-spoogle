package no.nav.helse.opprydding

import kotliquery.queryOf
import kotliquery.sessionOf
import no.nav.helse.rapids_rivers.testsupport.TestRapid
import org.intellij.lang.annotations.Language
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test
import java.time.LocalDateTime
import java.util.*

internal class AppTest: AbstractDatabaseTest() {
    private val testRapid = TestRapid()

    init {
        SlettPersonRiver(testRapid, personDao)
    }

    @Test
    fun `slettemelding medfører at person slettes fra databasen`() {
        opprettTre("12345678910", "987654321")
        val nodeId = finnNodeId("12345678910")
        testRapid.sendTestMessage(slettemelding("12345678910"))
        assertFinnesIkke(nodeId)
    }

    @Test
    fun `sletter kun aktuelt fnr`() {
        val fnr1 = "12345678910"
        val fnr2 = "99999999999"
        opprettTre(fnr1, "987654321")
        opprettTre(fnr2, "999999999")
        val nodeId1 = finnNodeId(fnr1)
        val nodeId2 = finnNodeId(fnr2)
        testRapid.sendTestMessage(slettemelding(fnr1))
        assertFinnesIkke(nodeId1)
        assertFinnes(nodeId2)
    }

    @Language("JSON")
    private fun slettemelding(fødselsnummer: String) = """
        {
          "@event_name": "slett_person",
          "@id": "${UUID.randomUUID()}",
          "opprettet": "${LocalDateTime.now()}",
          "fødselsnummer": "$fødselsnummer"
        }
    """.trimIndent()

    private fun assertFinnesIkke(nodeId: Long?) {
        assertNodeAntall(nodeId, 0)
        assertEdgeAntall(nodeId, 0)
    }

    private fun assertFinnes(nodeId: Long?) {
        assertNodeAntall(nodeId, 1)
        assertEdgeAntall(nodeId, 1)
    }

    private fun assertNodeAntall(nodeId: Long?, forventetAntall: Int) {
        @Language("PostgreSQL")
        val query = """SELECT COUNT(1) FROM node WHERE node_id = ?"""
        val antall = sessionOf(dataSource).use { session ->
            session.run(queryOf(query, nodeId).map { it.int(1) }.asSingle)
        }
        assertEquals(forventetAntall, antall)
    }

    private fun assertEdgeAntall(nodeId: Long?, forventetAntall: Int) {
        @Language("PostgreSQL")
        val query = """SELECT COUNT(1) FROM edge WHERE node_a = ?"""
        val antall = sessionOf(dataSource).use { session ->
            session.run(queryOf(query, nodeId).map { it.int(1) }.asSingle)
        }
        assertEquals(forventetAntall, antall)
    }

    private fun finnNodeId(fødselsnummer: String): Long? {
        @Language("PostgreSQL")
        val query = """
           SELECT node_id FROM node WHERE id = ? AND id_type = 'FØDSELSNUMMER'
        """
        return sessionOf(dataSource).use { session ->
            session.run(queryOf(query, fødselsnummer).map { it.long("node_id") }.asSingle)
        }
    }
}