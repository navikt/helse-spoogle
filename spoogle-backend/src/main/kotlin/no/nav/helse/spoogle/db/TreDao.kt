package no.nav.helse.spoogle.db

import kotliquery.queryOf
import kotliquery.sessionOf
import no.nav.helse.spoogle.tre.Identifikatortype
import no.nav.helse.spoogle.tre.Identifikatortype.*
import no.nav.helse.spoogle.tre.Node
import no.nav.helse.spoogle.tre.NodeDto
import org.intellij.lang.annotations.Language
import java.time.LocalDateTime
import javax.sql.DataSource

internal class TreDao(private val dataSource: DataSource) {

    internal fun nyNode(node: NodeDto) {
        @Language("PostgreSQL")
        val query = "INSERT INTO node (id, id_type) VALUES (:id, :idType) ON CONFLICT DO NOTHING"
        sessionOf(dataSource).use {
            it.run(queryOf(query, mapOf("id" to node.id, "idType" to node.type)).asUpdate)
        }
    }

    internal fun nySti(forelder: NodeDto, barn: NodeDto) {
        @Language("PostgreSQL")
        val query =
            """
                INSERT INTO sti (forelder, barn) 
                VALUES (
                    (SELECT node_id FROM node WHERE id = :nodeAId), 
                    (SELECT node_id FROM node WHERE id = :nodeBId)
                ) 
                ON CONFLICT DO NOTHING
            """
        sessionOf(dataSource).use {
            it.run(queryOf(query, mapOf("nodeAId" to forelder.id, "nodeBId" to barn.id)).asUpdate)
        }
    }

    internal fun invaliderRelasjonerFor(node: NodeDto) {
        @Language("PostgreSQL")
        val query = """
           UPDATE sti
           SET ugyldig = now()
           WHERE forelder = (SELECT node_id FROM node WHERE id = :node AND id_type = :node_type) OR
           barn = (SELECT node_id FROM node WHERE id = :node AND id_type = :node_type)
        """

        sessionOf(dataSource).use {
            it.run(
                queryOf(
                    query,
                    mapOf(
                        "node" to node.id,
                        "node_type" to node.type,
                    )
                ).asUpdate
            )
        }
    }

    internal fun finnTre(id: String): List<Triple<Node, Node, LocalDateTime?>> {
        val fødselsnummer = finnFødselsnummer(id) ?: return emptyList()

        @Language("PostgreSQL")
        val query = """
            WITH RECURSIVE traverse(forelder_id, forelder_type, barn_node_id, barn_id, barn_type, ugyldig_fra) AS (
                SELECT
                    null::varchar, null::varchar, node_id, id, id_type, null::timestamp
                FROM
                    node
                WHERE
                        node.id = :fodselsnummer AND id_type = 'FØDSELSNUMMER'
                UNION ALL
                SELECT
                    traverse.barn_id,
                    traverse.barn_type,
                    node.node_id,
                    node.id,
                    node.id_type,
                    sti.ugyldig
                FROM traverse
                    JOIN sti ON traverse.barn_node_id = sti.forelder
                    JOIN node ON sti.barn = node.node_id
            )
            SELECT
                traverse.forelder_id, traverse.forelder_type, traverse.barn_id, traverse.barn_type, traverse.ugyldig_fra
            FROM traverse
            GROUP BY traverse.forelder_id, traverse.forelder_type, traverse.barn_id, traverse.barn_type, traverse.barn_node_id, traverse.ugyldig_fra
            ORDER BY traverse.barn_node_id ASC;
        """

        val uniqueNodes = mutableMapOf<Pair<String, String>, Node>()

        return sessionOf(dataSource).use { session ->
            session.run(
                queryOf(query, mapOf("fodselsnummer" to fødselsnummer)).map<Triple<Node, Node, LocalDateTime?>?> {
                    val parentId = it.stringOrNull("forelder_id") ?: return@map null
                    val parentType = it.stringOrNull("forelder_type") ?: return@map null
                    val childId = it.string("barn_id")
                    val childType = it.string("barn_type")
                    val ugyldigFra = it.localDateTimeOrNull("ugyldig_fra")
                    val parentNode = uniqueNodes.getOrPut(parentId to parentType) { toNode(parentId, parentType, fødselsnummer) }
                    val childNode = uniqueNodes.getOrPut(childId to childType) { toNode(childId, childType, fødselsnummer) }
                    Triple(parentNode, childNode, ugyldigFra)
                }.asList
            ).filterNotNull()
        }
    }

    private fun toNode(id: String, type: String, fødselsnummer: String) =
        when (enumValueOf<Identifikatortype>(type)) {
            ORGANISASJONSNUMMER -> Node.organisasjonsnummer(id.split("+").first(), fødselsnummer)
            FØDSELSNUMMER -> Node.fødselsnummer(id)
            AKTØR_ID -> Node.aktørId(id)
            VEDTAKSPERIODE_ID -> Node.vedtaksperiodeId(id)
            UTBETALING_ID -> Node.utbetalingId(id)
            SØKNAD_ID -> Node.søknadId(id)
            INNTEKTSMELDING_ID -> Node.inntektsmeldingId(id)
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
                            JOIN sti ON sti.forelder = node_id
                            INNER JOIN find_root_node ON find_root_node.node_id = sti.barn
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