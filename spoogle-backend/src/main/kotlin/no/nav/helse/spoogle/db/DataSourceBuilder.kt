package no.nav.helse.spoogle.db

import com.zaxxer.hikari.HikariConfig
import com.zaxxer.hikari.HikariDataSource
import io.micrometer.core.instrument.Clock
import io.micrometer.prometheus.PrometheusConfig
import io.micrometer.prometheus.PrometheusMeterRegistry
import io.prometheus.client.CollectorRegistry
import org.flywaydb.core.Flyway
import java.time.Duration
import javax.sql.DataSource

internal class DataSourceBuilder(env: Map<String, String>) {
    private val dbUrl = requireNotNull(env["DATABASE_JDBC_URL"]) { "JDBC url må være satt" }

    private val hikariConfig =
        HikariConfig().apply {
            jdbcUrl = dbUrl
            idleTimeout = Duration.ofMinutes(1).toMillis()
            maxLifetime = idleTimeout * 5
            initializationFailTimeout = Duration.ofMinutes(1).toMillis()
            connectionTimeout = Duration.ofSeconds(30).toMillis()
            minimumIdle = 1
            maximumPoolSize = 10
            metricRegistry =
                PrometheusMeterRegistry(
                    PrometheusConfig.DEFAULT,
                    CollectorRegistry.defaultRegistry,
                    Clock.SYSTEM,
                )
        }

    private val hikariMigrationConfig =
        HikariConfig().apply {
            jdbcUrl = dbUrl
            initializationFailTimeout = Duration.ofMinutes(1).toMillis()
            connectionTimeout = Duration.ofMinutes(1).toMillis()
            maximumPoolSize = 2
        }

    private fun runMigration(dataSource: DataSource) =
        Flyway.configure()
            .dataSource(dataSource)
            .lockRetryCount(-1)
            .load()
            .migrate()

    internal fun getDataSource(): HikariDataSource {
        return HikariDataSource(hikariConfig)
    }

    internal fun migrate() {
        HikariDataSource(hikariMigrationConfig).use { runMigration(it) }
    }
}
