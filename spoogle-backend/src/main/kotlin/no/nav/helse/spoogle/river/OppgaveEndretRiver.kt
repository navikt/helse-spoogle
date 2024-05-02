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
) : River.PacketListener {
    init {
        River(rapidsConnection).apply {
            validate {
                it.demandAny("@event_name", listOf("oppgave_opprettet", "oppgave_oppdatert"))
                it.requireKey("fødselsnummer", "oppgaveId")
            }
        }.register(this)
    }

    override fun onPacket(
        packet: JsonMessage,
        context: MessageContext,
    ) {
        val fødselsnummer = packet["fødselsnummer"].asText()
        val oppgaveId = packet["oppgaveId"].asText()

        val fødselsnummerNode = Node.fødselsnummer(fødselsnummer)
        val oppgaveIdNode = Node.oppgaveId(oppgaveId.toString())

        oppgaveIdNode barnAv fødselsnummerNode

        val tre = Tre.byggTre(fødselsnummerNode)
        treService.nyGren(tre)
    }
}
