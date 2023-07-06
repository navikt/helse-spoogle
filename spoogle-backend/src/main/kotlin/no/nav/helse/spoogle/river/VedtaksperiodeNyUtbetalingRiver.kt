package no.nav.helse.spoogle.river

import no.nav.helse.rapids_rivers.JsonMessage
import no.nav.helse.rapids_rivers.MessageContext
import no.nav.helse.rapids_rivers.RapidsConnection
import no.nav.helse.rapids_rivers.River
import no.nav.helse.spoogle.TreeService
import no.nav.helse.spoogle.asUUID
import no.nav.helse.spoogle.tree.Identifikatortype.UTBETALING_ID
import no.nav.helse.spoogle.tree.Identifikatortype.VEDTAKSPERIODE_ID
import no.nav.helse.spoogle.tree.Node
import no.nav.helse.spoogle.tree.Tree

internal class VedtaksperiodeNyUtbetalingRiver(
    private val treeService: TreeService,
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

        val vedtaksperiodeIdNode = Node(vedtaksperiodeId.toString(), VEDTAKSPERIODE_ID)
        val utbetalingIdNode = Node(utbetalingId.toString(), UTBETALING_ID)

        vedtaksperiodeIdNode parentOf utbetalingIdNode

        val tre = Tree.buildTree(vedtaksperiodeIdNode)
        treeService.nyGren(tre)
    }
}