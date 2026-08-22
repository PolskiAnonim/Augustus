package org.octavius.report.column.type

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import org.octavius.report.CellRendererUtils
import org.octavius.report.ColumnWidth
import org.octavius.report.column.ReportColumn
import org.octavius.report.filter.Filter
import org.octavius.report.filter.type.BooleanFilter
import org.octavius.report.localization.ReportTr

/**
 * Kolumna do wyświetlania wartości boolean w raporcie.
 * Może wyświetlać dane jako ikony (check/close) lub jako tekst.
 * Obsługuje filtrowanie przez wybór wartości true/false.
 *
 * @param header Nagłówek kolumny
 * @param width Względna szerokość kolumny (domyślnie 1.0)
 * @param filterable Czy kolumna obsługuje filtrowanie (domyślnie true)
 * @param sortable Czy kolumna obsługuje sortowanie (domyślnie true)
 * @param trueText Tekst wyświetlany dla wartości true
 * @param falseText Tekst wyświetlany dla wartości false
 * @param showIcon Czy pokazywać ikony zamiast tekstu (domyślnie true)
 */
class BooleanColumn(
    header: String,
    width: Float = 1f,
    filterable: Boolean = true,
    sortable: Boolean = true,
    private val trueText: String = ReportTr.Report.Column.Boolean.`true`(),
    private val falseText: String = ReportTr.Report.Column.Boolean.`false`(),
    private val showIcon: Boolean = true
) : ReportColumn(header, ColumnWidth.Flexible(width), filterable, sortable) {

    override fun createFilter(): Filter<*> {
        return BooleanFilter(trueText, falseText)
    }

    @Composable
    override fun RenderCell(item: Any?, modifier: Modifier) {
        val value = item as? Boolean

        CellRendererUtils.StandardCellWrapper(
            modifier = modifier,
            alignment = Alignment.Center
        ) {
            if (value == null) {
                return@StandardCellWrapper
            }

            if (showIcon) {
                Icon(
                    imageVector = if (value) Icons.Default.Check
                    else Icons.Default.Close,
                    contentDescription = if (value) trueText else falseText,
                    tint = if (value) MaterialTheme.colorScheme.primary
                    else MaterialTheme.colorScheme.error
                )
            } else {
                Text(
                    text = if (value) trueText else falseText, style = MaterialTheme.typography.bodyMedium
                )
            }

        }
    }
}