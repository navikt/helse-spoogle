package no.nav.helse.spoogle.tre

import no.nav.helse.spoogle.tre.Identifikatortype.*
import org.intellij.lang.annotations.Language
import java.time.LocalDateTime

data class Node private constructor(
    private val id: String,
    private val type: Identifikatortype
) {
    private var forelder: Node? = null
    private val barn: MutableSet<Node> = mutableSetOf()
    private val ugyldigeBarn: MutableMap<Node, LocalDateTime> = mutableMapOf()

    internal companion object {
        internal fun fødselsnummer(fødselsnummer: String): Node {
            return Node(fødselsnummer, FØDSELSNUMMER)
        }
        internal fun aktørId(aktørId: String): Node {
            return Node(aktørId, AKTØR_ID)
        }
        internal fun organisasjonsnummer(organisasjonsnummer: String, fødselsnummer: String): Node {
            return Node("$organisasjonsnummer+$fødselsnummer", ORGANISASJONSNUMMER)
        }
        internal fun søknadId(søknadId: String): Node {
            return Node(søknadId, SØKNAD_ID)
        }
        internal fun inntektsmeldingId(inntektsmeldingId: String): Node {
            return Node(inntektsmeldingId, INNTEKTSMELDING_ID)
        }
        internal fun vedtaksperiodeId(vedtaksperiodeId: String): Node {
            return Node(vedtaksperiodeId, VEDTAKSPERIODE_ID)
        }
        internal fun utbetalingId(utbetalingId: String): Node {
            return Node(utbetalingId, UTBETALING_ID)
        }
    }

    internal fun toDto(): NodeDto = NodeDto(
        id = id,
        type = type.toString(),
        barn = barn.map(Node::toDto),
        ugyldigeBarn = ugyldigeBarn.map { (key, _) -> key.toDto() }
    )

    internal fun finn(targetId: String): List<String> {
        val id = if (type == ORGANISASJONSNUMMER) this.id.split("+").first() else this.id
        if (targetId == id) return listOf(id)
        val foo = barn.flatMap { it.finn(targetId) }
        if (foo.contains(targetId)) return listOf(id) + foo
        return emptyList()
    }

    internal fun harForelder() = forelder != null

    @Language("JSON")
    internal fun toJson(): String {
        val ugyldigFra = forelder?.let { it.ugyldigeBarn[this]?.toString() }
        val id = if (type == ORGANISASJONSNUMMER) this.id.split("+").first() else this.id
        return """
            {
                "id": "$id",
                "type": "${type.name}",
                "children": ${barn.map(Node::toJson)},
                "ugyldig_fra": ${ ugyldigFra?.let { """"$it"""" }}
            }
        """
    }

    internal infix fun forelderAv(other: Node) {
        if (other.type == ORGANISASJONSNUMMER) {
            val fødselsnummer = other.id.split("+").last()
            if (id != fødselsnummer)
                throw IllegalArgumentException("Barn er type=ORGANISASJONSNUMMER og må dermed ha samme fødselsnummer som forelder")
        }
        barn.add(other)
        other.forelder = this
    }

    internal fun ugyldigRelasjon(other: Node, tidspunkt: LocalDateTime) {
        ugyldigeBarn[other] = tidspunkt
    }
}

data class NodeDto(
    val id: String,
    val type: String,
    val barn: List<NodeDto>,
    val ugyldigeBarn: List<NodeDto>
)