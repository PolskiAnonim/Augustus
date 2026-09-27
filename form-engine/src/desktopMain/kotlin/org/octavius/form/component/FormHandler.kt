package org.octavius.form.component

import androidx.compose.runtime.State
import kotlinx.coroutines.*
import org.octavius.dialog.ErrorDialogConfig
import org.octavius.dialog.GlobalDialogManager
import org.octavius.form.control.base.Control
import org.octavius.form.control.base.ControlState
import org.octavius.form.control.base.FormResultData
import org.octavius.form.localization.FormTr
import org.octavius.ui.snackbar.SnackbarManager

/**
 * Klasa obsługująca cykl życia formularza - główny koordynator systemu formularzy.
 *
 * FormHandler koordynuje pracę wszystkich komponentów formularza:
 * - **FormSchema** - definicja struktury i kontrolek formularza
 * - **FormState** - reaktywne zarządzanie stanem wszystkich kontrolek
 * - **FormDataManager** - ładowanie danych z bazy i przetwarzanie wyników
 * - **FormValidator** - walidacja pól i reguł biznesowych
 *
 * Cykl życia:
 * 1. Inicjalizacja komponentów i ustawienie referencji między nimi
 * 2. Ładowanie danych (dla edycji) lub ustawienie wartości domyślnych (nowy rekord)
 * 3. Obsługa akcji użytkownika (walidacja + wykonanie akcji)
 *
 * Handler nie wie nic o nawigacji ani o tym, co dzieje się po akcji - oddaje tylko `true`/`false`
 * z [triggerAction]. Decyzję "zapis zamyka ekran" podejmuje lambda przycisku w schema builderze.
 *
 * @param formSchemaBuilder Builder dostarczający definicję struktury formularza.
 * @param formDataManager Manager odpowiedzialny za operacje na danych.
 * @param formValidator Validator do sprawdzania poprawności danych.
 * @param payload Dodatkowe dane przekazane do formularza (np. ID rodzica).
 * @param handlerScope CoroutineScope do operacji asynchronicznych.
 */
class FormHandler(
    formSchemaBuilder: FormSchemaBuilder,
    val formDataManager: FormDataManager,
    val formValidator: FormValidator = FormValidator(),
    private val payload: Map<String, Any?> = emptyMap(),
    private val handlerScope: CoroutineScope = CoroutineScope(SupervisorJob() + Dispatchers.IO)
) : FormActionTrigger {
    private val formState: FormState = FormState()
    // Błędy pól są kluczowane tą samą pełną ścieżką co stany kontrolek, łącznie z wierszami repeatable.
    internal val errorManager: ErrorManager = ErrorManager { path -> formState.getControlState(path) != null }
    private val formSchema: FormSchema = formSchemaBuilder.build()

    val isLoading: State<Boolean> get() = formState.isLoading
    val actionTriggered: State<Boolean> get() = formState.actionTriggered

    init {
        initializeControlsLifecycle()
        loadData()
    }

    /**
     * Ustawia referencje do komponentów formularza dla wszystkich kontrolek.
     *
     * Ta metoda jest kluczowa dla działania systemu - umożliwia kontrolkom
     * dostęp do globalnego stanu, schemy i menedżera błędów.
     */
    private fun initializeControlsLifecycle() {
        formSchema.getAllControls().values.forEach { control ->
            control.initializeControlLifecycle(formState, formSchema, errorManager, this)
        }
        formValidator.setupFormReferences(formState, formSchema, errorManager)
        formDataManager.setupFormReferences(errorManager)
    }


    /**
     * Publiczne API dla FormView - metody dostępu do komponentów formularza
     */
    internal fun getContentControlsInOrder(): List<String> = formSchema.contentOrder
    internal fun getActionBarControlsInOrder(): List<String> = formSchema.actionBarOrder
    internal fun getControl(name: String): Control<*>? = formSchema.getControl(name)
    internal fun getControlState(name: String): ControlState<*>? = formState.getControlState(name)

    /**
     * Asynchronicznie ładuje dane dla edytowanej encji z bazy danych i inicjalizuje stan formularza.
     *
     * Proces ładowania:
     * 1. Ustawia flagę isLoading na true
     * 2. Pobiera wartości inicjalne z FormDataManager (w Dispatchers.IO)
     * 3. Inicjalizuje stany wszystkich kontrolek
     * 4. Ustawia flagę isLoading na false
     */
    private fun loadData() {
        handlerScope.launch {
            val initValues = formDataManager.initData(payload)
            formState.initializeStates(formSchema, initValues)
            formState.isLoading.value = false
        }
    }

    override suspend fun triggerAction(actionKey: String, validates: Boolean): Boolean {
        // Celowo pod jobem handlerScope, nie wołającego: przycisk odpala akcję w scope ekranu, który
        // ginie razem z ekranem, a zapis złożony z kilku kroków ma dojść do końca, nawet gdy
        // użytkownik zdąży ekran zamknąć. Stąd też brak anulowania handlerScope przy wyjściu.
        return withContext(handlerScope.coroutineContext) {
            val formActions = formDataManager.definedFormActions()
            val action = formActions[actionKey] ?: run {
                GlobalDialogManager.show(ErrorDialogConfig("Exception", "No form action defined for key: $actionKey"))
                return@withContext false
            }

            formState.actionTriggered.value = true
            try {
                errorManager.clearAll()

                val formData = validatedFormData(actionKey, validates) ?: run {
                    SnackbarManager.showMessage(FormTr.Form.Actions.containsErrors())
                    return@withContext false
                }

                action.invoke(formData)
            } finally {
                // Nakładka blokuje cały formularz, więc musi zejść na każdej drodze wyjścia -
                // także gdy walidator albo akcja rzucą wyjątkiem.
                formState.actionTriggered.value = false
            }
        }
    }

    /**
     * Zbiera dane formularza, jeśli przejdą walidację, albo zwraca `null`, gdy któryś etap ją oblał.
     * Błędy do pokazania zapisuje sam etap, w `errorManager`.
     */
    private fun validatedFormData(actionKey: String, validates: Boolean): FormResultData? {
        if (validates && !formValidator.validateFields()) return null

        val formData = formState.collectFormData(formSchema)

        // Walidacja reguł biznesowych (może odpytywać bazę)
        if (validates && !formValidator.validateBusinessRules(formData)) return null

        // Walidacja specyficzna dla akcji (zawsze uruchamiana, niezależnie od flagi 'validates')
        val actionValidator = formValidator.defineActionValidations()[actionKey]
        if (actionValidator != null && !actionValidator.invoke(formData)) return null

        return formData
    }
}