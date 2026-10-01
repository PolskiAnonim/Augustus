package org.octavius.form.control.base

import androidx.compose.runtime.MutableState
import androidx.compose.runtime.mutableStateOf
import org.octavius.form.component.PathResolver

/**
 * Wynik przetwarzania pojedynczej kontrolki formularza.
 *
 * Zawiera zarówno bieżącą wartość (zmodyfikowaną przez użytkownika)
 * jak i wartość początkową (z bazy lub domyślną) dla porównań.
 *
 * @param currentValue Bieżąca wartość kontrolki przetworzona przez getResult.
 * @param initialValue Pierwotna wartość załadowana z bazy lub ustawiona domyślnie.
 */
data class ControlResultData(
    val currentValue: Any?,
    val initialValue: Any?
)

/**
 * Reaktywny stan pojedynczej kontrolki formularza.
 *
 * Przechowuje wszystkie informacje o stanie kontrolki potrzebne
 * do renderowania, walidacji i śledzenia zmian, wykorzystując
 * Compose State API dla automatycznej rekomposycji.
 *
 * @param T Typ danych przechowywanych przez kontrolkę.
 * @param value Bieżąca wartość kontrolki (edytowana przez użytkownika) - MutableState.
 * @param initValue Pierwotna wartość załadowana z bazy lub ustawiona domyślnie - MutableState.
 * @param displayText Tekst pokazywany użytkownikowi, gdy nie da się go wyprowadzić z [value] przy
 *                    każdym renderowaniu: bufor edycyjny pola liczbowego (`1.` nie jest jeszcze
 *                    liczbą) albo etykieta wybranej pozycji dropdowna (wymaga zapytania).
 *                    `null` znaczy "nieustalony" - kontrolka wypełni go sama, synchronicznie albo
 *                    zapytaniem. Dlatego `updateControl` z zewnątrz zeruje to pole: zapisanie
 *                    wartości unieważnia tekst, a samo zerowanie jest sygnałem do przeliczenia.
 * @param labelOverride Etykieta nadpisana z akcji, zamiast tej ze schematu.
 */
data class ControlState<T>(
    val value: MutableState<T?> = mutableStateOf(null),
    val initValue: MutableState<T?> = mutableStateOf(null),
    val displayText: MutableState<String?> = mutableStateOf(null),
    val labelOverride: MutableState<String?> = mutableStateOf(null)
)

/**
 * Kontekst renderowania i stanu kontrolki w hierarchii formularza.
 *
 * @param localName Nazwa kontrolki w jej bezpośrednim kontekście (np. "firstName", "publications").
 * @param statePath Ścieżka do kontenera nadrzędnego (np. "user", "publications[uuid]") dla stanu.
 * @param controlPath Ścieżka do kontenera nadrzędnego (np. "user", "publications") dla kontrolek
 * @param parent  Kontekst renderowania nadrzędnej kontrolki. Umożliwia nawigację w górę drzewa.
 */
data class ControlContext(
    val localName: String,
    val statePath: String = "",
    val controlPath: String = "",
    val parent: ControlContext? = null
) {
    val fullControlPath: String by lazy {
        if (controlPath.isEmpty()) localName else "$controlPath/$localName"
    }

    val fullStatePath: String by lazy {
        if (statePath.isEmpty()) localName else "$statePath/$localName"
    }

    /**
     * Tworzy kontekst dla kontrolki-dziecka w sekcji. Sekcja to jeden poziom danych - jak wiersz
     * repeatable, tylko bez `[rowId]` - więc dziecko leży pod `sekcja/nazwa`.
     */
    fun forSectionChild(childLocalName: String): ControlContext {
        return ControlContext(
            localName = childLocalName,
            statePath = fullStatePath,
            controlPath = fullControlPath,
            parent = this
        )
    }

    /**
     * Tworzy kontekst dla kontrolki-dziecka w kontenerze powtarzalnym (Repeatable).
     */
    fun forRepeatableChild(childLocalName: String, rowId: String): ControlContext {
        return ControlContext(
            localName = childLocalName,
            statePath = "$fullStatePath[$rowId]",
            controlPath = fullControlPath,
            parent = this
        )
    }
}

/**
 * Alias dla mapy zawierającej wyniki wszystkich kontrolek formularza.
 *
 * Kluczem jest nazwa kontrolki, wartością ControlResultData z bieżącą
 * i początkową wartością. Używane jako główny typ danych przekazywany
 * do logiki walidacji, zapisu i akcji formularza.
 *
 * Sekcja to osobny poziom: jej wynik niesie mapę wyników dzieci, więc pole w sekcji
 * czyta się ścieżką, np. `getCurrent("basic_info/name")`.
 */
typealias FormResultData = Map<String, ControlResultData>

/**
 * Pobiera bieżącą wartość kontrolki jako określony typ `T`.
 * Obsługuje rzutowanie na typ T
 *
 * Zastępuje: `formData["name"]!!.currentValue as String`
 * Użycie: `formData.getCurrentAs<String>("basic_info/name")`
 */
inline fun <reified T> FormResultData.getCurrentAs(path: String): T {
    return getCurrent(path) as T
}

/**
 * Pobiera bieżącą wartość kontrolki jako Any?. Pole ukrytej sekcji ma `null`, jak każda ukryta kontrolka.
 *
 * Zastępuje: `formData["name"]!!.currentValue`
 */
fun FormResultData.getCurrent(path: String): Any? {
    return resultAt(path) { it.currentValue }?.currentValue
}

/**
 * Pobiera początkową wartość kontrolki jako określony typ `T`.
 *
 * Zastępuje: `rowData["id"]!!.initialValue!! as Int`
 * Użycie: `rowData.getInitialAs<Int>("id")`
 */
inline fun <reified T> FormResultData.getInitialAs(path: String): T {
    return getInitial(path) as T
}

/**
 * Pobiera początkową wartość kontrolki jako Any?. Ukrycie nie zmienia wartości początkowej,
 * także w ukrytej sekcji.
 *
 * Zastępuje: `rowData["id"]!!.initialValue`
 * Użycie: `rowData.getInitial("id")`
 */
fun FormResultData.getInitial(path: String): Any? {
    return resultAt(path) { it.initialValue }?.initialValue
}

// Schodzi po ścieżce przez sekcje, w każdej biorąc mapę wyników dzieci z wartości wskazanej przez
// [levelOf]. `null` znaczy, że po drodze była ukryta sekcja, a jej pola nie mają bieżącej wartości.
private fun FormResultData.resultAt(path: String, levelOf: (ControlResultData) -> Any?): ControlResultData? {
    val names = path.split(PathResolver.SEPARATOR)
    var level: FormResultData = this
    for (name in names.dropLast(1)) {
        @Suppress("UNCHECKED_CAST")
        level = levelOf(level.resultNamed(name, path)) as FormResultData? ?: return null
    }
    return level.resultNamed(names.last(), path)
}

private fun FormResultData.resultNamed(name: String, path: String): ControlResultData {
    return this[name] ?: throw IllegalArgumentException("Brak kontrolki '$name' w wynikach formularza (ścieżka '$path')")
}