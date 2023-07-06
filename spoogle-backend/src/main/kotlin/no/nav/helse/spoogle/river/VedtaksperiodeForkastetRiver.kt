package no.nav.helse.spoogle.river

import no.nav.helse.rapids_rivers.JsonMessage
import no.nav.helse.rapids_rivers.MessageContext
import no.nav.helse.rapids_rivers.RapidsConnection
import no.nav.helse.rapids_rivers.River
import no.nav.helse.spoogle.TreeService
import no.nav.helse.spoogle.asUUID
import no.nav.helse.spoogle.tree.Identifikatortype.ORGANISASJONSNUMMER
import no.nav.helse.spoogle.tree.Identifikatortype.VEDTAKSPERIODE_ID
import no.nav.helse.spoogle.tree.Node

internal class VedtaksperiodeForkastetRiver(
    private val treeService: TreeService,
    rapidsConnection: RapidsConnection
): River.PacketListener {

    init {
        River(rapidsConnection).apply {
            validate {
                it.demandValue("@event_name", "vedtaksperiode_forkastet")
                it.requireKey("fødselsnummer", "aktørId", "organisasjonsnummer", "vedtaksperiodeId")
            }
        }.register(this)
    }

    override fun onPacket(packet: JsonMessage, context: MessageContext) {
        val organisasjonsnummer = packet["organisasjonsnummer"].asText()
        val vedtaksperiodeId = packet["vedtaksperiodeId"].asUUID()

        val organisasjonsnummerNode = Node(organisasjonsnummer, ORGANISASJONSNUMMER)
        val vedtaksperiodeIdNode = Node(vedtaksperiodeId.toString(), VEDTAKSPERIODE_ID)

        treeService.invaliderRelasjon(organisasjonsnummerNode, vedtaksperiodeIdNode)
    }
}