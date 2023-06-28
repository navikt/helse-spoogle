package no.nav.helse.spoogle.graph

internal class Graph(
    private val edges: List<Edge>
) {
    internal fun toDto(): GraphDto = GraphDto(edges.map(Edge::toDto))

    override fun equals(other: Any?) = this === other || (other is Graph && edges == other.edges)

    override fun hashCode() = edges.hashCode()

    internal companion object {
        internal fun buildGraph(vararg nodes: Node): Graph {
            val edges = mutableSetOf<Edge>()
            nodes.forEachIndexed { index, node ->
                val tail = nodes.drop(index + 1)
                tail.forEach {
                    edges.add(node.connectTo(it))
                }
            }
            return Graph(edges.toList())
        }
    }
}

data class GraphDto(
    val edges: List<EdgeDto>
)