package no.nav.helse.spoogle.river

import no.nav.helse.rapids_rivers.MessageContext
import no.nav.helse.rapids_rivers.MessageProblems
import no.nav.helse.rapids_rivers.River
import org.slf4j.LoggerFactory

sealed class SpoogleRiver: River.PacketListener {
    abstract fun eventName(): String
    private companion object {
        private val logg = LoggerFactory.getLogger(this::class.java)
        private val sikkerlogg = LoggerFactory.getLogger("tjenestekall")
    }

    final override fun onError(problems: MessageProblems, context: MessageContext) {
        logg.error("Klart ikke lese melding ${eventName()}")
        sikkerlogg.error("Klart ikke lese melding ${eventName()}: ${problems.toExtendedReport()}")
        throw RuntimeException("Klart ikke lese melding ${eventName()}")
    }
}
