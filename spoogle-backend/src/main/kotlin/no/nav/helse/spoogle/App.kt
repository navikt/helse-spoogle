package no.nav.helse.spoogle

import com.github.navikt.tbd_libs.rapids_and_rivers_api.RapidsConnection
import io.ktor.server.application.*
import io.ktor.server.auth.*
import io.ktor.server.http.content.*
import io.ktor.server.routing.*
import io.micrometer.core.instrument.Metrics
import io.micrometer.prometheusmetrics.PrometheusConfig
import io.micrometer.prometheusmetrics.PrometheusMeterRegistry
import no.nav.helse.rapids_rivers.RapidApplication
import no.nav.helse.spoogle.db.DataSourceBuilder
import no.nav.helse.spoogle.microsoft.AzureAD
import no.nav.helse.spoogle.plugins.configureAuthentication
import no.nav.helse.spoogle.plugins.configureServerContentNegotiation
import no.nav.helse.spoogle.plugins.configureUtilities
import no.nav.helse.spoogle.plugins.statusPages
import no.nav.helse.spoogle.river.BehandlingOpprettetRiver
import no.nav.helse.spoogle.river.InntektsmeldingHåndtertRiver
import no.nav.helse.spoogle.river.OppgaveEndretRiver
import no.nav.helse.spoogle.river.SøknadHåndtertRiver
import no.nav.helse.spoogle.river.UtbetalingForkastetRiver
import no.nav.helse.spoogle.river.VedtaksperiodeEndretRiver
import no.nav.helse.spoogle.river.VedtaksperiodeForkastetRiver
import no.nav.helse.spoogle.river.VedtaksperiodeNyUtbetalingRiver
import no.nav.helse.spoogle.routes.brukerRoutes
import no.nav.helse.spoogle.routes.treRoutes

fun main() {
    RapidApp(System.getenv()).start()
}

private class RapidApp(env: Map<String, String>) {
    private lateinit var rapidsConnection: RapidsConnection
    private val app = App(env) { rapidsConnection }

    init {
        rapidsConnection = RapidApplication.create(
            env = env,
            meterRegistry = PrometheusMeterRegistry(PrometheusConfig.DEFAULT).also(Metrics.globalRegistry::add),
            builder = {
                withKtorModule {
                    app.ktorApp(this)
                }
            })
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
        BehandlingOpprettetRiver(service, rapidsConnection)
        VedtaksperiodeNyUtbetalingRiver(service, rapidsConnection)
        UtbetalingForkastetRiver(service, rapidsConnection)
        SøknadHåndtertRiver(service, rapidsConnection)
        InntektsmeldingHåndtertRiver(service, rapidsConnection)
        OppgaveEndretRiver(service, rapidsConnection)
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
