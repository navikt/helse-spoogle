package no.nav.helse.spoogle

import no.nav.helse.spoogle.db.TreDao
import no.nav.helse.spoogle.tre.Node
import no.nav.helse.spoogle.tre.NodeDto
import no.nav.helse.spoogle.tre.Relasjon.Companion.byggTre
import no.nav.helse.spoogle.tre.Tre
import javax.sql.DataSource

internal interface ITreeService {
    fun finnTre(id: String): Tre?
}

internal class TreService(dataSource: DataSource) : ITreeService {
    private val dao = TreDao(dataSource)

    override fun finnTre(id: String): Tre? {
        val relasjoner = dao.finnTre(id)
        return relasjoner.byggTre()
    }

    internal fun nyGren(tre: Tre) {
        val rotnode = tre.toDto().rotnode
        dao.nyRelasjon(rotnode, null)
        rotnode.barn.forEach { direkteEtterkommer ->
            nyRelasjon(direkteEtterkommer, rotnode)
        }
    }

    internal fun invaliderRelasjonerFor(node: Node) {
        dao.invaliderRelasjonerFor(node.toDto())
    }

    private fun nyRelasjon(
        barn: NodeDto,
        forelder: NodeDto,
    ) {
        dao.nyRelasjon(barn, forelder)
        barn.barn.forEach { barnebarn ->
            nyRelasjon(barnebarn, barn)
        }
    }
}
