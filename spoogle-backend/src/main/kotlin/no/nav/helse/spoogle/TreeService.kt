package no.nav.helse.spoogle

import no.nav.helse.spoogle.db.GraphDao
import no.nav.helse.spoogle.graph.Tree
import no.nav.helse.spoogle.graph.NodeDto
import javax.sql.DataSource

internal class TreeService(dataSource: DataSource) {
    private val dao = GraphDao(dataSource)

    internal fun finnTre(id: String): Tree? {
        val nodes = dao.finnGraph(id)
        nodes.forEach { (parent, child) ->
            parent parentOf child
        }
        val (rootNode, _) = nodes.find { !it.first.hasParent() } ?: return null
        return Tree.buildTree(rootNode)
    }

    internal fun nyGren(tree: Tree) {
        val dto = tree.toDto()
        dto.rootNode.children.forEach {
            nyRelasjon(dto.rootNode, it)
        }
    }

    private fun nyRelasjon(parent: NodeDto, child: NodeDto) {
        dao.nyNode(parent)
        dao.nyNode(child)
        dao.nyEdge(parent, child)
        child.children.forEach {
            nyRelasjon(child, it)
        }
    }
}