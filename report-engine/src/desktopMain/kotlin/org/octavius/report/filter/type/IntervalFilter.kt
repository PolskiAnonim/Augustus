package org.octavius.report.filter.type

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import kotlinx.serialization.json.JsonObject
import io.github.octaviusframework.client.query.QueryFragment
import io.github.octaviusframework.client.query.withParam
import io.github.octaviusframework.client.query.withParams
import org.octavius.report.FilterMode
import org.octavius.report.IntervalFilterDataType
import org.octavius.report.ReportEvent
import org.octavius.report.filter.EnumDropdownMenu
import org.octavius.report.filter.Filter
import org.octavius.report.filter.FilterSpacer
import org.octavius.report.filter.data.type.IntervalFilterData
import org.octavius.ui.datetime.IntervalPickerDialog
import org.octavius.ui.datetime.PickerTextField
import kotlin.time.Duration
import kotlin.time.ExperimentalTime

@OptIn(ExperimentalTime::class)
class IntervalFilter : Filter<IntervalFilterData>() {

    /**
     * Formatuje Duration na czytelny string HH:MM:SS do wyświetlenia w UI.
     * Ignoruje części składowe większe niż godziny (dni, miesiące, lata).
     */
    private fun formatDurationForDisplay(duration: Duration?): String {
        return duration?.let {
            val totalSeconds = it.inWholeSeconds
            val hours = totalSeconds / 3600
            val minutes = (totalSeconds % 3600) / 60
            val seconds = totalSeconds % 60
            String.format("%02d:%02d:%02d", hours, minutes, seconds)
        } ?: ""
    }

    override fun createDefaultData(): IntervalFilterData = IntervalFilterData()

    override fun deserializeData(data: JsonObject): IntervalFilterData = IntervalFilterData.deserialize(data)

