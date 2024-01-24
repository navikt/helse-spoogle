package no.nav.helse.spoogle.tre

import java.time.LocalDateTime

data class Relasjon(
    private val forelder: Node,
    private val barn: Node,
    private val ugyldigFra: LocalDateTime?
) {
    internal companion object {
        internal fun List<Relasjon>.byggTre(): Tre? {
            this.forEach { (forelder, barn, ugyldigFra) ->
                barn barnAv forelder
                if (ugyldigFra != null) forelder.ugyldigRelasjon(barn, ugyldigFra)
            }
            val (rotnode, _) = find { !it.forelder.harForelder() } ?: return null
            return Tre.byggTre(rotnode)
        }
    }
}