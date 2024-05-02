package no.nav.helse.spoogle.river

import no.nav.helse.rapids_rivers.JsonMessage
import no.nav.helse.rapids_rivers.MessageContext
import no.nav.helse.rapids_rivers.RapidsConnection
import no.nav.helse.rapids_rivers.River
import no.nav.helse.spoogle.TreService
import no.nav.helse.spoogle.asUUID
import no.nav.helse.spoogle.tre.Node
import no.nav.helse.spoogle.tre.Tre

internal class BehandlingOpprettetRiver(
    private val treService: TreService,
    rapidsConnection: RapidsConnection
): River.PacketListener {

    init {
        River(rapidsConnection).apply {
            validate {
                it.demandValue("@event_name", "behandling_opprettet")
                it.requireKey("fødselsnummer", "aktørId", "organisasjonsnummer", "vedtaksperiodeId", "behandlingId")
            }
        }.register(this)
    }

    override fun onPacket(packet: JsonMessage, context: MessageContext) {
        val fødselsnummer = packet["fødselsnummer"].asText()
        val aktørId = packet["aktørId"].asText()
        val organisasjonsnummer = packet["organisasjonsnummer"].asText()
        val vedtaksperiodeId = packet["vedtaksperiodeId"].asUUID()
        val behandlingId = packet["behandlingId"].asUUID()

        val fødselsnummerNode = Node.fødselsnummer(fødselsnummer)
        val aktørIdNode = Node.aktørId(aktørId)
        val organisasjonsnummerNode = Node.organisasjonsnummer(organisasjonsnummer, fødselsnummer)
        val vedtaksperiodeIdNode = Node.vedtaksperiodeId(vedtaksperiodeId.toString())
        val behandlingIdNode = Node.behandlingId(behandlingId.toString())

        aktørIdNode barnAv fødselsnummerNode
        organisasjonsnummerNode barnAv fødselsnummerNode
        vedtaksperiodeIdNode barnAv organisasjonsnummerNode
        behandlingIdNode barnAv vedtaksperiodeIdNode

        val tre = Tre.byggTre(fødselsnummerNode)
        treService.nyGren(tre)
    }
}