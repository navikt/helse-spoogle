package no.nav.helse.spoogle.river

import no.nav.helse.rapids_rivers.JsonMessage
import no.nav.helse.rapids_rivers.MessageContext
import no.nav.helse.rapids_rivers.RapidsConnection
import no.nav.helse.rapids_rivers.River
import no.nav.helse.spoogle.TreeService
import no.nav.helse.spoogle.asUUID
import no.nav.helse.spoogle.tree.Node

internal class UtbetalingForkastetRiver(
    private val treeService: TreeService,
    rapidsConnection: RapidsConnection
): River.PacketListener {
    init {
        River(rapidsConnection).apply {
            validate {
                it.demandValue("@event_name", "utbetaling_endret")
                it.requireKey("utbetalingId")
                it.requireValue("gjeldendeStatus", "FORKASTET")
            }
        }.register(this)
    }

    override fun onPacket(packet: JsonMessage, context: MessageContext) {
        val utbetalingId = packet["utbetalingId"].asUUID()
        val utbetalingIdNode = Node.utbetalingId(utbetalingId.toString())

        treeService.invaliderRelasjonerFor(utbetalingIdNode)
    }
}