    @Composable
    override fun RenderFilterUI(onEvent: (ReportEvent) -> Unit, columnKey: String, data: IntervalFilterData) {
        // Stany do zarządzania widocznością dialogów dla każdego pola
        var showSingleValuePicker by remember { mutableStateOf(false) }
        var showMinValuePicker by remember { mutableStateOf(false) }
        var showMaxValuePicker by remember { mutableStateOf(false) }

        EnumDropdownMenu(
            currentValue = data.filterType,
            options = IntervalFilterDataType.entries,
            onValueChange = { onEvent.invoke(ReportEvent.FilterChanged(columnKey, data.copy(filterType = it))) }
        )

        FilterSpacer()

        if (data.filterType == IntervalFilterDataType.Range) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                // Picker dla minValue
                PickerTextField(
                    value = formatDurationForDisplay(data.minValue),
                    onClick = { showMinValuePicker = true },
                    onClear = { onEvent.invoke(ReportEvent.FilterChanged(columnKey, data.copy(minValue = null))) },
                    isRequired = false // Zakładamy, że minValue nie jest wymagane
                )
                // Picker dla maxValue
                PickerTextField(
                    value = formatDurationForDisplay(data.maxValue),
                    onClick = { showMaxValuePicker = true },
                    onClear = { onEvent.invoke(ReportEvent.FilterChanged(columnKey, data.copy(maxValue = null))) },
                    isRequired = false // Zakładamy, że maxValue nie jest wymagane
                )
            }
        } else {
            // Picker dla pojedynczej wartości
            PickerTextField(
                value = formatDurationForDisplay(data.value),
                onClick = { showSingleValuePicker = true },
                onClear = { onEvent.invoke(ReportEvent.FilterChanged(columnKey, data.copy(value = null))) },
                isRequired = false // Zakładamy, że wartość nie jest wymagana
            )
        }

        // --- Dialogi ---
        if (showSingleValuePicker) {
            IntervalPickerDialog(
                initialValue = data.value,
                onDismiss = { showSingleValuePicker = false },
                onConfirm = { newDuration ->
                    onEvent.invoke(ReportEvent.FilterChanged(columnKey, data.copy(value = newDuration)))
                    showSingleValuePicker = false
                }
            )
        }
        if (showMinValuePicker) {
            IntervalPickerDialog(
                initialValue = data.minValue,
                onDismiss = { showMinValuePicker = false },
                onConfirm = { newDuration ->
                    onEvent.invoke(ReportEvent.FilterChanged(columnKey, data.copy(minValue = newDuration)))
                    showMinValuePicker = false
                }
            )
        }
        if (showMaxValuePicker) {
            IntervalPickerDialog(
                initialValue = data.maxValue,
                onDismiss = { showMaxValuePicker = false },
                onConfirm = { newDuration ->
                    onEvent.invoke(ReportEvent.FilterChanged(columnKey, data.copy(maxValue = newDuration)))
                    showMaxValuePicker = false
                }
            )
        }
    }

    override fun buildBaseQueryFragment(
        columnName: String,
        data: IntervalFilterData
    ): QueryFragment? {
        if (data.value == null && data.minValue == null && data.maxValue == null) return null
        val singleValue = data.value
        val min = data.minValue
        val max = data.maxValue
        val filterType = data.filterType

        return when (data.mode) {
            FilterMode.Single -> buildSingleIntervalQuery(columnName, singleValue, min, max, filterType)
            FilterMode.ListAny -> buildListIntervalQuery(columnName, singleValue, min, max, filterType, false)
            FilterMode.ListAll -> buildListIntervalQuery(columnName, singleValue, min, max, filterType, true)
        }
    }

    private fun buildSingleIntervalQuery(
        columnName: String,
        singleValue: Duration?,
        min: Duration?,
        max: Duration?,
        filterType: IntervalFilterDataType
    ): QueryFragment? {
        if (filterType == IntervalFilterDataType.Range) {
            return when {
                min != null && max != null ->
                    "$columnName BETWEEN @${columnName}_min AND @${columnName}_max" withParams mapOf(
                        "${columnName}_min" to min,
                        "${columnName}_max" to max
                    )
                min != null -> "$columnName >= @$columnName" withParam (columnName to min)
                max != null -> "$columnName <= @$columnName" withParam (columnName to max)
                else -> null
            }
        }

        if (singleValue == null) {
            return null
        }

        // Parametry typu interval w PostgreSQL są obsługiwane bezpośrednio
        return when (filterType) {
            IntervalFilterDataType.Equals -> {
                "$columnName = @$columnName" withParam (columnName to singleValue)
            }

            IntervalFilterDataType.NotEquals -> {
                "$columnName != @$columnName" withParam (columnName to singleValue)
            }

            IntervalFilterDataType.LessThan -> {
                "$columnName < @$columnName" withParam (columnName to singleValue)
            }

            IntervalFilterDataType.LessEquals -> {
                "$columnName <= @$columnName" withParam (columnName to singleValue)
            }

            IntervalFilterDataType.GreaterThan -> {
                "$columnName > @$columnName" withParam (columnName to singleValue)
            }

            IntervalFilterDataType.GreaterEquals -> {
                "$columnName >= @$columnName" withParam (columnName to singleValue)
            }
        }
    }

    private fun buildExistsForOperator(columnName: String, operator: String, paramName: String, isAllMode: Boolean): String {
        val unnest = "FROM unnest($columnName) AS elem"
        return if (isAllMode) {
            // "Dla wszystkich": NIE ISTNIEJE żaden element, który NIE spełnia warunku.
            // Np. "Wszystkie są < 5" == "Nie istnieje żaden, który jest >= 5"
            val negatedOperator = negateOperator(operator)
            "NOT EXISTS (SELECT 1 $unnest WHERE elem $negatedOperator @$paramName)"
        } else {
            // "Jakikolwiek": ISTNIEJE element, który spełnia warunek.
            "EXISTS (SELECT 1 $unnest WHERE elem $operator @$paramName)"
        }
    }

    private fun buildExistsForBetween(columnName: String, minParam: String, maxParam: String, isAllMode: Boolean): String {
        val unnest = "FROM unnest($columnName) AS elem"
        return if (isAllMode) {
            "NOT EXISTS (SELECT 1 $unnest WHERE elem NOT BETWEEN @$minParam AND @$maxParam)"
        } else {
            "EXISTS (SELECT 1 $unnest WHERE elem BETWEEN @$minParam AND @$maxParam)"
        }
    }

    // Mały helper do negacji operatorów
    private fun negateOperator(operator: String): String = when (operator) {
        "<" -> ">="
        "<=" -> ">"
        ">" -> "<="
        ">=" -> "<"
        "=" -> "!="
        "!=" -> "="
        else -> throw IllegalArgumentException("Unknown operator: $operator")
    }

    private fun buildListIntervalQuery(
        columnName: String,
        singleValue: Duration?,
        min: Duration?,
        max: Duration?,
        filterType: IntervalFilterDataType,
        isAllMode: Boolean
    ): QueryFragment? {

        if (filterType == IntervalFilterDataType.Equals || filterType == IntervalFilterDataType.NotEquals) {
            if (singleValue == null) return null

            val arrayOperator = if (isAllMode) "@>" else "&&"
            val sql = "$columnName $arrayOperator @$columnName"
            val finalSql = if (filterType == IntervalFilterDataType.NotEquals) "NOT ($sql)" else sql

            return QueryFragment(finalSql, mapOf(columnName to listOf(singleValue)))
        }

        if (filterType == IntervalFilterDataType.Range) {
            return when {
                min != null && max != null -> buildExistsForBetween(
                    columnName, "${columnName}_min", "${columnName}_max", isAllMode
                ) withParams mapOf("${columnName}_min" to min, "${columnName}_max" to max)

                // Zakresy z jednym końcem sprowadzają się do prostych porównań
                min != null ->
                    buildExistsForOperator(columnName, ">=", columnName, isAllMode) withParam (columnName to min)

                max != null ->
                    buildExistsForOperator(columnName, "<=", columnName, isAllMode) withParam (columnName to max)

                else -> null
            }
        }

        // Obsługa wszystkich pozostałych prostych porównań (<, <=, >, >=)
        if (singleValue == null) return null

        val operator = when (filterType) {
            IntervalFilterDataType.LessThan -> "<"
            IntervalFilterDataType.LessEquals -> "<="
            IntervalFilterDataType.GreaterThan -> ">"
            IntervalFilterDataType.GreaterEquals -> ">="
        }

        val sql = buildExistsForOperator(columnName, operator, columnName, isAllMode)
        return QueryFragment(sql, mapOf(columnName to singleValue))
    }
}