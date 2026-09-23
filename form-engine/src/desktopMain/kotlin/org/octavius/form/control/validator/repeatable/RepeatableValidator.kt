package org.octavius.form.control.validator.repeatable

import org.octavius.form.control.base.ControlContext
import org.octavius.form.control.base.ControlState
import org.octavius.form.control.base.ControlValidator
import org.octavius.form.control.base.RepeatableValidation
import org.octavius.form.control.type.repeatable.RepeatableControl
import org.octavius.form.control.type.repeatable.RepeatableRow
import org.octavius.form.localization.FormTr

/**
 * Walidator dla kontrolek typu RepeatableControl.
 *
 * Odpowiada za walidację kontrolek umożliwiających dodawanie
 * wielu wierszy danych (np. lista autorów, tagów, kategorii).
 *
 * Funkcjonalności:
 * - Sprawdzanie unikalności wartości w określonych polach
 * - Walidacja minimalnej i maksymalnej liczby elementów
 */
class RepeatableValidator(
    private val validationOptions: RepeatableValidation? = null
) : ControlValidator<List<RepeatableRow>>() {

    /**
     * Waliduje unikalność wartości w wierszach kontrolki powtarzalnej.
     * Używa globalnego stanu zamiast lokalnych stanów wierszy.
     *
     * Algorytm walidacji:
     * 1. Pobiera wszystkie wiersze z kontrolki
     * 2. Dla każdego wiersza tworzy klucz unikalności z wartości określonych pól
     * 3. Sprawdza czy klucz nie został już wcześniej napotkany
     * 4. Jeśli znajdzie duplikat, ustawia błąd z numerem wiersza
     *
     * Sprawdzanie odbywa się tylko gdy lista uniqueFields nie jest pusta.
     *
     * @param controlContext nazwa kontrolki z kontekstem wiersza (potrzebna do budowania hierarchicznych nazw)
     * @param state stan kontrolki powtarzalnej zawierający listę wierszy
     */
    override fun validateSpecific(controlContext: ControlContext, state: ControlState<*>) {
        @Suppress("UNCHECKED_CAST")
        val rows = state.value.value as List<RepeatableRow>

        val control = formSchema.getControl(controlContext.fullStatePath) as? RepeatableControl ?: return

        // 1. Walidacja kontrolek-dzieci (zawsze musi być wykonana)
        validateChildControls(rows, control, controlContext)

        // 2. Walidacja reguł samej listy
        val allErrors = mutableListOf<String>()

        validationOptions?.let { options ->
            // Sprawdź min/max
            allErrors.addAll(validateListRules(rows, options))

            // Sprawdź unikalność
            validateUniqueness(rows, options, controlContext)?.let { uniquenessError ->
                allErrors.add(uniquenessError)
            }
        }

        // 3. Ustaw błędy dla GŁÓWNEJ kontrolki
        errorManager.setFieldErrors(controlContext.fullStatePath, allErrors)
    }

    private fun validateUniqueness(
        rows: List<RepeatableRow>,
        options: RepeatableValidation,
        controlContext: ControlContext
    ): String? { // Zwraca string błędu lub null
        if (options.uniqueFields.isEmpty()) {
            return null
        }

        val allStates = formState.getAllStates()
        val seenValues = mutableSetOf<List<Any?>>()

        for ((index, row) in rows.withIndex()) {
            // Bierzemy pod uwagę tylko wiersze, które nie mają błędów w polach unikalności
            val uniqueKey = options.uniqueFields.map { field ->
                val hierarchicalContext = controlContext.forRepeatableChild(field, row.id)
                allStates[hierarchicalContext.fullStatePath]?.value?.value
            }

            // Ignoruj klucze z pustymi polami
            if (uniqueKey.any { it == null || (it is String && it.isBlank()) }) {
                continue
            }

            if (!seenValues.add(uniqueKey)) {
                // Znaleziono duplikat, zwróć błąd i zakończ
                return FormTr.Validation.duplicateInRow(index + 1)
            }
        }

        return null // Brak duplikatów
    }

    private fun validateListRules(
        rows: List<RepeatableRow>,
        options: RepeatableValidation
    ): List<String> {
        val errors = mutableListOf<String>()

        // Sprawdź minimalną liczbę elementów
        options.minItems?.let { minItems ->
            if (rows.size < minItems) {
                errors.add(FormTr.Validation.minItems(minItems))
            }
        }

        // Sprawdź maksymalną liczbę elementów
        options.maxItems?.let { maxItems ->
            if (rows.size > maxItems) {
                errors.add(FormTr.Validation.maxItems(maxItems))
            }
        }

        return errors
    }

    private fun validateChildControls(
        rows: List<RepeatableRow>,
        control: RepeatableControl,
        controlContext: ControlContext
    ) {
        val allStates = formState.getAllStates()
        for (row in rows) {
            for ((fieldName, fieldControl) in control.rowControls) {
                val hierarchicalContext = controlContext.forRepeatableChild(fieldName, row.id)
                val fieldState = allStates.getValue(hierarchicalContext.fullStatePath)
                fieldControl.validateControl(hierarchicalContext, fieldState)
            }
        }
    }
}