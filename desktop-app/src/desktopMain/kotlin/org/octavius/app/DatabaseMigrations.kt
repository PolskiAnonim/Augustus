package org.octavius.app

import io.github.octaviusframework.driver.identifier.quoteAsPgIdentifier
import io.github.octaviusframework.driver.jdbc.getOctaviusSession
import io.github.octaviusframework.migrations.MigratorConfig
import io.github.octaviusframework.migrations.OctaviusMigrator
import io.github.octaviusframework.migrations.history.MigrationType
import javax.sql.DataSource

/** Schemat części wspólnej (desktop-app i report-engine). Nie należy do featura i migruje się pierwszy. */
const val PUBLIC_SCHEMA = "public"

/** Nazwa tabeli historii w każdym schemacie. Domyślna nazwa migratora, pod którą stała dawna wspólna historia. */
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
    splitSharedHistory(dataSource, featureSchemas)

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

private fun historyOf(schema: String) = "${schema.quoteAsPgIdentifier()}.${HISTORY_TABLE.quoteAsPgIdentifier()}"

/**
 * Jednorazowe przejście ze wspólnej historii na historie per schemat.
 *
 * Do października 2026 wszystkie moduły miały jeden ciąg migracji z historią w
 * `public.octavius_migration_history`. Ta tabela zostaje historią samego `public`. Wiersze featurów
 * przechodzą do historii ich schematów razem z sumami kontrolnymi, więc dalej pilnują, że wykonanej
 * migracji nikt nie zmienił. Wiersz BASELINE, jeśli jest, trafia do każdej nowej historii, bo wersja,
 * na której bazę przejęto, dotyczyła całego wspólnego ciągu.
 *
 * Bazę sprzed podziału poznaje się po wierszach w historii `public`, których `public` nie zna. Na świeżej
 * bazie i na bazie już podzielonej nic się tu nie dzieje. Podział idzie w jednej transakcji, więc baza
 * jest albo cała przed nim, albo cała po nim. Wiersz, którego nie przejął żaden feature, zostaje, a
 * migrator `public` zatrzyma start i nazwie go migracją bez pliku.
 *
 * Do usunięcia, kiedy żadna baza nie ma już wspólnej historii.
 */
private fun splitSharedHistory(dataSource: DataSource, featureSchemas: List<String>) {
    // `info()` niczego nie tworzy i niczego nie odrzuca. Wiersz bez pliku ma `origin == null`.
    val foreign = OctaviusMigrator(dataSource, migratorConfig(PUBLIC_SCHEMA)).info()
        .filter { it.origin == null }
        .mapNotNull { it.applied }
    if (foreign.isEmpty()) return

    // Liczone przed transakcją: `info()` bierze z puli własne połączenie.
    val versionsBySchema = featureSchemas.associateWith { schema ->
        OctaviusMigrator(dataSource, migratorConfig(schema)).info()
            .filter { it.origin != null }
            .mapNotNull { it.version }
            .toSet()
    }.filterValues { it.isNotEmpty() }

    val shared = historyOf(PUBLIC_SCHEMA)
    dataSource.getOctaviusSession().use { session ->
        session.autoCommit = false
        try {
            for ((schema, versions) in versionsBySchema) {
                // Wersje porównuje się tak jak migrator: po częściach, nie po napisie. Do SQL-a idzie
                // napis z historii, więc trafia dokładnie w te wiersze.
                val moved = foreign.mapNotNull { row -> row.version?.takeIf { it in versions }?.canonical }
                    .toTypedArray()
                val history = historyOf(schema)

                session.createNativeQuery("CREATE SCHEMA IF NOT EXISTS ${schema.quoteAsPgIdentifier()}").execute()
                // Kolumny, wartości domyślne i identity jak w public. Unikalne indeksy migrator dołoży sam
                // (CREATE INDEX IF NOT EXISTS), nazwane tak jak w historii założonej od zera.
                session.createNativeQuery("CREATE TABLE $history (LIKE $shared INCLUDING DEFAULTS INCLUDING IDENTITY)")
                    .execute()
                session.createNativeQuery("ALTER TABLE $history ADD PRIMARY KEY (id)").execute()
                session.createNativeQuery(
                    """
                    INSERT INTO $history (version, description, type, script, checksum, state, failed_statement,
                                          execution_time_ms, installed_by, installed_on)
                    SELECT version, description, type, script, checksum, state, failed_statement,
                           execution_time_ms, installed_by, installed_on
                    FROM $shared
                    WHERE type = $1 OR version = ANY($2)
                    ORDER BY id
                    """.trimIndent()
                ).update(MigrationType.BASELINE.name, moved)
                session.createNativeQuery("DELETE FROM $shared WHERE type <> $1 AND version = ANY($2)")
                    .update(MigrationType.BASELINE.name, moved)

                println("Octavius: historia $schema wydzielona z $shared, przeniesione wiersze: ${moved.size}")
            }
            session.commit()
        } catch (e: Throwable) {
            session.rollback()
            throw e
        }
    }
}
