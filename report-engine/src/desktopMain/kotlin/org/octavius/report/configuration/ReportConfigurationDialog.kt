package org.octavius.report.configuration

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Bookmark
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Download
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import org.octavius.report.ReportEvent
import org.octavius.report.component.ReportState
import org.octavius.report.localization.ReportTr

@Composable
fun ReportConfigurationDialog(
    onEvent: (ReportEvent) -> Unit,
    reportName: String,
    reportState: ReportState,
    onDismiss: () -> Unit
) {
    var showSaveDialog by remember { mutableStateOf(false) }
    var configurations by remember { mutableStateOf(emptyList<ReportConfiguration>()) }
    val configManager = remember { ReportConfigurationManager() }

    LaunchedEffect(reportName) {
        configurations = configManager.listConfigurations(reportName)
    }

    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Card(
            modifier = Modifier
                .widthIn(min = 500.dp, max = 600.dp)
                .heightIn(min = 400.dp, max = 600.dp)
        ) {
            Column(modifier = Modifier.fillMaxSize().padding(16.dp)) {
                Text(ReportTr.Report.Configuration.management(), style = MaterialTheme.typography.headlineSmall)
                Spacer(modifier = Modifier.height(16.dp))
                Button(onClick = { showSaveDialog = true }, modifier = Modifier.fillMaxWidth()) {
                    Icon(Icons.Default.Bookmark, contentDescription = null, modifier = Modifier.padding(end = 8.dp))
                    Text(ReportTr.Report.Configuration.saveCurrent())
                }
                Spacer(modifier = Modifier.height(16.dp))
                HorizontalDivider()
                Spacer(modifier = Modifier.height(16.dp))
                Text(ReportTr.Report.Configuration.savedConfigurations(), style = MaterialTheme.typography.titleMedium)
                Spacer(modifier = Modifier.height(8.dp))
                if (configurations.isEmpty()) {
                    Box(modifier = Modifier.weight(1f).fillMaxWidth(), contentAlignment = Alignment.Center) {
                        Text(ReportTr.Report.Configuration.noSavedConfigurations(), style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    }
                } else {
                    LazyColumn(modifier = Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        items(configurations) { config ->
                            ConfigurationItem(
                                configuration = config,
                                onLoad = {
                                    onEvent(ReportEvent.ApplyConfiguration(config))
                                    onDismiss()
                                },
                                onDelete = {
                                    if(configManager.deleteConfiguration(config.name, reportName)) {
                                        configurations = configurations.filter { it.id != config.id }
                                    }
                                }
                            )
                        }
                    }
                }
                Spacer(modifier = Modifier.height(16.dp))
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.End) {
                    TextButton(onClick = onDismiss) { Text(ReportTr.Action.close()) }
                }
            }
        }
    }

    if (showSaveDialog) {
        SaveConfigurationDialog(
            reportName = reportName,
            reportState = reportState,
            configManager = configManager,
            existingConfigurations = configurations,
            onDismiss = { showSaveDialog = false },
            onSaved = {
                showSaveDialog = false
                configurations = configManager.listConfigurations(reportName)
            }
        )
    }
}

@Composable
fun ConfigurationItem(
    configuration: ReportConfiguration,
    onLoad: () -> Unit,
    onDelete: () -> Unit
) {
    OutlinedCard(modifier = Modifier.fillMaxWidth()) {
        Row(modifier = Modifier.padding(12.dp).fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
            Column(modifier = Modifier.weight(1f)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(text = configuration.name, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Medium)
                    if (configuration.isDefault) {
                        Surface(modifier = Modifier.padding(start = 8.dp), shape = MaterialTheme.shapes.small, color = MaterialTheme.colorScheme.primaryContainer) {
                            Text(text = ReportTr.Report.Configuration.defaultConfig().uppercase(), style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onPrimaryContainer, modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp))
                        }
                    }
                }
                configuration.description?.takeIf { it.isNotEmpty() }?.let {
                    Text(text = it, style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
            }
            Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                IconButton(onClick = onLoad) { Icon(Icons.Default.Download, contentDescription = ReportTr.Action.load()) }
                IconButton(onClick = onDelete) { Icon(Icons.Default.Delete, contentDescription = ReportTr.Action.remove(), tint = MaterialTheme.colorScheme.error) }
            }
        }
    }
}

