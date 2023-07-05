package no.nav.helse.spoogle.graph

internal class Tree private constructor(
    private val rootNode: Node
) {
    internal fun toDto(): TreeDto = TreeDto(rootNode.toDto())

    override fun equals(other: Any?) = this === other || (other is Tree && rootNode == other.rootNode)

    override fun hashCode() = rootNode.hashCode()

    internal companion object {
        internal fun growTree(rootNode: Node) = Tree(rootNode)
    }
}

data class TreeDto(
    val rootNode: NodeDto
)