package org.octavius.form.control.type.selection

import io.github.octaviusframework.client.DataResult
import io.github.octaviusframework.client.OctaviusClient
import io.github.octaviusframework.client.query.QueryFragment
import io.github.octaviusframework.client.query.withParam
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.koin.core.component.KoinComponent
import org.koin.core.component.inject
import org.octavius.dialog.ErrorDialogConfig
import org.octavius.dialog.GlobalDialogManager
import org.octavius.form.control.base.ControlAction
import org.octavius.form.control.base.ControlDependency
import org.octavius.form.control.type.selection.dropdown.AsyncPaginatedDropdownControl
import org.octavius.form.control.type.selection.dropdown.DropdownOption
import org.octavius.form.control.type.selection.dropdown.DropdownPage

/**
 * Kontrolka do wyboru rekordu z bazy danych z listy rozwijanej.
 *
 * Źródłem jest dowolne zapytanie. Kontrolka owija je podzapytaniem i dokleja wyszukiwanie,
 * sortowanie i stronicowanie, nigdy go nie przepisując - ten sam kontrakt co `buildQuery()`
 * w report-engine. Joiny, stały filtr czy wyliczany tekst pozycji piszesz więc w SQL-u, a nie
 * w parametrach kontrolki:
 *
 * ```kotlin
 * "author_id" to DatabaseControl(
 *     label = BooksTr.Form.author(),
 *     query = QueryFragment("SELECT id, name FROM books.authors"),
 *     displayColumn = "name"
 * )
 * ```
 *
 * Wartością kontrolki jest [idColumn] wybranego wiersza. Cały wiersz - łącznie z kolumnami, których
 * kontrolka nie pokazuje - trafia przy wyborze do `ActionContext.payload`, więc akcja może z niego
 * wypełnić inne kontrolki.
 *
 * @param query Zapytanie źródłowe. Jego parametry są dołączane do każdego wywołania.
 * @param displayColumn Kolumna wyniku pokazywana jako tekst pozycji.
 * @param idColumn Kolumna wyniku będąca wartością kontrolki.
 * @param orderBy Sortowanie listy, domyślnie po [displayColumn]. [idColumn] jest zawsze doklejane
 *   na końcu jako rozstrzygnięcie remisów: bez tego stronicowanie przez `OFFSET` nie jest
 *   deterministyczne, a przy doklejaniu kolejnych stron pozycja o powtórzonym tekście na granicy
 *   strony mogłaby się pojawić dwa razy albo wcale.
 * @param searchFilter Warunek wyszukiwania dla wpisanego tekstu. Brak lambdy albo `null` z niej
 *   oznacza domyślne `displayColumn::text ILIKE %tekst%`. Ten sam kształt co `buildQuickSearch`
 *   w report-engine.
 * @param pageSize Liczba pozycji doczytywanych naraz.
 */
class DatabaseControl(
    label: String?,
    private val query: QueryFragment,
    private val displayColumn: String,
    private val idColumn: String = "id",
    private val orderBy: String = displayColumn,
    private val searchFilter: ((String) -> QueryFragment?)? = null,
    private val pageSize: Long = 10,
    required: Boolean? = false,
    dependencies: Map<String, ControlDependency<*>>? = null,
    actions: List<ControlAction<Int>>? = null,
) : AsyncPaginatedDropdownControl<Int>(
    label, required, dependencies, actions
), KoinComponent {

    private val db: OctaviusClient by inject()

    /**
     * Wołane raz na wartość, poza renderowaniem, a wynik zapisuje się w `ControlState.displayText`.
     *
     * Nie ma tu cache'a i nie powinno być: jedna instancja kontrolki obsługuje wszystkie wiersze
     * repeatable (patrz `RepeatableControl.registerChildrenInGlobalMap`), więc pole na instancji
     * mieszałoby wiersze ze sobą. Właściwym cache'em jest stan kluczowany ścieżką.
     */
    override suspend fun resolveDisplayText(value: Int): String? = withContext(Dispatchers.IO) {
        // Nullable T celowo: wiersz mógł zniknąć spod zapisanego id, a pod nienullowalnym typem
        // brak wiersza i NULL lecą wyjątkiem, którego .asResult() nie zamienia na Failure.
        val result = db.select(displayColumn).fromSubquery(query.sql).where("$idColumn = @selectedId")
            .asResult().fetchField<String?>(query.params + ("selectedId" to value))

        when (result) {
            is DataResult.Failure -> {
                GlobalDialogManager.show(ErrorDialogConfig(result.error))
                null
            }

            is DataResult.Success<String?> -> result.value
        }
    }

    override suspend fun loadPage(searchQuery: String, page: Long): DropdownPage<Int> {
        return withContext(Dispatchers.IO) {
            val search = buildSearchFilter(searchQuery)

            // Bierzemy jeden wiersz ponad stronę: jeśli przyszedł, to jest co doczytywać.
            // To zastępuje osobne COUNT(*), które szło do bazy przy każdym znaku obok właściwego
            // zapytania - a przy doczytywaniu w trakcie przewijania liczba stron i tak nie jest
            // nigdzie pokazywana.
            val optionsResult = db.select("*").fromSubquery(query.sql)
                .where(search.sql)
                .orderBy("$orderBy, $idColumn")
                .limit(pageSize + 1)
                .offset(page * pageSize)
                .asResult().fetchObjects<Map<String, Any?>>(params = query.params + search.params)

            when (optionsResult) {
                is DataResult.Success -> {
                    val rows = optionsResult.value
                    val mappedOptions = rows.take(pageSize.toInt()).map { row ->
                        DropdownOption(
                            value = row[idColumn] as Int,
                            displayText = row[displayColumn] as String,
                            payload = row
                        )
                    }
                    DropdownPage(mappedOptions, hasMore = rows.size > pageSize)
                }

                is DataResult.Failure -> {
                    GlobalDialogManager.show(ErrorDialogConfig(optionsResult.error))
                    DropdownPage(emptyList(), hasMore = false)
                }
            }
        }
    }

    private fun buildSearchFilter(searchQuery: String): QueryFragment {
        if (searchQuery.isBlank()) return QueryFragment("")

        return searchFilter?.invoke(searchQuery)
            ?: ("$displayColumn::text ILIKE @searchQuery" withParam ("searchQuery" to "%$searchQuery%"))
    }
}
