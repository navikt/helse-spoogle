package no.nav.helse.spoogle.graph

data class Node(
    private val id: String,
    private val type: Identifikatortype
) {
    private var parent: Node? = null
    private val children: MutableSet<Node> = mutableSetOf()

    internal fun toDto(): NodeDto = NodeDto(id, type.toString(), children.map { it.toDto() })

    internal fun hasParent() = parent != null

    internal infix fun parentOf(other: Node) {
        children.add(other)
        other.parent = this
    }
}

data class NodeDto(
    val id: String,
    val type: String,
    val children: List<NodeDto>
)