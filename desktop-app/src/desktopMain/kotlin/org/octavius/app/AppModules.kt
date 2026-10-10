package org.octavius.app

import com.zaxxer.hikari.HikariConfig
import com.zaxxer.hikari.HikariDataSource
import io.github.octaviusframework.client.OctaviusClient
import io.github.octaviusframework.client.scanner.registerAnnotatedTypes
import io.github.octaviusframework.driver.exception.findOctaviusCause
import io.github.octaviusframework.driver.jdbc.OctaviusDataSource
import org.koin.dsl.module
import org.koin.dsl.onClose
import org.octavius.app.settings.AppSettingsManager
import javax.sql.DataSource

/**
 * Moduł Koin konfigurujący zależności związane z bazą danych.
 *
 * Kolejność jest tu istotna: migracje idą przed zbudowaniem klienta, bo to one tworzą typy
 * (enumy, kompozyty), a katalog typów sterownik czyta raz na bazę. `install()` domyka to
 * przeładowaniem katalogu, więc rejestracja typów po nim widzi już wszystko, co migracje utworzyły.
 *
 * @param featureSchemas Schematy znalezionych featurów, w ich kolejności. Razem z `public` trafiają do
 * `search_path` każdego połączenia i każdy z nich migruje się osobno (patrz [migrateDatabase]).
 */
fun databaseModule(featureSchemas: List<String>) = module {
    single<OctaviusClient> {
        val settings = get<AppSettingsManager>().currentSettings.database

        val dataSource: DataSource
        try {
            val octavius = OctaviusDataSource().apply {
                url = settings.url
                user = settings.username
                password = settings.password
                logParameterValues = true
                setProperty("search_path", (listOf(PUBLIC_SCHEMA) + featureSchemas).joinToString(","))
            }
            dataSource = HikariDataSource(HikariConfig().apply {
                this.dataSource = octavius
                poolName = "octavius-app"
            })
        } catch (e: Exception) {
            throw e.findOctaviusCause() ?: e
        }


        // Cokolwiek pójdzie nie tak przed oddaniem klienta, pula zostaje bez właściciela - a ekran
        // błędu bazy pozwala spróbować ponownie, więc nieodebrana pula zostawałaby przy każdej próbie.
        try {
            migrateDatabase(dataSource, featureSchemas)

            OctaviusClient.fromDataSource(dataSource, ownsDataSource = true).apply {
                // Tworzy public.dynamic_dto wraz z konstruktorami i przeładowuje katalog typów.
                dynamicTypes.install()

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
