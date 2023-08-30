package no.nav.helse.spoogle.river

import no.nav.helse.rapids_rivers.JsonMessage
import no.nav.helse.rapids_rivers.MessageContext
import no.nav.helse.rapids_rivers.RapidsConnection
import no.nav.helse.rapids_rivers.River
import no.nav.helse.spoogle.TreService
import no.nav.helse.spoogle.asUUID
import no.nav.helse.spoogle.tre.Node
import no.nav.helse.spoogle.tre.Tre

internal class VedtaksperiodeNyUtbetalingRiver(
    private val treService: TreService,
    rapidsConnection: RapidsConnection
): River.PacketListener {
    init {
        River(rapidsConnection).apply {
            validate {
                it.demandValue("@event_name", "vedtaksperiode_ny_utbetaling")
                it.requireKey("utbetalingId", "vedtaksperiodeId")
            }
        }.register(this)
    }

    override fun onPacket(packet: JsonMessage, context: MessageContext) {
        val utbetalingId = packet["utbetalingId"].asUUID()
        val vedtaksperiodeId = packet["vedtaksperiodeId"].asUUID()

        val vedtaksperiodeIdNode = Node.vedtaksperiodeId(vedtaksperiodeId.toString())
        val utbetalingIdNode = Node.utbetalingId(utbetalingId.toString())

        utbetalingIdNode barnAv vedtaksperiodeIdNode

        val tre = Tre.byggTre(vedtaksperiodeIdNode)
        treService.nyGren(tre)
    }
}