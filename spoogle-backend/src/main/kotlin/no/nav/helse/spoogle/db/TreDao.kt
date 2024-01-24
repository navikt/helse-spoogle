package no.nav.helse.spoogle.db

import kotliquery.queryOf
import kotliquery.sessionOf
import no.nav.helse.spoogle.tre.Identifikatortype
import no.nav.helse.spoogle.tre.Identifikatortype.*
import no.nav.helse.spoogle.tre.Node
import no.nav.helse.spoogle.tre.NodeDto
import no.nav.helse.spoogle.tre.Relasjon
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
                    (SELECT key FROM node WHERE id = :nodeAId), 
                    (SELECT key FROM node WHERE id = :nodeBId)
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
           WHERE forelder = (SELECT key FROM node WHERE id = :id AND id_type = :node_type) OR
           barn = (SELECT key FROM node WHERE id = :id AND id_type = :node_type)
        """

        sessionOf(dataSource).use {
            it.run(
                queryOf(
                    query,
                    mapOf(
                        "id" to node.id,
                        "node_type" to node.type,
                    )
                ).asUpdate
            )
        }
    }

    internal fun finnTre(id: String): List<Relasjon> {
        val fødselsnummer = finnFødselsnummer(id) ?: return emptyList()

        @Language("PostgreSQL")
        val query = """
            WITH RECURSIVE alle_noder(forelder_key, forelder_type, barn_key, barn_id, barn_type, ugyldig_fra) AS (
                SELECT
                    null::varchar, null::varchar, key, id, id_type, null::timestamp
                FROM
                    node
                WHERE
                        node.id = :fodselsnummer AND id_type = 'FØDSELSNUMMER'
                UNION ALL
                SELECT
                    alle_noder.barn_id,
                    alle_noder.barn_type,
                    node.key,
                    node.id,
                    node.id_type,
                    sti.ugyldig
                FROM alle_noder
                    JOIN sti ON alle_noder.barn_key = sti.forelder
                    JOIN node ON sti.barn = node.key
            )
            SELECT
                alle_noder.forelder_key, alle_noder.forelder_type, alle_noder.barn_id, alle_noder.barn_type, alle_noder.ugyldig_fra
            FROM alle_noder
            GROUP BY alle_noder.forelder_key, alle_noder.forelder_type, alle_noder.barn_id, alle_noder.barn_type, alle_noder.barn_key, alle_noder.ugyldig_fra
            ORDER BY alle_noder.barn_key ASC;
        """

        val uniqueNodes = mutableMapOf<Pair<String, String>, Node>()

        return sessionOf(dataSource).use { session ->
            session.run(
                queryOf(query, mapOf("fodselsnummer" to fødselsnummer)).map<Relasjon?> {
                    val parentId = it.stringOrNull("forelder_key") ?: return@map null
                    val parentType = it.stringOrNull("forelder_type") ?: return@map null
                    val childId = it.string("barn_id")
                    val childType = it.string("barn_type")
                    val ugyldigFra = it.localDateTimeOrNull("ugyldig_fra")
                    val parentNode = uniqueNodes.getOrPut(parentId to parentType) { toNode(parentId, parentType, fødselsnummer) }
                    val childNode = uniqueNodes.getOrPut(childId to childType) { toNode(childId, childType, fødselsnummer) }
                    Relasjon(parentNode, childNode, ugyldigFra)
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
                        key,
                        id,
                        id_type
                    FROM
                        node
                    WHERE node.id = :id
                    UNION
                    SELECT
                        node.key,
                        node.id,
                        node.id_type
                    FROM
                        node
                            JOIN sti ON sti.forelder = key
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