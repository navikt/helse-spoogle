package no.nav.helse.spoogle.river

import com.github.navikt.tbd_libs.rapids_and_rivers.River
import com.github.navikt.tbd_libs.rapids_and_rivers_api.MessageContext
import com.github.navikt.tbd_libs.rapids_and_rivers_api.MessageMetadata
import com.github.navikt.tbd_libs.rapids_and_rivers_api.MessageProblems
import org.slf4j.LoggerFactory

sealed class SpoogleRiver: River.PacketListener {
    abstract fun eventName(): String
    private companion object {
        private val logg = LoggerFactory.getLogger(this::class.java)
        private val sikkerlogg = LoggerFactory.getLogger("tjenestekall")
    }

    final override fun onError(problems: MessageProblems, context: MessageContext, metadata: MessageMetadata) {
        logg.error("Klart ikke lese melding ${eventName()}")
        sikkerlogg.error("Klart ikke lese melding ${eventName()}: ${problems.toExtendedReport()}")
        throw RuntimeException("Klart ikke lese melding ${eventName()}")
    }
}
