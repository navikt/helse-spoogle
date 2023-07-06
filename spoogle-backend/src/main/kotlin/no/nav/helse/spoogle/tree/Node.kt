package no.nav.helse.spoogle.tree

import org.intellij.lang.annotations.Language
import java.time.LocalDateTime

data class Node(
    private val id: String,
    private val type: Identifikatortype
) {
    private var parent: Node? = null
    private val children: MutableSet<Node> = mutableSetOf()
    private val invalidChildren: MutableMap<Node, LocalDateTime> = mutableMapOf()

    internal fun toDto(): NodeDto = NodeDto(
        id = id,
        type = type.toString(),
        children = children.map(Node::toDto),
        invalidChildren = invalidChildren.map { (key, _) -> key.toDto() }
    )

    internal fun hasParent() = parent != null

    @Language("JSON")
    internal fun toJson(): String {
        return """
            {
                "id": "$id",
                "type": "${type.name}",
                "children": ${children.map { it.toJson() }}
            } 
        """
    }

    internal infix fun parentOf(other: Node) {
        children.add(other)
        other.parent = this
    }

    internal fun invalidRelation(other: Node, tidspunkt: LocalDateTime) {
        invalidChildren[other] = tidspunkt
    }
}

data class NodeDto(
    val id: String,
    val type: String,
    val children: List<NodeDto>,
    val invalidChildren: List<NodeDto>
)