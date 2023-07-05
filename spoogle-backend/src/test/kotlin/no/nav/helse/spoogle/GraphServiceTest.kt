package no.nav.helse.spoogle

import kotliquery.queryOf
import kotliquery.sessionOf
import no.nav.helse.spoogle.db.AbstractDatabaseTest
import no.nav.helse.spoogle.graph.Graph
import no.nav.helse.spoogle.graph.Identifikatortype
import no.nav.helse.spoogle.graph.Node
import org.intellij.lang.annotations.Language
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.Test

internal class GraphServiceTest: AbstractDatabaseTest() {
    private val service = GraphService(dataSource)

    @Test
    fun `ny sub-graph`() {
        val fnrNode = fnrNode("fnr")
        val orgnrNode = orgnrNode("orgnr")
        val periodeNode = periodeNode("periode_id_1")
        val periodeNode2 = periodeNode("periode_id_2")
        val utbetalingNode = utbetalingNode("utbetaling_id")

        val graph = Graph.buildGraph(
            fnrNode to orgnrNode,
            orgnrNode to periodeNode,
            orgnrNode to periodeNode2,
            periodeNode to utbetalingNode
        )

        service.nySubGraph(graph)
        assertNodes("fnr", "orgnr", "periode_id_1", "utbetaling_id")
        assertEdge("fnr", "orgnr")
        assertEdge("orgnr", "periode_id_1")
        assertEdge("periode_id_1", "utbetaling_id")
    }

    private fun assertNodes(vararg ider: String) {
        val questionMarks = ider.joinToString { "?" }
        @Language("PostgreSQL")
        val query = "SELECT COUNT(1) FROM node WHERE id IN ($questionMarks)"
        val antall = sessionOf(dataSource).use { session ->
            session.run(queryOf(query, *ider).map { it.int(1) }.asSingle)
        }
        assertEquals(ider.size, antall)
    }

    private fun assertEdge(idNodeA: String, idNodeB: String) {
        @Language("PostgreSQL")
        val query = "SELECT COUNT(1) FROM edge WHERE node_A = (SELECT node_id FROM node WHERE id = ?) AND node_B = (SELECT node_id FROM node WHERE id = ?)"
        val antall = sessionOf(dataSource).use { session ->
            session.run(queryOf(query, idNodeA, idNodeB).map { it.int(1) }.asSingle)
        }
        assertEquals(1, antall)
    }

    private fun fnrNode(fnr: String) = Node(fnr, Identifikatortype.FØDSELSNUMMER)
    private fun orgnrNode(orgnr: String) = Node(orgnr, Identifikatortype.ORGANISASJONSNUMMER)
    private fun periodeNode(id: String) = Node(id, Identifikatortype.VEDTAKSPERIODE_ID)
    private fun utbetalingNode(id: String) = Node(id, Identifikatortype.UTBETALING_ID)
}