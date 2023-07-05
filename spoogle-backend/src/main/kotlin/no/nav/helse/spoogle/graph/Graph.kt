package no.nav.helse.spoogle.graph

internal class Graph(
    private val edges: List<Edge>
) {
    internal fun toDto(): GraphDto = GraphDto(edges.map(Edge::toDto))

    override fun equals(other: Any?) = this === other || (other is Graph && edges == other.edges)

    override fun hashCode() = edges.hashCode()

    internal companion object {
        internal fun buildGraph(vararg edges: Edge) = Graph(edges.toList())
    }
}

data class GraphDto(
    val edges: List<EdgeDto>
)