private class SaveConfigurationDialogStateHolder(
    private val reportName: String,
    private val reportState: ReportState,
    private val configManager: ReportConfigurationManager,
    private val existingConfigurations: List<ReportConfiguration>,
    private val onSaved: () -> Unit
) {
    // === Stan UI ===
    var name by mutableStateOf("")
    var description by mutableStateOf("")
    var isDefault by mutableStateOf(false)
    var nameError by mutableStateOf<String?>(null)
    var generalError by mutableStateOf<String?>(null)

    val isOverwriting by derivedStateOf {
        existingConfigurations.any { it.name.equals(name.trim(), ignoreCase = true) }
    }

    // === Logika Akcji ===
    fun onNameChange(newName: String) {
        name = newName
        nameError = null
        generalError = null
    }

    fun onSaveClick() {
        if (name.isBlank()) {
            nameError = ReportTr.Report.Configuration.nameRequired()
            return
        }

        val newConfiguration = buildConfiguration()

        if (configManager.saveConfiguration(newConfiguration)) {
            onSaved()
        } else {
            generalError = ReportTr.Report.Configuration.errorSaving()
        }
    }

    private fun buildConfiguration(): ReportConfiguration {
        return ReportConfiguration(
            name = name.trim(),
            reportName = reportName,
            description = description.trim().takeIf { it.isNotEmpty() },
            isDefault = isDefault,
            visibleColumns = reportState.visibleColumns.toList(),
            columnOrder = reportState.columnKeysOrder,
            sortOrder = reportState.sortOrder.map { (col, dir) -> SortConfiguration(col, dir) },
            pageSize = reportState.pagination.pageSize,
            filters = reportState.filterData.map { (col, data) -> FilterConfig(col, data.serialize()) }
        )
    }
}

@Composable
fun SaveConfigurationDialog(
    reportName: String,
    reportState: ReportState,
    configManager: ReportConfigurationManager,
    existingConfigurations: List<ReportConfiguration>,
    onDismiss: () -> Unit,
    onSaved: () -> Unit
) {
    val state = remember(reportName, reportState, configManager, existingConfigurations, onSaved) {
        SaveConfigurationDialogStateHolder(reportName, reportState, configManager, existingConfigurations, onSaved)
    }

    Dialog(onDismissRequest = onDismiss, properties = DialogProperties(usePlatformDefaultWidth = false)) {
        Card(modifier = Modifier.width(400.dp)) {
            Column(modifier = Modifier.padding(16.dp)) {
                Text(text = ReportTr.Report.Configuration.saveConfiguration(), style = MaterialTheme.typography.headlineSmall)
                Spacer(modifier = Modifier.height(16.dp))

                // === Pola formularza ===
                ConfigurationNameInput(
                    name = state.name,
                    onNameChange = state::onNameChange,
                    nameError = state.nameError,
                    isOverwriting = state.isOverwriting
                )

                Spacer(modifier = Modifier.height(8.dp))
                OutlinedTextField(
                    value = state.description,
                    onValueChange = { state.description = it },
                    label = { Text(ReportTr.Report.Configuration.description()) },
                    modifier = Modifier.fillMaxWidth()
                )

                Spacer(modifier = Modifier.height(8.dp))
                Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.fillMaxWidth()) {
                    Checkbox(checked = state.isDefault, onCheckedChange = { state.isDefault = it })
                    Text(ReportTr.Report.Configuration.setAsDefault())
                }

                // === Błędy i przyciski ===
                if (state.generalError != null) {
                    Text(text = state.generalError!!, color = MaterialTheme.colorScheme.error, style = MaterialTheme.typography.bodySmall, modifier = Modifier.padding(top = 8.dp))
                }
                Spacer(modifier = Modifier.height(16.dp))

                DialogActions(
                    onDismiss = onDismiss,
                    onSaveClick = state::onSaveClick,
                    isOverwriting = state.isOverwriting
                )
            }
        }
    }
}

@Composable
private fun ConfigurationNameInput(
    name: String,
    onNameChange: (String) -> Unit,
    nameError: String?,
    isOverwriting: Boolean
) {
    OutlinedTextField(
        value = name,
        onValueChange = onNameChange,
        label = { Text(ReportTr.Report.Configuration.name() + "*") },
        modifier = Modifier.fillMaxWidth(),
        isError = nameError != null,
        singleLine = true,
        supportingText = {
            if (nameError != null) Text(nameError, color = MaterialTheme.colorScheme.error)
            else if (isOverwriting) Text(ReportTr.Report.Configuration.overwriteWarning(), color = MaterialTheme.colorScheme.tertiary)
        }
    )
}

@Composable
private fun DialogActions(
    onDismiss: () -> Unit,
    onSaveClick: () -> Unit,
    isOverwriting: Boolean
) {
    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.End) {
        TextButton(onClick = onDismiss) { Text(ReportTr.Action.cancel()) }
        Spacer(modifier = Modifier.width(8.dp))
        Button(
            onClick = onSaveClick,
            colors = if (isOverwriting) ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.secondary) else ButtonDefaults.buttonColors()
        ) {
            Text(if (isOverwriting) ReportTr.Action.overwrite() else ReportTr.Action.save())
        }
    }
}