package no.nav.helse.spoogle

import io.ktor.server.application.Application
import io.ktor.server.auth.authenticate
import io.ktor.server.http.content.ignoreFiles
import io.ktor.server.http.content.react
import io.ktor.server.http.content.singlePageApplication
import io.ktor.server.routing.routing
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

fun main() {
    RapidApp(System.getenv()).start()
}

private class RapidApp(env: Map<String, String>) {
    private lateinit var rapidsConnection: RapidsConnection
    private val app = App(env) { rapidsConnection }

    init {
        rapidsConnection =
            Builder(RapidApplication.RapidApplicationConfig.fromEnv(env))
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

internal fun Application.app(
    env: Map<String, String>,
    service: ITreeService,
    azureAD: AzureAD,
) {
    val isLocalDevelopment = env["LOCAL_DEVELOPMENT"]?.toBooleanStrict() ?: false
    configureUtilities()
    configureServerContentNegotiation()
    statusPages()
    configureAuthentication(azureAD)

    routing {
        authenticate("ValidToken") {
            singlePageApplication {
                useResources = !isLocalDevelopment
                react("spoogle-frontend/dist")
                ignoreFiles { it.endsWith(".txt") }
            }
            treRoutes(service, azureAD.issuer())
            brukerRoutes(azureAD.issuer())
        }
    }
}
