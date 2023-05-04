package no.nav.helse.spoogle

import no.nav.helse.rapids_rivers.RapidApplication
import no.nav.helse.rapids_rivers.RapidApplication.Builder

fun main() {
    val env = System.getenv()
    val app = Builder(RapidApplication.RapidApplicationConfig.fromEnv(env)).withKtorModule {}.build()
    app.start()
}