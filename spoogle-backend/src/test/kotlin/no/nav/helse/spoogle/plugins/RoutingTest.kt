package no.nav.helse.spoogle.plugins

import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.plugins.cookies.HttpCookies
import io.ktor.client.request.get
import io.ktor.client.request.header
import io.ktor.http.HttpStatusCode
import io.ktor.server.testing.ApplicationTestBuilder
import io.ktor.server.testing.testApplication
import no.nav.helse.spoogle.ITreeService
import no.nav.helse.spoogle.app
import no.nav.helse.spoogle.microsoft.AzureAD
import no.nav.helse.spoogle.tree.Node
import no.nav.helse.spoogle.tree.Tree
import no.nav.security.mock.oauth2.MockOAuth2Server
import no.nav.security.mock.oauth2.http.objectMapper
import no.nav.security.mock.oauth2.token.DefaultOAuth2TokenCallback
import org.intellij.lang.annotations.Language
import org.junit.jupiter.api.AfterAll
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.BeforeAll
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import java.util.*

internal class RoutingTest {

    private lateinit var client: HttpClient

    @BeforeEach
    fun beforeEach() {
        client = HttpClient {
            install(HttpCookies)
            configureClientContentNegotiation()
        }
    }

    @Test
    fun `finn tre med gyldig token`() = withTestApplication {
        val response = client.get("/api/sok/$vedtaksperiodeId") {
            header("Authorization", "Bearer ${accessToken()}")
        }
        assertEquals(HttpStatusCode.OK, response.status)
        val body = response.body<String>()
        val expected = objectMapper.readTree(expectedJson)
        val actual = objectMapper.readTree(body)
        assertEquals(expected, actual)
    }

    @Test
    fun `finn tre med gyldig token med flere grupperider`() = withTestApplication {
        val response = client.get("/api/sok/$vedtaksperiodeId") {
            header(
                "Authorization",
                "Bearer ${accessToken(grupper = listOf(groupId.toString(), "${UUID.randomUUID()}"))}"
            )
        }
        assertEquals(HttpStatusCode.OK, response.status)
        val body = response.body<String>()
        val expected = objectMapper.readTree(expectedJson)
        val actual = objectMapper.readTree(body)
        assertEquals(expected, actual)
    }

    @Test
    fun `finn tre med gyldig token med flere scopes`() = withTestApplication {
        val response = client.get("/api/sok/$vedtaksperiodeId") {
            header("Authorization", "Bearer ${accessToken()}")
        }
        assertEquals(HttpStatusCode.OK, response.status)
        val body = response.body<String>()
        val expected = objectMapper.readTree(expectedJson)
        val actual = objectMapper.readTree(body)
        assertEquals(expected, actual)
    }

    @Test
    fun `finn tre med gyldig token med andre claims i tillegg`() = withTestApplication {
        val response = client.get("/api/sok/$vedtaksperiodeId") {
            header("Authorization", "Bearer ${accessToken(andreClaims = mapOf("Some other claim" to "some value"))}")
        }
        assertEquals(HttpStatusCode.OK, response.status)
        val body = response.body<String>()
        val expected = objectMapper.readTree(expectedJson)
        val actual = objectMapper.readTree(body)
        assertEquals(expected, actual)
    }

    @Test
    fun `finn tre uten gyldige groups`() = withTestApplication {
        val response = client.get("/api/sok/$vedtaksperiodeId") {
            header("Authorization", "Bearer ${accessToken(grupper = listOf("${UUID.randomUUID()}"))}")
        }
        assertEquals(HttpStatusCode.Unauthorized, response.status)
    }

    @Test
    fun `finn tre uten NAVident`() = withTestApplication {
        val response = client.get("/api/sok/$vedtaksperiodeId") {
            header("Authorization", "Bearer ${accessToken(harNavIdent = false)}")
        }
        assertEquals(HttpStatusCode.Unauthorized, response.status)
    }

