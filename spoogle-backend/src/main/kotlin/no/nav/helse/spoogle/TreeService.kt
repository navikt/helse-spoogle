package no.nav.helse.spoogle

import no.nav.helse.spoogle.db.TreeDao
import no.nav.helse.spoogle.tree.Node
import no.nav.helse.spoogle.tree.Tree
import no.nav.helse.spoogle.tree.NodeDto
import javax.sql.DataSource

internal interface ITreeService {
    fun finnTre(id: String): Tree?
}

internal class TreeService(dataSource: DataSource): ITreeService {
    private val dao = TreeDao(dataSource)

    override fun finnTre(id: String): Tree? {
        val nodes = dao.finnTre(id)
        nodes.forEach { (parent, child, ugyldigFra) ->
            parent forelderAv child
            if (ugyldigFra != null) parent.ugyldigRelasjon(child, ugyldigFra)
        }
        val (rootNode, _) = nodes.find { !it.first.harForelder() } ?: return null
        return Tree.buildTree(rootNode)
    }

    internal fun nyGren(tree: Tree) {
        val dto = tree.toDto()
        dto.rootNode.barn.forEach {
            nyRelasjon(dto.rootNode, it)
        }
    }

    internal fun invaliderRelasjonerFor(node: Node) {
        dao.invaliderRelasjonerFor(node.toDto())
    }

    private fun nyRelasjon(parent: NodeDto, child: NodeDto) {
        dao.nyNode(parent)
        dao.nyNode(child)
        dao.nyEdge(parent, child)
        child.barn.forEach {
            nyRelasjon(child, it)
        }
    }
}