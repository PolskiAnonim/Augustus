package org.octavius.report.component

import org.koin.core.component.KoinComponent
import org.koin.core.component.inject
import io.github.octaviusframework.client.OctaviusClient
import io.github.octaviusframework.client.DataResult
import io.github.octaviusframework.client.query.QueryFragment
import io.github.octaviusframework.client.query.join
import org.octavius.report.ReportDataResult
import org.octavius.report.ReportPaginationState
import org.octavius.report.configuration.SortDirection
import org.octavius.report.filter.Filter
import org.octavius.report.filter.data.FilterData

/**
 * Manager odpowiedzialny za budowanie zapytań SQL i pobieranie danych dla raportów.
 *
 * ReportDataManager:
 * - Buduje dynamiczne zapytania SQL na podstawie filtrów, sortowania i wyszukiwania
 * - Zarządza paginacją (oblicza całkowitą liczbę elementów)
 * - Łączy różne typy filtrów w spójne klauzule WHERE
 * - Obsługuje globalne wyszukiwanie po wszystkich kolumnach
 * - Wykonuje zapytania przez OctaviusClient, zwracając wynik jako DataResult
 *
 * Proces pobierania danych:
 * 1. Buduje klauzulę WHERE na podstawie filtrów i wyszukiwania
 * 2. Wykonuje zapytanie COUNT dla paginacji
 * 3. Jeśli są dane, wykonuje zapytanie główne z LIMIT/OFFSET
 * 4. Zwraca wyniki w bezpiecznej otoczce ReportDataResult
 *
 * @param reportStructure Definicja struktury raportu z zapytaniem bazowym i kolumnami.
 */
class ReportDataManager(
    val reportStructure: ReportStructure
) : KoinComponent {
    val db: OctaviusClient by inject()
    private fun <T : FilterData> getQueryFragment(
        columnName: String,
        filter: Filter<T>,
        data: FilterData
    ): QueryFragment? {
        @Suppress("UNCHECKED_CAST")
        val specificData = data as T

        return filter.createQueryFragment(columnName, specificData)
    }

    private fun buildFilterClause(reportState: ReportState): QueryFragment {
        val columnFilters = reportState.filterData.mapNotNull { (columnKey, filterData) ->
            val column = reportStructure.getColumn(columnKey)
            getQueryFragment(columnKey, column.filter!!, filterData)
        }.join(" AND ")

        val searchFilter = reportStructure.getQuickSearchFilter(reportState.searchQuery)

        return listOf(searchFilter, columnFilters).join(" AND ")
    }

    private fun updatePagination(
        reportState: ReportState,
        sourceSql: String,
        filterClause: String,
        params: Map<String, Any?>
    ): DataResult<ReportPaginationState> {

        val countResult =
            db.select("COUNT(*)").fromSubquery(sourceSql).where(filterClause).asResult().fetchField<Long>(params)

        return when (countResult) {
            is DataResult.Success -> {
                val totalItems = countResult.value
                val pageSize = reportState.pagination.pageSize
                val totalPages = if (totalItems == 0L) 0 else (totalItems + pageSize - 1) / pageSize
                val newPaginationState = reportState.pagination.copy(totalItems = totalItems, totalPages = totalPages)
                DataResult.Success(newPaginationState)
            }

            is DataResult.Failure -> {
                // Po prostu propagujemy błąd dalej
                countResult
            }
        }
    }

    private fun buildOrderClause(reportState: ReportState): String {
        if (reportState.sortOrder.isEmpty()) return ""

        val sortDirectionMap = mapOf(
            SortDirection.Descending to "DESC",
            SortDirection.Ascending to "ASC"
        )

        val orderFragments = reportState.sortOrder.map { (columnKey, direction) ->
            "$columnKey ${sortDirectionMap[direction]}"
        }

        return orderFragments.joinToString(", ")
    }

    fun fetchData(reportState: ReportState): ReportDataResult {

        val query = reportStructure.queryFragment
        val params = query.params.toMutableMap()
        val filterClause = buildFilterClause(reportState)
        params.putAll(filterClause.params)
        val orderClause = buildOrderClause(reportState)

        val paginationResult = updatePagination(reportState, query.sql, filterClause.sql, params)

        val newPaginationState = when (paginationResult) {
            is DataResult.Success -> paginationResult.value
            is DataResult.Failure -> {
                return ReportDataResult.Failure(paginationResult.error)
            }
        }

        if (newPaginationState.totalItems == 0L) {
            return ReportDataResult.Success(emptyList(), newPaginationState)
        }

        val dataResult = db.select("*").fromSubquery(query.sql)
            .where(filterClause.sql) // builder grzecznie nie weźmie pustych wartości
            .orderBy(orderClause)
            .page(reportState.pagination.currentPage, reportState.pagination.pageSize)
            .asResult().fetchObjects<Map<String, Any?>>(params)

        return when (dataResult) {
            is DataResult.Success -> {
                ReportDataResult.Success(dataResult.value, newPaginationState)
            }

            is DataResult.Failure -> {
                ReportDataResult.Failure(dataResult.error)
            }
        }
    }
}