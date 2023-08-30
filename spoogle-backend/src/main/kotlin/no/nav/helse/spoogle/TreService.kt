package no.nav.helse.spoogle

import no.nav.helse.spoogle.db.TreDao
import no.nav.helse.spoogle.tre.Node
import no.nav.helse.spoogle.tre.Tre
import no.nav.helse.spoogle.tre.NodeDto
import javax.sql.DataSource

internal interface ITreeService {
    fun finnTre(id: String): Tre?
}

internal class TreService(dataSource: DataSource): ITreeService {
    private val dao = TreDao(dataSource)

    override fun finnTre(id: String): Tre? {
        val noder = dao.finnTre(id)
        noder.forEach { (forelder, barn, ugyldigFra) ->
            forelder forelderAv barn
            if (ugyldigFra != null) forelder.ugyldigRelasjon(barn, ugyldigFra)
        }
        val (rotnode, _) = noder.find { !it.first.harForelder() } ?: return null
        return Tre.byggTre(rotnode)
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