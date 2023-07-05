package no.nav.helse.spoogle.graph

data class Node(
    private val id: String,
    private val type: Identifikatortype
) {
    private val children: MutableList<Node> = mutableListOf()

    internal fun toDto(): NodeDto = NodeDto(id, type.toString(), children.map { it.toDto() })

    internal infix fun to(other: Node) = Edge(this, other)

    internal infix fun parentOf(other: Node) {
        children.add(other)
    }
}

data class NodeDto(
    val id: String,
    val type: String,
    val children: List<NodeDto>
)