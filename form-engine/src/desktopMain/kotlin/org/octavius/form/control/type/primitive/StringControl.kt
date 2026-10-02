package org.octavius.form.control.type.primitive

import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.OutlinedTextField
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import org.octavius.error.rememberSupervisedCoroutineScope
import org.octavius.form.control.base.*
import org.octavius.form.control.validator.primitive.StringValidator
import org.octavius.theme.FormSpacing

/**
 * Kontrolka do wprowadzania tekstu jednoliniowego.
 *
 * Renderuje standardowe pole tekstowe z etykietą i walidacją.
 * Wspiera wszystkie standardowe funkcje kontrolek jak zależności,
 * wymagalność i komunikaty błędów.
 */
class StringControl(
    label: String?,
    required: Boolean? = false,
    dependencies: Map<String, ControlDependency<*>>? = null,
    validationOptions: StringValidation? = null,
    actions: List<ControlAction<String>>? = null
) : Control<String>(
    label,
    required,
    dependencies,
    validationOptions = validationOptions,
    actions = actions
) {
    override val validator: ControlValidator<String> = StringValidator(validationOptions)


    @Composable
    override fun Display(controlContext: ControlContext, controlState: ControlState<String>, isRequired: Boolean) {
        val scope = rememberSupervisedCoroutineScope()

        OutlinedTextField(
            value = controlState.value.value.orEmpty(),
            onValueChange = {
                controlState.value.value = it
                executeActions(controlContext, it, scope)
            },
            modifier = Modifier.fillMaxWidth().padding(
                vertical = FormSpacing.fieldPaddingVertical,
                horizontal = FormSpacing.fieldPaddingHorizontal
            ),
            singleLine = true
        )
    }
}