package org.octavius.report.column

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.FilterAlt
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import org.octavius.report.ColumnWidth
import org.octavius.report.ReportEvent
import org.octavius.report.component.ReportState
import org.octavius.report.filter.Filter
import org.octavius.report.filter.data.FilterData
import org.octavius.report.localization.ReportTr

/**
 * Klasa bazowa dla wszystkich kolumn w systemie raportowania.
 * Definiuje wspólną funkcjonalność dla wyświetlania danych w tabeli z obsługą filtrowania i sortowania.
 * 
 * @param header Nagłówek kolumny wyświetlany w tabeli
 * @param width Szerokość kolumny (stała lub elastyczna)
 * @param filterable Czy kolumna może być filtrowana
 * @param sortable Czy kolumna może być sortowana
 */
abstract class ReportColumn(
    val header: String,
    val width: ColumnWidth,
    val filterable: Boolean,
    val sortable: Boolean
) {

    /**
     * Leniwa inicjalizacja filtra dla kolumny.
     * Filtr jest tworzony tylko jeśli kolumna obsługuje filtrowanie.
     */
    val filter: Filter<*>? by lazy {
        if (filterable) createFilter() else null
    }

    /**
     * Tworzy instancję filtra odpowiedniego dla typu danych tej kolumny.
     * Musi być zaimplementowana przez klasy potomne.
     * 
     * @return Instancja filtra specyficznego dla typu danych kolumny
     */
    abstract fun createFilter(): Filter<*>

    /**
     * Tworzy domyślne dane filtra dla danego filtra.
     * Może być nadpisana przez klasy potomne dla zmiany domyślnego działania.
     * 
     * @param filter Instancja filtra dla której tworzone są dane
     * @return Dane filtra z domyślnymi wartościami
     */
    protected open fun createFilterData(filter: Filter<*>): FilterData {
        return filter.createDefaultData()
    }

    /**
     * Tworzy filtr i odpowiadające mu dane filtra.
     * Używane przy inicjalizacji filtrów w tabeli.
     * 
     * @return Dane filtra lub null jeśli kolumna nie jest filtrowalna
     */
    fun createFilterAndFilterData(): FilterData? {
        return filter?.let { createFilterData(it) }
    }

    /**
     * Renderuje nagłówek kolumny z obsługą menu filtrowania.
     * Automatycznie wyświetla ikone aktywnego filtra i obsługuje kliknięcia w nagłówek.
     * 
     * @param onEvent Funkcja obsługi zdarzeń raportu
     * @param columnKey Klucz kolumny w systemie raportów/nazwa kolumny w bazie danych
     * @param reportState Stan raportu zawierający dane filtrów
     * @param modifier Modyfikator Compose
     */
    @Composable
    fun RenderHeader(
        onEvent: (ReportEvent) -> Unit,
        columnKey: String,
        reportState: ReportState,
        modifier: Modifier = Modifier
    ) {
        val filterData = reportState.filterData[columnKey]

        var showColumnMenu by remember { mutableStateOf(false) }

        Box(
            modifier = modifier
                .padding(horizontal = 4.dp),
            contentAlignment = Alignment.Center
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.Center,
                modifier = if (filterData != null) {
                    Modifier.clickable { showColumnMenu = true }
                } else Modifier
            ) {
                Text(
                    text = header,
                    style = MaterialTheme.typography.titleMedium,
                    textAlign = TextAlign.Center
                )

                // Wskaźnik aktywnego filtra
                if (filterData?.isActive() ?: false) {
                    Icon(
                        imageVector = Icons.Default.FilterAlt,
                        contentDescription = ReportTr.Filter.General.active(),
                        tint = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.size(16.dp)
                    )
                }
            }

            // Menu filtra dla kolumny
            if (filterData != null) {
                DropdownMenu(
                    expanded = showColumnMenu,
                    onDismissRequest = { showColumnMenu = false }
                ) {
                    Column(
                        modifier = Modifier.padding(16.dp)
                    ) {
                        Text(
                            text = ReportTr.Filter.General.label() + " $header",
                            style = MaterialTheme.typography.titleSmall,
                            modifier = Modifier.padding(bottom = 8.dp)
                        )

                        RenderFilter(onEvent, columnKey, filter!!, filterData)

                        // Przycisk wyczyść filtr
                        if (filterData.isActive()) {
                            Row(
                                modifier = Modifier.padding(top = 8.dp),
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                OutlinedButton(
                                    onClick = {
                                        onEvent.invoke(ReportEvent.ClearFilter(columnKey))
                                        showColumnMenu = false
                                    }
                                ) {
                                    Text(ReportTr.Filter.General.clear())
                                }

                                Button(
                                    onClick = { showColumnMenu = false }
                                ) {
                                    Text(ReportTr.Action.close())
                                }
                            }
                        }
                    }
                }
            }
        }
    }

    /**
     * Funkcja renderująca filtr - zmienia * w T dla typu generycznego.
     * Wymagana aby zadowolić kompilator przy obsłudze typów generycznych.
     * 
     * @param onEvent Funkcja obsługi zdarzeń raportowania
     * @param columnKey Klucz kolumny w systemie raportowania
     * @param filter Instancja filtra do renderowania
     * @param data Dane filtra do wyświetlenia
     */
    @Composable
    private fun <T : FilterData> RenderFilter(
        onEvent: (ReportEvent) -> Unit,
        columnKey: String,
        filter: Filter<T>,
        data: FilterData
    ) {
        @Suppress("UNCHECKED_CAST")
        val specificData = data as T

        filter.Render(onEvent, columnKey, specificData)
    }

    /**
     * Renderuje zawartość komórki dla danego elementu danych.
     * Musi być zaimplementowana przez klasy potomne.
     * 
     * @param item Obiekt danych do wyświetlenia w komórce
     * @param modifier Modyfikator Compose dla komórki
     */
    @Composable
    abstract fun RenderCell(item: Any?, modifier: Modifier)
}