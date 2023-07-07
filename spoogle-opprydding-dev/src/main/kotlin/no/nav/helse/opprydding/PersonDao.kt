package no.nav.helse.opprydding

import kotliquery.queryOf
import kotliquery.sessionOf
import org.intellij.lang.annotations.Language
import javax.sql.DataSource

internal class PersonDao(private val dataSource: DataSource) {
    internal fun slett(fødselsnummer: String) {
        val nodeId = finnNodeId(fødselsnummer) ?: return
        slettEdge(nodeId)
        slettNode(nodeId)
    }

    private fun slettEdge(nodeId: Long) {
        @Language("PostgreSQL")
        val query = """ DELETE FROM edge WHERE node_a = ? """
        sessionOf(dataSource).use {
            it.run(queryOf(query, nodeId).asExecute)
        }
    }

    private fun slettNode(nodeId: Long) {
        @Language("PostgreSQL")
        val query = """ DELETE FROM node WHERE node_id = ? """
        sessionOf(dataSource).use {
            it.run(queryOf(query, nodeId).asExecute)
        }
    }

    private fun finnNodeId(fødselsnummer: String): Long? {
        @Language("PostgreSQL")
        val query = """
           SELECT node_id FROM node WHERE id = ? AND id_type = 'FØDSELSNUMMER'
        """
        return sessionOf(dataSource).use { session ->
            session.run(queryOf(query, fødselsnummer).map { it.long("node_id") }.asSingle)
        }
    }
}
