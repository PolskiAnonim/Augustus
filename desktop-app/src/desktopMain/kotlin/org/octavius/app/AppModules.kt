package org.octavius.app

import com.zaxxer.hikari.HikariConfig
import com.zaxxer.hikari.HikariDataSource
import io.github.octaviusframework.client.OctaviusClient
import io.github.octaviusframework.client.scanner.registerAnnotatedTypes
import io.github.octaviusframework.driver.exception.findOctaviusCause
import io.github.octaviusframework.migrations.OctaviusMigrator
import org.koin.dsl.module
import org.koin.dsl.onClose
import org.octavius.app.settings.AppSettingsManager
import javax.sql.DataSource

/** Schematy, w których żyje aplikacja - trafiają do `search_path` każdego połączenia. */
private val appSchemas = listOf("public", "asian_media", "games", "books")

/**
 * Dokleja `search_path` do URL-a, o ile użytkownik sam go nie podał.
 *
 * Sterownik traktuje nierozpoznane parametry URL-a jako parametry startowe połączenia, więc ustawiony
 * w ten sposób `search_path` jest częścią tożsamości połączenia - nie ma czego czyścić przy oddaniu
 * go do puli i nie ma jak wyciec do następnego pożyczającego.
 */
private fun String.withSearchPath(): String {
    if (contains("search_path=")) return this
    val separator = if (contains('?')) "&" else "?"
    return "$this${separator}search_path=${appSchemas.joinToString(",")}"
}

/**
 * Moduł Koin konfigurujący zależności związane z bazą danych.
 *
 * Kolejność jest tu istotna: migracje idą przed zbudowaniem klienta, bo to one tworzą typy
 * (enumy, kompozyty), a katalog typów sterownik czyta raz na bazę. `install()` domyka to
 * przeładowaniem katalogu, więc rejestracja typów po nim widzi już wszystko, co migracje utworzyły.
 */
val databaseModule = module {
    single<OctaviusClient> {
        val settings = get<AppSettingsManager>().currentSettings.database

        val dataSource: DataSource
        try {
            dataSource = HikariDataSource(HikariConfig().apply {
                jdbcUrl = settings.url.withSearchPath()
                username = settings.username
                password = settings.password
                poolName = "octavius-app"
            })
        } catch (e: Exception) {
            throw e.findOctaviusCause() ?: e
        }


        // Cokolwiek pójdzie nie tak przed oddaniem klienta, pula zostaje bez właściciela - a ekran
        // błędu bazy pozwala spróbować ponownie, więc nieodebrana pula zostawałaby przy każdej próbie.
        try {
            val report = OctaviusMigrator(dataSource).migrate()
            println("Octavius migrations: $report")

            OctaviusClient.fromDataSource(dataSource, ownsDataSource = true).apply {
                // Tworzy public.dynamic_dto wraz z konstruktorami i przeładowuje katalog typów.
                dynamicTypes.install()

                // Stara biblioteka wyszukiwała GlobalTypeHandler-y skanerem i sama mapowała interval na
                // Duration. Nowa nie robi ani jednego, ani drugiego, więc rejestrujemy je wprost.
                execute {
                    typeManager.registerParameterConverter(CleanStringParameterConverter)
                    typeManager.registerResultConverter(PgIntervalAsDurationConverter)
                    typeManager.registerParameterConverter(DurationAsPgIntervalConverter)
                }

                val scan = registerAnnotatedTypes("org.octavius")
                println("Octavius registered $scan")
                if (scan.unresolved.isNotEmpty()) {
                    println("Octavius: brak typu w bazie dla ${scan.unresolved}")
                }
            }
        } catch (e: Throwable) {
            dataSource.close()
            throw e
        }
    } onClose { it?.close() }
}