    private fun accessToken(
        harNavIdent: Boolean = true,
        grupper: List<String> = listOf("$groupId"),
        andreClaims: Map<String, String> = emptyMap(),
        oid: UUID = UUID.randomUUID()
    ): String {
        val claims: Map<String, Any> = mutableMapOf<String, Any>(
            "groups" to grupper
        ).apply {
            if (harNavIdent) putAll(
                mapOf(
                    "NAVident" to "EN_IDENT",
                    "preferred_username" to "some_username",
                    "name" to "some name",
                    "oid" to "$oid"
                )
            )
            putAll(andreClaims)
        }
        return oauthMock.issueToken(
            issuerId = issuerId.toString(),
            clientId = clientId.toString(),
            tokenCallback = DefaultOAuth2TokenCallback(
                issuerId = issuerId.toString(),
                audience = listOf(clientId.toString()),
                claims = claims,
            )
        ).serialize()
    }

    private fun withTestApplication(block: suspend ApplicationTestBuilder.() -> Unit) {
        testApplication {
            environment {
                module {
                    app(env, repository(), AzureAD.fromEnv(env))
                }
            }
            block(this)
        }
    }

    @Language("JSON")
    private val expectedJson = """{
    "path": [
        "123456791011",
        "987654321",
        "$vedtaksperiodeId"
    ],
    "tree": {
        "id": "123456791011",
        "type": "FØDSELSNUMMER",
        "children": [
            {
                "id": "987654321",
                "type": "ORGANISASJONSNUMMER",
                "children": [
                    {
                        "id": "$vedtaksperiodeId",
                        "type": "VEDTAKSPERIODE_ID",
                        "children": [
                            {
                                "id": "$utbetalingId",
                                "type": "UTBETALING_ID",
                                "children": [],
                                "ugyldig_fra": null
                            }
                        ],
                        "ugyldig_fra": null
                    }
                ],
                "ugyldig_fra": null
            }
        ],
        "ugyldig_fra": null
    }
}
    """

    private companion object {
        private val oauthMock = MockOAuth2Server().also {
            it.start()
        }
        private val issuerId = UUID.randomUUID()
        private val groupId = UUID.randomUUID()
        private val clientId = UUID.randomUUID()

        private val vedtaksperiodeId = UUID.randomUUID()
        private val utbetalingId = UUID.randomUUID()

        private val env = mapOf(
            "AZURE_APP_WELL_KNOWN_URL" to oauthMock.wellKnownUrl(issuerId.toString()).toString(),
            "AZURE_APP_CLIENT_ID" to "$clientId",
            "LOCAL_DEVELOPMENT" to "true",
            "AZURE_VALID_GROUP_ID" to "$groupId",
            "AZURE_APP_JWK" to "some_jwk"
        )

        private fun repository() = object : ITreeService {

            private val tree = let {
                val fnrNode = fnrNode("123456791011")
                val orgnrNode = orgnrNode("987654321", "123456791011")
                val periodeNode = periodeNode("$vedtaksperiodeId")
                val utbetalingNode = utbetalingNode("$utbetalingId")

                fnrNode forelderAv orgnrNode
                orgnrNode forelderAv periodeNode
                periodeNode forelderAv utbetalingNode
                Tree.buildTree(fnrNode)
            }

            override fun finnTre(id: String): Tree = tree

            private fun fnrNode(fnr: String) = Node.fødselsnummer(fnr)
            private fun orgnrNode(orgnr: String, fnr: String) = Node.organisasjonsnummer(orgnr, fnr)
            private fun periodeNode(id: String) = Node.vedtaksperiodeId(id)
            private fun utbetalingNode(id: String) = Node.utbetalingId(id)
        }

        @BeforeAll
        @JvmStatic
        fun setupMock() {
        }

        @AfterAll
        @JvmStatic
        fun shutdownMock() {
            oauthMock.shutdown()
        }
    }
}
