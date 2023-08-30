import io.ktor.server.engine.applicationEngineEnvironment
import io.ktor.server.engine.connector
import io.ktor.server.engine.embeddedServer
import io.ktor.server.netty.Netty
import no.nav.helse.rapids_rivers.testsupport.TestRapid
import no.nav.helse.spoogle.App
import no.nav.helse.spoogle.db.AbstractDatabaseTest
import no.nav.security.mock.oauth2.MockOAuth2Server
import no.nav.security.mock.oauth2.token.DefaultOAuth2TokenCallback
import org.intellij.lang.annotations.Language
import java.io.File

internal class LocalApp: AbstractDatabaseTest(doTruncate = false) {
    private companion object {
        private const val fødselsnummer1 = "12345678910"
        private const val fødselsnummer2 = "99999999999"
        private const val aktørId1 = "1234567891011"
        private const val aktørId2 = "9999999999999"
        private const val organisasjonsnummer1 = "987654321"
        private const val organisasjonsnummer2 = "123456789"
        private val vedtaksperiodeId1 = "f2bead04-ea07-4441-8c39-d72cb10a0f22"
        private val vedtaksperiodeId2 = "cd54733a-9c24-4daf-b317-5a3cb4783494"
        private val vedtaksperiodeId3 = "c78ce56d-2e9c-44e8-a8e8-bf3f4f00d0e9"
        private val vedtaksperiodeId4 = "6e0b087e-9c57-4b0a-86b6-1b6beb753cc3"
        private val utbetalingId1 = "a26a03df-ef7d-4a76-aa2c-fb97e3655747"
    }
    private val oauthMock = OauthMock
    private val environmentVariables: Map<String, String> = mutableMapOf(
        "DATABASE_HOST" to hostAddress,
        "DATABASE_PORT" to port,
        "DATABASE_DATABASE" to database,
        "DATABASE_USERNAME" to "test",
        "DATABASE_PASSWORD" to "test",
        "LOCAL_DEVELOPMENT" to "true",
    ).apply {
        putAll(oauthMock.oauthConfig)
    }.toMap()

    private val testRapid = TestRapid()

    private val app: App by lazy { App(environmentVariables) { testRapid } }

    internal fun start() {
        val server = embeddedServer(Netty, applicationEngineEnvironment {
            module {
                app.ktorApp(this)
            }
            connector {
                port = 8080
            }
        })

        app.start()
        server.start(wait = false)
        opprettDummyData()
    }

    private fun opprettDummyData() {
        testRapid.sendTestMessage(vedtaksperiodeEndret(fødselsnummer1, aktørId1, organisasjonsnummer1, vedtaksperiodeId1))
        testRapid.sendTestMessage(vedtaksperiodeEndret(fødselsnummer1, aktørId1, organisasjonsnummer1, vedtaksperiodeId2))
        testRapid.sendTestMessage(vedtaksperiodeEndret(fødselsnummer1, aktørId1, organisasjonsnummer2, vedtaksperiodeId3))
        testRapid.sendTestMessage(vedtaksperiodeEndret(fødselsnummer2, aktørId2, organisasjonsnummer2, vedtaksperiodeId4))
        testRapid.sendTestMessage(vedtaksperiodeNyUtbetaling(utbetalingId1, vedtaksperiodeId1))
    }

    @Language("JSON")
    private fun vedtaksperiodeEndret(
        fødselsnummer: String,
        aktørId: String,
        organisasjonsnummer: String,
        vedtaksperiodeId: String
    ) = """{
    "@event_name": "vedtaksperiode_endret",
    "organisasjonsnummer": "$organisasjonsnummer",
    "vedtaksperiodeId": "$vedtaksperiodeId",
    "gjeldendeTilstand": "START",
    "forrigeTilstand": "AVVENTER_INNTEKTSMELDING",
    "hendelser": [
        "c9214688-b47a-448b-bbc6-d4cb51dc0380"
    ],
    "makstid": "2018-01-01T00:00:00.000",
    "fom": "2018-01-01",
    "tom": "2018-01-31",
    "@id": "4c443e35-e993-49d3-a5c1-e230fa32f5e0",
    "@opprettet": "2018-01-01T00:00:00.000",
    "aktørId": "$aktørId",
    "fødselsnummer": "$fødselsnummer"
}
    """

    @Language("JSON")
    private fun vedtaksperiodeNyUtbetaling(
        utbetalingId: String,
        vedtaksperiodeId: String
    ) = """{
    "@event_name": "vedtaksperiode_ny_utbetaling",
    "vedtaksperiodeId": "$vedtaksperiodeId",
    "utbetalingId": "$utbetalingId",
    "@id": "4c443e35-e993-49d3-a5c1-e230fa32f5e0",
    "@opprettet": "2018-01-01T00:00:00.000"
}
    """
}

internal fun main() {
    val tokenFile = File("testtoken.json")

    @Language("JSON")
    val json = """ { "token": "${OauthMock.accessToken()}" } 
    """
    tokenFile.writeText(json)

    LocalApp().start()
}

private object OauthMock {
    private const val acceptedGroupId = "00000000-0000-0000-0000-000000000000"
    private const val issuerId = "00000000-0000-0000-0000-000000000000"
    private const val clientId = "00000000-0000-0000-0000-000000000000"
    private const val oid = "00000000-0000-0000-0000-000000000000"
    private val server = MockOAuth2Server().also {
        it.start(0)
    }
    val oauthConfig = mapOf(
        "AZURE_APP_WELL_KNOWN_URL" to server.wellKnownUrl(issuerId).toString(),
        "AZURE_APP_CLIENT_ID" to clientId,
        "AZURE_VALID_GROUP_ID" to acceptedGroupId,
        "AZURE_APP_JWK" to "some_jwk"
    )

    fun accessToken(
        harNavIdent: Boolean = true,
        grupper: List<String> = listOf(acceptedGroupId),
        andreClaims: Map<String, String> = emptyMap()
    ): String {
        val claims: Map<String, Any> = mutableMapOf<String, Any>(
            "groups" to grupper
        ).apply {
            if (harNavIdent) putAll(
                mapOf(
                    "NAVident" to "X999999",
                    "preferred_username" to "saksbehandler@nav.no",
                    "name" to "En Saksbehandler",
                    "oid" to oid
                )
            )
            putAll(andreClaims)
        }
        return server.issueToken(
            issuerId = issuerId,
            clientId = clientId,
            tokenCallback = DefaultOAuth2TokenCallback(
                issuerId = issuerId,
                audience = listOf(clientId),
                claims = claims,
                expiry = 604800 // en uke
            )
        ).serialize()
    }
}