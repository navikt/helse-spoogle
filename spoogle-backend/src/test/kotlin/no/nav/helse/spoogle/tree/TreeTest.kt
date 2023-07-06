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

    @Language("JSON")
    val expectedJson = """
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
                            "children": []
                        }
                    ]
                },
                {
                    "id": "orgnr2",
                    "type": "ORGANISASJONSNUMMER",
                    "children": [
                        {
                            "id": "periode2",
                            "type": "VEDTAKSPERIODE_ID",
                            "children": []
                        }
                    ]
                }
            ]
       } 
    """
}