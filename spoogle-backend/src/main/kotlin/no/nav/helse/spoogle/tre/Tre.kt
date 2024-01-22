package no.nav.helse.spoogle.tre

internal class Tre private constructor(
    private val rotnode: Node
) {
    internal fun toDto(): TreDto = TreDto(rotnode.toDto())

    override fun equals(other: Any?) = this === other || (other is Tre && rotnode == other.rotnode)

    override fun hashCode() = rotnode.hashCode()

    internal fun toJson(): String = rotnode.toJson()

    internal fun pathTo(id: String): List<String> = rotnode.finn(id)

    internal companion object {
        internal fun byggTre(rootNode: Node) = Tre(rootNode)
    }
}

data class TreDto(
    val rotnode: NodeDto
)