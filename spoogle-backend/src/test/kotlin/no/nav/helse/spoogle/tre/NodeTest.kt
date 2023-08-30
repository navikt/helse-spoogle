package no.nav.helse.spoogle.tre

import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.Test
import java.time.LocalDateTime

class NodeTest {

    @Test
    fun forelder() {
        val fnr = Node.fødselsnummer("12345678910")
        val orgnr = Node.organisasjonsnummer("987654321", "12345678910")
        orgnr barnAv fnr
        assertFalse(fnr.harForelder())
        assertTrue(orgnr.harForelder())
    }

    @Test
    fun ugyldigRelasjon() {
        val fnr = Node.fødselsnummer("12345678910")
        val orgnr = Node.organisasjonsnummer("987654321", "12345678910")
        orgnr barnAv fnr
        fnr.ugyldigRelasjon(orgnr, LocalDateTime.now())
        assertEquals(1, fnr.toDto().ugyldigeBarn.size)
    }

    @Test
    fun `finn direkte`() {
        val fnr = Node.fødselsnummer("12345678910")
        val sti = fnr.finn("12345678910")
        assertEquals(listOf("12345678910"), sti)
    }

    @Test
    fun `finn et nivå ned`() {
        val fnr = Node.fødselsnummer("12345678910")
        val orgnr = Node.organisasjonsnummer("987654321", "12345678910")
        orgnr barnAv fnr
        val sti = fnr.finn("987654321")
        assertEquals(listOf("12345678910", "987654321"), sti)
    }

    @Test
    fun `finn ved flere nivåer og flere grener`() {
        val fnr = Node.fødselsnummer("12345678910")
        val orgnr = Node.organisasjonsnummer("987654321", "12345678910")
        val periode1 = Node.vedtaksperiodeId("79dbe8d6-fd2d-4a84-8051-b64460f69523")
        val periode2 = Node.vedtaksperiodeId("cf24c678-05f1-4cd1-b650-0f2658355971")
        orgnr barnAv fnr
        periode1 barnAv orgnr
        periode2 barnAv orgnr
        val sti = fnr.finn("cf24c678-05f1-4cd1-b650-0f2658355971")
        assertEquals(listOf("12345678910", "987654321", "cf24c678-05f1-4cd1-b650-0f2658355971"), sti)
    }
}