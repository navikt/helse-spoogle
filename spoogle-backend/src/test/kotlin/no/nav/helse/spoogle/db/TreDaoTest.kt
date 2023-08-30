package no.nav.helse.spoogle.db

import kotliquery.queryOf
import kotliquery.sessionOf
import no.nav.helse.spoogle.tre.NodeDto
import org.intellij.lang.annotations.Language
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.Test

internal class TreDaoTest: AbstractDatabaseTest() {
    private val dao = TreDao(dataSource)

    @Test
    fun `opprett node`() {
        dao.nyNode(nodeDto("A"))
        assertNode("A")
    }

    @Test
    fun `forsøk å opprette samme node to ganger`() {
        dao.nyNode(nodeDto("A"))
        assertDoesNotThrow {
            dao.nyNode(nodeDto("A"))
        }
    }

    @Test
    fun `opprett edge`() {
        dao.nyNode(nodeDto("A"))
        dao.nyNode(nodeDto("B"))
        dao.nyKant(nodeDto("A"), nodeDto("B"))
        assertEdge("A", "B")
    }

    @Test
    fun `forsøk å opprette samme edge to ganger`() {
        dao.nyNode(nodeDto("A"))
        dao.nyNode(nodeDto("B"))
        dao.nyKant(nodeDto("A"), nodeDto("B"))
        assertDoesNotThrow {
            dao.nyKant(nodeDto("B"), nodeDto("A"))
        }
        assertEdge("A", "B")
    }

    @Test
    fun `invalider relasjon for node`() {
        dao.nyNode(nodeDto("A"))
        dao.nyNode(nodeDto("B"))
        dao.nyNode(nodeDto("C"))
        dao.nyKant(nodeDto("A"), nodeDto("B"))
        dao.nyKant(nodeDto("B"), nodeDto("C"))
        dao.invaliderRelasjonerFor(nodeDto("B"))
        assertUgyldig("A", "B")
        assertUgyldig("B", "C")
    }

    private fun assertUgyldig(idNodeA: String, idNodeB: String) {
        @Language("PostgreSQL")
        val query = """
             SELECT ugyldig FROM edge 
             WHERE 
                node_a = (SELECT node_id FROM node WHERE id = ?) AND
                node_b = (SELECT node_id FROM node WHERE id = ?)
        """

        val ugyldig = sessionOf(dataSource).use { session ->
            session.run(queryOf(query, idNodeA, idNodeB).map { it.localDateTimeOrNull("ugyldig") }.asSingle)
        }

        assertNotNull(ugyldig)
    }

    private fun assertNode(id: String) {
        @Language("PostgreSQL")
        val query = "SELECT COUNT(1) FROM node WHERE id = ?"
        val antall = sessionOf(dataSource).use { session ->
            session.run(queryOf(query, id).map { it.int(1) }.asSingle)
        }
        assertEquals(1, antall)
    }

    private fun assertEdge(idNodeA: String, idNodeB: String) {
        @Language("PostgreSQL")
        val query = "SELECT COUNT(1) FROM edge WHERE node_A = (SELECT node_id FROM node WHERE id = ?) AND node_B = (SELECT node_id FROM node WHERE id = ?)"
        val antall = sessionOf(dataSource).use { session ->
            session.run(queryOf(query, idNodeA, idNodeB).map { it.int(1) }.asSingle)
        }
        assertEquals(1, antall)
    }

    private fun nodeDto(id: String) = NodeDto(id, "type", emptyList(), emptyList())
}