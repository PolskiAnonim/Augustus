package org.octavius.form.control.type.selection.dropdown

import androidx.compose.foundation.ScrollState
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.delay
import org.octavius.form.control.base.ControlAction
import org.octavius.form.control.base.ControlContext
import org.octavius.form.control.base.ControlDependency
import org.octavius.form.control.base.ControlState
import org.octavius.localization.Tr
import org.octavius.theme.FormSpacing

/**
 * O ile pikseli przed końcem menu zaczynamy doczytywać następną stronę.
 * Mniej więcej dwie pozycje, żeby lista dojechała zanim użytkownik zobaczy koniec.
 */
private const val LoadMoreThresholdPx = 100

/**
 * Pozycja w stronicowaniu dla jednego wyszukiwania, wymieniana w całości.
 *
 * Trzymanie tego jako jednego obiektu, a nie czterech osobnych zmiennych stanu, jest tu celowe:
 * ładowanie pierwszej strony i doczytywanie kolejnych to dwa niezależne efekty, a przy osobnych
 * zmiennych ten drugi mógłby wystartować z numerem strony należącym jeszcze do poprzedniego
 * wyszukiwania. [query] rozstrzyga, do czego ta pozycja należy - dopóki nie zgadza się z tekstem
 * w polu wyszukiwania, doczytywanie jest wyłączone i nie zależy to od kolejności startu efektów.
 *
 * `null` w [query] to stan początkowy, który nie pasuje do żadnego wyszukiwania - łącznie z pustym.
 */
private data class Paging<T>(
    val query: String? = null,
    val options: List<DropdownOption<T>> = emptyList(),
    val nextPage: Long = 0,
    val hasMore: Boolean = false
)

