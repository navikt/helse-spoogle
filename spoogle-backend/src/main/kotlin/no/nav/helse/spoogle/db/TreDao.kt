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
    internal fun nyRelasjon(
        barn: NodeDto,
        forelder: NodeDto?,
    ) {
        @Language("PostgreSQL")
        val query =
            """
                INSERT INTO relasjon (node, forelder, type, opprettet) 
                VALUES (:node_id, :forelder_id, :node_id_type::id_type, :now) 
                ON CONFLICT(node) DO UPDATE SET forelder = excluded.forelder WHERE relasjon.forelder IS NULL;
            """
        sessionOf(dataSource).use {
            it.run(
                queryOf(
                    query,
                    mapOf(
                        "node_id" to barn.id,
                        "forelder_id" to forelder?.id,
                        "node_id_type" to barn.type,
                        "now" to LocalDateTime.now(),
                    ),
                ).asUpdate,
            )
        }
    }

    internal fun invaliderRelasjonerFor(node: NodeDto) {
        @Language("PostgreSQL")
        val query = """
           UPDATE relasjon
           SET ugyldig = now()
           WHERE forelder = :node_id OR
           node = :node_id
        """

        sessionOf(dataSource).use {
            it.run(
                queryOf(
                    query,
                    mapOf(
                        "node_id" to node.id,
                    ),
                ).asUpdate,
            )
        }
    }

    internal fun finnTre(id: String): List<Relasjon> {
        val fødselsnummer = finnFødselsnummer(id) ?: return emptyList()

        @Language("PostgreSQL")
        val query = """
            WITH RECURSIVE alle_noder(forelder_id, forelder_type, barn_id, barn_type, ugyldig_fra) AS (
                SELECT
                    null::varchar, null::id_type, node, type, null::timestamp
                FROM
                    relasjon
                WHERE
                        node = :fodselsnummer AND type = 'FØDSELSNUMMER'
                UNION ALL
                SELECT
                    alle_noder.barn_id,
                    alle_noder.barn_type,
                    node,
                    type,
                    ugyldig
                FROM alle_noder
                    JOIN relasjon ON alle_noder.barn_id = relasjon.forelder
            )
            SELECT
                alle_noder.forelder_id, alle_noder.forelder_type, alle_noder.barn_id, alle_noder.barn_type, alle_noder.ugyldig_fra
            FROM alle_noder
            GROUP BY alle_noder.forelder_id, alle_noder.forelder_type, alle_noder.barn_id, alle_noder.barn_type, alle_noder.ugyldig_fra
            ORDER BY alle_noder.barn_id ASC
        """

        val uniqueNodes = mutableMapOf<Pair<String, String>, Node>()

        return sessionOf(dataSource).use { session ->
            session.run(
                queryOf(query, mapOf("fodselsnummer" to fødselsnummer)).map<Relasjon?> {
                    val parentId = it.stringOrNull("forelder_id") ?: return@map null
                    val parentType = it.stringOrNull("forelder_type") ?: return@map null
                    val childId = it.string("barn_id")
                    val childType = it.string("barn_type")
                    val ugyldigFra = it.localDateTimeOrNull("ugyldig_fra")
                    val parentNode = uniqueNodes.getOrPut(parentId to parentType) { toNode(parentId, parentType, fødselsnummer) }
                    val childNode = uniqueNodes.getOrPut(childId to childType) { toNode(childId, childType, fødselsnummer) }
                    Relasjon(parentNode, childNode, ugyldigFra)
                }.asList,
            ).filterNotNull()
        }
    }

    private fun toNode(
        id: String,
        type: String,
        fødselsnummer: String,
    ) = when (enumValueOf<Identifikatortype>(type)) {
        ORGANISASJONSNUMMER -> Node.organisasjonsnummer(id.split("+").first(), fødselsnummer)
        FØDSELSNUMMER -> Node.fødselsnummer(id)
        AKTØR_ID -> Node.aktørId(id)
        VEDTAKSPERIODE_ID -> Node.vedtaksperiodeId(id)
        BEHANDLING_ID -> Node.behandlingId(id)
        UTBETALING_ID -> Node.utbetalingId(id)
        SØKNAD_ID -> Node.søknadId(id)
        INNTEKTSMELDING_ID -> Node.inntektsmeldingId(id)
        OPPGAVE_ID -> Node.oppgaveId(id)
    }

    private fun finnFødselsnummer(id: String): String? {
        @Language("PostgreSQL")
        val query = """
            WITH RECURSIVE find_root_node(id, id_type) AS (
                SELECT node, type FROM relasjon
                WHERE node = :id
                UNION
                SELECT 
                    forelder,
                    (SELECT type FROM relasjon r2 WHERE node = relasjon.forelder)
                FROM find_root_node 
                    JOIN relasjon ON relasjon.node = find_root_node.id
            )
                SELECT id
                FROM find_root_node
                WHERE id_type = 'FØDSELSNUMMER'
        """

        return sessionOf(dataSource).use { session ->
            session.run(queryOf(query, mapOf("id" to id)).map { it.string("id") }.asSingle)
        }
    }
}
