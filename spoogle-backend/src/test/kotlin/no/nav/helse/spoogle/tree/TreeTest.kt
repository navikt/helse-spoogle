package no.nav.helse.spoogle.tree

import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import no.nav.helse.spoogle.tree.Identifikatortype.*
import org.intellij.lang.annotations.Language
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.Test

internal class TreeTest {

    @Test
    fun `to json`() {
        val rootNode = Node("fnr", FØDSELSNUMMER)
        val childNode1 = Node("orgnr1", ORGANISASJONSNUMMER)
        val childNode2 = Node("orgnr2", ORGANISASJONSNUMMER)
        val grandChildNode1 = Node("periode1", VEDTAKSPERIODE_ID)
        val grandChildNode2 = Node("periode2", VEDTAKSPERIODE_ID)

        rootNode parentOf childNode1
        rootNode parentOf childNode2
        childNode1 parentOf grandChildNode1
        childNode2 parentOf grandChildNode2

        val tree = Tree.buildTree(rootNode)
        val json = jacksonObjectMapper().readTree(tree.toJson())
        val expectedJson = jacksonObjectMapper().readTree(expectedJson)

        assertEquals(expectedJson, json)
    }

    @Test
    fun `path to - ett fnr`() {
        val rootNode = Node("fnr", FØDSELSNUMMER)
        val childNode1 = Node("orgnr1", ORGANISASJONSNUMMER)
        val childNode2 = Node("orgnr2", ORGANISASJONSNUMMER)
        val grandChildNode1 = Node("periode1", VEDTAKSPERIODE_ID)
        val grandChildNode2 = Node("periode2", VEDTAKSPERIODE_ID)

        rootNode parentOf childNode1
        rootNode parentOf childNode2
        childNode1 parentOf grandChildNode1
        childNode2 parentOf grandChildNode2

        val tree = Tree.buildTree(rootNode)
        val path1 = tree.pathTo("periode2")
        val path2 = tree.pathTo("periode1")
        val path3 = tree.pathTo("orgnr1")
        assertEquals(listOf("fnr", "orgnr2", "periode2"), path1)
        assertEquals(listOf("fnr", "orgnr1", "periode1"), path2)
        assertEquals(listOf("fnr", "orgnr1"), path3)
    }

    @Test
    fun `path to - to fnr`() {
        val fnrNode1 = Node("fnr1", FØDSELSNUMMER)
        val fnrNode2 = Node("fnr2", FØDSELSNUMMER)
        val orgnrNode1 = Node("orgnr1", ORGANISASJONSNUMMER)
        val orgnrNode2 = Node("orgnr2", ORGANISASJONSNUMMER)
        val periodeNode1 = Node("periode1", VEDTAKSPERIODE_ID)
        val periodeNode2 = Node("periode2", VEDTAKSPERIODE_ID)
        val periodeNode3 = Node("periode3", VEDTAKSPERIODE_ID)
        val periodeNode4 = Node("periode4", VEDTAKSPERIODE_ID)

        fnrNode1 parentOf orgnrNode1
        fnrNode1 parentOf orgnrNode2
        orgnrNode1 parentOf periodeNode1
        orgnrNode2 parentOf periodeNode2

        fnrNode2 parentOf orgnrNode1
        orgnrNode1 parentOf periodeNode3
        orgnrNode1 parentOf periodeNode4

        val tree1 = Tree.buildTree(fnrNode1)
        val path1 = tree1.pathTo("periode2")
        val path2 = tree1.pathTo("periode1")
        val path3 = tree1.pathTo("orgnr1")

        val tree2 = Tree.buildTree(fnrNode2)
        val path4 = tree2.pathTo("periode4")
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