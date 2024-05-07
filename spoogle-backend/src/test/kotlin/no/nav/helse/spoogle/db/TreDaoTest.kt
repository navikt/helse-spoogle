package no.nav.helse.spoogle.db

import kotliquery.queryOf
import kotliquery.sessionOf
import no.nav.helse.spoogle.tre.NodeDto
import org.intellij.lang.annotations.Language
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.Test

internal class TreDaoTest : AbstractDatabaseTest() {
    private val dao = TreDao(dataSource)

    @Test
    fun `opprett edge`() {
        dao.nyRelasjon(nodeDto("A"), null) // Oppretter rotnode
        dao.nyRelasjon(nodeDto("B"), nodeDto("A"))
        assertRelasjon("A", "B")
    }

    @Test
    fun `forsøk å opprette samme edge to ganger`() {
        dao.nyRelasjon(nodeDto("A"), null) // Oppretter rotnode
        dao.nyRelasjon(nodeDto("B"), nodeDto("A"))
        assertDoesNotThrow {
            dao.nyRelasjon(nodeDto("B"), nodeDto("A"))
        }
        assertRelasjon("A", "B")
    }

    @Test
    fun `invalider relasjon for node`() {
        dao.nyRelasjon(nodeDto("A"), null) // Oppretter rotnode
        dao.nyRelasjon(nodeDto("B"), nodeDto("A"))
        dao.nyRelasjon(nodeDto("C"), nodeDto("B"))
        dao.invaliderRelasjonerFor(nodeDto("B"))
        assertUgyldig("A", "B")
        assertUgyldig("B", "C")
    }

    private fun assertUgyldig(
        forelderId: String,
        barnId: String,
    ) {
        @Language("PostgreSQL")
        val query = """
             SELECT ugyldig FROM relasjon 
             WHERE 
                forelder = ? AND
                node = ?
        """

        val ugyldig =
            sessionOf(dataSource).use { session ->
                session.run(queryOf(query, forelderId, barnId).map { it.localDateTimeOrNull("ugyldig") }.asSingle)
            }

        assertNotNull(ugyldig)
    }

    private fun assertRelasjon(
        forelderId: String,
        barnId: String,
    ) {
        @Language("PostgreSQL")
        val query = "SELECT COUNT(1) FROM relasjon WHERE forelder = ? AND node = ?"
        val antall =
            sessionOf(dataSource).use { session ->
                session.run(queryOf(query, forelderId, barnId).map { it.int(1) }.asSingle)
            }
        assertEquals(1, antall)
    }

    private fun nodeDto(id: String) = NodeDto(id, "FØDSELSNUMMER", emptyList(), emptyList())
}
