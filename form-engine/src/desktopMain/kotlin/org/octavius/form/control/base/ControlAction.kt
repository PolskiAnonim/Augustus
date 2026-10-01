package org.octavius.form.control.base

import kotlinx.coroutines.CoroutineScope
import org.octavius.form.component.ErrorManager
import org.octavius.form.component.FormActionTrigger
import org.octavius.form.component.FormSchema
import org.octavius.form.component.FormState
import org.octavius.form.component.PathResolver

/**
 * Definiuje akcję do wykonania po zmianie wartości kontrolki.
 *
 * @param T typ wartości kontrolki wyzwalającej akcję.
 * @param action Lambda, która zostanie wykonana. Otrzymuje ActionContext jako receiver (`this`).
 */
class ControlAction<T>(
    val executeOnInit: Boolean = false,
    val action: suspend ActionContext<T>.() -> Unit
)

/**
 * Kontekst dostarczany do ControlAction, dający dostęp do stanu formularza i narzędzi.
 *
 * @param T typ wartości kontrolki, która wyzwoliła akcję.
 * @property sourceValue Nowa wartość kontrolki, która wyzwoliła akcję.
 * @property sourceControlContext Kontekst kontrolki, która wyzwoliła akcję.
 * @property formState Dostęp do globalnego stanu formularza.
 * @property formSchema Dostęp do schemy formularza.
 * @property errorManager Dostęp do managera błędów.
 * @property coroutineScope Scope do uruchamiania operacji asynchronicznych (np. API).
 */
data class ActionContext<T>(
    val sourceValue: T?,
    val sourceControlContext: ControlContext,
    val formState: FormState,
    val formSchema: FormSchema,
    val errorManager: ErrorManager,
    val trigger: FormActionTrigger,
    val coroutineScope: CoroutineScope,
    val payload: Any? = null // Dodatkowe dane, przykładowo dla kontrolek dropdown dodatkowa wartość
) {
    /**
     * Czyta bieżącą wartość kontrolki spod ścieżki względnej (./, ../) lub bezwzględnej.
     *
     * Czytająca połowa pary z [updateControl]. Potrzebna wszędzie tam, gdzie akcja nie podmienia
     * wartości, tylko ją uzupełnia - na przykład dokłada do listy tytuły z kolejnego źródła,
     * zamiast kasować to, co już w niej jest.
     *
     * Zwraca `null`, gdy kontrolka nie ma wartości. Ścieżka bez kontrolki rzuca wyjątek.
     */
    fun <V : Any> readControl(controlPath: String): V? {
        // Rzutowanie "unsafe" tak samo jak w updateControl - za zgodność typu odpowiada programista.
        @Suppress("UNCHECKED_CAST")
        return stateAt(controlPath).value.value as V?
    }

    /**
     * Aktualizuje wartość kontrolki używając ścieżki względnej (./, ../) lub bezwzględnej.
     */
    fun <V: Any> updateControl(controlPath: String, newValue: V?) {
        setValue(stateAt(controlPath), newValue)
    }

    /**
     * Aktualizuje wartości wielu kontrolek pasujących do wzorca (np. z wildcardem *).
     * Wzorzec może nie trafić w nic - lista wierszy bywa pusta. Ścieżka bez wildcardu działa
     * jak [updateControl], więc bez kontrolki rzuca wyjątek.
     */
    fun <V: Any> updateControls(controlPath: String, newValue: V?) {
        if ("*" !in controlPath) return updateControl(controlPath, newValue)

        PathResolver.resolvePaths(controlPath, sourceControlContext, formState).forEach { resolvedName ->
            setValue(formState.getControlState(resolvedName)!!, newValue)
        }
    }

    /**
     * Aktualizuje etykietę kontrolki (nadpisuje domyślną).
     */
    fun updateLabel(controlPath: String, newLabel: String?) {
        stateAt(controlPath).labelOverride.value = newLabel
    }

    // Zła ścieżka to błąd w schemacie - po cichu pominięta akcja ukryłaby go na zawsze.
    private fun stateAt(controlPath: String): ControlState<*> {
        val resolvedName = PathResolver.resolvePath(controlPath, sourceControlContext)
        return formState.getControlState(resolvedName) ?: throw IllegalArgumentException(
            "Akcja kontrolki '${sourceControlContext.fullStatePath}' wskazuje '$controlPath' " +
                "(rozwiązane do '$resolvedName'), ale w formularzu nie ma takiej kontrolki"
        )
    }

    private fun <V : Any> setValue(state: ControlState<*>, newValue: V?) {
        // Używamy "unsafe" cast, ponieważ programista jest odpowiedzialny za poprawny typ
        @Suppress("UNCHECKED_CAST")
        val typedState = state as ControlState<V>
        typedState.value.value = newValue
        // Tekst opisywał starą wartość, więc przestaje obowiązywać. Kontrolka wyznaczy go
        // ponownie - liczba od razu, dropdown zapytaniem.
        typedState.displayText.value = null
    }
}