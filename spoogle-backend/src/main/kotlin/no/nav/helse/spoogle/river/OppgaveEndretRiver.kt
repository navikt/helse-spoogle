package no.nav.helse.spoogle.river

import com.github.navikt.tbd_libs.rapids_and_rivers.JsonMessage
import com.github.navikt.tbd_libs.rapids_and_rivers.River
import com.github.navikt.tbd_libs.rapids_and_rivers_api.MessageContext
import com.github.navikt.tbd_libs.rapids_and_rivers_api.MessageMetadata
import com.github.navikt.tbd_libs.rapids_and_rivers_api.RapidsConnection
import io.micrometer.core.instrument.MeterRegistry
import no.nav.helse.spoogle.TreService
import no.nav.helse.spoogle.tre.Node
import no.nav.helse.spoogle.tre.Tre

internal class OppgaveEndretRiver(
    private val treService: TreService,
    rapidsConnection: RapidsConnection,
) : SpoogleRiver() {
    override fun eventName(): String = "oppgave_opprettet"

    init {
        River(rapidsConnection).apply {
            precondition {
                it.requireAny("@event_name", listOf(eventName(), "oppgave_oppdatert"))
            }
            validate {
                it.requireKey("oppgaveId", "behandlingId")
            }
        }.register(this)
    }

    override fun onPacket(
        packet: JsonMessage, context: MessageContext, metadata: MessageMetadata, meterRegistry: MeterRegistry
    ) {
        val oppgaveId = packet["oppgaveId"].asText()
        val behandlingId = packet["behandlingId"].asText()

        val oppgaveIdNode = Node.oppgaveId(oppgaveId)
        val behandlingIdNode = Node.behandlingId(behandlingId)

        oppgaveIdNode barnAv behandlingIdNode

        val tre = Tre.byggTre(behandlingIdNode)
        treService.nyGren(tre)
    }
}
