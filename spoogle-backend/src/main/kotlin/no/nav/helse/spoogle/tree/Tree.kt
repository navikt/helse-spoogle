package no.nav.helse.spoogle.tree

internal class Tree private constructor(
    private val rootNode: Node
) {
    internal fun toDto(): TreeDto = TreeDto(rootNode.toDto())

    override fun equals(other: Any?) = this === other || (other is Tree && rootNode == other.rootNode)

    override fun hashCode() = rootNode.hashCode()

    internal fun toJson(): String = rootNode.toJson()

    internal fun pathTo(id: String): List<String> = rootNode.find(id)

    internal companion object {
        internal fun buildTree(rootNode: Node) = Tree(rootNode)
    }
}

data class TreeDto(
    val rootNode: NodeDto
)