package no.nav.helse.spoogle

import kotliquery.queryOf
import kotliquery.sessionOf
import no.nav.helse.spoogle.db.AbstractDatabaseTest
import no.nav.helse.spoogle.tree.Tree
import no.nav.helse.spoogle.tree.Identifikatortype
import no.nav.helse.spoogle.tree.Node
import org.intellij.lang.annotations.Language
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.Test

internal class TreeServiceTest: AbstractDatabaseTest() {
    private val service = TreeService(dataSource)

    @Test
    fun `ny gren`() {
        val fnrNode = fnrNode("fnr")
        val orgnrNode = orgnrNode("orgnr")
        val periodeNode = periodeNode("periode_id_1")
        val periodeNode2 = periodeNode("periode_id_2")
        val utbetalingNode = utbetalingNode("utbetaling_id")

        fnrNode parentOf orgnrNode
        orgnrNode parentOf periodeNode
        orgnrNode parentOf periodeNode2
        periodeNode parentOf utbetalingNode

        val tree = Tree.buildTree(fnrNode)

        service.nyGren(tree)
        assertNodes("fnr", "orgnr", "periode_id_1", "utbetaling_id")
        assertEdge("fnr", "orgnr")
        assertEdge("orgnr", "periode_id_1")
        assertEdge("periode_id_1", "utbetaling_id")
    }

    @Test
    fun `finn tre`() {
        val orgnrNode1 = orgnrNode("orgnr1")
        val orgnrNode2 = orgnrNode("orgnr2")
        val periodeNode1 = periodeNode("periode_id_1")
        val periodeNode2 = periodeNode("periode_id_2")
        val utbetalingNode1 = utbetalingNode("utbetaling_id_1")
        val utbetalingNode2 = utbetalingNode("utbetaling_id_2")

        run {
            val fnrNode = fnrNode("fnr")
            fnrNode parentOf orgnrNode1
            orgnrNode1 parentOf periodeNode1
            periodeNode1 parentOf utbetalingNode1
            val gren1 = Tree.buildTree(fnrNode)
            service.nyGren(gren1)
        }

        run {
            val fnrNode = fnrNode("fnr")
            fnrNode parentOf orgnrNode2
            orgnrNode2 parentOf periodeNode2
            periodeNode2 parentOf utbetalingNode2
            val gren2 = Tree.buildTree(fnrNode)
            service.nyGren(gren2)
        }

        val tree = service.finnTre("utbetaling_id_2")
        assertNotNull(tree)

        run {
            val fnrNode = fnrNode("fnr")
            fnrNode parentOf orgnrNode1
            orgnrNode1 parentOf periodeNode1
            periodeNode1 parentOf utbetalingNode1

            fnrNode parentOf orgnrNode2
            orgnrNode2 parentOf periodeNode2
            periodeNode2 parentOf utbetalingNode2

            val expected = Tree.buildTree(fnrNode)
            assertEquals(expected, tree)
        }
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