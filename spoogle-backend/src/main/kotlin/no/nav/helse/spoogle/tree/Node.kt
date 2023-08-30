package no.nav.helse.spoogle.tree

import org.intellij.lang.annotations.Language
import java.time.LocalDateTime

data class Node private constructor(
    private val id: String,
    private val type: Identifikatortype
) {
    private var parent: Node? = null
    private val children: MutableSet<Node> = mutableSetOf()
    private val invalidChildren: MutableMap<Node, LocalDateTime> = mutableMapOf()

    internal companion object {
        internal fun fødselsnummer(fødselsnummer: String): Node {
            return Node(fødselsnummer, Identifikatortype.FØDSELSNUMMER)
        }
        internal fun aktørId(aktørId: String): Node {
            return Node(aktørId, Identifikatortype.AKTØR_ID)
        }
        internal fun organisasjonsnummer(organisasjonsnummer: String, fødselsnummer: String): Node {
            return Node("$organisasjonsnummer+$fødselsnummer", Identifikatortype.ORGANISASJONSNUMMER)
        }
        internal fun søknadId(søknadId: String): Node {
            return Node(søknadId, Identifikatortype.SØKNAD_ID)
        }
        internal fun inntektsmeldingId(inntektsmeldingId: String): Node {
            return Node(inntektsmeldingId, Identifikatortype.INNTEKTSMELDING_ID)
        }
        internal fun vedtaksperiodeId(vedtaksperiodeId: String): Node {
            return Node(vedtaksperiodeId, Identifikatortype.VEDTAKSPERIODE_ID)
        }
        internal fun utbetalingId(utbetalingId: String): Node {
            return Node(utbetalingId, Identifikatortype.UTBETALING_ID)
        }
    }

    internal fun toDto(): NodeDto = NodeDto(
        id = id,
        type = type.toString(),
        children = children.map(Node::toDto),
        invalidChildren = invalidChildren.map { (key, _) -> key.toDto() }
    )

    internal fun find(targetId: String): List<String> {
        val id = if (type == Identifikatortype.ORGANISASJONSNUMMER) this.id.split("+").first() else this.id
        if (targetId == id) return listOf(id)
        val foo = children.flatMap { it.find(targetId) }
        if (foo.contains(targetId)) return listOf(id) + foo
        return emptyList()
    }

    internal fun hasParent() = parent != null

    @Language("JSON")
    internal fun toJson(): String {
        val ugyldigFra = parent?.let { it.invalidChildren[this]?.toString() }
        val id = if (type == Identifikatortype.ORGANISASJONSNUMMER) this.id.split("+").first() else this.id
        return """
            {
                "id": "$id",
                "type": "${type.name}",
                "children": ${children.map(Node::toJson)},
                "ugyldig_fra": ${ ugyldigFra?.let { """"$it"""" }}
            }
        """
    }

    internal infix fun parentOf(other: Node) {
        if (other.type == Identifikatortype.ORGANISASJONSNUMMER) {
            if (id != other.id.split("+").last()) throw IllegalArgumentException("Barn er type=ORGANISASJONSNUMMER og må dermed ha samme fødselsnummer som forelder")
        }
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