package org.octavius.report.filter.type

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import kotlinx.serialization.json.JsonObject
import io.github.octaviusframework.client.query.QueryFragment
import io.github.octaviusframework.client.query.withParam
import io.github.octaviusframework.client.query.withParams
import org.octavius.report.FilterMode
import org.octavius.report.NumberFilterDataType
import org.octavius.report.ReportEvent
import org.octavius.report.filter.EnumDropdownMenu
import org.octavius.report.filter.Filter
import org.octavius.report.filter.FilterSpacer
import org.octavius.report.filter.data.type.NumberFilterData
import kotlin.reflect.KClass
import org.octavius.report.localization.ReportTr

class NumberFilter<T : Number>(
    private val numberClass: KClass<T>,
    private val valueParser: (String) -> T?
) : Filter<NumberFilterData<T>>() {

    override fun createDefaultData(): NumberFilterData<T> = NumberFilterData(numberClass)

    override fun deserializeData(data: JsonObject): NumberFilterData<T> = NumberFilterData.deserialize(data, numberClass)

    @Composable
    override fun RenderFilterUI(onEvent: (ReportEvent) -> Unit, columnKey: String, data: NumberFilterData<T>) {
        EnumDropdownMenu(
            currentValue = data.filterType,
            options = NumberFilterDataType.entries,
            onValueChange = { onEvent.invoke(ReportEvent.FilterChanged(columnKey, data.copy(filterType = it))) }
        )

        FilterSpacer()

        if (data.filterType == NumberFilterDataType.Range) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                OutlinedTextField(
                    value = data.minValue?.toString() ?: "",
                    onValueChange = { input ->
                        try {
                            val value = if (input.isEmpty()) null else valueParser(input)
                            onEvent.invoke(ReportEvent.FilterChanged(columnKey, data.copy(minValue = value)))
                        } catch (e: NumberFormatException) {
                            // Ignore invalid input
                            onEvent.invoke(ReportEvent.FilterChanged(columnKey, data.copy(minValue = null)))
                        }
                    },
                    label = { Text(ReportTr.Filter.Number.from()) },
                    singleLine = true,
                    modifier = Modifier.weight(1f)
                )

                OutlinedTextField(
                    value = data.maxValue?.toString() ?: "",
                    onValueChange = { input ->
                        try {
                            val value = if (input.isEmpty()) null else valueParser(input)
                            onEvent.invoke(ReportEvent.FilterChanged(columnKey, data.copy(maxValue = value)))
                        } catch (e: NumberFormatException) {
                            // Ignore invalid input
                            onEvent.invoke(ReportEvent.FilterChanged(columnKey, data.copy(maxValue = null)))
                        }
                    },
                    label = { Text(ReportTr.Filter.Number.to()) },
                    singleLine = true,
                    modifier = Modifier.weight(1f)
                )
            }
        } else {
            OutlinedTextField(
                value = data.minValue?.toString() ?: "",
                onValueChange = { input ->
                    try {
                        val value = if (input.isEmpty()) null else valueParser(input)
                        onEvent.invoke(ReportEvent.FilterChanged(columnKey, data.copy(minValue = value)))
                    } catch (e: NumberFormatException) {
                        // Ignore invalid input
                        onEvent.invoke(ReportEvent.FilterChanged(columnKey, data.copy(minValue = null)))
                    }
                },
                label = { Text(ReportTr.Filter.Number.value()) },
                singleLine = true,
                modifier = Modifier.fillMaxWidth(),
                trailingIcon = {
                    if (data.minValue != null) {
                        IconButton(onClick = {
                            onEvent.invoke(
                                ReportEvent.FilterChanged(
                                    columnKey,
                                    data.copy(minValue = null)
                                )
                            )
                        }) {
                            Icon(Icons.Default.Clear, ReportTr.Filter.General.clear())
                        }
                    }
                }
            )
        }
    }

    override fun buildBaseQueryFragment(
        columnName: String,
        data: NumberFilterData<T>
    ): QueryFragment? {
        if (data.minValue == null && data.maxValue == null) return null
        val min = data.minValue
        val max = data.maxValue
        val filterType = data.filterType

        return when (data.mode) {
            FilterMode.Single -> buildSingleNumberQuery(columnName, min, max, filterType)
            FilterMode.ListAny -> buildListNumberQuery(columnName, min, max, filterType, false)
            FilterMode.ListAll -> buildListNumberQuery(columnName, min, max, filterType, true)
        }
    }

    private fun buildSingleNumberQuery(
        columnName: String,
        min: T?,
        max: T?,
        filterType: NumberFilterDataType
    ): QueryFragment? {
        return when (filterType) {
            NumberFilterDataType.Equals -> {
                if (min != null) {
                    "$columnName = @$columnName" withParam (columnName to min)
                } else null
            }

            NumberFilterDataType.NotEquals -> {
                if (min != null) {
                    "$columnName != @$columnName" withParam (columnName to min)
                } else null
            }

            NumberFilterDataType.LessThan -> {
                if (min != null) {
                    "$columnName < @$columnName" withParam (columnName to min)
                } else null
            }

            NumberFilterDataType.LessEquals -> {
                if (min != null) {
                    "$columnName <= @$columnName" withParam (columnName to min)
                } else null
            }

            NumberFilterDataType.GreaterThan -> {
                if (min != null) {
                    "$columnName > @$columnName" withParam (columnName to min)
                } else null
            }

            NumberFilterDataType.GreaterEquals -> {
                if (min != null) {
                    "$columnName >= @$columnName" withParam (columnName to min)
                } else null
            }

            NumberFilterDataType.Range -> {
                when {
                    min != null && max != null ->
                        "$columnName BETWEEN @${columnName}_min AND @${columnName}_max" withParams mapOf(
                            "${columnName}_min" to min,
                            "${columnName}_max" to max
                        )

                    min != null ->
                        "$columnName >= @$columnName" withParam (columnName to min)

                    max != null ->
                        "$columnName <= @$columnName" withParam (columnName to max)

                    else -> null
                }
            }
        }
    }

    private fun buildListNumberQuery(
        columnName: String,
        min: T?,
        max: T?,
        filterType: NumberFilterDataType,
        isAllMode: Boolean
    ): QueryFragment? {
        return when (filterType) {
            NumberFilterDataType.Equals -> {
                if (min != null) {
                    val operator = if (isAllMode) "@>" else "&&"
                    "$columnName $operator @$columnName" withParam (columnName to listOf(min))
                } else null
            }

            NumberFilterDataType.NotEquals -> {
                if (min != null) {
                    val operator = if (isAllMode) "@>" else "&&"
                    "NOT ($columnName $operator @$columnName)" withParam (columnName to listOf(min))
                } else null
            }

            NumberFilterDataType.LessThan -> {
                if (min != null) {
                    val existsType =
                        if (isAllMode) "NOT EXISTS (SELECT 1 FROM unnest($columnName) AS elem WHERE elem >= @$columnName)" else "EXISTS (SELECT 1 FROM unnest($columnName) AS elem WHERE elem < @$columnName)"
                    existsType withParam (columnName to min)
                } else null
            }

            NumberFilterDataType.LessEquals -> {
                if (min != null) {
                    val existsType =
                        if (isAllMode) "NOT EXISTS (SELECT 1 FROM unnest($columnName) AS elem WHERE elem > @$columnName)" else "EXISTS (SELECT 1 FROM unnest($columnName) AS elem WHERE elem <= @$columnName)"
                    existsType withParam (columnName to min)
                } else null
            }

            NumberFilterDataType.GreaterThan -> {
                if (min != null) {
                    val existsType =
                        if (isAllMode) "NOT EXISTS (SELECT 1 FROM unnest($columnName) AS elem WHERE elem <= @$columnName)" else "EXISTS (SELECT 1 FROM unnest($columnName) AS elem WHERE elem > @$columnName)"
                    existsType withParam (columnName to min)
                } else null
            }

            NumberFilterDataType.GreaterEquals -> {
                if (min != null) {
                    val existsType =
                        if (isAllMode) "NOT EXISTS (SELECT 1 FROM unnest($columnName) AS elem WHERE elem < @$columnName)" else "EXISTS (SELECT 1 FROM unnest($columnName) AS elem WHERE elem >= @$columnName)"
                    existsType withParam (columnName to min)
                } else null
            }

            NumberFilterDataType.Range -> {
                when {
                    min != null && max != null -> {
                        val existsType =
                            if (isAllMode) "NOT EXISTS (SELECT 1 FROM unnest($columnName) AS elem WHERE elem NOT BETWEEN @${columnName}_min AND @${columnName}_max)" else "EXISTS (SELECT 1 FROM unnest($columnName) AS elem WHERE elem BETWEEN @${columnName}_min AND @${columnName}_max)"
                        existsType withParams mapOf("${columnName}_min" to min, "${columnName}_max" to max)
                    }

                    min != null -> {
                        val existsType =
                            if (isAllMode) "NOT EXISTS (SELECT 1 FROM unnest($columnName) AS elem WHERE elem < @$columnName)" else "EXISTS (SELECT 1 FROM unnest($columnName) AS elem WHERE elem >= @$columnName)"
                        existsType withParam (columnName to min)
                    }

                    max != null -> {
                        val existsType =
                            if (isAllMode) "NOT EXISTS (SELECT 1 FROM unnest($columnName) AS elem WHERE elem > @$columnName)" else "EXISTS (SELECT 1 FROM unnest($columnName) AS elem WHERE elem <= @$columnName)"
                        existsType withParam (columnName to max)
                    }

                    else -> null
                }
            }
        }
    }
}