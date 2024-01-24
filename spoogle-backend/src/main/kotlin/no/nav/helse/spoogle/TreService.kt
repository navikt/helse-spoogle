package no.nav.helse.spoogle

import no.nav.helse.spoogle.db.TreDao
import no.nav.helse.spoogle.tre.Node
import no.nav.helse.spoogle.tre.Tre
import no.nav.helse.spoogle.tre.NodeDto
import no.nav.helse.spoogle.tre.Relasjon.Companion.byggTre
import javax.sql.DataSource

internal interface ITreeService {
    fun finnTre(id: String): Tre?
}

internal class TreService(dataSource: DataSource): ITreeService {
    private val dao = TreDao(dataSource)

    override fun finnTre(id: String): Tre? {
        val relasjoner = dao.finnTre(id)
        return relasjoner.byggTre()
    }

    internal fun nyGren(tre: Tre) {
        val dto = tre.toDto()
        dto.rotnode.barn.forEach {
            nyRelasjon(dto.rotnode, it)
        }
    }

    internal fun invaliderRelasjonerFor(node: Node) {
        dao.invaliderRelasjonerFor(node.toDto())
    }

    private fun nyRelasjon(forelder: NodeDto, barn: NodeDto) {
        dao.nyNode(forelder)
        dao.nyNode(barn)
        dao.nySti(forelder, barn)
        barn.barn.forEach {
            nyRelasjon(barn, it)
        }
    }
}