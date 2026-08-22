package org.octavius.report.management

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowDownward
import androidx.compose.material.icons.filled.ArrowUpward
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import org.octavius.report.component.ReportState
import org.octavius.report.localization.ReportTr

@Composable
fun ColumnManagementPanel(
    reportState: ReportState,
    modifier: Modifier = Modifier
) {
    var isExpanded by remember { mutableStateOf(false) }

    Card(
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(8.dp)
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            // Header z przyciskiem rozwijania
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Button(onClick = { isExpanded = !isExpanded }, modifier = Modifier.fillMaxWidth()) {
                    Text(
                        text = ReportTr.Report.Management.columnManagement(),
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )
                    Icon(
                        imageVector = if (isExpanded) Icons.Default.ArrowUpward else Icons.Default.ArrowDownward,
                        contentDescription = if (isExpanded) ReportTr.Expandable.collapse() else ReportTr.Expandable.expand()
                    )
                }

            }

            if (isExpanded) {
                Spacer(modifier = Modifier.height(16.dp))

                // Sekcja widocznych kolumn z możliwością sortowania
                ColumnsSection(reportState)

                Spacer(modifier = Modifier.height(16.dp))

                // Sekcja sortowania
                SortingSection(reportState)
            }
        }
    }
}