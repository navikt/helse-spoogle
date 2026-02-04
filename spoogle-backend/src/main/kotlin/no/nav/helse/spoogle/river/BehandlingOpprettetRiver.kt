package no.nav.helse.spoogle.river

import com.github.navikt.tbd_libs.rapids_and_rivers.JsonMessage
import com.github.navikt.tbd_libs.rapids_and_rivers.River
import com.github.navikt.tbd_libs.rapids_and_rivers.isMissingOrNull
import com.github.navikt.tbd_libs.rapids_and_rivers_api.MessageContext
import com.github.navikt.tbd_libs.rapids_and_rivers_api.MessageMetadata
import com.github.navikt.tbd_libs.rapids_and_rivers_api.RapidsConnection
import io.micrometer.core.instrument.MeterRegistry
import no.nav.helse.spoogle.TreService
import no.nav.helse.spoogle.asUUID
import no.nav.helse.spoogle.tre.Node
import no.nav.helse.spoogle.tre.Node.Companion.organisasjonsnummer
import no.nav.helse.spoogle.tre.Tre

internal class BehandlingOpprettetRiver(
    private val treService: TreService,
    rapidsConnection: RapidsConnection
): SpoogleRiver() {

    override fun eventName(): String = "behandling_opprettet"

    init {
        River(rapidsConnection).apply {
            precondition {
                it.requireValue("@event_name", eventName())
            }
            validate {
                it.requireKey("fødselsnummer", "vedtaksperiodeId", "behandlingId", "yrkesaktivitetstype")
                it.interestedIn("organisasjonsnummer")
            }
        }.register(this)
    }

    override fun onPacket(
        packet: JsonMessage, context: MessageContext, metadata: MessageMetadata, meterRegistry: MeterRegistry
    ) {
        val fødselsnummer = packet["fødselsnummer"].asText()
        val yrkesaktivitetstype = packet["yrkesaktivitetstype"].asText()
        val organisasjonsnummer = packet["organisasjonsnummer"].takeUnless { it.isMissingOrNull() }?.asText() ?: yrkesaktivitetstype
        val vedtaksperiodeId = packet["vedtaksperiodeId"].asUUID()
        val behandlingId = packet["behandlingId"].asUUID()

        val fødselsnummerNode = Node.fødselsnummer(fødselsnummer)
        val organisasjonsnummerNode = Node.organisasjonsnummer(organisasjonsnummer, fødselsnummer)
        val vedtaksperiodeIdNode = Node.vedtaksperiodeId(vedtaksperiodeId.toString())
        val behandlingIdNode = Node.behandlingId(behandlingId.toString())

        organisasjonsnummerNode barnAv fødselsnummerNode
        vedtaksperiodeIdNode barnAv organisasjonsnummerNode
        behandlingIdNode barnAv vedtaksperiodeIdNode

        val tre = Tre.byggTre(fødselsnummerNode)
        treService.nyGren(tre)
    }
}
