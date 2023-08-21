import io.ktor.server.engine.applicationEngineEnvironment
import io.ktor.server.engine.connector
import io.ktor.server.engine.embeddedServer
import io.ktor.server.netty.Netty
import kotliquery.queryOf
import kotliquery.sessionOf
import no.nav.helse.rapids_rivers.testsupport.TestRapid
import no.nav.helse.spoogle.App
import no.nav.helse.spoogle.db.AbstractDatabaseTest
import no.nav.security.mock.oauth2.MockOAuth2Server
import no.nav.security.mock.oauth2.token.DefaultOAuth2TokenCallback
import org.intellij.lang.annotations.Language
import java.io.File
import javax.sql.DataSource

internal class LocalApp: AbstractDatabaseTest(doTruncate = false) {
    private companion object {
        private const val fødselsnummer = "12345678910"
        private const val organisasjonsnummer1 = "987654321"
        private const val organisasjonsnummer2 = "123456789"
        private val vedtaksperiodeId1 = "f2bead04-ea07-4441-8c39-d72cb10a0f22"
        private val vedtaksperiodeId2 = "cd54733a-9c24-4daf-b317-5a3cb4783494"
        private val vedtaksperiodeId3 = "c78ce56d-2e9c-44e8-a8e8-bf3f4f00d0e9"
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

    private val app: App by lazy { App(environmentVariables) { TestRapid() } }

    internal fun start() {
        val server = embeddedServer(Netty, applicationEngineEnvironment {
            module {
                app.ktorApp(this)
            }
            connector {
                port = 8080
            }
        })
        opprettDummyData(dataSource)

        app.start()
        server.start(wait = true)
    }

    private fun opprettDummyData(dataSource: DataSource) {
        @Language("PostgreSQL")
        val query = """
            INSERT INTO node(node_id, id, id_type) VALUES (1, :fodselsnummer, 'FØDSELSNUMMER');
            INSERT INTO node(node_id, id, id_type) VALUES (2, :organisasjonsnummer1, 'ORGANISASJONSNUMMER');
            INSERT INTO node(node_id, id, id_type) VALUES (3, :organisasjonsnummer2, 'ORGANISASJONSNUMMER');
            INSERT INTO node(node_id, id, id_type) VALUES (4, :vedtaksperiodeId1, 'VEDTAKSPERIODE_ID');
            INSERT INTO node(node_id, id, id_type) VALUES (5, :vedtaksperiodeId2, 'VEDTAKSPERIODE_ID');
            INSERT INTO node(node_id, id, id_type) VALUES (6, :vedtaksperiodeId3, 'VEDTAKSPERIODE_ID');
            INSERT INTO node(node_id, id, id_type) VALUES (7, :utbetalingId1, 'UTBETALING_ID');
            
            INSERT INTO edge(node_a, node_b, ugyldig) VALUES (1, 2, null);
            INSERT INTO edge(node_a, node_b, ugyldig) VALUES (1, 3, null);
            INSERT INTO edge(node_a, node_b, ugyldig) VALUES (2, 4, null);
            INSERT INTO edge(node_a, node_b, ugyldig) VALUES (2, 5, null);
            INSERT INTO edge(node_a, node_b, ugyldig) VALUES (3, 6, null);
            INSERT INTO edge(node_a, node_b, ugyldig) VALUES (4, 7, null);
            """

        sessionOf(dataSource).use { session ->
            session.run(
                queryOf(
                    query,
                    mapOf(
                        "fodselsnummer" to fødselsnummer,
                        "organisasjonsnummer1" to organisasjonsnummer1,
                        "organisasjonsnummer2" to organisasjonsnummer2,
                        "vedtaksperiodeId1" to vedtaksperiodeId1,
                        "vedtaksperiodeId2" to vedtaksperiodeId2,
                        "vedtaksperiodeId3" to vedtaksperiodeId3,
                        "utbetalingId1" to utbetalingId1,
                    )
                ).asUpdate
            )
        }
    }
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
    internal val oauthConfig = mapOf(
        "AZURE_APP_WELL_KNOWN_URL" to server.wellKnownUrl(issuerId).toString(),
        "AZURE_APP_CLIENT_ID" to clientId,
        "AZURE_VALID_GROUP_ID" to acceptedGroupId,
        "AZURE_APP_JWK" to "some_jwk"
    )

    internal fun accessToken(
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