/**
 * Abstrakcyjna baza dla kontrolek dropdown, które ładują dane asynchronicznie,
 * wspierają wyszukiwanie i doczytywanie kolejnych stron przy przewijaniu.
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
    protected abstract suspend fun loadPage(searchQuery: String, page: Long): DropdownPage<T>

    /**
     * Ile odczekać po ostatnim wpisanym znaku, zanim poleci zapytanie.
     *
     * Domyślnie zero, bo lokalna baza odpowiada szybciej, niż trwałoby czekanie - debounce tylko
     * opóźniałby wynik, który i tak zdąży. Nadpisuje go źródło, gdzie każde zapytanie kosztuje:
     * API po sieci liczy się w sekundach i potrafi odpowiedzieć 429.
     */
    protected open val debounceMs: Long = 0

    @Composable
    override fun ColumnScope.RenderMenuItems(
        controlContext: ControlContext,
        scope: CoroutineScope,
        controlState: ControlState<T>,
        menuScrollState: ScrollState,
        closeMenu: () -> Unit
    ) {
        var searchQuery by remember { mutableStateOf("") }
        var paging by remember { mutableStateOf(Paging<T>()) }
        var isLoadingMore by remember { mutableStateOf(false) }
        var loadError by remember { mutableStateOf<String?>(null) }

        // Czy pozycja w stronicowaniu opisuje to, co jest teraz wpisane w wyszukiwarce.
        // Dopóki nie opisuje, pokazujemy ładowanie i nie doczytujemy kolejnych stron.
        val isCurrent = paging.query == searchQuery

        // Pierwsza strona, i przeładowanie od zera przy każdej zmianie wyszukiwania.
        // Efekt anuluje się przy kolejnym znaku, więc wyniki nie wracają w złej kolejności.
        // To samo anulowanie robi z opóźnienia debounce: kolejny znak w trakcie `delay` zabija
        // czekający efekt, zanim zapytanie w ogóle wyjdzie. Puste pole ładuje od razu.
        LaunchedEffect(searchQuery) {
            if (searchQuery.isNotEmpty() && debounceMs > 0) delay(debounceMs)
            loadError = null
            try {
                val page = loadPage(searchQuery, 0)
                menuScrollState.scrollTo(0)
                paging = Paging(searchQuery, page.options, nextPage = 1, hasMore = page.hasMore)
            } catch (e: CancellationException) {
                throw e
            } catch (e: Exception) {
                // Komunikat w menu, a nie dialog: przy źródle po sieci nieudane zapytanie zdarza
                // się w trakcie pisania, a modal zamykałby wtedy formularz pod użytkownikiem.
                loadError = e.message ?: e.toString()
            }
        }

        // Doczytywanie kolejnych stron z pozycji przewijania menu.
        //
        // `maxValue == 0` znaczy, że zawartość nie wypełniła jeszcze menu - wtedy też dobieramy,
        // bo inaczej krótka pierwsza strona nie daje czym przewinąć i lista utyka na starcie.
        LaunchedEffect(menuScrollState, searchQuery) {
            snapshotFlow { Triple(menuScrollState.value, menuScrollState.maxValue, paging) }
                .collect { (position, maxPosition, current) ->
                    val atEnd = maxPosition == 0 || position >= maxPosition - LoadMoreThresholdPx
                    if (current.query == searchQuery && current.hasMore && atEnd && !isLoadingMore) {
                        isLoadingMore = true
                        try {
                            val page = loadPage(searchQuery, current.nextPage)
                            paging = current.copy(
                                options = current.options + page.options,
                                nextPage = current.nextPage + 1,
                                hasMore = page.hasMore
                            )
                        } catch (e: CancellationException) {
                            throw e
                        } catch (e: Exception) {
                            loadError = e.message ?: e.toString()
                            // Gasimy doczytywanie. Pozycja przewijania się nie zmieniła, więc ten sam
                            // warunek spełniłby się natychmiast i lecielibyśmy w kółko tym samym
                            // zapytaniem. Wznowi je dopiero zmiana wyszukiwania.
                            paging = current.copy(hasMore = false)
                        } finally {
                            isLoadingMore = false
                        }
                    }
                }
        }

        // 1. Pole wyszukiwania
        SearchField(
            searchQuery = searchQuery,
            onQueryChange = { searchQuery = it }
        )

        // 2. Właściwa zawartość menu
        // Póki nowe wyniki nie przyszły, zostają widoczne poprzednie ze wskaźnikiem pod spodem.
        // Przy lokalnej bazie różnicy nie widać, ale przy zapytaniu po sieci lista znikałaby na
        // kilka sekund przy każdym znaku. Duży wskaźnik zamiast listy tylko wtedy, gdy nie ma
        // czego pokazać. Doczytywanie z nieaktualnej pozycji i tak jest wyłączone (patrz wyżej).
        MenuContent(
            options = paging.options,
            isRequired = required ?: false,
            isLoadingFirstPage = !isCurrent && paging.options.isEmpty() && loadError == null,
            isLoadingMore = isLoadingMore || (!isCurrent && paging.options.isNotEmpty() && loadError == null),
            errorMessage = loadError,
            onOptionSelected = { selectedOption ->
                controlState.value.value = selectedOption?.value
                // Menu zna etykietę wybranej pozycji, więc zapisujemy ją od razu - nie ma powodu
                // pytać bazy o tekst, który właśnie trzymamy w ręku.
                controlState.displayText.value = selectedOption?.displayText
                // Payload jest tylko tutaj, przy wyborze z menu. Akcja typu "wypełnij resztę
                // z wybranego wiersza" nie może dostać go przy wczytaniu formularza - nadpisałaby
                // wtedy zapisane dane tym, co akurat jest w słowniku.
                executeActions(controlContext, selectedOption?.value, scope, payload = selectedOption?.payload)
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
     * Komponent renderujący główną zawartość menu.
     *
     * Ładowanie pierwszej strony zastępuje listę, doczytywanie kolejnej dokłada wskaźnik pod nią -
     * inaczej lista znikałaby przy każdym dobraniu porcji.
     */
    @Composable
    private fun MenuContent(
        options: List<DropdownOption<T>>,
        isRequired: Boolean,
        isLoadingFirstPage: Boolean,
        isLoadingMore: Boolean,
        errorMessage: String?,
        onOptionSelected: (DropdownOption<T>?) -> Unit
    ) {
        if (errorMessage != null) {
            DropdownMenuItem(
                enabled = false,
                text = { Text(errorMessage, color = MaterialTheme.colorScheme.error) },
                onClick = {}
            )
            HorizontalDivider()
        }

        if (isLoadingFirstPage) {
            LoadingIndicator()
            return
        }

        OptionsList(
            options = options,
            isRequired = isRequired,
            onOptionSelected = onOptionSelected
        )

        if (isLoadingMore) {
            LoadingIndicator()
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
