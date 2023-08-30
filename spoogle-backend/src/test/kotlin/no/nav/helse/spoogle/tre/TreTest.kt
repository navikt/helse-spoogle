package no.nav.helse.spoogle.tre

import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import org.intellij.lang.annotations.Language
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test

internal class TreTest {

    @Test
    fun `to json`() {
        val rootNode = Node.fødselsnummer("fnr")
        val childNode1 = Node.organisasjonsnummer("orgnr1", "fnr")
        val childNode2 = Node.organisasjonsnummer("orgnr2", "fnr")
        val grandChildNode1 = Node.vedtaksperiodeId("periode1")
        val grandChildNode2 = Node.vedtaksperiodeId("periode2")

        childNode1 barnAv rootNode
        childNode2 barnAv rootNode
        grandChildNode1 barnAv childNode1
        grandChildNode2 barnAv childNode2

        val tre = Tre.byggTre(rootNode)
        val json = jacksonObjectMapper().readTree(tre.toJson())
        val expectedJson = jacksonObjectMapper().readTree(expectedJson)

        assertEquals(expectedJson, json)
    }

    @Test
    fun `path to - ett fnr`() {
        val rootNode = Node.fødselsnummer("fnr")
        val childNode1 = Node.organisasjonsnummer("orgnr1", "fnr")
        val childNode2 = Node.organisasjonsnummer("orgnr2", "fnr")
        val grandChildNode1 = Node.vedtaksperiodeId("periode1")
        val grandChildNode2 = Node.vedtaksperiodeId("periode2")

        childNode1 barnAv rootNode
        childNode2 barnAv rootNode
        grandChildNode1 barnAv childNode1
        grandChildNode2 barnAv childNode2

        val tre = Tre.byggTre(rootNode)
        val path1 = tre.pathTo("periode2")
        val path2 = tre.pathTo("periode1")
        val path3 = tre.pathTo("orgnr1")
        assertEquals(listOf("fnr", "orgnr2", "periode2"), path1)
        assertEquals(listOf("fnr", "orgnr1", "periode1"), path2)
        assertEquals(listOf("fnr", "orgnr1"), path3)
    }

    @Test
    fun `path to - to fnr`() {
        val fnrNode1 = Node.fødselsnummer("fnr1")
        val fnrNode2 = Node.fødselsnummer("fnr2")
        val orgnrNode1 = Node.organisasjonsnummer("orgnr1", "fnr1")
        val orgnrNode2 = Node.organisasjonsnummer("orgnr2", "fnr1")
        val orgnrNode3 = Node.organisasjonsnummer("orgnr1", "fnr2")
        val periodeNode1 = Node.vedtaksperiodeId("periode1")
        val periodeNode2 = Node.vedtaksperiodeId("periode2")
        val periodeNode3 = Node.vedtaksperiodeId("periode3")
        val periodeNode4 = Node.vedtaksperiodeId("periode4")

        orgnrNode1 barnAv fnrNode1
        orgnrNode2 barnAv fnrNode1
        periodeNode1 barnAv orgnrNode1
        periodeNode2 barnAv orgnrNode2

        orgnrNode3 barnAv fnrNode2
        periodeNode3 barnAv orgnrNode3
        periodeNode4 barnAv orgnrNode3

        val tre1 = Tre.byggTre(fnrNode1)
        val path1 = tre1.pathTo("periode2")
        val path2 = tre1.pathTo("periode1")
        val path3 = tre1.pathTo("orgnr1")

        val tre2 = Tre.byggTre(fnrNode2)
        val path4 = tre2.pathTo("periode4")
        assertEquals(listOf("fnr1", "orgnr2", "periode2"), path1)
        assertEquals(listOf("fnr1", "orgnr1", "periode1"), path2)
        assertEquals(listOf("fnr1", "orgnr1"), path3)
        assertEquals(listOf("fnr2", "orgnr1", "periode4"), path4)
    }

    @Language("JSON")
    private val expectedJson = """
       {
            "id": "fnr",
            "type": "FØDSELSNUMMER",
            "children": [
                {
                    "id": "orgnr1",
                    "type": "ORGANISASJONSNUMMER",
                    "children": [
                        {
                            "id": "periode1",
                            "type": "VEDTAKSPERIODE_ID",
                            "children": [],
                            "ugyldig_fra": null
                        }
                    ],
                    "ugyldig_fra": null
                },
                {
                    "id": "orgnr2",
                    "type": "ORGANISASJONSNUMMER",
                    "children": [
                        {
                            "id": "periode2",
                            "type": "VEDTAKSPERIODE_ID",
                            "children": [],
                            "ugyldig_fra": null
                        }
                    ],
                    "ugyldig_fra": null
                }
            ],
            "ugyldig_fra": null
       } 
    """
}