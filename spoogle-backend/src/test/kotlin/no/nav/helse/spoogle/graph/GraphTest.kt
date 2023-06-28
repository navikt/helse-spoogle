package no.nav.helse.spoogle.graph

import no.nav.helse.spoogle.graph.Identifikatortype.*
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.Test

internal class GraphTest {

    @Test
    fun bygg() {
        val fnrNode = fnrNode("FNR")
        val orgnrNode = orgnrNode("ORGNR")
        val periodeNode = periodeNode("PERIODE_ID")
        val utbetalingNode = utbetalingNode("UTBETALING_ID")
        val graph = Graph.buildGraph(fnrNode, orgnrNode, periodeNode, utbetalingNode)

        val expected = Graph(
            listOf(
                Edge(fnrNode, orgnrNode),
                Edge(fnrNode, periodeNode),
                Edge(fnrNode, utbetalingNode),
                Edge(orgnrNode, periodeNode),
                Edge(orgnrNode, utbetalingNode),
                Edge(periodeNode, utbetalingNode),
            )
        )
        assertEquals(expected, graph)
    }

    private fun fnrNode(fnr: String) = Node(fnr, FØDSELSNUMMER)
    private fun orgnrNode(orgnr: String) = Node(orgnr, ORGANISASJONSNUMMER)
    private fun periodeNode(id: String) = Node(id, VEDTAKSPERIODE_ID)
    private fun utbetalingNode(id: String) = Node(id, UTBETALING_ID)
}
