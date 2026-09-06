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
     * Aktualizuje wartość kontrolki używając ścieżki względnej (./, ../) lub bezwzględnej.
     */
    fun <V: Any> updateControl(controlPath: String, newValue: V?) {
        val resolvedName = PathResolver.resolvePath(controlPath, sourceControlContext)
        formState.getControlState(resolvedName)?.let { state ->
            // Używamy "unsafe" cast, ponieważ programista jest odpowiedzialny za poprawny typ
            @Suppress("UNCHECKED_CAST")
            val typedState = state as ControlState<V>
            typedState.value.value = newValue
            // Tekst opisywał starą wartość, więc przestaje obowiązywać. Kontrolka wyznaczy go
            // ponownie - liczba od razu, dropdown zapytaniem.
            typedState.displayText.value = null
        }
    }

    /**
     * Aktualizuje wartości wielu kontrolek pasujących do wzorca (np. z wildcardem *).
     */
    fun <V: Any> updateControls(controlPath: String, newValue: V?) {
        val resolvedNames = PathResolver.resolvePaths(controlPath, sourceControlContext, formState)
        resolvedNames.forEach { resolvedName ->
            formState.getControlState(resolvedName)?.let { state ->
                @Suppress("UNCHECKED_CAST")
                val typedState = state as ControlState<V>
                typedState.value.value = newValue
                typedState.displayText.value = null
            }
        }
    }

    /**
     * Aktualizuje etykietę kontrolki (nadpisuje domyślną).
     */
    fun updateLabel(controlPath: String, newLabel: String?) {
        val resolvedName = PathResolver.resolvePath(controlPath, sourceControlContext)
        formState.getControlState(resolvedName)?.let { state ->
            state.labelOverride.value = newLabel
        }
    }
}