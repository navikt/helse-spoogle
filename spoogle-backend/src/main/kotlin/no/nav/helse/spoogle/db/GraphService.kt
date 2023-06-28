package no.nav.helse.spoogle.db

import no.nav.helse.spoogle.graph.Graph
import no.nav.helse.spoogle.graph.NodeDto
import javax.sql.DataSource

internal class GraphService(dataSource: DataSource) {
    private val dao = GraphDao(dataSource)

    internal fun nySubGraph(graph: Graph) {
        val dto = graph.toDto()
        dto.edges.forEach {
            nyRelasjon(it.nodeA to it.nodeB)
        }
    }

    private fun nyRelasjon(nodes: Pair<NodeDto, NodeDto>) {
        val (nodeA, nodeB) = nodes
        dao.nyNode(nodeA)
        dao.nyNode(nodeB)
        dao.nyEdge(nodeA, nodeB)
    }
}