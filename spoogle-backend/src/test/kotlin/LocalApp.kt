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
import java.util.*

internal class LocalApp: AbstractDatabaseTest(doTruncate = false) {
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

        app.start()
        server.start(wait = true)
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