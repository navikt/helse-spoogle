package no.nav.helse.opprydding

import com.zaxxer.hikari.HikariConfig
import com.zaxxer.hikari.HikariDataSource
import kotliquery.queryOf
import kotliquery.sessionOf
import org.flywaydb.core.Flyway
import org.intellij.lang.annotations.Language
import org.junit.jupiter.api.BeforeEach
import org.testcontainers.containers.PostgreSQLContainer
import javax.sql.DataSource

internal abstract class AbstractDatabaseTest {
    protected val personDao = PersonDao(dataSource)

    protected companion object {
        private val postgres =
            PostgreSQLContainer<Nothing>("postgres:15.5").apply {
                withReuse(false)
                withLabel("app-navn", "spoogle-opprydding")
                start()

                println("Database: jdbc:postgresql://localhost:$firstMappedPort/test startet opp, credentials: test og test")
            }

        val dataSource =
            HikariDataSource(
                HikariConfig().apply {
                    jdbcUrl = postgres.jdbcUrl
                    username = postgres.username
                    password = postgres.password
                    maximumPoolSize = 5
                    minimumIdle = 1
                    idleTimeout = 500001
                    connectionTimeout = 10000
                    maxLifetime = 600001
                    initializationFailTimeout = 5000
                },
            )

        private fun createTruncateFunction(dataSource: DataSource) {
            sessionOf(dataSource).use {
                @Language("PostgreSQL")
                val query = """
            CREATE OR REPLACE FUNCTION truncate_tables() RETURNS void AS $$
            DECLARE
            truncate_statement text;
            BEGIN
                SELECT 'TRUNCATE ' || string_agg(format('%I.%I', schemaname, tablename), ',') || ' RESTART IDENTITY CASCADE'
                    INTO truncate_statement
                FROM pg_tables
                WHERE schemaname='public'
                AND tablename not in ('flyway_schema_history');

                EXECUTE truncate_statement;
            END;
            $$ LANGUAGE plpgsql;
        """
                it.run(queryOf(query).asExecute)
            }
        }

        init {
            Flyway.configure()
                .dataSource(dataSource)
                .locations("classpath:db/migration")
                .load()
                .migrate()

            createTruncateFunction(dataSource)
        }
    }

    protected fun opprettTre(
        fødselsnummer: String,
        organisasjonsnummer: String,
    ) {
        Flyway
            .configure()
            .dataSource(dataSource)
            .placeholders(
                mapOf(
                    "fødselsnummer" to fødselsnummer,
                    "organisasjonsnummer" to organisasjonsnummer,
                ),
            )
            .locations("classpath:db/testperson")
            .load()
            .migrate()
    }

    @BeforeEach
    fun resetDatabase() {
        sessionOf(dataSource).use {
            it.run(queryOf("SELECT truncate_tables()").asExecute)
        }
    }
}
