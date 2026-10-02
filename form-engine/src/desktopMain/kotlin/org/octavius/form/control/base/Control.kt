package org.octavius.form.control.base

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.layout.Column
import androidx.compose.runtime.Composable
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch
import org.octavius.dialog.ErrorDialogConfig
import org.octavius.dialog.GlobalDialogManager
import org.octavius.error.rememberSupervisedCoroutineScope
import org.octavius.form.component.ErrorManager
import org.octavius.form.component.FormActionTrigger
import org.octavius.form.component.FormSchema
import org.octavius.form.component.FormState
import org.octavius.form.control.layout.RenderFieldError
import org.octavius.form.control.layout.RenderNormalLabel
import org.octavius.form.control.validator.DefaultValidator
import org.octavius.localization.Tr

/**
 * Abstrakcyjna klasa bazowa dla wszystkich kontrolek formularza.
 *
 * @param T typ danych przechowywanych przez kontrolkę
 */
abstract class Control<T : Any> internal constructor(
    // --- 1. Konfiguracja i Właściwości ---
    internal val label: String?,
    internal val required: Boolean?,
    internal val dependencies: Map<String, ControlDependency<*>>?,
    internal val validationOptions: ValidationOptions? = null,
    private val actions: List<ControlAction<T>>? = null,
    protected val hasStandardLayout: Boolean = true
) {

    // --- 2. Zarządzanie Hierarchią ---

    /**
     * Funkcja do rejestracji dzieci na globalnej mapie
     * Zawiera referencje do kontrolek zdefiniowanych w kontrolce posiadającej własne
     */
    internal open fun registerChildrenInGlobalMap(controlContext: ControlContext): Map<String, Control<*>> {
        return emptyMap()
    }

    /**
     * Dzieci tej kontrolki w bieżącym stanie formularza, każde z kontekstem, w którym ta kontrolka
     * jest rodzicem: sekcja ma jeden zestaw, repeatable - po jednym na wiersz. Po nich silnik schodzi
     * w dół drzewa przy inicjalizacji, walidacji i zbieraniu danych.
     */
    internal open fun childContexts(controlContext: ControlContext): List<Pair<ControlContext, Control<*>>> {
        return emptyList()
    }


    // --- 3. Cykl Życia i Kontekst Formularza ---
    // Referencje wstrzykiwane przez FormHandler przy inicjalizacji.
    protected lateinit var formState: FormState
    protected lateinit var formSchema: FormSchema
    protected lateinit var errorManager: ErrorManager
    protected lateinit var formActionTrigger: FormActionTrigger

    /**
     * Ustawia referencje do komponentów formularza. Jest to drugi etap inicjalizacji kontrolki,
     * wywoływany przez FormHandler. Daje dostęp do globalnego stanu, schemy i managera błędów.
     * Dodatkowo umożliwia wykonanie dodatkowych kroków inicjalizacyjnych wymaganych przez określone kontrolki
     */
    internal open fun initializeControlLifecycle(
        formState: FormState,
        formSchema: FormSchema,
        errorManager: ErrorManager,
        formActionTrigger: FormActionTrigger
    ) {
        this.formState = formState
        this.formSchema = formSchema
        this.errorManager = errorManager
        this.formActionTrigger = formActionTrigger
        validator.setupFormReferences(formState, formSchema, errorManager)
    }

    // --- 4. Zarządzanie Stanem i Danymi ---
    /**
     * Ustawia początkową wartość kontrolki i tworzy jej stan (`ControlState`).
     * @param value Wartość z bazy danych lub wartość domyślna.
     * @return Utworzony stan kontrolki.
     */
    internal open fun setInitValue(controlContext: ControlContext, value: Any?): ControlState<T> {
        val state = ControlState<T>()
        if (value == null) {
            return state
        }

        try {
            @Suppress("UNCHECKED_CAST")
            state.initValue.value = value as T
            state.value.value = copyInitToValue(value)
        } catch (e: ClassCastException) {
            GlobalDialogManager.show(
                ErrorDialogConfig(
                    Tr.Dialog.error(),
                    "Nie można skonwertować wartości '$value' na typ kontrolki ${this::class.simpleName}"
                )
            )
        }
        return state
    }

    /**
     * Tworzy głęboką kopię wartości początkowej dla bieżącej wartości.
     * Kluczowe dla typów mutowalnych (np. List), aby zmiany w `value` nie wpływały na `initValue`.
     */
    internal open fun copyInitToValue(value: T): T {
        return value // Domyślna implementacja dla typów niemutowalnych.
    }

    /**
     * Pobiera wynik kontrolki (wartość bieżącą i początkową) do dalszego przetwarzania.
     * Jeśli kontrolka jest niewidoczna, jej `currentValue` jest ustawiane na null.
     */
    internal fun getResult(controlContext: ControlContext, state: ControlState<*>): ControlResultData {
        val result = convertToResult(controlContext, state)
        // Niewidoczne kontrolki nie powinny przekazywać swojej wartości do zapisu.
        return if (!validator.isControlVisible(this, controlContext)) {
            result.copy(currentValue = null)
        } else {
            result
        }
    }

    /**
     * Konwertuje wewnętrzny stan kontrolki (`ControlState`) na wynik do zapisu (`ControlResultData`).
     * Może być przesłonięta dla niestandardowych konwersji (np. w `RepeatableControl`).
     */
    protected open fun convertToResult(controlContext: ControlContext, state: ControlState<*>): ControlResultData {
        return ControlResultData(currentValue = state.value.value, initialValue = state.initValue.value)
    }

    // --- 5. Logika Walidacji ---
    /**
     * Validator odpowiedzialny za walidację tej kontrolki.
     * Każdy typ kontrolki dostarcza własną implementację (np. `StringValidator`).
     * Domyślnie pusta implementacja
     */
    protected open val validator: ControlValidator<T> = DefaultValidator()

    /**
     * Uruchamia proces walidacji dla tej kontrolki przy użyciu przypisanego walidatora.
     */
    internal fun validateControl(controlContext: ControlContext, state: ControlState<*>) {
        validator.validate(controlContext, state, this)
    }

    // --- 6. Obsługa Akcji ---
    /**
     * Wykonuje zdefiniowane akcje dla tej kontrolki, zazwyczaj po zmianie wartości.
     */
    protected fun executeActions(
        controlContext: ControlContext,
        newValue: T?,
        scope: CoroutineScope,
        payload: Any? = null
    ) {
        actions?.forEach { action ->
            scope.launch {
                val context = ActionContext(
                    sourceValue = newValue,
                    sourceControlContext = controlContext,
                    formState = formState,
                    formSchema = formSchema,
                    errorManager = errorManager,
                    trigger = formActionTrigger,
                    coroutineScope = scope,
                    payload = payload
                )
                action.action.invoke(context)
            }
        }
    }

    internal fun executeInitActions(
        controlContext: ControlContext,
        currentValue: T?,
        scope: CoroutineScope
    ) {
        actions?.filter { it.executeOnInit }?.forEach { action ->
            scope.launch {
                val context = ActionContext(
                    sourceValue = currentValue,
                    sourceControlContext = controlContext,
                    formState = formState,
                    formSchema = formSchema,
                    errorManager = errorManager,
                    trigger = formActionTrigger,
                    coroutineScope = scope
                )
                action.action.invoke(context)
            }
        }
    }

    // --- 7. Renderowanie UI ---
    /**
     * Renderuje kontrolkę w interfejsie użytkownika.
     * Ta metoda jest publicznym API dla `FormScreen`. Obsługuje logikę widoczności.
     */
    @Composable
    internal fun Render(controlContext: ControlContext, controlState: ControlState<*>) {
        val isVisible = validator.isControlVisible(this, controlContext)
        val isRequired = validator.isControlRequired(this, controlContext)
        val scope = rememberSupervisedCoroutineScope()

        @Suppress("UNCHECKED_CAST")
        val typedState = controlState as ControlState<T>

        androidx.compose.runtime.LaunchedEffect(Unit) {
            executeInitActions(controlContext, typedState.value.value, scope)
        }

        AnimatedVisibility(visible = isVisible) {
            if (hasStandardLayout) {
                // Standardowy układ: Etykieta nad kontrolką, błędy pod.
                Column {
                    RenderNormalLabel(controlState.labelOverride.value ?: label, isRequired)
                    Display(controlContext, typedState, isRequired)
                    DisplayFieldErrors(controlContext)
                }
            } else {
                // Układ niestandardowy: Kontrolka sama zarządza swoim layoutem.
                Display(controlContext, typedState, isRequired)
            }
        }
    }

    /**
     * Abstrakcyjna metoda renderowania specyficznego interfejsu kontrolki.
     * Każdy konkretny typ kontrolki (np. `StringControl`) musi ją zaimplementować.
     */
    @Composable
    protected abstract fun Display(
        controlContext: ControlContext,
        controlState: ControlState<T>,
        isRequired: Boolean
    )

    /**
     * Pomocnicza funkcja renderująca błędy walidacji dla tej kontrolki.
     */
    @Composable
    protected fun DisplayFieldErrors(controlContext: ControlContext) {
        val formatError = errorManager.getFormatError(controlContext.fullStatePath)
        formatError?.let { error -> RenderFieldError(error) }

        val fieldErrors = errorManager.getFieldErrors(controlContext.fullStatePath)
        fieldErrors.forEach { error ->
            RenderFieldError(error)
        }
    }
}