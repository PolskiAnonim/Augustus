package org.octavius.report.column.type.special

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Start
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import org.octavius.report.ReportActionContext
import org.octavius.report.ReportEvent
import org.octavius.report.ReportRowAction
import org.octavius.report.component.ReportState
import org.octavius.report.localization.ReportTr

/**
 * Specjalna kolumna której kliknięcie umożliwia wykonanie wybranej akcji.
 * Wyświetla ikonę trzech kropek która po kliknięciu otwiera menu z dostępnymi akcjami.
 *
 * Każda akcja otrzymuje kontekst zawierający:
 * - Dane wiersza
 * - Stan raportu
 * - Funkcję obsługi zdarzeń
 * - Scope dla operacji asynchronicznych
 *
 * @param action akcja wykonywana po kliknięciu
 */
class QuickActionColumn(
    private val action: ReportRowAction
) : SpecialColumn(
    width = 60.dp
) {
    @Composable
    override fun RenderCell(
        item: Any?,
        reportState: ReportState,
        onEvent: (ReportEvent) -> Unit,
        modifier: Modifier
    ) {
        val scope = rememberCoroutineScope()

        @Suppress("UNCHECKED_CAST")
        val rowData = item as? Map<String, Any?> ?: return

        Box(
            modifier = modifier.padding(horizontal = 4.dp),
            contentAlignment = Alignment.Center
        ) {
            IconButton(onClick = {
                val context = ReportActionContext(rowData, reportState, onEvent, scope)
                action.action.invoke(context)
            }) {
                Icon(
                    imageVector = Icons.Default.Start,
                    contentDescription = ReportTr.Report.Actions.menuDescription(),
                    modifier = Modifier.size(18.dp)
                )
            }
        }
    }
}