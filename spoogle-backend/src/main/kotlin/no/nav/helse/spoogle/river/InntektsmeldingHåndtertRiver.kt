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

internal class InntektsmeldingHåndtertRiver(
    private val treService: TreService,
    rapidsConnection: RapidsConnection
): SpoogleRiver() {
    override fun eventName(): String = "inntektsmelding_håndtert"

    init {
        River(rapidsConnection).apply {
            validate {
                it.demandValue("@event_name", eventName())
                it.requireKey("inntektsmeldingId", "vedtaksperiodeId")
            }
        }.register(this)
    }

    override fun onPacket(
        packet: JsonMessage, context: MessageContext, metadata: MessageMetadata, meterRegistry: MeterRegistry
    ) {
        val vedtaksperiodeId = packet["vedtaksperiodeId"].asUUID()
        val søknadId = packet["inntektsmeldingId"].asUUID()
        val vedtaksperiodeIdNode = Node.vedtaksperiodeId(vedtaksperiodeId.toString())
        val inntektsmeldingIdNode = Node.inntektsmeldingId(søknadId.toString())

        inntektsmeldingIdNode barnAv vedtaksperiodeIdNode

        treService.nyGren(Tre.byggTre(vedtaksperiodeIdNode))
    }
}
