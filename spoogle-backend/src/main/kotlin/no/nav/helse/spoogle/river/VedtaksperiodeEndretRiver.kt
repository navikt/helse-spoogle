package no.nav.helse.spoogle.river

import no.nav.helse.rapids_rivers.JsonMessage
import no.nav.helse.rapids_rivers.MessageContext
import no.nav.helse.rapids_rivers.RapidsConnection
import no.nav.helse.rapids_rivers.River
import no.nav.helse.spoogle.TreService
import no.nav.helse.spoogle.asUUID
import no.nav.helse.spoogle.tre.Node
import no.nav.helse.spoogle.tre.Tre

internal class VedtaksperiodeEndretRiver(
    private val treService: TreService,
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

        val fødselsnummerNode = Node.fødselsnummer(fødselsnummer)
        val aktørIdNode = Node.aktørId(aktørId)
        val organisasjonsnummerNode = Node.organisasjonsnummer(organisasjonsnummer, fødselsnummer)
        val vedtaksperiodeIdNode = Node.vedtaksperiodeId(vedtaksperiodeId.toString())

        aktørIdNode barnAv fødselsnummerNode
        organisasjonsnummerNode barnAv fødselsnummerNode
        vedtaksperiodeIdNode barnAv organisasjonsnummerNode

        val tre = Tre.byggTre(fødselsnummerNode)
        treService.nyGren(tre)
    }
}