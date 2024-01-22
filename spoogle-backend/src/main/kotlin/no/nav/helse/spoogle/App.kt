package no.nav.helse.spoogle

import io.ktor.http.CacheControl
import io.ktor.http.ContentType
import io.ktor.server.application.Application
import io.ktor.server.application.ApplicationCall
import io.ktor.server.application.call
import io.ktor.server.auth.authenticate
import io.ktor.server.http.content.ignoreFiles
import io.ktor.server.http.content.react
import io.ktor.server.http.content.singlePageApplication
import io.ktor.server.response.cacheControl
import io.ktor.server.response.respondBytesWriter
import io.ktor.server.routing.get
import io.ktor.server.routing.routing
import io.ktor.server.websocket.webSocket
import io.ktor.utils.io.writeStringUtf8
import io.ktor.websocket.Frame
import kotlinx.coroutines.DelicateCoroutinesApi
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.shareIn
import no.nav.helse.rapids_rivers.RapidApplication
import no.nav.helse.rapids_rivers.RapidApplication.Builder
import no.nav.helse.rapids_rivers.RapidsConnection
import no.nav.helse.spoogle.db.DataSourceBuilder
import no.nav.helse.spoogle.microsoft.AzureAD
import no.nav.helse.spoogle.plugins.configureAuthentication
import no.nav.helse.spoogle.plugins.configureServerContentNegotiation
import no.nav.helse.spoogle.plugins.configureUtilities
import no.nav.helse.spoogle.plugins.statusPages
import no.nav.helse.spoogle.river.*
import no.nav.helse.spoogle.routes.brukerRoutes
import no.nav.helse.spoogle.routes.treRoutes
import org.slf4j.LoggerFactory
import kotlin.time.Duration.Companion.seconds

fun main() {
    RapidApp(System.getenv()).start()
}

private class RapidApp(env: Map<String, String>) {
    private lateinit var rapidsConnection: RapidsConnection
    private val app = App(env) { rapidsConnection }

    init {
        rapidsConnection = Builder(RapidApplication.RapidApplicationConfig.fromEnv(env))
            .withKtorModule {
                app.ktorApp(this)
            }.build()
    }

    fun start() = app.start()
}


internal class App(
    private val env: Map<String, String>,
    rapidsConnection: () -> RapidsConnection,
) : RapidsConnection.StatusListener {
    private val rapidsConnection: RapidsConnection by lazy { rapidsConnection() }
    private val dataSourceBuilder = DataSourceBuilder(env)
    private val service = TreService(dataSourceBuilder.getDataSource())
    private val azureAD = AzureAD.fromEnv(env)

    internal fun ktorApp(application: Application) = application.app(env, service, azureAD)
    internal fun start() {
        VedtaksperiodeEndretRiver(service, rapidsConnection)
        VedtaksperiodeForkastetRiver(service, rapidsConnection)
        VedtaksperiodeNyUtbetalingRiver(service, rapidsConnection)
        UtbetalingForkastetRiver(service, rapidsConnection)
        SøknadHåndtertRiver(service, rapidsConnection)
        InntektsmeldingHåndtertRiver(service, rapidsConnection)
        rapidsConnection.register(this)
        rapidsConnection.start()
    }

    override fun onStartup(rapidsConnection: RapidsConnection) {
        dataSourceBuilder.migrate()
    }
}

private val logg = LoggerFactory.getLogger(App::class.java)

@OptIn(DelicateCoroutinesApi::class)
internal fun Application.app(
    env: Map<String, String>,
    service: ITreeService,
    azureAD: AzureAD,
) {
    val isLocalDevelopment = env["LOCAL_DEVELOPMENT"]?.toBooleanStrict() ?: false
    statusPages()
    configureUtilities()
    configureServerContentNegotiation()
    configureAuthentication(azureAD)

    val sseFlow = flow {
        var n = 0
        while (true) {
            emit(SseEvent("demo$n"))
            delay(1.seconds)
            n++
        }
    }.shareIn(GlobalScope, SharingStarted.Eagerly)

    routing {
        authenticate("ValidToken") {
            singlePageApplication {
                useResources = !isLocalDevelopment
                react("spoogle-frontend/dist")
                ignoreFiles { it.endsWith(".txt") }
            }
            treRoutes(service)
            brukerRoutes(azureAD.issuer())
            webSocket("/echo") {
                while (true) {
                    logg.debug("Forsøker å sende frame over websocket")
                    send(Frame.Text("Websocket ping"))
                    delay(1000L)
                }
            }
            get("/sse") {
                call.respondSse(sseFlow)
            }
        }
    }
}

data class SseEvent(val data: String, val event: String? = null, val id: String? = null)

suspend fun ApplicationCall.respondSse(eventFlow: Flow<SseEvent>) {
    response.cacheControl(CacheControl.NoCache(null))
    respondBytesWriter(contentType = ContentType.Text.EventStream) {
        eventFlow.collect { event ->
            if (event.id != null) writeStringUtf8("id: ${event.id}\n")
            if (event.event != null) writeStringUtf8("event: ${event.event}\n")
            for (line in event.data.lines()) writeStringUtf8("data: ${line}\n")
            writeStringUtf8("\n")
            flush()
        }
    }
}
