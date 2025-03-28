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

internal class VedtaksperiodeForkastetRiver(
    private val treService: TreService,
    rapidsConnection: RapidsConnection
): SpoogleRiver() {

    override fun eventName(): String = "vedtaksperiode_forkastet"

    init {
        River(rapidsConnection).apply {
            precondition {
                it.requireValue("@event_name", eventName())
            }
            validate {
                it.requireKey("vedtaksperiodeId")
            }
        }.register(this)
    }

    override fun onPacket(
        packet: JsonMessage, context: MessageContext, metadata: MessageMetadata, meterRegistry: MeterRegistry
    ) {
        val vedtaksperiodeId = packet["vedtaksperiodeId"].asUUID()
        val vedtaksperiodeIdNode = Node.vedtaksperiodeId(vedtaksperiodeId.toString())
        treService.invaliderRelasjonerFor(vedtaksperiodeIdNode)
    }
}
