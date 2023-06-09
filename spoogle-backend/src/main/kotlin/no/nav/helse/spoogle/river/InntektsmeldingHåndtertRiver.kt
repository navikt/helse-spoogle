package no.nav.helse.spoogle.river

import no.nav.helse.rapids_rivers.JsonMessage
import no.nav.helse.rapids_rivers.MessageContext
import no.nav.helse.rapids_rivers.RapidsConnection
import no.nav.helse.rapids_rivers.River
import no.nav.helse.spoogle.TreeService
import no.nav.helse.spoogle.asUUID
import no.nav.helse.spoogle.tree.Identifikatortype
import no.nav.helse.spoogle.tree.Identifikatortype.VEDTAKSPERIODE_ID
import no.nav.helse.spoogle.tree.Node
import no.nav.helse.spoogle.tree.Tree

internal class InntektsmeldingHåndtertRiver(
    private val treeService: TreeService,
    rapidsConnection: RapidsConnection
): River.PacketListener {
    init {
        River(rapidsConnection).apply {
            validate {
                it.demandValue("@event_name", "inntektsmelding_håndtert")
                it.requireKey("inntektsmeldingId", "vedtaksperiodeId")
            }
        }.register(this)
    }

    override fun onPacket(packet: JsonMessage, context: MessageContext) {
        val vedtaksperiodeId = packet["vedtaksperiodeId"].asUUID()
        val søknadId = packet["inntektsmeldingId"].asUUID()
        val vedtaksperiodeIdNode = Node(vedtaksperiodeId.toString(), VEDTAKSPERIODE_ID)
        val inntektsmeldingIdNode = Node(søknadId.toString(), Identifikatortype.INNTEKTSMELDING_ID)

        vedtaksperiodeIdNode parentOf inntektsmeldingIdNode

        treeService.nyGren(Tree.buildTree(vedtaksperiodeIdNode))
    }
}