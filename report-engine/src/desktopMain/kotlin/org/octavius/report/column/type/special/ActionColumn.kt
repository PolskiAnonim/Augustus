package org.octavius.report.column.type.special

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import org.octavius.report.ReportActionContext
import org.octavius.report.ReportEvent
import org.octavius.report.ReportRowAction
import org.octavius.report.component.ReportState
import org.octavius.report.localization.ReportTr

/**
 * Specjalna kolumna renderująca menu akcji (dropdown) dla każdego wiersza w raporcie.
 * Wyświetla ikonę trzech kropek która po kliknięciu otwiera menu z dostępnymi akcjami.
 * 
 * Każda akcja otrzymuje kontekst zawierający:
 * - Dane wiersza
 * - Stan raportu  
 * - Funkcję obsługi zdarzeń
 * - Scope dla operacji asynchronicznych
 * 
 * @param actions Lista akcji do wyświetlenia w menu
 */
class ActionColumn(
    private val actions: List<ReportRowAction>
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
        var expanded by remember { mutableStateOf(false) }
        val scope = rememberCoroutineScope()

        @Suppress("UNCHECKED_CAST")
        val rowData = item as? Map<String, Any?> ?: return

        Box(
            modifier = modifier.padding(horizontal = 4.dp),
            contentAlignment = Alignment.Center
        ) {
            IconButton(onClick = { expanded = true }) {
                Icon(
                    imageVector = Icons.Default.MoreVert,
                    contentDescription = ReportTr.Report.Actions.menuDescription(),
                    modifier = Modifier.size(18.dp)
                )
            }

            DropdownMenu(
                expanded = expanded,
                onDismissRequest = { expanded = false }
            ) {
                actions.forEach { reportAction ->
                    DropdownMenuItem(
                        text = { Text(reportAction.label) },
                        leadingIcon = reportAction.icon?.let {
                            { Icon(it, contentDescription = reportAction.label) }
                        },
                        onClick = {
                            val context = ReportActionContext(rowData, reportState, onEvent, scope)
                            reportAction.action.invoke(context)
                            expanded = false
                        }
                    )
                }
            }
        }
    }
}