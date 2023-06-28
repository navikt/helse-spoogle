package no.nav.helse.spoogle.graph

import no.nav.helse.spoogle.graph.Identifikatortype.FØDSELSNUMMER
import no.nav.helse.spoogle.graph.Identifikatortype.ORGANISASJONSNUMMER
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.Test

internal class NodeTest {

    @Test
    fun `connect two nodes`() {
        val nodeA = Node("FNR", FØDSELSNUMMER)
        val nodeB = Node("ORGNR", ORGANISASJONSNUMMER)
        val edge = nodeA.connectTo(nodeB)
        assertEquals(Edge(nodeA, nodeB), edge)
    }
}