package org.octavius.report.filter

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.RadioButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import kotlinx.serialization.json.JsonObject
import io.github.octaviusframework.db.api.QueryFragment
import io.github.octaviusframework.db.api.withParams
import org.octavius.report.FilterMode
import org.octavius.report.NullHandling
import org.octavius.report.ReportEvent
import org.octavius.report.filter.data.FilterData
import org.octavius.report.localization.ReportTr

/**
 * Abstrakcyjna klasa bazowa dla wszystkich filtrów kolumn raportów.
 *
 * System filtrowania opiera się na współpracy dwóch komponentów:
 * - **Filter**: Logika filtrowania i generowanie SQL
 * - **FilterData**: Stan filtra (wartości wprowadzone przez użytkownika)
 *
 * Każdy typ kolumny ma własny filter (StringFilter, NumberFilter, etc.).
 * Filter odpowiada za:
 * - Tworzenie domyślnych danych filtra
 * - Renderowanie interfejsu użytkownika
 * - Generowanie fragmentów zapytań SQL
 * - Serializację/deserializację stanu
 *
 * @param T Typ danych filtra dziedziczący z FilterData.
 */
abstract class Filter<T : FilterData> {

    /**
     * Tworzy domyślny, pusty obiekt danych dla tego filtra.
     *
     * @return Nowa instancja FilterData z wartościami domyślnymi.
     */
    abstract fun createDefaultData(): T

    /**
     * Deserializuje dane filtra z konfiguracji JSON.
     *
     * Używane przy ładowaniu zapisanych konfiguracji raportów.
     *
     * @param data Obiekt JSON zawierający zserializowany stan filtra.
     * @return Zdeserialized obiekt FilterData.
     */
    abstract fun deserializeData(data: JsonObject): T

    /**
     * Renderuje interfejs użytkownika specyficzny dla tego typu filtra.
     *
     * Każdy typ filtra implementuje własny UI (pola tekstowe, listy rozwijane, etc.).
     * UI automatycznie wysyła zdarzenia ReportEvent.FilterChanged przy zmianach.
     *
     * @param onEvent Funkcja obsługi zdarzeń raportowania.
     * @param columnKey Klucz kolumny dla której renderowany jest filtr.
     * @param data Aktualny stan filtra do wyświetlenia.
     */
    @Composable
    abstract fun RenderFilterUI(onEvent: (ReportEvent) -> Unit, columnKey: String, data: T)

    /**
     * Tworzy fragment zapytania SQL na podstawie aktualnego stanu filtra.
     *
     * Implementowane przez każdy typ filtra zgodnie z jego logiką.
     * Zwraca null jeśli filtr nie jest aktywny lub nie ma co filtrować.
     *
     * @param columnName Nazwa kolumny w bazie danych.
     * @param data Aktualny stan filtra.
     * @return Query z fragmentem SQL i parametrami lub null.
     */
    protected abstract fun buildBaseQueryFragment(columnName: String, data: T): QueryFragment?

    fun createQueryFragment(columnName: String, data: T): QueryFragment? {
        if (!data.isActive()) return null

        val baseQuery = buildBaseQueryFragment(columnName, data)
        return applyNullHandling(columnName, baseQuery, data)
    }

    private fun applyNullHandling(columnName: String, baseQueryFragment: QueryFragment?, data: T): QueryFragment? {
        return when (data.nullHandling) {
            NullHandling.Ignore -> baseQueryFragment
            NullHandling.Include -> baseQueryFragment?.let {
                "(${it.sql} OR $columnName IS NULL)" withParams it.params
            } ?: QueryFragment("$columnName IS NULL")

            NullHandling.Exclude -> baseQueryFragment?.let {
                "(${it.sql} AND $columnName IS NOT NULL)" withParams it.params
            } ?: QueryFragment("$columnName IS NOT NULL")
        }
    }

    /**
     * Główny punkt wejścia do renderowania filtra.
     * Buduje cały UI, włączając w to wspólne panele.
     */
    @Composable
    fun Render(onEvent: (ReportEvent) -> Unit, columnKey: String, data: T) {
        Column(modifier = Modifier.padding(8.dp)) {
            // Renderujemy unikalną część dla danego typu filtra
            RenderFilterUI(onEvent, columnKey, data)

            FilterSpacer()
            FilterModePanel(onEvent, columnKey, data)
            FilterSpacer()
            NullHandlingPanel(onEvent, columnKey, data)
        }
    }


    // --- WSPÓLNE KOMPONENTY UI ---

    @Composable
    fun NullHandlingPanel(onEvent: (ReportEvent) -> Unit, columnKey: String, filterData: FilterData) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = ReportTr.Filter.Null.values(),
                style = MaterialTheme.typography.bodyMedium,
                modifier = Modifier.padding(end = 8.dp)
            )

            RadioButton(
                selected = filterData.nullHandling == NullHandling.Ignore,
                onClick = {
                    onEvent.invoke(
                        ReportEvent.FilterChanged(
                            columnKey,
                            filterData.withNullHandling(NullHandling.Ignore)
                        )
                    )
                }
            )
            Text(ReportTr.Filter.Null.ignore(), modifier = Modifier.padding(end = 12.dp))

            RadioButton(
                selected = filterData.nullHandling == NullHandling.Include,
                onClick = {
                    onEvent.invoke(
                        ReportEvent.FilterChanged(
                            columnKey,
                            filterData.withNullHandling(NullHandling.Include)
                        )
                    )
                }
            )
            Text(ReportTr.Filter.Null.include(), modifier = Modifier.padding(end = 12.dp))

            RadioButton(
                selected = filterData.nullHandling == NullHandling.Exclude,
                onClick = {
                    onEvent.invoke(
                        ReportEvent.FilterChanged(
                            columnKey,
                            filterData.withNullHandling(NullHandling.Exclude)
                        )
                    )

                }
            )
            Text(ReportTr.Filter.Null.exclude())
        }
    }

    @Composable
    fun FilterModePanel(onEvent: (ReportEvent) -> Unit, columnKey: String, filterData: FilterData) {
        if (filterData.mode == FilterMode.Single) return
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = ReportTr.Filter.mode(),
                style = MaterialTheme.typography.bodyMedium,
                modifier = Modifier.padding(end = 8.dp)
            )

            RadioButton(
                selected = filterData.mode == FilterMode.ListAny,
                onClick = {
                    onEvent.invoke(ReportEvent.FilterChanged(columnKey, filterData.withMode(FilterMode.ListAny)))
                }

            )
            Text(FilterMode.ListAny.toDisplayString(), modifier = Modifier.padding(end = 12.dp))

            RadioButton(
                selected = filterData.mode == FilterMode.ListAll,
                onClick = {
                    onEvent.invoke(ReportEvent.FilterChanged(columnKey, filterData.withMode(FilterMode.ListAll)))
                }
            )
            Text(FilterMode.ListAll.toDisplayString())
        }
    }
}