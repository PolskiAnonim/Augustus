package org.octavius.form.control.type.selection.dropdown

import androidx.compose.foundation.ScrollState
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.CoroutineScope
import org.octavius.form.control.base.*
import org.octavius.form.control.layout.RenderNormalLabel
import org.octavius.localization.Tr

/**
 * Super-bazowa klasa dla kontrolek dropdown.
 * Odpowiada wyłącznie za renderowanie ramki UI (ExposedDropdownMenuBox)
 * i deleguje renderowanie zawartości menu do podklas.
 */
abstract class DropdownControlBase<T : Any>(
    label: String?,
    required: Boolean?,
    dependencies: Map<String, ControlDependency<*>>?,
    actions: List<ControlAction<T>>?
) : Control<T>(
    label,
    required,
    dependencies,
    hasStandardLayout = false,
    actions = actions
) {

    /**
     * Wyznacza tekst dla wybranej wartości. Wołane **raz na wartość**, poza ścieżką renderowania,
     * a wynik ląduje w `ControlState.displayText` - dlatego wolno tu odpytać bazę albo API.
     *
     * Zwrócenie `null` znaczy "nie ma czego pokazać" (np. wiersz zniknął spod zapisanego id);
     * kontrolka pokaże wtedy tekst zastępczy.
     */
    protected abstract suspend fun resolveDisplayText(value: T): String?

    /**
     * Podklasy muszą zaimplementować tę metodę, aby wyrenderować
     * zawartość menu rozwijanego.
     *
     * Dostają cały [ControlState], nie samą wartość, bo wybór pozycji ustawia wartość **i** tekst -
     * menu zna już etykietę, więc nie ma powodu wyznaczać jej drugi raz.
     *
     * [menuScrollState] jest stanem przewijania samego menu. Tworzy go ta klasa, bo to ona wywołuje
     * `ExposedDropdownMenu`, ale czyta go podklasa doładowująca kolejne strony - stąd w parametrze.
     * Podklasa z pełną listą opcji po prostu go ignoruje.
     */
    @Composable
    protected abstract fun ColumnScope.RenderMenuItems(
        controlContext: ControlContext,
        scope: CoroutineScope,
        controlState: ControlState<T>,
        menuScrollState: ScrollState,
        closeMenu: () -> Unit
    )

    @OptIn(ExperimentalMaterial3Api::class)
    @Composable
    override fun Display(controlContext: ControlContext, controlState: ControlState<T>, isRequired: Boolean) {
        var expanded by remember { mutableStateOf(false) }
        val scope = rememberCoroutineScope()

        // Wartość mamy z bazy, tekstu do niej nie - i jego wyznaczenie potrafi kosztować zapytanie
        // albo round-trip po sieci, więc nie może się dziać przy każdej rekompozycji. Rozwiązujemy
        // go raz, tutaj, a wynik siedzi w stanie kluczowanym ścieżką, czyli osobno dla każdego
        // wiersza repeatable.
        //
        // Trzy stany tekstu: `null` - jeszcze nierozwiązany, `""` - rozwiązany i nie ma czego
        // pokazać (wiersz zniknął spod zapisanego id), reszta - etykieta. Bez tego rozróżnienia
        // nieudane rozwiązanie zostawiałoby pole na "ładowanie" na zawsze.
        val resolvedText = controlState.displayText.value
        val unresolved = controlState.value.value != null && resolvedText == null

        LaunchedEffect(controlState.value.value, resolvedText) {
            val value = controlState.value.value
            if (value != null && controlState.displayText.value == null) {
                // Rozwiązanie etykiety dzieje się bez udziału użytkownika, przy samym otwarciu
                // formularza, i nie ma gdzie pokazać błędu - menu jest zamknięte. Zamiast wysypać
                // aplikację albo zasypać ją dialogami przy kilku wierszach naraz, zostawiamy tekst
                // zastępczy; komunikat zobaczy dopiero po otwarciu listy.
                controlState.displayText.value = try {
                    resolveDisplayText(value) ?: ""
                } catch (e: CancellationException) {
                    throw e
                } catch (e: Exception) {
                    ""
                }
            }
        }

        Column(modifier = Modifier.fillMaxWidth()) {
            RenderNormalLabel(label, isRequired)

            ExposedDropdownMenuBox(
                expanded = expanded,
                onExpandedChange = { expanded = it },
                modifier = Modifier.fillMaxWidth()
            ) {
                OutlinedTextField(
                    value = resolvedText?.takeIf { it.isNotEmpty() } ?: when {
                        unresolved -> Tr.App.loading()
                        !isRequired -> Tr.Form.Dropdown.noSelection()
                        else -> Tr.Form.Dropdown.selectOption()
                    },
                    onValueChange = {},
                    readOnly = true,
                    trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expanded) },
                    modifier = Modifier
                        .fillMaxWidth()
                        .menuAnchor(ExposedDropdownMenuAnchorType.PrimaryNotEditable)
                )

                // Menu z opcjami. Stan przewijania trzymamy sami, bo doładowywanie kolejnych stron
                // jedzie z pozycji przewijania menu - samo menu już się przewija, więc nie ma po co
                // wkładać w nie drugiej listy przewijalnej.
                val menuScrollState = rememberScrollState()

                ExposedDropdownMenu(
                    expanded = expanded,
                    onDismissRequest = { expanded = false },
                    scrollState = menuScrollState
                ) {
                    RenderMenuItems(
                        controlContext = controlContext,
                        scope = scope,
                        controlState = controlState,
                        menuScrollState = menuScrollState,
                        closeMenu = { expanded = false }
                    )
                }
            }
            DisplayFieldErrors(controlContext)
        }
    }
}
