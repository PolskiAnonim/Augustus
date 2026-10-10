package org.octavius.app

import io.github.octaviusframework.migrations.MigratorConfig
import io.github.octaviusframework.migrations.OctaviusMigrator
import javax.sql.DataSource

/** Schemat części wspólnej (desktop-app i report-engine). Nie należy do featura i migruje się pierwszy. */
const val PUBLIC_SCHEMA = "public"

/** Nazwa tabeli historii w każdym schemacie (domyślna nazwa migratora). */
private const val HISTORY_TABLE = "octavius_migration_history"

/**
 * Migruje bazę. Każdy schemat ma osobny ciąg migracji i osobną historię.
 *
 * Pliki leżą w zasobach modułów pod `db/migration/<schemat>/`, a historia w
 * `<schemat>.octavius_migration_history`. Najpierw idzie `public`, bo featury korzystają z jego funkcji
 * i typów (a starsze featury także ze schematów, które tworzy). Potem idą schematy featurów, w ich
 * kolejności.
 *
 * Osobne historie są po to, żeby feature nie musiał być wszędzie: może leżeć na gałęzi albo w katalogu,
 * którego nie ma na GitHubie. Baza, która wykonała jego migracje, dalej startuje z kodem bez niego, bo
 * jego historii nikt wtedy nie czyta. We wspólnej historii jego wiersze byłyby dla reszty migracjami bez
 * pliku i start by stanął, a jego wersje przeplatałyby się z wersjami reszty (out of order).
 *
 * @param featureSchemas Schematy featurów w kolejności featurów, bez `public`.
 */
fun migrateDatabase(dataSource: DataSource, featureSchemas: List<String>) {
    for (schema in listOf(PUBLIC_SCHEMA) + featureSchemas) {
        val report = OctaviusMigrator(dataSource, migratorConfig(schema)).migrate()
        println("Octavius migrations ($schema): $report")
    }
}

private fun migratorConfig(schema: String) = MigratorConfig(
    sqlLocations = listOf("db/migration/$schema"),
    placeholders = mapOf("databaseName" to "augustus"),
    historySchema = schema,
    historyTable = HISTORY_TABLE
)
