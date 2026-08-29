package org.octavius.report.configuration

import kotlinx.serialization.json.JsonObject
import io.github.octaviusframework.annotation.PgCompositeType
import io.github.octaviusframework.annotation.PgEnumType

data class ReportConfiguration(
    val id: Int? = null,
    val name: String,
    val reportName: String,
    val description: String? = null,
    val isDefault: Boolean = false,
    val visibleColumns: List<String>,
    val columnOrder: List<String>,
    val sortOrder: List<SortConfiguration>,
    val pageSize: Long,
    val filters: List<FilterConfig>
)

@PgEnumType
enum class SortDirection {
    Ascending, // Rosnąca
    Descending // Malejąca
}

@PgCompositeType
data class SortConfiguration(
    val columnName: String,
    val sortDirection: SortDirection
)

@PgCompositeType
data class FilterConfig(
    val columnName: String,
    val config: JsonObject
)