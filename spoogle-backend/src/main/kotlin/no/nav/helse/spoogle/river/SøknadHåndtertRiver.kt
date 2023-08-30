package no.nav.helse.spoogle.river

import no.nav.helse.rapids_rivers.JsonMessage
import no.nav.helse.rapids_rivers.MessageContext
import no.nav.helse.rapids_rivers.RapidsConnection
import no.nav.helse.rapids_rivers.River
import no.nav.helse.spoogle.TreeService
import no.nav.helse.spoogle.asUUID
import no.nav.helse.spoogle.tree.Node
import no.nav.helse.spoogle.tree.Tree

internal class SøknadHåndtertRiver(
    private val treeService: TreeService,
    rapidsConnection: RapidsConnection
): River.PacketListener {
    init {
        River(rapidsConnection).apply {
            validate {
                it.demandValue("@event_name", "søknad_håndtert")
                it.requireKey("søknadId", "vedtaksperiodeId")
            }
        }.register(this)
    }

    override fun onPacket(packet: JsonMessage, context: MessageContext) {
        val vedtaksperiodeId = packet["vedtaksperiodeId"].asUUID()
        val søknadId = packet["søknadId"].asUUID()
        val vedtaksperiodeIdNode = Node.vedtaksperiodeId(vedtaksperiodeId.toString())
        val søknadIdNode = Node.søknadId(søknadId.toString())

        vedtaksperiodeIdNode forelderAv søknadIdNode

        treeService.nyGren(Tree.buildTree(vedtaksperiodeIdNode))
    }
}