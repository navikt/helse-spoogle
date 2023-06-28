package no.nav.helse.spoogle.graph

data class Edge(
    private val nodeA: Node,
    private val nodeB: Node
) {
    internal fun toDto() = EdgeDto(nodeA.toDto(), nodeB.toDto())
}

data class EdgeDto(
    val nodeA: NodeDto,
    val nodeB: NodeDto
)