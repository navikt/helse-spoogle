package no.nav.helse.spoogle.db

import kotliquery.queryOf
import kotliquery.sessionOf
import no.nav.helse.spoogle.graph.NodeDto
import org.intellij.lang.annotations.Language
import javax.sql.DataSource

internal class GraphDao(private val dataSource: DataSource) {

    internal fun nyNode(node: NodeDto) {
        @Language("PostgreSQL")
        val query = "INSERT INTO node (id, id_type) VALUES (:id, :idType) ON CONFLICT DO NOTHING"
        sessionOf(dataSource).use {
            it.run(queryOf(query, mapOf("id" to node.id, "idType" to node.type)).asUpdate)
        }
    }

    internal fun nyEdge(nodeA: NodeDto, nodeB: NodeDto) {
        @Language("PostgreSQL")
        val query =
            """
                INSERT INTO edge (node_A, node_B) 
                VALUES (
                    (SELECT node_id FROM node WHERE id = :nodeAId), 
                    (SELECT node_id FROM node WHERE id = :nodeBId)
                ) 
                ON CONFLICT DO NOTHING
            """
        sessionOf(dataSource).use {
            it.run(queryOf(query, mapOf("nodeAId" to nodeA.id, "nodeBId" to nodeB.id)).asUpdate)
        }
    }

}