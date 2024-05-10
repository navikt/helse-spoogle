package no.nav.helse.opprydding

import kotliquery.queryOf
import kotliquery.sessionOf
import org.intellij.lang.annotations.Language
import javax.sql.DataSource

internal class PersonDao(private val dataSource: DataSource) {
    internal fun slett(fødselsnummer: String) {
        @Language("PostgreSQL")
        val query =
            """
                WITH RECURSIVE find_root_node(id) AS (
                    SELECT node FROM relasjon
                    WHERE node = :fodselsnummer
                    UNION
                    SELECT 
                        forelder
                    FROM find_root_node 
                        JOIN relasjon ON relasjon.node = find_root_node.id
                )
            DELETE FROM relasjon WHERE node IN (SELECT id FROM find_root_node);
            """.trimIndent()

        sessionOf(dataSource).use {
            it.run(queryOf(query, mapOf("fodselsnummer" to fødselsnummer)).asUpdate)
        }
    }
}
