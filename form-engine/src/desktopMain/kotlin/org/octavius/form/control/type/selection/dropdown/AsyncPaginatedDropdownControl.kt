package org.octavius.form.control.type.selection.dropdown

import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.CoroutineScope
import org.octavius.form.control.base.ControlAction
import org.octavius.form.control.base.ControlContext
import org.octavius.form.control.base.ControlDependency
import org.octavius.form.control.base.ControlState
import org.octavius.localization.Tr
import org.octavius.theme.FormSpacing

/**
 * Abstrakcyjna baza dla kontrolek dropdown, które ładują dane asynchronicznie,
 * wspierają wyszukiwanie i paginację.
 */
abstract class AsyncPaginatedDropdownControl<T : Any>(
    label: String?,
    required: Boolean?,
    dependencies: Map<String, ControlDependency<*>>?,
    actions: List<ControlAction<T>>?
) : DropdownControlBase<T>(label, required, dependencies, actions) {

    /**
     * Podklasy implementują tę metodę, aby dostarczyć dane dla konkretnej strony.
     * Użycie `suspend` jest kluczowe dla operacji asynchronicznych.
     */
    protected abstract suspend fun loadPage(searchQuery: String, page: Long): Pair<List<DropdownOption<T>>, Long>

    @Composable
    override fun ColumnScope.RenderMenuItems(
        controlContext: ControlContext,
        scope: CoroutineScope,
        controlState: ControlState<T>,
        closeMenu: () -> Unit
    ) {
        var searchQuery by remember { mutableStateOf("") }
        var options by remember { mutableStateOf<List<DropdownOption<T>>>(emptyList()) }
        var isLoading by remember { mutableStateOf(false) }
        var currentPage by remember { mutableStateOf(0L) }
        var totalPages by remember { mutableStateOf(1L) }

        // Używamy klucza Unit, aby ten efekt uruchomił się tylko raz przy otwarciu menu
        // i był aktywny, dopóki jest ono otwarte. Zmiany searchQuery i currentPage
        // spowodują jego ponowne uruchomienie.
        LaunchedEffect(searchQuery, currentPage) {
            isLoading = true
            try {
                val (items, pages) = loadPage(searchQuery, currentPage)
                options = items
                totalPages = pages
            } finally {
                isLoading = false
            }
        }

        // 1. Pole wyszukiwania
        SearchField(
            searchQuery = searchQuery,
            onQueryChange = {
                searchQuery = it
                currentPage = 0 // Resetuj stronę po zmianie wyszukiwania
            }
        )

        // 2. Właściwa zawartość menu
        MenuContent(
            isLoading = isLoading,
            options = options,
            isRequired = required ?: false,
            totalPages = totalPages,
            currentPage = currentPage,
            onPageChange = { newPage -> currentPage = newPage },
            onOptionSelected = { selectedOption ->
                controlState.value.value = selectedOption?.value
                // Menu zna etykietę wybranej pozycji, więc zapisujemy ją od razu - nie ma powodu
                // pytać bazy o tekst, który właśnie trzymamy w ręku.
                controlState.displayText.value = selectedOption?.displayText
                executeActions(controlContext, selectedOption?.value, scope)
                closeMenu()
            }
        )
    }

    /**
     * Komponent renderujący pole wyszukiwania wewnątrz menu.
     */
    @Composable
    private fun SearchField(
        searchQuery: String,
        onQueryChange: (String) -> Unit
    ) {
        OutlinedTextField(
            value = searchQuery,
            onValueChange = onQueryChange,
            placeholder = { Text(Tr.Search.placeholder()) },
            singleLine = true,
            modifier = Modifier
                .fillMaxWidth()
                .padding(
                    horizontal = FormSpacing.dropdownPaddingHorizontal,
                    vertical = FormSpacing.dropdownPaddingVertical
                ),
            leadingIcon = {
                Icon(
                    imageVector = Icons.Default.Search,
                    contentDescription = Tr.Search.search()
                )
            },
            trailingIcon = {
                if (searchQuery.isNotEmpty()) {
                    IconButton(onClick = { onQueryChange("") }) {
                        Icon(
                            imageVector = Icons.Default.Clear,
                            contentDescription = Tr.Search.clear()
                        )
                    }
                }
            }
        )
    }

    /**
     * Komponent renderujący główną zawartość menu (wskaźnik ładowania lub listę opcji i paginację).
     */
    @Composable
    private fun MenuContent(
        isLoading: Boolean,
        options: List<DropdownOption<T>>,
        isRequired: Boolean,
        currentPage: Long,
        totalPages: Long,
        onPageChange: (Long) -> Unit,
        onOptionSelected: (DropdownOption<T>?) -> Unit
    ) {
        if (isLoading) {
            LoadingIndicator()
        } else {
            OptionsList(
                options = options,
                isRequired = isRequired,
                onOptionSelected = onOptionSelected
            )

            if (totalPages > 1) {
                PaginationPanel(
                    currentPage = currentPage,
                    totalPages = totalPages,
                    onPageChange = onPageChange
                )
            }
        }
    }

    /**
     * Komponent renderujący listę opcji do wyboru.
     */
    @Composable
    private fun OptionsList(
        options: List<DropdownOption<T>>,
        isRequired: Boolean,
        onOptionSelected: (DropdownOption<T>?) -> Unit
    ) {
        // Opcja "null" (brak wyboru), tylko jeśli kontrolka nie jest wymagana
        if (!isRequired) {
            DropdownMenuItem(
                text = { Text(Tr.Form.Dropdown.noSelection()) },
                onClick = { onOptionSelected(null) }
            )
            HorizontalDivider()
        }

        if (options.isEmpty()) {
            DropdownMenuItem(
                enabled = false,
                text = {
                    Text(
                        Tr.Form.Dropdown.noResults(),
                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                    )
                },
                onClick = {}
            )
        } else {
            options.forEach { option ->
                DropdownMenuItem(
                    text = { Text(option.displayText) },
                    onClick = { onOptionSelected(option) }
                )
            }
        }
    }

    /**
     * Komponent renderujący panel paginacji.
     */
    @Composable
    private fun PaginationPanel(
        currentPage: Long,
        totalPages: Long,
        onPageChange: (Long) -> Unit
    ) {
        HorizontalDivider(modifier = Modifier.padding(vertical = FormSpacing.itemSpacing))
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(
                    horizontal = FormSpacing.dropdownPaddingHorizontal,
                    vertical = FormSpacing.dropdownPaddingVertical
                ),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconButton(
                onClick = { onPageChange(currentPage - 1) },
                enabled = currentPage > 0
            ) {
                Icon(
                    imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                    contentDescription = Tr.Pagination.previousPage()
                )
            }

            Text(Tr.Pagination.page() + " ${currentPage + 1} " + Tr.Pagination.of() + " $totalPages")

            IconButton(
                onClick = { onPageChange(currentPage + 1) },
                enabled = currentPage < totalPages - 1
            ) {
                Icon(
                    imageVector = Icons.AutoMirrored.Filled.ArrowForward,
                    contentDescription = Tr.Pagination.nextPage()
                )
            }
        }
    }

    /**
     * Komponent renderujący wskaźnik ładowania.
     */
    @Composable
    private fun LoadingIndicator() {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .padding(FormSpacing.sectionPadding),
            contentAlignment = Alignment.Center
        ) {
            CircularProgressIndicator(modifier = Modifier.size(24.dp))
        }
    }
}