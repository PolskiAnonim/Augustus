package org.octavius.form.control.type.selection

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.koin.core.component.KoinComponent
import org.koin.core.component.inject
import io.github.octaviusframework.client.OctaviusClient
import io.github.octaviusframework.client.DataResult
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
 * Umożliwia wyszukiwanie i wybór rekordu z określonej tabeli bazy danych.
 * Obsługuje wyszukiwanie i paginację wyników. Wyświetla określoną kolumnę
 * jako tekst wyboru, a zwraca ID wybranego rekordu.
 */
class DatabaseControl(
    label: String?,
    private val relatedTable: String,
    private val displayColumn: String,
    private val pageSize: Long = 10,
    required: Boolean? = false,
    dependencies: Map<String, ControlDependency<*>>? = null,
    actions: List<ControlAction<Int>>? = null,
) : AsyncPaginatedDropdownControl<Int>( // <-- ZMIANA TUTAJ
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
        val result = db.select(displayColumn).from(relatedTable).where("id = @id")
            .asResult().fetchField<String?>("id" to value)

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
            val filter = if (searchQuery.isNotBlank()) "$displayColumn ILIKE @search" else null
            val params = if (searchQuery.isNotBlank()) mapOf("search" to "%$searchQuery%") else emptyMap()

            // Bierzemy jeden wiersz ponad stronę: jeśli przyszedł, to jest co doczytywać.
            // To zastępuje osobne COUNT(*), które szło do bazy przy każdym znaku obok właściwego
            // zapytania - a przy doczytywaniu w trakcie przewijania liczba stron i tak nie jest
            // nigdzie pokazywana.
            val optionsResult = db.select("id, $displayColumn").from(relatedTable)
                .where(filter)
                .orderBy(displayColumn)
                .limit(pageSize + 1)
                .offset(page * pageSize)
                .asResult().fetchObjects<Map<String, Any?>>(params = params)

            when (optionsResult) {
                is DataResult.Success -> {
                    val rows = optionsResult.value
                    val mappedOptions = rows.take(pageSize.toInt()).map { row ->
                        val id = row["id"] as Int
                        val text = row[displayColumn] as String
                        DropdownOption(id, text)
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
}