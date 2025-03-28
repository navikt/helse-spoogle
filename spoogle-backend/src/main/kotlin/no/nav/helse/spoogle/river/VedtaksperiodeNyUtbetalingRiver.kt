package no.nav.helse.spoogle.river

import com.github.navikt.tbd_libs.rapids_and_rivers.JsonMessage
import com.github.navikt.tbd_libs.rapids_and_rivers.River
import com.github.navikt.tbd_libs.rapids_and_rivers_api.MessageContext
import com.github.navikt.tbd_libs.rapids_and_rivers_api.MessageMetadata
import com.github.navikt.tbd_libs.rapids_and_rivers_api.RapidsConnection
import io.micrometer.core.instrument.MeterRegistry
import no.nav.helse.spoogle.TreService
import no.nav.helse.spoogle.asUUID
import no.nav.helse.spoogle.tre.Node
import no.nav.helse.spoogle.tre.Tre

internal class VedtaksperiodeNyUtbetalingRiver(
    private val treService: TreService,
    rapidsConnection: RapidsConnection
): SpoogleRiver() {
    override fun eventName(): String = "vedtaksperiode_ny_utbetaling"
    init {
        River(rapidsConnection).apply {
            precondition {
                it.requireValue("@event_name", eventName())
            }
            validate {
                it.requireKey("utbetalingId", "vedtaksperiodeId")
            }
        }.register(this)
    }

    override fun onPacket(
        packet: JsonMessage, context: MessageContext, metadata: MessageMetadata, meterRegistry: MeterRegistry
    ) {
        val utbetalingId = packet["utbetalingId"].asUUID()
        val vedtaksperiodeId = packet["vedtaksperiodeId"].asUUID()

        val vedtaksperiodeIdNode = Node.vedtaksperiodeId(vedtaksperiodeId.toString())
        val utbetalingIdNode = Node.utbetalingId(utbetalingId.toString())

        utbetalingIdNode barnAv vedtaksperiodeIdNode

        val tre = Tre.byggTre(vedtaksperiodeIdNode)
        treService.nyGren(tre)
    }
}
