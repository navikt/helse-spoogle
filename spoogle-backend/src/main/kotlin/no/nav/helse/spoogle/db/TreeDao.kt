package no.nav.helse.spoogle.db

import kotliquery.queryOf
import kotliquery.sessionOf
import no.nav.helse.spoogle.tree.*
import org.intellij.lang.annotations.Language
import javax.sql.DataSource

internal class TreeDao(private val dataSource: DataSource) {

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

    internal fun invaliderRelasjon(parent: NodeDto, child: NodeDto) {
        @Language("PostgreSQL")
        val query = """
           UPDATE edge
           SET ugyldig = now()
           WHERE edge.node_a = (SELECT node_id FROM node WHERE id = :parent AND id_type = :parent_type) AND
           edge.node_b = (SELECT node_id FROM node WHERE id = :child AND id_type = :child_type)
        """

        sessionOf(dataSource).use {
            it.run(
                queryOf(
                    query,
                    mapOf(
                        "parent" to parent.id,
                        "parent_type" to parent.type,
                        "child" to child.id,
                        "child_type" to child.type,
                    )
                ).asUpdate
            )
        }
    }

    internal fun finnTre(id: String): List<Pair<Node, Node>> {
        val fødselsnummer = finnFødselsnummer(id) ?: return emptyList()

        @Language("PostgreSQL")
        val query = """
            WITH RECURSIVE traverse(parent_id, parent_type, child_node_id, child_id, child_type) AS (
                SELECT
                    null::varchar, null::varchar, node_id, id, id_type
                FROM
                    node
                WHERE
                        node.id = :fodselsnummer AND id_type = 'FØDSELSNUMMER'
                UNION ALL
                SELECT
                    traverse.child_id,
                    traverse.child_type,
                    node.node_id,
                    node.id,
                    node.id_type
                FROM traverse
                    JOIN edge ON traverse.child_node_id = node_a
                    JOIN node ON node_b = node.node_id
            )
            SELECT
                traverse.parent_id, traverse.parent_type, traverse.child_id, traverse.child_type
            FROM traverse
            GROUP BY traverse.parent_id, traverse.parent_type, traverse.child_id, traverse.child_type, traverse.child_node_id
            ORDER BY traverse.child_node_id ASC;
        """

        val uniqueNodes = mutableMapOf<Pair<String, String>, Node>()

        return sessionOf(dataSource).use { session ->
            session.run(
                queryOf(query, mapOf("fodselsnummer" to fødselsnummer)).map<Pair<Node, Node>?> {
                    val parentId = it.stringOrNull("parent_id") ?: return@map null
                    val parentType = it.stringOrNull("parent_type") ?: return@map null
                    val childId = it.string("child_id")
                    val childType = it.string("child_type")
                    val parentNode = uniqueNodes.getOrPut(parentId to parentType) { Node(parentId, enumValueOf(parentType)) }
                    val childNode = uniqueNodes.getOrPut(childId to childType) { Node(childId, enumValueOf(childType)) }
                    parentNode to childNode
                }.asList
            ).filterNotNull()
        }
    }

    private fun finnFødselsnummer(id: String): String? {
        @Language("PostgreSQL")
        val query = """
            WITH fødselsnummer(fødselsnummer) AS (
                WITH RECURSIVE find_root_node(node_id, id, id_type) AS (
                    SELECT
                        node_id,
                        id,
                        id_type
                    FROM
                        node
                    WHERE node.id = :id
                    UNION
                    SELECT
                        node.node_id,
                        node.id,
                        node.id_type
                    FROM
                        node
                            JOIN edge ON node_a = node_id
                            INNER JOIN find_root_node ON find_root_node.node_id = edge.node_b
                )
                SELECT id FROM find_root_node WHERE id_type = 'FØDSELSNUMMER'
            )
            SELECT fødselsnummer FROM fødselsnummer;
        """

        return sessionOf(dataSource).use { session ->
            session.run(queryOf(query, mapOf("id" to id)).map { it.string("fødselsnummer") }.asSingle)
        }
    }

}