package no.nav.helse.spoogle

import io.ktor.server.application.Application
import no.nav.helse.rapids_rivers.RapidApplication
import no.nav.helse.rapids_rivers.RapidApplication.Builder
import no.nav.helse.rapids_rivers.RapidsConnection
import no.nav.helse.spoogle.db.DataSourceBuilder
import no.nav.helse.spoogle.river.VedtaksperiodeEndretRiver

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
    private val service = TreeService(dataSourceBuilder.getDataSource())

    internal fun ktorApp(application: Application) = Unit
    internal fun start() {
        VedtaksperiodeEndretRiver(service, rapidsConnection)
        rapidsConnection.register(this)
        rapidsConnection.start()
    }

    override fun onStartup(rapidsConnection: RapidsConnection) {
        dataSourceBuilder.migrate()
    }
}
