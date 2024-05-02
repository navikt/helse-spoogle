package no.nav.helse.spoogle.tre

import no.nav.helse.spoogle.tre.Identifikatortype.*
import org.intellij.lang.annotations.Language
import java.time.LocalDateTime

open class Node private constructor(
    private val id: String,
    private val type: Identifikatortype,
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

        internal fun organisasjonsnummer(
            organisasjonsnummer: String,
            fødselsnummer: String,
        ): Node {
            return OrganisasjonsnummerNode(organisasjonsnummer, fødselsnummer)
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

        internal fun behandlingId(behandlingId: String): Node {
            return Node(behandlingId, BEHANDLING_ID)
        }

        internal fun oppgaveId(oppgaveId: String): Node {
            return Node(oppgaveId, OPPGAVE_ID)
        }

        internal fun utbetalingId(utbetalingId: String): Node {
            return Node(utbetalingId, UTBETALING_ID)
        }
    }

    internal fun finn(targetId: String): List<String> {
        if (targetId == id) return listOf(id)
        val foo = barn.flatMap { it.finn(targetId) }
        if (foo.contains(targetId)) return listOf(id) + foo
        return emptyList()
    }

    internal fun harForelder() = forelder != null

    internal open infix fun barnAv(other: Node) {
        other.barn.add(this)
        this.forelder = other
    }

    internal fun ugyldigRelasjon(
        other: Node,
        tidspunkt: LocalDateTime,
    ) {
        ugyldigeBarn[other] = tidspunkt
    }

    internal open fun toDto(): NodeDto =
        NodeDto(
            id = id,
            type = type.toString(),
            barn = barn.map(Node::toDto),
            ugyldigeBarn = ugyldigeBarn.map { (key, _) -> key.toDto() },
        )

    @Language("JSON")
    internal fun toJson(): String {
        val ugyldigFra = forelder?.let { it.ugyldigeBarn[this]?.toString() }
        return """
            {
                "id": "$id",
                "type": "${type.name}",
                "children": ${barn.map(Node::toJson)},
                "ugyldig_fra": ${ ugyldigFra?.let { """"$it"""" }}
            }
        """
    }

    override fun equals(other: Any?): Boolean =
        this === other ||
            (other is Node && id == other.id && type == other.type)

    override fun hashCode(): Int {
        var result = id.hashCode()
        result = 31 * result + type.hashCode()
        return result
    }

    private class OrganisasjonsnummerNode(
        private val organisasjonsnummer: String,
        private val fødselsnummer: String,
    ) : Node(organisasjonsnummer, ORGANISASJONSNUMMER) {
        override fun toDto(): NodeDto = super.toDto().copy(id = "$organisasjonsnummer+$fødselsnummer")

        override fun barnAv(other: Node) {
            if (other.type == FØDSELSNUMMER && other.id != fødselsnummer) {
                throw IllegalArgumentException("Barn er type=ORGANISASJONSNUMMER og må dermed ha samme fødselsnummer som forelder")
            }
            super.barnAv(other)
        }

        override fun equals(other: Any?): Boolean =
            super.equals(other) &&
                other is OrganisasjonsnummerNode &&
                other.organisasjonsnummer == organisasjonsnummer &&
                other.fødselsnummer == fødselsnummer

        override fun hashCode(): Int {
            var result = super.hashCode()
            result = 31 * result + organisasjonsnummer.hashCode()
            result = 31 * result + fødselsnummer.hashCode()
            return result
        }
    }
}

data class NodeDto(
    val id: String,
    val type: String,
    val barn: List<NodeDto>,
    val ugyldigeBarn: List<NodeDto>,
)
