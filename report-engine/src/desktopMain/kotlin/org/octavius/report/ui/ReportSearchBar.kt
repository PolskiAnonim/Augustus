package org.octavius.report.ui

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Input
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import org.octavius.report.localization.ReportTr

@Composable
fun ReportSearchBar(
    searchQuery: String,
    onSearchChange: (String) -> Unit,
    onAddMenuClick: () -> Unit,
    addMenuExpanded: Boolean,
    onAddMenuDismiss: () -> Unit,
    addMenuContent: @Composable () -> Unit,
    onConfigurationClick: (() -> Unit)? = null,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .padding(8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        OutlinedTextField(
            value = searchQuery,
            onValueChange = onSearchChange,
            modifier = Modifier.weight(1f),
            placeholder = { Text(ReportTr.Search.placeholder()) },
            leadingIcon = {
                Icon(
                    imageVector = Icons.Default.Search,
                    contentDescription = ReportTr.Search.search()
                )
            },
            trailingIcon = {
                if (searchQuery.isNotEmpty()) {
                    IconButton(
                        onClick = { onSearchChange("") }
                    ) {
                        Icon(
                            imageVector = Icons.Default.Clear,
                            contentDescription = ReportTr.Search.clear()
                        )
                    }
                }
            },
            singleLine = true
        )

        // Przycisk konfiguracji
        if (onConfigurationClick != null) {
            IconButton(
                onClick = onConfigurationClick,
                modifier = Modifier.padding(start = 8.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.Settings,
                    contentDescription = ReportTr.Report.Configuration.configuration(1)
                )
            }
        }

        // Menu dodawania
        Box(
            modifier = Modifier.padding(start = 8.dp)
        ) {
            IconButton(
                onClick = onAddMenuClick
            ) {
                Icon(
                    imageVector = Icons.AutoMirrored.Filled.Input,
                    contentDescription = ReportTr.Action.add()
                )
            }

            DropdownMenu(
                expanded = addMenuExpanded,
                onDismissRequest = onAddMenuDismiss
            ) {
                addMenuContent()
            }
        }
    }
}