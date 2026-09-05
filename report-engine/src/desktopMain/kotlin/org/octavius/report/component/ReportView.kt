package org.octavius.report.component

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.Icon
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.CoroutineScope
import org.octavius.report.ReportEvent
import org.octavius.report.configuration.ReportConfigurationDialog
import org.octavius.report.management.ColumnManagementPanel
import org.octavius.report.ui.PaginationComponent
import org.octavius.report.ui.ReportSearchBar
import org.octavius.report.ui.table.ReportTable

/**
 * UI raportu - renderuje tabelę, filtry i paginację na podstawie [ReportHandler].
 *
 * Wcześniej była to klasa `ReportScreen` implementująca `org.octavius.navigation.Screen` - to było
 * jedyne powiązanie report-engine z nawigacją. Tytuł ekranu należy do nawigacji, nie do silnika,
 * więc moduł znający nawigację opakowuje to wywołaniem
 * `ComponentScreen(title) { ReportView(reportHandler) }`.
 */
@Composable
fun ReportView(reportHandler: ReportHandler) {
    val state by reportHandler.state.collectAsState()

    DisposableEffect(Unit) {
        reportHandler.onEvent(ReportEvent.Initialize)
        onDispose {
            reportHandler.cancelJobs()
        }
    }

    val uiState = rememberReportUIState()

    // Przy zmianie danych
    LaunchedEffect(state.data) {
        // Przewiń listę na samą górę (do elementu o indeksie 0)
        uiState.lazyListState.animateScrollToItem(0)
    }

    CompositionLocalProvider(LocalReportHandler provides reportHandler) {
        Scaffold(
            topBar = {
                ColumnManagementPanel(
                    reportState = state,
                    modifier = Modifier.padding(8.dp)
                )
            },
            bottomBar = {
                PaginationComponent(state.pagination, onEvent = reportHandler::onEvent)
            }
        ) { innerPadding ->
            Column(modifier = Modifier.padding(innerPadding)) {
                // Pasek wyszukiwania
                ReportSearchBar(
                    searchQuery = state.searchQuery,
                    onSearchChange = { query ->
                        reportHandler.onEvent(ReportEvent.SearchQueryChanged(query))
                    },
                    onAddMenuClick = { uiState.addMenuExpanded.value = !uiState.addMenuExpanded.value },
                    addMenuExpanded = uiState.addMenuExpanded.value,
                    onAddMenuDismiss = { uiState.addMenuExpanded.value = false },
                    addMenuContent = {
                        val addActions = reportHandler.reportStructure.mainActions
                        addActions.forEach { action ->
                            DropdownMenuItem(
                                text = { Text(action.label) },
                                onClick = {
                                    action.action.invoke()
                                    uiState.addMenuExpanded.value = false // Zamknij menu po kliknięciu
                                },
                                leadingIcon = action.icon?.let {
                                    { Icon(it, contentDescription = action.label) }
                                }

                            )
                        }
                    },
                    onConfigurationClick = { uiState.configurationDialogVisible.value = true }
                )
                ReportTable(state, uiState.lazyListState)
            }
        }
    }

    // Dialog konfiguracji
    if (uiState.configurationDialogVisible.value) {
        ReportConfigurationDialog(
            onEvent = reportHandler::onEvent,
            reportName = reportHandler.reportStructure.reportName,
            reportState = reportHandler.state.value,
            onDismiss = { uiState.configurationDialogVisible.value = false },
        )
    }
}

@Composable
private fun rememberReportUIState(): ReportUIState {
    return ReportUIState(
        lazyListState = rememberLazyListState(),
        coroutineScope = rememberCoroutineScope(),
        addMenuExpanded = remember { mutableStateOf(false) },
        configurationDialogVisible = remember { mutableStateOf(false) }
    )
}

private data class ReportUIState(
    val lazyListState: LazyListState,
    val coroutineScope: CoroutineScope,
    val addMenuExpanded: MutableState<Boolean>,
    val configurationDialogVisible: MutableState<Boolean>
)
