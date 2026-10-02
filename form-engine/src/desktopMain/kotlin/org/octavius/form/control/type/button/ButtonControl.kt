package org.octavius.form.control.type.button

import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import org.octavius.error.rememberSupervisedCoroutineScope
import org.octavius.form.control.base.*
import org.octavius.theme.FormSpacing

/**
 * Kontrolka renderująca przycisk, którego jedynym celem jest wywołanie zdefiniowanych akcji.
 *
 * Jest to kontrolka "bezstanowa" (używa typu `Unit`), nie przechowuje żadnej wartości
 * i nie jest powiązana z kolumną w bazie danych. Służy jako element interfejsu
 * do wyzwalania logiki biznesowej, takiej jak wywołania API, otwieranie okien dialogowych,
 * czy modyfikowanie innych kontrolek w formularzu.
 *
 * @param text Tekst wyświetlany na przycisku.
 * @param buttonType Styl wizualny przycisku (domyślnie FILLED).
 * @param dependencies Zależności widoczności od innych kontrolek.
 * @param actions Lista akcji do wykonania po kliknięciu. Musi zawierać co najmniej jedną akcję.
 */
class ButtonControl(
    val text: String,
    val buttonType: ButtonType = ButtonType.Filled,
    dependencies: Map<String, ControlDependency<*>>? = null,
    actions: List<ControlAction<Unit>> // Akcje są kluczowe, więc nie są opcjonalne
) : Control<Unit>(
    label = null, // Przyciski nie mają etykiety, tekst jest na nich
    required = false, // Nie dotyczy
    dependencies = dependencies,
    hasStandardLayout = false, // Pełna kontrola nad renderowaniem
    actions = actions
) {
    @Composable
    override fun Display(controlContext: ControlContext, controlState: ControlState<Unit>, isRequired: Boolean) {
        val scope = rememberSupervisedCoroutineScope()

        val modifier = Modifier
            .fillMaxWidth()
            .padding(
                vertical = FormSpacing.fieldPaddingVertical,
                horizontal = FormSpacing.fieldPaddingHorizontal
            )

        when (buttonType) {
            ButtonType.Filled -> {
                Button(
                    onClick = { executeActions(controlContext, Unit, scope) },
                    modifier = modifier
                ) {
                    Text(text)
                }
            }
            ButtonType.Outlined -> {
                OutlinedButton(
                    onClick = { executeActions(controlContext, Unit, scope) },
                    modifier = modifier
                ) {
                    Text(text)
                }
            }
            ButtonType.Text -> {
                TextButton(
                    onClick = { executeActions(controlContext, Unit, scope) },
                    modifier = modifier
                ) {
                    Text(text)
                }
            }
        }
    }
}