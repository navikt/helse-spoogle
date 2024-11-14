package no.nav.helse.spoogle.river

import no.nav.helse.rapids_rivers.JsonMessage
import no.nav.helse.rapids_rivers.MessageContext
import no.nav.helse.rapids_rivers.RapidsConnection
import no.nav.helse.rapids_rivers.River
import no.nav.helse.spoogle.TreService
import no.nav.helse.spoogle.tre.Node
import no.nav.helse.spoogle.tre.Tre

internal class OppgaveEndretRiver(
    private val treService: TreService,
    rapidsConnection: RapidsConnection,
) : SpoogleRiver() {
    override fun eventName(): String  = "oppgave_opprettet"
    init {
        River(rapidsConnection).apply {
            validate {
                it.demandAny("@event_name", listOf(eventName(), "oppgave_oppdatert"))
                it.requireKey("oppgaveId", "behandlingId")
            }
        }.register(this)
    }

    override fun onPacket(
        packet: JsonMessage,
        context: MessageContext,
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
