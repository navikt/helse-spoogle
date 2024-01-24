package no.nav.helse.opprydding

import kotliquery.queryOf
import kotliquery.sessionOf
import org.intellij.lang.annotations.Language
import javax.sql.DataSource

internal class PersonDao(private val dataSource: DataSource) {
    internal fun slett(fødselsnummer: String) {
        @Language("PostgreSQL")
        val query = """
            WITH RECURSIVE alle_noder(forelder_key, forelder_id_type, barn_key, barn_id, barn_id_type, ugyldig_fra) AS (
                SELECT
                    null::varchar, null::varchar, key, id, id_type, null::timestamp
                FROM
                    node
                WHERE
                        node.id = :fodselsnummer AND id_type = 'FØDSELSNUMMER'
                UNION ALL
                SELECT
                    alle_noder.barn_id,
                    alle_noder.barn_id_type,
                    node.key,
                    node.id,
                    node.id_type,
                    sti.ugyldig
                FROM alle_noder
                    JOIN sti ON alle_noder.barn_key = sti.forelder
                    JOIN node ON sti.barn = node.key
            )
            DELETE FROM node WHERE node.key IN (SELECT alle_noder.barn_key FROM alle_noder);
        """.trimIndent()

        sessionOf(dataSource).use {
            it.run(queryOf(query, mapOf("fodselsnummer" to fødselsnummer)).asUpdate)
        }
    }
}
