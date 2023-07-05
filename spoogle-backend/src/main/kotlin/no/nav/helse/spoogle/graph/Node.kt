package no.nav.helse.spoogle.graph

data class Node(
    private val id: String,
    private val type: Identifikatortype
) {
    internal fun toDto() = NodeDto(id, type.toString())
    internal infix fun to(other: Node) = Edge(this, other)
}

data class NodeDto(
    val id: String,
    val type: String
)