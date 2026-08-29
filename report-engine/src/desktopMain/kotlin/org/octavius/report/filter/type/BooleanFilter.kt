package org.octavius.report.filter.type

import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import kotlinx.serialization.json.JsonObject
import io.github.octaviusframework.client.query.QueryFragment
import io.github.octaviusframework.client.query.withParam
import org.octavius.report.FilterMode
import org.octavius.report.ReportEvent
import org.octavius.report.filter.Filter
import org.octavius.report.filter.data.type.BooleanFilterData
import org.octavius.report.localization.ReportTr

class BooleanFilter(
    private val trueText: String,
    private val falseText: String
) : Filter<BooleanFilterData>() {

    override fun createDefaultData() = BooleanFilterData()

    override fun deserializeData(data: JsonObject) = BooleanFilterData.deserialize(data)

    @Composable
    override fun RenderFilterUI(onEvent: (ReportEvent) -> Unit, columnKey: String, data: BooleanFilterData) {
        Text(
            text = ReportTr.Filter.Boolean.filterByValue(),
            style = MaterialTheme.typography.titleMedium,
            modifier = Modifier.padding(bottom = 16.dp)
        )

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            FilterChip(
                selected = data.value == true,
                onClick = {
                    val value = if (data.value == true) null else true
                    onEvent.invoke(ReportEvent.FilterChanged(columnKey, data.copy(value = value)))
                },
                label = { Text(trueText) },
                leadingIcon = if (data.value == true) {
                    {
                        Icon(
                            imageVector = Icons.Default.Check,
                            contentDescription = null,
                            modifier = Modifier.size(FilterChipDefaults.IconSize)
                        )
                    }
                } else null
            )

            FilterChip(
                selected = data.value == false,
                onClick = {
                    val value = if (data.value == false) null else false
                    onEvent.invoke(ReportEvent.FilterChanged(columnKey, data.copy(value = value)))
                },
                label = { Text(falseText) },
                leadingIcon = if (data.value == false) {
                    {
                        Icon(
                            imageVector = Icons.Default.Close,
                            contentDescription = null,
                            modifier = Modifier.size(FilterChipDefaults.IconSize)
                        )
                    }
                } else null
            )
        }
    }

    override fun buildBaseQueryFragment(
        columnName: String,
        data: BooleanFilterData
    ): QueryFragment? {
        val booleanValue = data.value ?: return null

        return when (data.mode) {
            FilterMode.Single -> {
                "$columnName = @$columnName" withParam (columnName to booleanValue)
            }

            FilterMode.ListAny -> {
                "$columnName && @$columnName" withParam (columnName to listOf(booleanValue))
            }

            FilterMode.ListAll -> {
                "$columnName @> @$columnName" withParam (columnName to listOf(booleanValue))
            }
        }
    }
}