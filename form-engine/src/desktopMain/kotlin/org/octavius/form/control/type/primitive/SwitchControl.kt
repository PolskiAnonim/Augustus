package org.octavius.form.control.type.primitive

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import org.octavius.error.rememberSupervisedCoroutineScope
import org.octavius.form.control.base.*
import org.octavius.form.control.layout.RenderCheckboxLabel
import org.octavius.theme.FormSpacing

/**
 * Kontrolka do wprowadzania wartości logicznych (prawda/fałsz).
 *
 * Renderuje standardowy przełącznik (Switch) z etykietą.
 * Używana zamiast Checkboxa w przypadkach, gdy zmiana stanu
 * przypomina włączanie/wyłączanie danej funkcji.
 */
class SwitchControl(
    label: String?,
    dependencies: Map<String, ControlDependency<*>>? = null,
    actions: List<ControlAction<Boolean>>? = null,
) : Control<Boolean>(
    label,
    required = true,
    dependencies,
    hasStandardLayout = false,
    actions = actions
) {
    @Composable
    override fun Display(controlContext: ControlContext, controlState: ControlState<Boolean>, isRequired: Boolean) {
        val scope = rememberSupervisedCoroutineScope()
        Surface(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = FormSpacing.booleanControlPadding),
        ) {
            Column {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(
                            vertical = FormSpacing.booleanRowPaddingVertical,
                            horizontal = FormSpacing.booleanRowPaddingHorizontal
                        ),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    val currentValue = controlState.value.value ?: false
                    // Wymuszamy stan non-null, jako że Switch nie posiada stanu nieokreślonego
                    if (controlState.value.value == null) {
                        controlState.value.value = false
                    }

                    Switch(
                        checked = currentValue,
                        onCheckedChange = {
                            controlState.value.value = it
                            executeActions(controlContext, it, scope)
                        }
                    )
                    
                    RenderCheckboxLabel(label, isRequired)
                }

                DisplayFieldErrors(controlContext)
            }
        }
    }
}
