package org.octavius.form.component

import androidx.compose.runtime.mutableStateMapOf
import androidx.compose.runtime.mutableStateOf
import org.octavius.form.control.base.*

/**
 * Reaktywne zarządzanie stanem wszystkich kontrolek formularza.
 *
 * FormState przechowuje i zarządza stanami wszystkich kontrolek w formularzu,
 * wykorzystując Compose State API dla automatycznej rekomposycji UI przy zmianach.
 *
 * Główne funkcje:
 * - Przechowywanie reaktywnych stanów kontrolek (MutableState)
 * - Automatyczne wyzwalanie rekomposycji przy zmianach
 * - Zarządzanie hierarchicznymi nazwami kontrolek (np. dla RepeatableControl)
 * - Zbieranie danych z wszystkich kontrolek do przetworzenia
 *
 * Stany kontrolek są indeksowane pełną ścieżką (np. "publications[uuid].title"),
 * co umożliwia obsługę złożonych, zagnieżdżonych struktur.
 */
class FormState {

    /**
     * Flaga ładowania danych formularza (initData).
     * True podczas początkowego pobierania danych z bazy.
     */
    val isLoading = mutableStateOf(true)

    /**
     * Flaga wysyłania formularza (triggerAction).
     * True podczas wykonywania akcji (np. zapis, usunięcie).
     */
    val actionTriggered = mutableStateOf(false)

    /**
     * Stan kontrolek formularza - reactive map która automatycznie triggeruje recomposition
     */
    private val _controlStates = mutableStateMapOf<String, ControlState<*>>()

    /**
     * Funkcja zwraca stan kontrolki o danej nazwie
     */
    fun getControlState(name: String): ControlState<*>? = _controlStates[name]

    /**
     * Funkcja zwraca stany wszystkich kontrolek formularza
     */
    fun getAllStates(): Map<String, ControlState<*>> = _controlStates

    /**
     * Inicjalizuje stany wszystkich kontrolek na podstawie wartości początkowych.
     *
     * @param schema Schemat formularza z definicjami kontrolek.
     * @param initValues Mapa wartości początkowych (klucz = nazwa kontrolki).
     */
    internal fun initializeStates(schema: FormSchema, initValues: Map<String, Any?>) {
        initializeLevel(schema.rootContexts(), initValues)
    }

    /**
     * Tworzy stany kontrolek jednego poziomu danych - formularza, sekcji albo wiersza repeatable -
     * z wartości pod ich nazwami. Kontenery zakładają w `setInitValue` swoje poziomy same, tą samą funkcją.
     *
     * Pola sekcji przychodzą pod ścieżkami (`"basic_info/name"`), bo tak oddaje je `loadData` i tylko
     * wtedy łączenie wartości domyślnych, z bazy i z payloadu zwykłym `+` nie gubi pól.
     */
    internal fun initializeLevel(controls: List<Pair<ControlContext, Control<*>>>, values: Map<String, Any?>) {
        val valuesByName = valuesByName(values)
        controls.forEach { (controlContext, control) ->
            _controlStates[controlContext.fullStatePath] = control.setInitValue(controlContext, valuesByName[controlContext.localName])
        }
    }

    // Grupuje ścieżki po pierwszym segmencie: "basic_info/name" trafia do mapy "basic_info" jako "name".
    // Głębsze segmenty rozłoży kolejny poziom, gdy sekcja przekaże mu swoją mapę.
    private fun valuesByName(values: Map<String, Any?>): Map<String, Any?> {
        return values.entries.groupBy { it.key.substringBefore(PathResolver.SEPARATOR) }.mapValues { (name, entries) ->
            val ownValue = entries.singleOrNull { it.key == name }
            // Żadna kontrolka nie trzyma mapy, więc mapa pod gołą nazwą to sekcja podana w całości.
            // `+` na takich mapach podmienia całą sekcję, czyli po cichu gubi pola - stąd tylko ścieżki.
            require(ownValue?.value !is Map<*, *>) {
                "Wartość początkowa pod '$name' jest mapą - pola sekcji podaje się ścieżkami, np. '$name/pole'"
            }
            when {
                ownValue == null -> entries.associate { it.key.substringAfter(PathResolver.SEPARATOR) to it.value }
                entries.size == 1 -> ownValue.value
                else -> throw IllegalArgumentException("Wartość początkowa jest i pod '$name', i pod ścieżkami w '$name/...'")
            }
        }
    }

    /**
     * Funkcja ustawia stan kontrolki o danej nazwie (może być hierarchiczna)
     * Automatycznie triggeruje recomposition dzięki mutableStateMapOf
     */
    internal fun setControlState(name: String, state: ControlState<*>) {
        _controlStates[name] = state
    }

    /**
     * Funkcja usuwa stan kontrolki o danej nazwie
     * Automatycznie triggeruje recomposition dzięki mutableStateMapOf
     */
    internal fun removeControlState(name: String) {
        _controlStates.remove(name)
    }

    /**
     * Funkcja usuwa wszystkie stany kontrolek zaczynające się od danego prefiksu
     * Automatycznie triggeruje recomposition dzięki mutableStateMapOf
     */
    internal fun removeControlStatesWithPrefix(prefix: String) {
        val keysToRemove = _controlStates.keys.filter { it.startsWith(prefix) }
        keysToRemove.forEach { _controlStates.remove(it) }
    }

    /**
     * Zbiera i przetwarza dane ze wszystkich kontrolek formularza.
     *
     * @param schema Schemat formularza z definicjami kontrolek.
     * @return FormResultData - mapa wyników (klucz = nazwa kontrolki, wartość = ControlResultData).
     */
    internal fun collectFormData(schema: FormSchema): FormResultData {
        return collectLevel(schema.rootContexts())
    }

    /**
     * Zbiera wyniki kontrolek jednego poziomu danych pod ich nazwami. Kontenery zbierają swoje poziomy
     * same, tą samą funkcją: sekcja raz, repeatable per wiersz. Każda kontrolka dostaje kontekst
     * z rodzicem, bo dopiero on mówi, czy jest widoczna - a niewidoczna nie przekazuje wartości do zapisu.
     */
    internal fun collectLevel(controls: List<Pair<ControlContext, Control<*>>>): FormResultData {
        return controls.associate { (controlContext, control) ->
            controlContext.localName to control.getResult(controlContext, _controlStates.getValue(controlContext.fullStatePath))
        }
    }
}