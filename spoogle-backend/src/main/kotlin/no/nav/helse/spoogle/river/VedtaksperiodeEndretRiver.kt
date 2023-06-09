package no.nav.helse.spoogle.river

import no.nav.helse.rapids_rivers.JsonMessage
import no.nav.helse.rapids_rivers.MessageContext
import no.nav.helse.rapids_rivers.RapidsConnection
import no.nav.helse.rapids_rivers.River
import no.nav.helse.spoogle.TreeService
import no.nav.helse.spoogle.asUUID
import no.nav.helse.spoogle.tree.Identifikatortype.*
import no.nav.helse.spoogle.tree.Node
import no.nav.helse.spoogle.tree.Tree

internal class VedtaksperiodeEndretRiver(
    private val treeService: TreeService,
    rapidsConnection: RapidsConnection
): River.PacketListener {

    init {
        River(rapidsConnection).apply {
            validate {
                it.demandValue("@event_name", "vedtaksperiode_endret")
                it.requireKey("fødselsnummer", "aktørId", "organisasjonsnummer", "vedtaksperiodeId")
            }
        }.register(this)
    }

    override fun onPacket(packet: JsonMessage, context: MessageContext) {
        val fødselsnummer = packet["fødselsnummer"].asText()
        val aktørId = packet["aktørId"].asText()
        val organisasjonsnummer = packet["organisasjonsnummer"].asText()
        val vedtaksperiodeId = packet["vedtaksperiodeId"].asUUID()

        val fødselsnummerNode = Node(fødselsnummer, FØDSELSNUMMER)
        val aktørIdNode = Node(aktørId, AKTØR_ID)
        val organisasjonsnummerNode = Node(organisasjonsnummer, ORGANISASJONSNUMMER)
        val vedtaksperiodeIdNode = Node(vedtaksperiodeId.toString(), VEDTAKSPERIODE_ID)

        fødselsnummerNode parentOf aktørIdNode
        fødselsnummerNode parentOf organisasjonsnummerNode
        organisasjonsnummerNode parentOf vedtaksperiodeIdNode

        val tre = Tree.buildTree(fødselsnummerNode)
        treeService.nyGren(tre)
    }
}