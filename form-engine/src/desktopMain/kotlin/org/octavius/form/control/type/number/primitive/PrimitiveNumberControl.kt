package org.octavius.form.control.type.number.primitive

import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.OutlinedTextField
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import org.octavius.form.control.base.*
import org.octavius.form.localization.FormTr
import org.octavius.theme.FormSpacing

/**
 * Abstrakcyjna klasa bazowa dla kontrolek numerycznych (Integer, Double, etc.).
 * Hermetyzuje wspólną logikę renderowania pola tekstowego, parsowania
 * i obsługi błędów nieprawidłowego formatu.
 */
abstract class PrimitiveNumberControl<T : Number>(
    label: String?,
    required: Boolean?,
    dependencies: Map<String, ControlDependency<*>>?,
    validationOptions: ValidationOptions?,
    actions: List<ControlAction<T>>?
) : Control<T>(
    label,
    required,
    dependencies,
    validationOptions = validationOptions,
    actions = actions
) {
    /**
     * Abstrakcyjna metoda, którą konkretne implementacje muszą dostarczyć,
     * aby sparsować tekst na docelowy typ numeryczny.
     * @param text Wartość z pola tekstowego.
     * @return Sparsowana wartość lub null w przypadku błędu formatu.
     */
    protected abstract fun parseValue(text: String): T?

    @Composable
    override fun Display(controlContext: ControlContext, controlState: ControlState<T>, isRequired: Boolean) {
        val scope = rememberCoroutineScope()

        // Bufor tekstowy musi być osobny od wartości, bo "1." ani "007" nie wracają z toString()
        // sparsowanej liczby. Żyje w ControlState, a nie w remember, żeby updateControl z zewnątrz
        // mógł go unieważnić - null znaczy "nieaktualny", więc wyprowadzamy tekst z wartości.
        val textValue = controlState.displayText.value ?: (controlState.value.value?.toString() ?: "")

        // Unieważnienie tekstu z zewnątrz kasuje też błąd formatu - inaczej użytkownik zostaje
        // z komunikatem o wpisie, którego już nie widzi.
        LaunchedEffect(controlState.displayText.value) {
            if (controlState.displayText.value == null) {
                errorManager.setFormatError(controlContext.fullStatePath, null)
            }
        }

        OutlinedTextField(
            value = textValue,
            onValueChange = { newText ->
                controlState.displayText.value = newText

                if (newText.isEmpty()) {
                    if (controlState.value.value != null) {
                        controlState.value.value = null
                        executeActions(controlContext, null, scope)
                    }
                    errorManager.setFormatError(controlContext.fullStatePath, null)
                } else {
                    val parsed = parseValue(newText)
                    if (parsed != null) {
                        if (controlState.value.value != parsed) {
                            controlState.value.value = parsed
                            executeActions(controlContext, parsed, scope)
                        }
                        errorManager.setFormatError(controlContext.fullStatePath, null)
                    } else {
                        errorManager.setFormatError(controlContext.fullStatePath, FormTr.Validation.invalidNumberFormat())
                    }
                }
            },
            modifier = Modifier.fillMaxWidth().padding(
                vertical = FormSpacing.fieldPaddingVertical,
                horizontal = FormSpacing.fieldPaddingHorizontal
            ),
            singleLine = true
        )
    }
}