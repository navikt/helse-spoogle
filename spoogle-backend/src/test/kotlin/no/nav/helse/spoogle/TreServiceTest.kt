package no.nav.helse.spoogle

import kotliquery.queryOf
import kotliquery.sessionOf
import no.nav.helse.spoogle.db.AbstractDatabaseTest
import no.nav.helse.spoogle.tre.Node
import no.nav.helse.spoogle.tre.Tre
import org.intellij.lang.annotations.Language
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertNotNull
import org.junit.jupiter.api.Test

internal class TreServiceTest: AbstractDatabaseTest() {
    private val service = TreService(dataSource)

    @Test
    fun `ny gren`() {
        val fnrNode = fnrNode("fnr")
        val orgnrNode = orgnrNode("orgnr", "fnr")
        val periodeNode = periodeNode("periode_id_1")
        val periodeNode2 = periodeNode("periode_id_2")
        val utbetalingNode = utbetalingNode("utbetaling_id")

        fnrNode forelderAv orgnrNode
        orgnrNode forelderAv periodeNode
        orgnrNode forelderAv periodeNode2
        periodeNode forelderAv utbetalingNode

        val tre = Tre.byggTre(fnrNode)

        service.nyGren(tre)
        assertNodes("fnr", "orgnr+fnr", "periode_id_1", "utbetaling_id")
        assertEdge("fnr", "orgnr+fnr")
        assertEdge("orgnr+fnr", "periode_id_1")
        assertEdge("periode_id_1", "utbetaling_id")
    }

    @Test
    fun `finn tre`() {
        val orgnrNode1 = orgnrNode("orgnr1", "fnr")
        val orgnrNode2 = orgnrNode("orgnr2", "fnr")
        val periodeNode1 = periodeNode("periode_id_1")
        val periodeNode2 = periodeNode("periode_id_2")
        val utbetalingNode1 = utbetalingNode("utbetaling_id_1")
        val utbetalingNode2 = utbetalingNode("utbetaling_id_2")

        run {
            val fnrNode = fnrNode("fnr")
            fnrNode forelderAv orgnrNode1
            orgnrNode1 forelderAv periodeNode1
            periodeNode1 forelderAv utbetalingNode1
            val gren1 = Tre.byggTre(fnrNode)
            service.nyGren(gren1)
        }

        run {
            val fnrNode = fnrNode("fnr")
            fnrNode forelderAv orgnrNode2
            orgnrNode2 forelderAv periodeNode2
            periodeNode2 forelderAv utbetalingNode2
            val gren2 = Tre.byggTre(fnrNode)
            service.nyGren(gren2)
        }

        val tree = service.finnTre("utbetaling_id_2")
        assertNotNull(tree)

        run {
            val fnrNode = fnrNode("fnr")
            fnrNode forelderAv orgnrNode1
            orgnrNode1 forelderAv periodeNode1
            periodeNode1 forelderAv utbetalingNode1

            fnrNode forelderAv orgnrNode2
            orgnrNode2 forelderAv periodeNode2
            periodeNode2 forelderAv utbetalingNode2

            val expected = Tre.byggTre(fnrNode)
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
        val query = "SELECT COUNT(1) FROM sti WHERE forelder = (SELECT node_id FROM node WHERE id = ?) AND barn = (SELECT node_id FROM node WHERE id = ?)"
        val antall = sessionOf(dataSource).use { session ->
            session.run(queryOf(query, idNodeA, idNodeB).map { it.int(1) }.asSingle)
        }
        assertEquals(1, antall)
    }

    private fun fnrNode(fnr: String) = Node.fødselsnummer(fnr)
    private fun orgnrNode(orgnr: String, fødselsnummer: String) = Node.organisasjonsnummer(orgnr, fødselsnummer)
    private fun periodeNode(id: String) = Node.vedtaksperiodeId(id)
    private fun utbetalingNode(id: String) = Node.utbetalingId(id)
}